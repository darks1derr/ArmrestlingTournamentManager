package tournament;

import static tournament.DashboardStyle.*;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

final class TournamentDashboard extends JPanel {
    private static final String[] DRAW_TYPES = {"Rating seeding", "Random draw"};

    private enum View {
        ATHLETES("Athletes"), BRACKET("Bracket"), REPORT("Report");

        private final String title;
        View(String title) { this.title = title; }
    }

    private final JPanel sidebar = stack(DARK);
    private final JPanel page = new JPanel(new BorderLayout(0, 18));
    private final JLabel status = label(" ", 13, false, MUTED);
    private final JComboBox<String> drawType = new JComboBox<>(DRAW_TYPES);
    private TournamentFacade tournament;
    private View view = View.ATHLETES;
    private List<Division> preview = List.of();
    private int divisionIndex;

    TournamentDashboard() {
        super(new BorderLayout());
        setBackground(PAPER);
        sidebar.setPreferredSize(new Dimension(204, 0));
        sidebar.setBorder(padding(28, 18, 22, 18));
        page.setBackground(PAPER);
        page.setBorder(padding(26, 28, 6, 28));
        status.setBorder(padding(9, 30, 13, 20));
        JPanel workspace = new JPanel(new BorderLayout());
        workspace.setBackground(PAPER);
        workspace.add(page);
        workspace.add(status, BorderLayout.SOUTH);
        add(sidebar, BorderLayout.WEST);
        add(workspace);

        useTournament(demoConfig(), true);
    }

    private static TournamentConfig demoConfig() {
        return new TournamentConfig.Builder("Campus Armwrestling Cup")
                .addWeightLimit(80).includeOpenClass().build();
    }

    private void useTournament(TournamentConfig config, boolean demo) {
        tournament = new TournamentFacade(config);
        if (demo) {
            tournament.register("Arman Sadykov", 75, 100);
            tournament.register("Dias Karimov", 78, 90);
            tournament.register("Ayan Zhaksylyk", 76, 80);
            tournament.register("Miras Bekov", 79, 70);
        }
        tournament.onResultChange(change -> {
            boolean recorded = change == ResultHistory.Change.RECORDED;
            refresh(recorded ? "Result saved. Bracket updated." : "Last result undone. Bracket updated.");
        });
        preview = List.of();
        divisionIndex = 0;
        view = View.ATHLETES;
        refresh(demo ? "Demo roster loaded. Open Bracket to begin." : "Tournament created. Add your athletes.");
    }

    private void refresh(String message) {
        status.setText(message.isBlank() ? " " : message);
        rebuildSidebar();
        page.removeAll();
        page.add(header(), BorderLayout.NORTH);
        JComponent body = switch (view) {
            case ATHLETES -> athleteTable();
            case BRACKET -> bracketView();
            case REPORT -> reportView();
        };
        page.add(body);
        revalidate();
        repaint();
    }

    private void rebuildSidebar() {
        sidebar.removeAll();
        sidebar.add(label("Armwrestling", 23, true, SIDE_TEXT));
        sidebar.add(Box.createVerticalStrut(2));
        sidebar.add(label("Tournament Manager", 13, false, SIDE_MUTED));
        sidebar.add(Box.createVerticalStrut(35));
        for (View destination : View.values()) {
            sidebar.add(sideButton(destination.title, view == destination, () -> {
                view = destination;
                refresh("");
            }));
            sidebar.add(Box.createVerticalStrut(6));
        }
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(sideButton("New tournament", false, this::createTournament));
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(sideButton("Reload demo", false, this::reloadDemo));
    }

