package dev.m00nl1ght.clockwork.utils.version;

import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Modified version of {@code com.vdurmont.semver4j.Semver} from
 * <a href="https://github.com/vdurmont/semver4j">https://github.com/vdurmont/semver4j</a>. <br>
 *
 * Represents a version number following the "semantic versioning" specification
 * (see <a href="http://semver.org">http://semver.org</a>).
 */
public final class Version implements Comparable<Version> {

    private final String originalValue;
    private final String value;
    private final Integer major;
    private final Integer minor;
    private final Integer patch;
    private final String[] suffixTokens;
    private final String build;
    private final VersionType type;

    public Version(String value) {
        this(value, VersionType.STRICT);
    }

    public Version(String value, VersionType type) {
        this.originalValue = value;
        this.type = type;
        value = value.trim();
        this.value = value;

        String[] tokens;
        if (hasPreRelease(value)) {
            tokens = value.split("-", 2);
        } else {
            tokens = new String[] { value };
        }

        String build = null;
        Integer minor = null;
        Integer patch = null;

        try {
            String[] mainTokens;
            if (tokens.length == 1) {
                // The build version may be in the main tokens
                if (tokens[0].endsWith("+")) throw new VersionException("The build cannot be empty.");
                String[] tmp = tokens[0].split("\\+");
                mainTokens = tmp[0].split("\\.");
                if (tmp.length == 2) build = tmp[1];
            } else {
                mainTokens = tokens[0].split("\\.");
            }

            try {
                this.major = Integer.valueOf(mainTokens[0]);
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                throw new VersionException("Invalid version (no major version): " + value);
            }

            try {
                minor = Integer.valueOf(mainTokens[1]);
            } catch (IndexOutOfBoundsException e) {
                if (type == VersionType.STRICT) {
                    throw new VersionException("Invalid version (no minor version): " + value);
                }
            } catch (NumberFormatException e) {
                throw new VersionException("Invalid version (no minor version): " + value);
            }

            try {
                patch = Integer.valueOf(mainTokens[2]);
            } catch (IndexOutOfBoundsException e) {
                if (type == VersionType.STRICT) {
                    throw new VersionException("Invalid version (no patch version): " + value);
                }
            } catch (NumberFormatException e) {
                throw new VersionException("Invalid version (no patch version): " + value);
            }
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            throw new VersionException("The version is invalid: " + value);
        }

        this.minor = minor;
        this.patch = patch;

        String[] suffix = new String[0];
        try {
            // The build version may be in the suffix tokens
            if (tokens[1].endsWith("+")) throw new VersionException("The build cannot be empty.");
            String[] tmp = tokens[1].split("\\+");
            if (tmp.length == 2) {
                suffix = tmp[0].split("\\.");
                build = tmp[1];
            } else {
                suffix = tokens[1].split("\\.");
            }
        } catch (IndexOutOfBoundsException ignored) {}

        this.suffixTokens = suffix;
        this.build = build;
        this.validate(type);
    }

    private void validate(VersionType type) {
        if (this.minor == null && type == VersionType.STRICT)
            throw new VersionException("Invalid version (no minor version): " + value);
        if (this.patch == null && type == VersionType.STRICT)
            throw new VersionException("Invalid version (no patch version): " + value);
    }

    private boolean hasPreRelease(String version) {
        int firstIndexOfPlus = value.indexOf("+");
        int firstIndexOfHyphen = value.indexOf("-");
        if (firstIndexOfHyphen == -1) return false;
        return firstIndexOfPlus == -1 || firstIndexOfHyphen < firstIndexOfPlus;
    }

    /**
     * Checks if the version satisfies a requirement.
     *
     * @param requirement the requirement to check
     * @return true if the version satisfies the requirement
     */
    public boolean satisfies(VersionRequirement requirement) {
        return requirement.isSatisfiedBy(this);
    }

    /**
     * Checks if the version satisfies a requirement string.
     *
     * @param requirement the requirement string to check
     * @return true if the version satisfies the requirement
     */
    public boolean satisfies(String requirement) {
        switch (this.type) {
            case STRICT: return this.satisfies(VersionRequirement.buildStrict(requirement));
            case LOOSE: return this.satisfies(VersionRequirement.buildLoose(requirement));
            case IVY: return this.satisfies(VersionRequirement.buildIvy(requirement));
            default: throw new VersionException("Invalid requirement type: " + type);
        }
    }

    /**
     * Checks if the version is greater than another version.
     *
     * @param version the version to compare
     * @return true if the current version is greater than the provided version
     */
    public boolean isGreaterThan(String version) {
        return this.isGreaterThan(new Version(version, this.getType()));
    }

    /**
     * Checks if the version is greater than another version.
     *
     * @param version the version to compare
     * @return true if the current version is greater than the provided version
     */
    public boolean isGreaterThan(Version version) {
        // Compare the main part
        if (this.getMajor() > version.getMajor()) return true;
        else if (this.getMajor() < version.getMajor()) return false;

        int otherMinor = version.getMinor() != null ? version.getMinor() : 0;
        if (this.getMinor() != null && this.getMinor() > otherMinor) return true;
        else if (this.getMinor() != null && this.getMinor() < otherMinor) return false;

        int otherPatch = version.getPatch() != null ? version.getPatch() : 0;
        if (this.getPatch() != null && this.getPatch() > otherPatch) return true;
        else if (this.getPatch() != null && this.getPatch() < otherPatch) return false;

        // Let's take a look at the suffix
        String[] tokens1 = this.getSuffixTokens();
        String[] tokens2 = version.getSuffixTokens();

        // If one of the versions has no suffix, it's greater!
        if (tokens1.length == 0 && tokens2.length > 0) return true;
        if (tokens2.length == 0 && tokens1.length > 0) return false;

        // Let's see if one of suffixes is greater than the other
        int i = 0;
        while (i < tokens1.length && i < tokens2.length) {
            int cmp;
            try {
                // Trying to resolve the suffix part with an integer
                int t1 = Integer.parseInt(tokens1[i]);
                int t2 = Integer.parseInt(tokens2[i]);
                cmp = t1 - t2;
            } catch (NumberFormatException e) {
                // Else, do a string comparison
                cmp = tokens1[i].compareToIgnoreCase(tokens2[i]);
            }
            if (cmp < 0) return false;
            else if (cmp > 0) return true;
            i++;
        }

        // If one of the versions has some remaining suffixes, it's greater
        return tokens1.length > tokens2.length;
    }

    /**
     * Checks if the version is greater than or equal to another version.
     *
     * @param version the version to compare
     * @return true if the current version is greater than or equal to the provided version
     */
    public boolean isGreaterThanOrEqualTo(String version) {
        return this.isGreaterThanOrEqualTo(new Version(version, this.type));
    }

    /**
     * Checks if the version is greater than or equal to another version.
     *
     * @param version the version to compare
     * @return true if the current version is greater than or equal to the provided version
     */
    public boolean isGreaterThanOrEqualTo(Version version) {
        return this.isGreaterThan(version) || this.isEquivalentTo(version);
    }

    /**
     * Checks if the version is lower than another version.
     *
     * @param version the version to compare
     * @return true if the current version is lower than the provided version
     */
    public boolean isLowerThan(String version) {
        return this.isLowerThan(new Version(version, this.type));
    }

    /**
     * Checks if the version is lower than another version.
     *
     * @param version the version to compare
     * @return true if the current version is lower than the provided version
     */
    public boolean isLowerThan(Version version) {
        return !this.isGreaterThan(version) && !this.isEquivalentTo(version);
    }

    /**
     * Checks if the version is lower than or equal to another version.
     *
     * @param version the version to compare
     * @return true if the current version is lower than or equal to the provided version
     */
    public boolean isLowerThanOrEqualTo(String version) {
        return this.isLowerThanOrEqualTo(new Version(version, this.type));
    }

    /**
     * Checks if the version is lower than or equal to another version.
     *
     * @param version the version to compare
     * @return true if the current version is lower than or equal to the provided version
     */
    public boolean isLowerThanOrEqualTo(Version version) {
        return !this.isGreaterThan(version);
    }

    /**
     * Checks if the version equals another version, without taking the build into account.
     *
     * @param version the version to compare
     * @return true if the current version equals the provided version (build excluded)
     */
    public boolean isEquivalentTo(String version) {
        return this.isEquivalentTo(new Version(version, this.type));
    }

    /**
     * Checks if the version equals another version, without taking the build into account.
     *
     * @param version the version to compare
     * @return true if the current version equals the provided version (build excluded)
     */
    public boolean isEquivalentTo(Version version) {
        // Get versions without build
        Version sem1 = this.getBuild() == null ? this : new Version(this.getValue().replace("+" + this.getBuild(), ""));
        Version sem2 = version.getBuild() == null ? version : new Version(version.getValue().replace("+" + version.getBuild(), ""));
        // Compare those new versions
        return sem1.isEqualTo(sem2);
    }

    /**
     * Checks if the version equals another version.
     *
     * @param version the version to compare
     * @return true if the current version equals the provided version
     */
    public boolean isEqualTo(String version) {
        return this.isEqualTo(new Version(version, this.type));
    }

    /**
     * Checks if the version equals another version.
     *
     * @param version the version to compare
     * @return true if the current version equals the provided version
     */
    public boolean isEqualTo(Version version) {
        return this.equals(version);
    }

    /**
     * Determines if the current version is stable or not.
     * Stable version have a major version number strictly positive and no suffix tokens.
     *
     * @return true if the current version is stable
     */
    public boolean isStable() {
        return (this.getMajor() != null && this.getMajor() > 0) &&
                (this.getSuffixTokens() == null || this.getSuffixTokens().length == 0);
    }

    /**
     * Returns the greatest difference between 2 versions.
     *
     * For example, if the current version is "1.2.3" and compared version is "1.3.0",
     * the biggest difference is the 'MINOR' number.
     *
     * @param version the version to compare
     * @return the greatest difference
     */
    public VersionDiff diff(String version) {
        return this.diff(new Version(version, this.type));
    }

    /**
     * Returns the greatest difference between 2 versions.
     *
     * For example, if the current version is "1.2.3" and compared version is "1.3.0",
     * the biggest difference is the 'MINOR' number.
     *
     * @param version the version to compare
     * @return the greatest difference
     */
    public VersionDiff diff(Version version) {
        if (!Objects.equals(this.major, version.getMajor())) return VersionDiff.MAJOR;
        if (!Objects.equals(this.minor, version.getMinor())) return VersionDiff.MINOR;
        if (!Objects.equals(this.patch, version.getPatch())) return VersionDiff.PATCH;
        if (!areSameSuffixes(version.getSuffixTokens())) return VersionDiff.SUFFIX;
        if (!Objects.equals(this.build, version.getBuild())) return VersionDiff.BUILD;
        return VersionDiff.NONE;
    }

    private boolean areSameSuffixes(String[] suffixTokens) {
        if (this.suffixTokens == null && suffixTokens == null) return true;
        else if (this.suffixTokens == null || suffixTokens == null) return false;
        else if (this.suffixTokens.length != suffixTokens.length) return false;
        return IntStream.range(0, this.suffixTokens.length)
                .allMatch(i -> this.suffixTokens[i].equals(suffixTokens[i]));
    }

    public Version toStrict() {
        Integer minor = this.minor != null ? this.minor : 0;
        Integer patch = this.patch != null ? this.patch : 0;
        return Version.create(VersionType.STRICT, this.major, minor, patch, this.suffixTokens, this.build);
    }

    public Version withIncMajor() {
        return this.withIncMajor(1);
    }

    public Version withIncMajor(int increment) {
        return this.withInc(increment, 0, 0);
    }

    public Version withIncMinor() {
        return this.withIncMinor(1);
    }

    public Version withIncMinor(int increment) {
        return this.withInc(0, increment, 0);
    }

    public Version withIncPatch() {
        return this.withIncPatch(1);
    }

    public Version withIncPatch(int increment) {
        return this.withInc(0, 0, increment);
    }

    private Version withInc(int majorInc, int minorInc, int patchInc) {
        Integer minor = this.minor;
        Integer patch = this.patch;
        if (this.minor != null) minor += minorInc;
        if (this.patch != null) patch += patchInc;
        return with(this.major + majorInc, minor, patch, true, true);
    }

    public Version withClearedSuffix() {
        return with(this.major, this.minor, this.patch, false, true);
    }

    public Version withClearedBuild() {
        return with(this.major, this.minor, this.patch, true, false);
    }

    public Version withClearedSuffixAndBuild() {
        return with(this.major, this.minor, this.patch, false, false);
    }

    public Version withSuffix(String suffix) {
    	return with(this.major, this.minor, this.patch, suffix.split("\\."), this.build);
    }

    public Version withBuild(String build) {
    	return with(this.major, this.minor, this.patch, this.suffixTokens, build);
    }

    public Version nextMajor() {
        return with(this.major + 1, 0, 0, false, false);
    }

    public Version nextMinor() {
        return with(this.major, this.minor + 1, 0, false, false);
    }

    public Version nextPatch() {
        return with(this.major, this.minor, this.patch + 1, false, false);
    }

    private Version with(int major, Integer minor, Integer patch, boolean suffix, boolean build) {
        minor = this.minor != null ? minor : null;
        patch = this.patch != null ? patch : null;
        String buildStr = build ? this.build : null;
        String[] suffixTokens = suffix ? this.suffixTokens : null;
        return Version.create(this.type, major, minor, patch, suffixTokens, buildStr);
    }

    private Version with(int major, Integer minor, Integer patch, String[] suffixTokens, String build) {
        minor = this.minor != null ? minor : null;
        patch = this.patch != null ? patch : null;
        return Version.create(this.type, major, minor, patch, suffixTokens, build);
    }

    private static Version create(VersionType type, int major, Integer minor, Integer patch, String[] suffix, String build) {
        StringBuilder sb = new StringBuilder().append(major);
        if (minor != null) sb.append(".").append(minor);
        if (patch != null) sb.append(".").append(patch);
        if (suffix != null) {
            boolean first = true;
            for (String suffixToken : suffix) {
                if (first) {
                    sb.append("-");
                    first = false;
                } else {
                    sb.append(".");
                }
                sb.append(suffixToken);
            }
        }
        if (build != null) sb.append("+").append(build);
        return new Version(sb.toString(), type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Version)) return false;
        Version version = (Version) o;
        return value.equals(version.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public int compareTo(Version version) {
        if (this.isGreaterThan(version)) return 1;
        else if (this.isLowerThan(version)) return -1;
        return 0;
    }

    @Override
    public String toString() {
        return this.getValue();
    }

    public String getOriginalValue() {
        return originalValue;
    }

    public String getValue() {
        return value;
    }

    public Integer getMajor() {
        return this.major;
    }

    public Integer getMinor() {
        return this.minor;
    }

    public Integer getPatch() {
        return this.patch;
    }

    public String[] getSuffixTokens() {
        return suffixTokens;
    }

    public String getBuild() {
        return build;
    }

    public VersionType getType() {
        return type;
    }

    /**
     * The types of diffs between two versions.
     */
    public enum VersionDiff {
        NONE, MAJOR, MINOR, PATCH, SUFFIX, BUILD
    }

    /**
     * The different types of supported version systems.
     */
    public enum VersionType {

        /**
         * The default type of version.
         * Major, minor and patch parts are required.
         * Suffixes and build are optional.
         */
        STRICT,

        /**
         * Major part is required.
         * Minor, patch, suffixes and build are optional.
         */
        LOOSE,

        /**
         * Follows the rules of ivy.
         * Supports dynamic parts (eg: 4.2.+) and ranges. <br>
         *
         * See <a href="http://ant.apache.org/ivy/history/latest-milestone/ivyfile/dependency.html">
         * http://ant.apache.org/ivy/history/latest-milestone/ivyfile/dependency.html</a>.
         */
        IVY

    }

}
