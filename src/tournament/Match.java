package tournament;

final class Match {
    private final int id;
    private final String round;
    private final Athlete firstSeed;
    private final Athlete secondSeed;
    private final Match firstSource;
    private final Match secondSource;
    private Athlete recordedWinner;

    Match(int id, String round, Athlete first, Athlete second) {
        this(id, round, first, second, null, null);
    }

    Match(int id, String round, Match first, Match second) {
        this(id, round, null, null, first, second);
    }

    private Match(int id, String round, Athlete firstSeed, Athlete secondSeed,
                  Match firstSource, Match secondSource) {
        this.id = id;
        this.round = round;
        this.firstSeed = firstSeed;
        this.secondSeed = secondSeed;
        this.firstSource = firstSource;
        this.secondSource = secondSource;
    }

    int id() {
        return id;
    }

    String round() {
        return round;
    }

    Athlete first() {
        return firstSource == null ? firstSeed : firstSource.winner();
    }

    Athlete second() {
        return secondSource == null ? secondSeed : secondSource.winner();
    }

    boolean isBye() {
        return firstSource == null && (firstSeed == null || secondSeed == null);
    }

    Athlete winner() {
        if (isBye()) {
            return firstSeed == null ? secondSeed : firstSeed;
        }
        return recordedWinner;
    }

    boolean isReady() {
        return winner() == null && first() != null && second() != null;
    }

    void recordWinner(int athleteId) {
        if (!isReady()) {
            throw new IllegalArgumentException("That match is not ready for a result.");
        }
        Athlete first = first();
        Athlete second = second();
        if (first.id() == athleteId) {
            recordedWinner = first;
        } else if (second.id() == athleteId) {
            recordedWinner = second;
        } else {
            throw new IllegalArgumentException("The winner must be an athlete in this match.");
        }
    }

    void clearResult() {
        recordedWinner = null;
    }

    String summary() {
        Athlete winner = winner();
        String status;
        if (isBye()) {
            status = "bye -> " + display(winner);
        } else if (winner != null) {
            status = "winner: " + winner.label();
        } else {
            status = isReady() ? "ready" : "waiting for previous results";
        }
        return String.format("M%d %-14s %s vs %s | %s",
                id, round, display(first()), display(second()), status);
    }

    private String display(Athlete athlete) {
        if (athlete != null) {
            return athlete.label();
        }
        return isBye() ? "bye" : "TBD";
    }

    void accept(TournamentVisitor visitor) {
        visitor.visit(this);
    }
}
