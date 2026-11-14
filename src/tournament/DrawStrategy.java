package tournament;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// Strategy: the tournament can choose how to order its athletes.
interface DrawStrategy {
    List<Athlete> seed(List<Athlete> athletes);
}

final class RatingDraw implements DrawStrategy {
    @Override
    public List<Athlete> seed(List<Athlete> athletes) {
        List<Athlete> sorted = new ArrayList<>(athletes);
        sorted.sort(new Comparator<Athlete>() {
            @Override
            public int compare(Athlete first, Athlete second) {
                int byRating = Integer.compare(second.rating(), first.rating());
                if (byRating != 0) {
                    return byRating;
                }
                return Integer.compare(first.id(), second.id());
            }
        });
        return sorted;
    }
}

final class RandomDraw implements DrawStrategy {
    @Override
    public List<Athlete> seed(List<Athlete> athletes) {
        List<Athlete> shuffled = new ArrayList<>(athletes);
        Collections.shuffle(shuffled);
        return shuffled;
    }
}
