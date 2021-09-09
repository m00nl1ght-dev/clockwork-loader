package dev.m00nl1ght.clockwork.utils.version;

import dev.m00nl1ght.clockwork.utils.version.Version.VersionType;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Modified version of {@code com.vdurmont.semver4j.Requirement} from
 * <a href="https://github.com/vdurmont/semver4j">https://github.com/vdurmont/semver4j</a>. <br>
 */
public class VersionRequirement {

    private static final Pattern IVY_DYNAMIC_PATCH_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.\\+");
    private static final Pattern IVY_DYNAMIC_MINOR_PATTERN = Pattern.compile("(\\d+)\\.\\+");
    private static final Pattern IVY_LATEST_PATTERN = Pattern.compile("latest\\.\\w+");
    private static final Pattern IVY_MATH_BOUNDED_PATTERN = Pattern.compile("([\\[\\]])([\\d.]+),([\\d.]+)([\\[\\]])");
    private static final Pattern IVY_MATH_LOWER_UNBOUNDED_PATTERN = Pattern.compile("\\(,([\\d.]+)([\\[\\]])");
    private static final Pattern IVY_MATH_UPPER_UNBOUNDED_PATTERN = Pattern.compile("([\\[\\]])([\\d.]+),\\)");

    protected final VersionRange range;
    protected final VersionRequirement req1;
    protected final RequirementOperator op;
    protected final VersionRequirement req2;

    /**
     * Builds a requirement. <br>
     *
     * A requirement has to be a range or a combination of an operator and 2 other requirements.
     *
     * @param range the range that will be used for the requirement (optional if all other params are provided)
     * @param req1 the requirement used as a left operand (requires the `op` and `req2` params to be provided)
     * @param op the operator used between the requirements (requires the `req1` and `req2` params to be provided)
     * @param req2 the requirement used as a right operand (requires the `req1` and `op` params to be provided)
     */
    protected VersionRequirement(VersionRange range, VersionRequirement req1, RequirementOperator op, VersionRequirement req2) {
        this.range = range;
        this.req1 = req1;
        this.op = op;
        this.req2 = req2;
    }

    /**
     * Builds a requirement (will test that the version is equivalent to the requirement).
     *
     * @param requirement the version of the requirement
     * @return the generated requirement
     */
    public static VersionRequirement build(Version requirement) {
        return new VersionRequirement(new VersionRange(requirement, VersionRange.RangeOperator.EQ), null, null, null);
    }

    /**
     * Builds a strict requirement (will test that the version is equivalent to the requirement).
     *
     * @param requirement the version of the requirement
     * @return the generated requirement
     */
    public static VersionRequirement buildStrict(String requirement) {
        return build(new Version(requirement, VersionType.STRICT));
    }

    /**
     * Builds a loose requirement (will test that the version is equivalent to the requirement).
     *
     * @param requirement the version of the requirement
     * @return the generated requirement
     */
    public static VersionRequirement buildLoose(String requirement) {
        return build(new Version(requirement, VersionType.LOOSE));
    }

    /**
     * Builds a requirement following the rules of Ivy.
     *
     * @param requirement the requirement as a string
     * @return the generated requirement
     */
    public static VersionRequirement buildIvy(String requirement) {
        try {return buildLoose(requirement);} catch (VersionException ignored) {}

        Matcher matcher = IVY_DYNAMIC_PATCH_PATTERN.matcher(requirement);
        if (matcher.find()) {
            int major = Integer.parseInt(matcher.group(1));
            int minor = Integer.parseInt(matcher.group(2));
            VersionRequirement lower = new VersionRequirement(new VersionRange(major + "." + minor + ".0", VersionRange.RangeOperator.GTE), null, null, null);
            VersionRequirement upper = new VersionRequirement(new VersionRange(major + "." + (minor + 1) + ".0", VersionRange.RangeOperator.LT), null, null, null);
            return new VersionRequirement(null, lower, RequirementOperator.AND, upper);
        }

        matcher = IVY_DYNAMIC_MINOR_PATTERN.matcher(requirement);
        if (matcher.find()) {
            int major = Integer.parseInt(matcher.group(1));
            VersionRequirement lower = new VersionRequirement(new VersionRange(major + ".0.0", VersionRange.RangeOperator.GTE), null, null, null);
            VersionRequirement upper = new VersionRequirement(new VersionRange((major + 1) + ".0.0", VersionRange.RangeOperator.LT), null, null, null);
            return new VersionRequirement(null, lower, RequirementOperator.AND, upper);
        }

        matcher = IVY_LATEST_PATTERN.matcher(requirement);
        if (matcher.find()) {
            return new VersionRequirement(new VersionRange("0.0.0", VersionRange.RangeOperator.GTE), null, null, null);
        }

        matcher = IVY_MATH_BOUNDED_PATTERN.matcher(requirement);
        if (matcher.find()) {
            VersionRange.RangeOperator lowerOp = "[".equals(matcher.group(1)) ? VersionRange.RangeOperator.GTE : VersionRange.RangeOperator.GT;
            Version lowerVersion = new Version(matcher.group(2), VersionType.LOOSE);
            Version upperVersion = new Version(matcher.group(3), VersionType.LOOSE);
            VersionRange.RangeOperator upperOp = "]".equals(matcher.group(4)) ? VersionRange.RangeOperator.LTE : VersionRange.RangeOperator.LT;
            VersionRequirement lower = new VersionRequirement(new VersionRange(extrapolateVersion(lowerVersion), lowerOp), null, null, null);
            VersionRequirement upper = new VersionRequirement(new VersionRange(extrapolateVersion(upperVersion), upperOp), null, null, null);
            return new VersionRequirement(null, lower, RequirementOperator.AND, upper);
        }

        matcher = IVY_MATH_LOWER_UNBOUNDED_PATTERN.matcher(requirement);
        if (matcher.find()) {
            Version version = new Version(matcher.group(1), VersionType.LOOSE);
            VersionRange.RangeOperator op = "]".equals(matcher.group(2)) ? VersionRange.RangeOperator.LTE : VersionRange.RangeOperator.LT;
            return new VersionRequirement(new VersionRange(extrapolateVersion(version), op), null, null, null);
        }

        matcher = IVY_MATH_UPPER_UNBOUNDED_PATTERN.matcher(requirement);
        if (matcher.find()) {
            VersionRange.RangeOperator op = "[".equals(matcher.group(1)) ? VersionRange.RangeOperator.GTE : VersionRange.RangeOperator.GT;
            Version version = new Version(matcher.group(2), VersionType.LOOSE);
            return new VersionRequirement(new VersionRange(extrapolateVersion(version), op), null, null, null);
        }

        throw new VersionException("Invalid requirement");
    }

