package tournament;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

// The history publishes successful result changes to its observers.
final class ResultHistory {
    enum Change { RECORDED, UNDONE }

    interface Observer {
        void resultChanged(Change change);
    }

    private final Deque<ResultCommand> entries = new ArrayDeque<>();
    private final List<Observer> observers = new ArrayList<>();

    void subscribe(Observer observer) {
        observers.add(observer);
    }

    void execute(ResultCommand command) {
        command.execute();
        entries.push(command);
        notifyObservers(Change.RECORDED);
    }

    boolean canUndo() {
        return !entries.isEmpty();
    }

    void undo() {
        if (!canUndo()) {
            throw new IllegalArgumentException("There is no result to undo.");
        }
        // Results are undone in reverse order, so later matches cannot
        // retain a winner from a result that has already been undone.
        entries.pop().undo();
        notifyObservers(Change.UNDONE);
    }

    private void notifyObservers(Change change) {
        for (Observer observer : observers) {
            observer.resultChanged(change);
        }
    }
}
