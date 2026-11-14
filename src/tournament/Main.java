package tournament;

import java.io.PrintStream;
import java.util.Scanner;

public final class Main {
    private final Scanner input;
    private final PrintStream output;

    private Main(Scanner input, PrintStream output) {
        this.input = input;
        this.output = output;
    }

    public static void main(String[] args) {
        new Main(new Scanner(System.in), System.out).run();
    }

    private void run() {
        output.println("=== Armwrestling Tournament ===");
        try {
            TournamentFacade tournament = chooseTournament();
            while (true) {
                output.println("""
                        
                        1. Register athlete         2. List athletes
                        3. Preview bracket          4. Start tournament
                        5. Show active bracket      6. Record match result
                        7. Undo last result         8. Tournament report
                        0. Exit""");
                try {
                    switch (ask("Option: ")) {
                        case "0" -> { return; }
                        case "1" -> output.println(tournament.register(ask("Name: "),
                                number("Weight (kg): "), number("Rating: ")));
                        case "2" -> output.print(tournament.roster());
                        case "3" -> output.print(tournament.preview(chooseStrategy()));
                        case "4" -> {
                            tournament.start(chooseStrategy());
                            output.print(tournament.bracket());
                        }
                        case "5" -> output.print(tournament.bracket());
                        case "6" -> tournament.recordResult(number("Match number (without M): "),
                                number("Winner athlete ID (without #): "));
                        case "7" -> tournament.undoResult();
                        case "8" -> output.print(tournament.report());
                        default -> output.println("Choose an option from the menu.");
                    }
                } catch (IllegalArgumentException error) {
                    output.println("Error: " + error.getMessage());
                }
            }
        } catch (EndOfInput end) {
            output.println();
        }
    }

    private TournamentFacade chooseTournament() {
        while (true) {
            String mode = ask("1. Create tournament   2. Load demo roster   0. Exit: ");
            try {
                switch (mode) {
                    case "0" -> throw new EndOfInput();
                    case "1" -> { return createTournament(); }
                    case "2" -> { return demoTournament(); }
                    default -> output.println("Choose 1, 2 or 0.");
                }
            } catch (IllegalArgumentException error) {
                output.println("Error: " + error.getMessage());
            }
        }
    }

    private TournamentFacade createTournament() {
        TournamentConfig.Builder builder = new TournamentConfig.Builder(ask("Tournament name: "));
        String limits = ask("Weight limits in kg, separated by commas (blank for open only): ");
        if (!limits.isEmpty()) {
            for (String limit : limits.split(",", -1)) {
                builder.addWeightLimit(parseNumber(limit.strip()));
            }
        }
        String open = limits.isEmpty() ? "y" : ask("Add a class above the highest limit? (y/n): ");
        if (open.equalsIgnoreCase("y")) {
            builder.includeOpenClass();
        } else if (!open.equalsIgnoreCase("n")) {
            throw new IllegalArgumentException("Enter y or n.");
        }
        return new TournamentFacade(builder.build(), output);
    }

    private TournamentFacade demoTournament() {
        TournamentConfig config = new TournamentConfig.Builder("Campus Armwrestling Cup")
                .addWeightLimit(80)
                .build();
        TournamentFacade tournament = new TournamentFacade(config, output);
        tournament.register("Arman", 75, 100);
        tournament.register("Dias", 78, 90);
        tournament.register("Ayan", 76, 80);
        tournament.register("Miras", 79, 70);
        output.print(tournament.roster());
        return tournament;
    }

    private DrawStrategy chooseStrategy() {
        String option = ask("Draw type (1 = rating, 2 = random): ");
        if (option.equals("1")) {
            return new RatingDraw();
        }
        if (option.equals("2")) {
            return new RandomDraw();
        }
        throw new IllegalArgumentException("Choose draw type 1 or 2.");
    }

    private int number(String prompt) {
        return parseNumber(ask(prompt));
    }

    private static int parseNumber(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException("Enter a whole number.");
        }
    }

    private String ask(String prompt) {
        output.print(prompt);
        output.flush();
        if (!input.hasNextLine()) {
            throw new EndOfInput();
        }
        return input.nextLine().strip();
    }

    private static final class EndOfInput extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