    private JComponent header() {
        JPanel header = stack(PAPER);
        JLabel name = label(tournament.name(), 13, true, ORANGE);
        name.setToolTipText(tournament.name());
        header.add(name);
        header.add(Box.createVerticalStrut(8));
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.add(label(view.title, 34, true, INK), BorderLayout.WEST);
        JPanel actions = toolbar();
        switch (view) {
            case ATHLETES -> {
                JButton add = button("Add athlete", true, this::registerAthlete);
                add.setEnabled(!tournament.started());
                actions.add(add);
                if (!tournament.started()) {
                    actions.add(button("Open bracket", false, () -> {
                        view = View.BRACKET;
                        refresh("");
                    }));
                }
            }
            case BRACKET -> {
                if (tournament.started()) {
                    JButton undo = button("Undo last result", false, () -> run(tournament::undoResult));
                    undo.setEnabled(tournament.canUndo());
                    actions.add(undo);
                } else {
                    drawType.setFont(new Font(FONT, Font.PLAIN, 13));
                    actions.add(drawType);
                    actions.add(button("Preview", false, this::previewBracket));
                    actions.add(button("Start tournament", true, this::startTournament));
                }
            }
            case REPORT -> {
                if (tournament.started()) {
                    actions.add(button("Copy report", false, () -> {
                        JTextArea copy = new JTextArea(tournament.report());
                        copy.selectAll();
                        copy.copy();
                        status.setText("Report copied.");
                    }));
                }
            }
        }
        row.add(actions, BorderLayout.EAST);
        header.add(row);
        return header;
    }

