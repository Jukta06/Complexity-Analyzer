public class Complexity {
    private String complexity;

    
    public Complexity(String notation) {
        this.complexity = notation;
    }

    
    public Complexity(String notation, int exponent) {
        if (exponent == 0) {
            this.complexity = "O(1)";
        } else if (exponent == 1) {
            this.complexity = "O(" + notation + ")";
        } else {
            this.complexity = "O(" + notation + "^" + exponent + ")";
        }
    }

    
    public Complexity(String notation, boolean isKnown) {
        if (isKnown) {
            this.complexity = "O(" + notation + ")";
        } else {
            this.complexity = notation;
        }
    }

   
    public Complexity add(Complexity other) {
        if (this.complexity.equals("O(1)")) {
            return other;
        } else if (other.complexity.equals("O(1)")) {
            return this;
        } else {
            return new Complexity("O(" + this.complexity.substring(2, this.complexity.length() - 1) + " + " +
                    other.complexity.substring(2, other.complexity.length() - 1) + ")");
        }
    }

    
    public Complexity multiply(Complexity other) {
        return new Complexity("O(" + this.complexity.substring(2, this.complexity.length() - 1) + " * " +
                other.complexity.substring(2, other.complexity.length() - 1) + ")");
    }

    @Override
    public String toString() {
        return complexity;
    }
}