    /**
     * Extrapolates the optional minor and patch numbers.
     *
     * @param version the original version
     * @return a version with the extrapolated minor and patch numbers
     */
    private static Version extrapolateVersion(Version version) {
        StringBuilder sb = new StringBuilder()
                .append(version.getMajor())
                .append(".")
                .append(version.getMinor() == null ? 0 : version.getMinor())
                .append(".")
                .append(version.getPatch() == null ? 0 : version.getPatch());

        boolean first = true;
        for (int i = 0; i < version.getSuffixTokens().length; i++) {
            if (first) {
                sb.append("-");
                first = false;
            } else {
                sb.append(".");
            }
            sb.append(version.getSuffixTokens()[i]);
        }

        if (version.getBuild() != null) {
            sb.append("+").append(version.getBuild());
        }

        return new Version(sb.toString(), version.getType());
    }

    /**
     * Checks if the requirement is satisfied by a version.
     *
     * @param version the version that will be checked
     * @return true if the version satisfies the requirement
     */
    public boolean isSatisfiedBy(String version) {
        if (this.range != null) {
            return this.isSatisfiedBy(new Version(version, this.range.version.getType()));
        } else {
            return this.isSatisfiedBy(new Version(version));
        }
    }

    /**
     * Checks if the requirement is satisfied by a version.
     *
     * @param version the version that will be checked
     * @return true if the version satisfies the requirement
     */
    public boolean isSatisfiedBy(Version version) {
        if (this.range != null) {
            // We are on a leaf
            return this.range.isSatisfiedBy(version);
        } else {
            // We have several sub-requirements
            switch (this.op) {
                case AND:
                    try {
                        List<VersionRange> set = getAllRanges(this, new ArrayList<>());
                        for (VersionRange range : set) if (!range.isSatisfiedBy(version)) return false;
                        if (version.getSuffixTokens().length > 0) {
                            // Find the set of versions that are allowed to have prereleases
                            // For example, ^1.2.3-pr.1 desugars to >=1.2.3-pr.1 <2.0.0
                            // That should allow `1.2.3-pr.2` to pass.
                            // However, `1.2.4-alpha.notready` should NOT be allowed,
                            // even though it's within the range set by the comparators.
                            // Version has a -pre, but it's not one of the ones we like.
                            return set.stream().filter(range -> range.version != null)
                                    .filter(range -> range.version.getSuffixTokens().length > 0).map(range -> range.version)
                                    .anyMatch(allowed -> Objects.equals(version.getMajor(), allowed.getMajor()) &&
                                    Objects.equals(version.getMinor(), allowed.getMinor()) &&
                                    Objects.equals(version.getPatch(), allowed.getPatch()));
                        }
                        return true;
                    } catch (Exception e) {
                        // Could be that we have a OR in AND - fallback to default test
                        return this.req1.isSatisfiedBy(version) && this.req2.isSatisfiedBy(version);
                    }
                case OR:
                    return this.req1.isSatisfiedBy(version) || this.req2.isSatisfiedBy(version);
                default:
                    throw new RuntimeException("Code error. Unknown RequirementOperator: " + this.op); // Should never happen
            }
        }
    }

    private List<VersionRange> getAllRanges(VersionRequirement requirement, List<VersionRange> res) {
        if (requirement.range != null) {
            res.add(requirement.range);
        } else if (requirement.op == RequirementOperator.AND) {
            getAllRanges(requirement.req1, res);
            getAllRanges(requirement.req2, res);
        } else {
            throw new RuntimeException("OR in AND not allowed");
        }
        return res;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VersionRequirement)) return false;
        VersionRequirement that = (VersionRequirement) o;
        return Objects.equals(range, that.range) &&
                Objects.equals(req1, that.req1) &&
                op == that.op &&
                Objects.equals(req2, that.req2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(range, req1, op, req2);
    }

    @Override
    public String toString() {
        if (this.range != null) return this.range.toString();
        return this.req1 + " " + (this.op == RequirementOperator.OR ? this.op.asString() + " " : "") + this.req2;
    }

    /**
     * The operators that can be used in a requirement.
     */
    protected enum RequirementOperator {

        AND(""), OR("||");

        private final String s;

        RequirementOperator(String s) {
            this.s = s;
        }

        public String asString() {
            return s;
        }

    }

}
