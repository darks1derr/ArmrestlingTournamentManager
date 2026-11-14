package tournament;

import java.io.PrintStream;
import java.util.List;

final class TournamentFacade {
    private final Tournament tournament;
    private final ResultHistory history = new ResultHistory();

    TournamentFacade(TournamentConfig config, PrintStream output) {
        this(config);
        history.subscribe(change -> output.printf("[Results] %s. Matches ready: %d%n",
                change == ResultHistory.Change.RECORDED ? "Recorded" : "Undone",
                tournament.readyMatchCount()));
        history.subscribe(change -> output.printf("[Bracket] Updated%n%s", bracket()));
    }

    TournamentFacade(TournamentConfig config) {
        tournament = new Tournament(config);
    }

    void onResultChange(ResultHistory.Observer observer) {
        history.subscribe(observer);
    }

    boolean started() {
        return tournament.started();
    }

    String name() {
        return tournament.name();
    }

    List<Athlete> athletes() {
        return tournament.athletes();
    }

    String weightClassLabel(Athlete athlete) {
        return tournament.weightClassFor(athlete.weight()).label();
    }

    List<Division> previewDivisions(DrawStrategy strategy) {
        return tournament.draw(strategy);
    }

    List<Division> activeDivisions() {
        requireStarted();
        return tournament.divisions();
    }

    String register(String name, int weight, int rating) {
        Athlete athlete = tournament.register(name, weight, rating);
        return "Registered " + athlete.label() + " | " + tournament.weightClassFor(weight).label();
    }

    String roster() {
        StringBuilder text = new StringBuilder();
        for (Athlete athlete : tournament.athletes()) {
            text.append(String.format("%s | %d kg | rating %d | %s%n", athlete.label(),
                    athlete.weight(), athlete.rating(), tournament.weightClassFor(athlete.weight()).label()));
        }
        return text.isEmpty() ? "No athletes registered." + System.lineSeparator() : text.toString();
    }

    String preview(DrawStrategy strategy) {
        return formatBracket(tournament.draw(strategy));
    }

    void start(DrawStrategy strategy) {
        tournament.start(strategy);
    }

    String bracket() {
        requireStarted();
        return formatBracket(tournament.divisions());
    }

    void recordResult(int matchId, int winnerId) {
        requireStarted();
        history.execute(new RecordWinnerCommand(tournament.findMatch(matchId), winnerId));
    }

    boolean canUndo() {
        return history.canUndo();
    }

    void undoResult() {
        history.undo();
    }

    String report() {
        requireStarted();
        ReportVisitor visitor = new ReportVisitor();
        tournament.accept(visitor);
        return visitor.report();
    }

    private void requireStarted() {
        if (!tournament.started()) {
            throw new IllegalArgumentException("Start the tournament first.");
        }
    }

    private String formatBracket(List<Division> divisions) {
        StringBuilder text = new StringBuilder();
        for (Division division : divisions) {
            text.append(String.format("%s | entrants: %d%n", division.label(), division.entrantCount()));
            if (division.matches().isEmpty()) {
                text.append(String.format("  At least two athletes are needed in this class.%n"));
            }
            for (Match match : division.matches()) {
                text.append(String.format("  %s%n", match.summary()));
            }
        }
        return text.toString();
    }
}
