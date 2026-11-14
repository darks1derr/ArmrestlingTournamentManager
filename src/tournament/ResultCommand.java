package tournament;

// Each command is added to history only after a successful result.
interface ResultCommand {
    void execute();

    void undo();
}

final class RecordWinnerCommand implements ResultCommand {
    private final Match match;
    private final int winnerId;

    RecordWinnerCommand(Match match, int winnerId) {
        this.match = match;
        this.winnerId = winnerId;
    }

    @Override
    public void execute() {
        match.recordWinner(winnerId);
    }

    @Override
    public void undo() {
        match.clearResult();
    }
}
