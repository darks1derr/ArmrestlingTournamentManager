package tournament;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class TournamentConfig {
    private final String name;
    private final List<WeightClass> weightClasses;

    private TournamentConfig(String name, List<WeightClass> weightClasses) {
        this.name = name;
        this.weightClasses = List.copyOf(weightClasses);
    }

    String name() {
        return name;
    }

    List<WeightClass> weightClasses() {
        return weightClasses;
    }

    // Builder assembles the configuration before any athletes are registered.
    static final class Builder {
        private final String name;
        private final List<Integer> limits = new ArrayList<>();
        private boolean includeOpenClass;

        Builder(String name) {
            if (name.isBlank()) {
                throw new IllegalArgumentException("Tournament name is required.");
            }
            this.name = name.strip();
        }

        Builder addWeightLimit(int kg) {
            if (kg <= 0 || limits.contains(kg)) {
                throw new IllegalArgumentException("Weight limits must be distinct positive integers.");
            }
            limits.add(kg);
            return this;
        }

        Builder includeOpenClass() {
            includeOpenClass = true;
            return this;
        }

        TournamentConfig build() {
            List<Integer> sortedLimits = new ArrayList<>(limits);
            Collections.sort(sortedLimits);
            List<WeightClass> classes = new ArrayList<>();
            for (int limit : sortedLimits) {
                classes.add(new WeightClass(limit));
            }
            if (includeOpenClass) {
                classes.add(new WeightClass(0));
            }
            if (classes.isEmpty()) {
                throw new IllegalArgumentException("Add at least one weight class.");
            }
            return new TournamentConfig(name, classes);
        }
    }
}
