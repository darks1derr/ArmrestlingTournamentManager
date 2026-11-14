package tournament;

interface TournamentVisitor {
    void visit(Tournament tournament);

    void visit(Division division);

    void visit(Match match);
}

final class ReportVisitor implements TournamentVisitor {
    private final StringBuilder report = new StringBuilder();

    @Override
    public void visit(Tournament tournament) {
        report.append("=== ").append(tournament.name()).append(" | Tournament Report ===")
                .append(System.lineSeparator());
        report.append("Registered athletes: ").append(tournament.athletes().size())
                .append(System.lineSeparator());
    }

    @Override
    public void visit(Division division) {
        report.append(System.lineSeparator()).append(division.label())
                .append(" | Entrants: ").append(division.entrantCount())
                .append(System.lineSeparator());
        if (division.entrantCount() < 2) {
            report.append("No contested bracket");
        } else if (division.champion() == null) {
            report.append("Champion: pending");
        } else {
            report.append("Champion: ").append(division.champion().label());
        }
        report.append(System.lineSeparator());
    }

    @Override
    public void visit(Match match) {
        report.append("  ").append(match.summary()).append(System.lineSeparator());
    }

    String report() {
        return report.toString();
    }
}
