public class TimeComplexity {

    public enum ComplexityType {
        CONSTANT, LINEAR, QUADRATIC, CUBIC, LOGARITHMIC, LINEARITHMIC, EXPONENTIAL, UNKNOWN
    }

    private String description;
    private ComplexityType complexity;
    private int degree; 
    public static final TimeComplexity CONSTANT = new TimeComplexity("Constant", ComplexityType.CONSTANT, 0);
    public static final TimeComplexity UNKNOWN = new TimeComplexity("Unknown", ComplexityType.UNKNOWN, 0);

    public TimeComplexity(String description, ComplexityType complexity, int degree) {
        this.description = description;
        this.complexity = complexity;
        this.degree = degree;
    }

    public ComplexityType getComplexity() {
        return complexity;
    }

    public int getDegree() {
        return degree;
    }

    public String getDescription() {
        return description;
    }

    public static TimeComplexity max(TimeComplexity t1, TimeComplexity t2) {
        if (t1.complexity.ordinal() > t2.complexity.ordinal()) {
            return t1;
        } else if (t1.complexity.ordinal() < t2.complexity.ordinal()) {
            return t2;
        } else {
            return (t1.degree >= t2.degree) ? t1 : t2;
        }
    }

    @Override
    public String toString() {
        switch (complexity) {
            case CONSTANT:
                return "O(1)";
            case LINEAR:
                return "O(n)";
            case QUADRATIC:
                return "O(n^2)";
            case CUBIC:
                return "O(n^3)";
            case LOGARITHMIC:
                return "O(log n)";
            case LINEARITHMIC:
                return "O(n log n)";
            case EXPONENTIAL:
                return "O(2^n)";
            default:
                return "O(?)";
        }
    }
}
