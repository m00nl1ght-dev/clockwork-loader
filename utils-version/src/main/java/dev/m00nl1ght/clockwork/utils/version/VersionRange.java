package dev.m00nl1ght.clockwork.utils.version;

import java.util.Objects;

/**
 * Modified version of {@code com.vdurmont.semver4j.Range} from
 * <a href="https://github.com/vdurmont/semver4j">https://github.com/vdurmont/semver4j</a>. <br>
 */
public class VersionRange {

    protected final Version version;
    protected final RangeOperator op;

    public VersionRange(Version version, RangeOperator op) {
        this.version = version;
        this.op = op;
    }

    public VersionRange(String version, RangeOperator op) {
        this(new Version(version, Version.VersionType.LOOSE), op);
    }

    public boolean isSatisfiedBy(String version) {
        return this.isSatisfiedBy(new Version(version, this.version.getType()));
    }

    public boolean isSatisfiedBy(Version version) {
        switch (this.op) {
            case EQ: return version.isEquivalentTo(this.version);
            case LT: return version.isLowerThan(this.version);
            case LTE: return version.isLowerThan(this.version) || version.isEquivalentTo(this.version);
            case GT: return version.isGreaterThan(this.version);
            case GTE: return version.isGreaterThan(this.version) || version.isEquivalentTo(this.version);
            default: throw new RuntimeException("Code error. Unknown RangeOperator: " + this.op); // Should never happen
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VersionRange)) return false;
        VersionRange range = (VersionRange) o;
        return Objects.equals(version, range.version) && op == range.op;
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, op);
    }

    @Override
    public String toString() {
        return this.op.asString() + this.version;
    }

    public enum RangeOperator {

        /**
         * The version and the requirement are equivalent.
         */
        EQ("="),

        /**
         * The version is lower than the requirent.
         */
        LT("<"),

        /**
         * The version is lower than or equivalent to the requirement.
         */
        LTE("<="),

        /**
         * The version is greater than the requirement.
         */
        GT(">"),

        /**
         * The version is greater than or equivalent to the requirement.
         */
        GTE(">=");

        private final String s;

        RangeOperator(String s) {
            this.s = s;
        }

        public String asString() {
            return s;
        }

    }

}
