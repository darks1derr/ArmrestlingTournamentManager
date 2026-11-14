package tournament;

final class Athlete {
    private final int id;
    private final String name;
    private final int weight;
    private final int rating;

    Athlete(int id, String name, int weight, int rating) {
        this.id = id;
        this.name = name;
        this.weight = weight;
        this.rating = rating;
    }

    int id() {
        return id;
    }

    String name() {
        return name;
    }

    int weight() {
        return weight;
    }

    int rating() {
        return rating;
    }

    String label() {
        return name + " (#" + id + ")";
    }
}
