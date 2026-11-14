package tournament;

final class WeightClass {
    private final int maximumKg;

    WeightClass(int maximumKg) {
        if (maximumKg < 0) {
            throw new IllegalArgumentException("Weight limit cannot be negative.");
        }
        this.maximumKg = maximumKg;
    }

    boolean accepts(int weight) {
        return maximumKg == 0 || weight <= maximumKg;
    }

    String label() {
        if (maximumKg == 0) {
            return "Open (above other limits)";
        }
        return "Up to " + maximumKg + " kg";
    }
}
