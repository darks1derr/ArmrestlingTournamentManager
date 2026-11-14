package tournament;

import java.util.ArrayList;
import java.util.List;

final class Tournament {
    private final TournamentConfig config;
    private final List<Athlete> athletes = new ArrayList<>();
    private List<Division> divisions = List.of();

    Tournament(TournamentConfig config) {
        this.config = config;
    }

    String name() {
        return config.name();
    }

    List<Athlete> athletes() {
        return List.copyOf(athletes);
    }

    List<Division> divisions() {
        return divisions;
    }

    boolean started() {
        return !divisions.isEmpty();
    }

    Athlete register(String name, int weight, int rating) {
        requireRegistrationOpen();
        if (name.isBlank() || weight <= 0 || rating < 0) {
            throw new IllegalArgumentException("Enter a name, positive weight and non-negative rating.");
        }
        weightClassFor(weight);
        Athlete athlete = new Athlete(athletes.size() + 1, name.strip(), weight, rating);
        athletes.add(athlete);
        return athlete;
    }

    WeightClass weightClassFor(int weight) {
        for (WeightClass weightClass : config.weightClasses()) {
            if (weightClass.accepts(weight)) {
                return weightClass;
            }
        }
        throw new IllegalArgumentException("No weight class accepts that weight.");
    }

    List<Division> draw(DrawStrategy strategy) {
        requireRegistrationOpen();
        List<Division> draw = new ArrayList<>();
        int nextMatchId = 1;
        for (WeightClass weightClass : config.weightClasses()) {
            List<Athlete> entrants = new ArrayList<>();
            for (Athlete athlete : athletes) {
                if (weightClassFor(athlete.weight()) == weightClass) {
                    entrants.add(athlete);
                }
            }
            Division division = new Division(weightClass, strategy.seed(entrants), nextMatchId);
            draw.add(division);
            nextMatchId += division.matches().size();
        }
        if (nextMatchId == 1) {
            throw new IllegalArgumentException("At least one weight class needs two or more athletes.");
        }
        return List.copyOf(draw);
    }

    void start(DrawStrategy strategy) {
        divisions = draw(strategy);
    }

    Match findMatch(int id) {
        for (Division division : divisions) {
            for (Match match : division.matches()) {
                if (match.id() == id) {
                    return match;
                }
            }
        }
        throw new IllegalArgumentException("Match M" + id + " was not found.");
    }

    int readyMatchCount() {
        int count = 0;
        for (Division division : divisions) {
            for (Match match : division.matches()) {
                if (match.isReady()) {
                    count++;
                }
            }
        }
        return count;
    }

    private void requireRegistrationOpen() {
        if (started()) {
            throw new IllegalArgumentException("The tournament has already started.");
        }
    }

    void accept(TournamentVisitor visitor) {
        visitor.visit(this);
        for (Division division : divisions) {
            division.accept(visitor);
        }
    }
}
