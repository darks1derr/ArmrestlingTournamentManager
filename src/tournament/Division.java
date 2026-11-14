package tournament;

import java.util.ArrayList;
import java.util.List;

final class Division {
    private final WeightClass weightClass;
    private final List<Athlete> entrants;
    private final List<Match> matches;

    Division(WeightClass weightClass, List<Athlete> orderedAthletes, int firstMatchId) {
        this.weightClass = weightClass;
        entrants = List.copyOf(orderedAthletes);
        matches = buildBracket(firstMatchId);
    }

    private List<Match> buildBracket(int nextId) {
        if (entrants.size() < 2) {
            return List.of();
        }

        // Expand the seeding order by inserting each seed's complement.
        // For four slots this produces 1–4 and 2–3.
        List<Integer> seeds = List.of(1);
        while (seeds.size() < entrants.size()) {
            int complement = seeds.size() * 2 + 1;
            List<Integer> expanded = new ArrayList<>();
            for (int seed : seeds) {
                expanded.add(seed);
                expanded.add(complement - seed);
            }
            seeds = expanded;
        }

        List<Match> round = new ArrayList<>();
        for (int index = 0; index < seeds.size(); index += 2) {
            round.add(new Match(nextId++, roundName(seeds.size() / 2),
                    entrantAtSeed(seeds.get(index)), entrantAtSeed(seeds.get(index + 1))));
        }

        List<Match> bracket = new ArrayList<>(round);
        while (round.size() > 1) {
            List<Match> nextRound = new ArrayList<>();
            for (int index = 0; index < round.size(); index += 2) {
                nextRound.add(new Match(nextId++, roundName(round.size() / 2),
                        round.get(index), round.get(index + 1)));
            }
            bracket.addAll(nextRound);
            round = nextRound;
        }
        return List.copyOf(bracket);
    }

    private Athlete entrantAtSeed(int seed) {
        return seed <= entrants.size() ? entrants.get(seed - 1) : null;
    }

    private static String roundName(int matchCount) {
        if (matchCount == 1) {
            return "Final";
        }
        if (matchCount == 2) {
            return "Semifinal";
        }
        if (matchCount == 4) {
            return "Quarterfinal";
        }
        return "Round of " + matchCount * 2;
    }

    String label() {
        return weightClass.label();
    }

    int entrantCount() {
        return entrants.size();
    }

    List<Match> matches() {
        return matches;
    }

    Athlete champion() {
        // A class needs at least two entrants to award a tournament win.
        return matches.isEmpty() ? null : matches.get(matches.size() - 1).winner();
    }

    void accept(TournamentVisitor visitor) {
        visitor.visit(this);
        for (Match match : matches) {
            match.accept(visitor);
        }
    }
}