    private JComponent athleteTable() {
        List<Athlete> athletes = tournament.athletes();
        JPanel box = new JPanel(new BorderLayout(0, 12));
        box.setBackground(WHITE);
        box.setBorder(cardBorder(18));
        String caption = athletes.size() + " registered" +
                (tournament.started() ? "  /  Registration closed" : "  /  Registration open");
        box.add(label(caption, 14, true, MUTED), BorderLayout.NORTH);
        if (athletes.isEmpty()) {
            box.add(empty("Add athletes to start building the bracket."));
            return box;
        }

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID", "Athlete", "Weight (kg)", "Rating", "Division"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }

            @Override
            public Class<?> getColumnClass(int column) {
                return switch (column) {
                    case 0, 2, 3 -> Integer.class;
                    default -> String.class;
                };
            }
        };
        for (Athlete athlete : athletes) {
            model.addRow(new Object[]{athlete.id(), athlete.name(), athlete.weight(),
                    athlete.rating(), tournament.weightClassLabel(athlete)});
        }
        JTable table = new JTable(model);
        table.setFont(new Font(FONT, Font.PLAIN, 14));
        table.setForeground(INK);
        table.setBackground(WHITE);
        table.setGridColor(LINE);
        table.setShowVerticalLines(false);
        table.setRowHeight(42);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setFont(new Font(FONT, Font.BOLD, 13));
        table.getTableHeader().setForeground(INK);
        table.getTableHeader().setBackground(PAPER);
        table.getTableHeader().setPreferredSize(new Dimension(0, 34));
        table.getColumnModel().getColumn(0).setMaxWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(205);
        table.getColumnModel().getColumn(4).setPreferredWidth(180);
        box.add(scroll(table, WHITE));
        return box;
    }

    private void previewBracket() {
        run(() -> {
            preview = tournament.previewDivisions(strategy());
            divisionIndex = firstContestedDivision(preview);
            refresh("Preview only. Starting a random draw shuffles the athletes again.");
        });
    }

    private void startTournament() {
        run(() -> {
            tournament.start(strategy());
            divisionIndex = firstContestedDivision(tournament.activeDivisions());
            preview = List.of();
            refresh("Tournament started. Select a ready match to record its winner.");
        });
    }

    private static int firstContestedDivision(List<Division> divisions) {
        for (int i = 0; i < divisions.size(); i++) {
            if (!divisions.get(i).matches().isEmpty()) {
                return i;
            }
        }
        return 0;
    }

    private DrawStrategy strategy() {
        return drawType.getSelectedIndex() == 0 ? new RatingDraw() : new RandomDraw();
    }

    private JComponent bracketView() {
        List<Division> divisions = tournament.started() ? tournament.activeDivisions() : preview;
        if (divisions.isEmpty()) {
            return empty("Choose a draw type, then preview the pairings or start the tournament.");
        }

        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 3));
        controls.setOpaque(false);
        JComboBox<String> selector = new JComboBox<>();
        for (Division division : divisions) {
            selector.addItem(division.label());
        }
        selector.setSelectedIndex(divisionIndex);
        selector.addActionListener(event -> {
            divisionIndex = selector.getSelectedIndex();
            refresh("");
        });
        controls.add(label("Weight class", 13, true, MUTED));
        controls.add(selector);
        Division division = divisions.get(divisionIndex);
        controls.add(label(division.entrantCount() + " athletes", 13, false, MUTED));
        if (division.champion() != null) {
            controls.add(label("Champion  /  " + division.champion().name(), 14, true, GREEN));
        }
        panel.add(controls, BorderLayout.NORTH);
        if (division.matches().isEmpty()) {
            panel.add(empty("At least two athletes are needed in this weight class."));
            return panel;
        }

        JPanel rounds = new JPanel();
        rounds.setBackground(PAPER);
        rounds.setLayout(new BoxLayout(rounds, BoxLayout.X_AXIS));
        rounds.setBorder(padding(2, 2, 12, 2));
        int roundSize = (division.matches().size() + 1) / 2;
        int offset = 0;
        while (roundSize >= 1) {
            if (offset > 0) {
                rounds.add(Box.createHorizontalStrut(15));
            }
            rounds.add(roundColumn(division.matches().subList(offset, offset + roundSize)));
            offset += roundSize;
            roundSize /= 2;
        }
        panel.add(scroll(rounds, PAPER));
        return panel;
    }

    private JComponent roundColumn(List<Match> matches) {
        JPanel cards = stack(PAPER);
        cards.setAlignmentY(Component.TOP_ALIGNMENT);
        cards.setBorder(padding(0, 0, 8, 0));
        cards.add(label(matches.get(0).round(), 16, true, INK));
        cards.add(Box.createVerticalStrut(10));
        for (Match match : matches) {
            cards.add(matchCard(match));
            cards.add(Box.createVerticalStrut(12));
        }
        cards.setPreferredSize(new Dimension(220, cards.getPreferredSize().height));
        cards.setMinimumSize(cards.getPreferredSize());
        cards.setMaximumSize(cards.getPreferredSize());
        return cards;
    }

    private JComponent matchCard(Match match) {
        JPanel card = stack(WHITE);
        card.setBorder(cardBorder(13));
        card.setAlignmentX(LEFT_ALIGNMENT);
        Athlete winner = match.winner();
        String stage = "Waiting";
        if (match.isBye()) {
            stage = "Bye";
        } else if (winner != null) {
            stage = "Complete";
        } else if (match.isReady()) {
            stage = "Ready";
        }
        card.add(label("M" + match.id() + "  /  " + stage, 12, true, winner != null ? GREEN : ORANGE));
        card.add(Box.createVerticalStrut(8));
        card.add(athleteLabel(match, match.first()));
        card.add(Box.createVerticalStrut(4));
        card.add(athleteLabel(match, match.second()));
        if (tournament.started() && match.isReady()) {
            card.add(Box.createVerticalStrut(10));
            card.add(button("Record winner", true, () -> chooseWinner(match)));
        }
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
        return card;
    }

    private JLabel athleteLabel(Match match, Athlete athlete) {
        if (athlete == null) {
            return label(match.isBye() ? "Bye" : "To be decided", 14, false, MUTED);
        }
        JLabel name = label(athlete.label(), 14, true, athlete.equals(match.winner()) ? GREEN : INK);
        name.setToolTipText(athlete.label());
        name.setMaximumSize(new Dimension(192, name.getPreferredSize().height));
        return name;
    }

    private void chooseWinner(Match match) {
        Athlete first = match.first();
        Athlete second = match.second();
        String[] options = {first.label(), second.label()};
        int index = JOptionPane.showOptionDialog(this, "Who won match M" + match.id() + "?",
                "Record winner", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, null);
        if (index >= 0) {
            run(() -> tournament.recordResult(match.id(), index == 0 ? first.id() : second.id()));
        }
    }

    private JComponent reportView() {
        if (!tournament.started()) {
            return empty("Start the tournament to see the match report.");
        }
        JTextArea report = new JTextArea(tournament.report());
        report.setEditable(false);
        report.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        report.setBackground(WHITE);
        report.setForeground(INK);
        report.setBorder(padding(20, 20, 20, 20));
        report.setCaretPosition(0);
        JScrollPane pane = scroll(report, WHITE);
        pane.setBorder(BorderFactory.createLineBorder(LINE));
        return pane;
    }

    private void registerAthlete() {
        JTextField name = new JTextField(20);
        JTextField weight = new JTextField(20);
        JTextField rating = new JTextField("0", 20);
        JPanel form = form(new String[]{"Name", "Weight (kg)", "Rating"}, name, weight, rating);
        while (confirmForm(form, "Add athlete")) {
            try {
                String message = tournament.register(name.getText(), number(weight.getText()), number(rating.getText()));
                preview = List.of();
                refresh(message);
                return;
            } catch (IllegalArgumentException error) {
                showError(error);
            }
        }
    }

    private void createTournament() {
        JTextField name = new JTextField(20);
        JTextField limits = new JTextField("70, 80, 90", 20);
        JCheckBox open = new JCheckBox("Include an open weight class", true);
        JPanel form = form(new String[]{"Tournament name", "Weight limits (kg, comma-separated)"}, name, limits);
        form.add(Box.createVerticalStrut(8));
        form.add(open);
        while (confirmForm(form, "New tournament")) {
            try {
                TournamentConfig.Builder builder = new TournamentConfig.Builder(name.getText());
                if (!limits.getText().isBlank()) {
                    for (String limit : limits.getText().split(",", -1)) {
                        builder.addWeightLimit(number(limit));
                    }
                }
                if (open.isSelected()) {
                    builder.includeOpenClass();
                }
                TournamentConfig config = builder.build();
                if (confirmReplacement()) {
                    useTournament(config, false);
                }
                return;
            } catch (IllegalArgumentException error) {
                showError(error);
            }
        }
    }

    private void reloadDemo() {
        if (confirmReplacement()) {
            useTournament(demoConfig(), true);
        }
    }

    private boolean confirmReplacement() {
        return tournament.athletes().isEmpty() || JOptionPane.showConfirmDialog(this,
                "Replace this tournament and clear its athletes and results?",
                "Replace tournament", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION;
    }

    private JPanel form(String[] captions, JComponent... fields) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        for (int i = 0; i < fields.length; i++) {
            JLabel label = new JLabel(captions[i]);
            JComponent field = fields[i];
            label.setLabelFor(field);
            panel.add(label);
            field.setAlignmentX(LEFT_ALIGNMENT);
            field.setMaximumSize(new Dimension(Integer.MAX_VALUE, field.getPreferredSize().height));
            panel.add(field);
            panel.add(Box.createVerticalStrut(7));
        }
        return panel;
    }

    private boolean confirmForm(JPanel form, String title) {
        return JOptionPane.showConfirmDialog(this, form, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
    }

    private static int number(String value) {
        try {
            return Integer.parseInt(value.strip());
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException("Enter whole numbers for weight, limits and rating.");
        }
    }

    private void run(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException error) {
            showError(error);
        }
    }

    private void showError(IllegalArgumentException error) {
        JOptionPane.showMessageDialog(this, error.getMessage(), "Check input", JOptionPane.WARNING_MESSAGE);
    }

}
