package tournament;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class DesktopMain {
    private DesktopMain() {}

    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("--console")) {
            Main.main(new String[0]);
            return;
        }
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("A graphical desktop is required. Use --console for terminal mode.");
            return;
        }
        SwingUtilities.invokeLater(() -> {
            UIManager.put("swing.boldMetal", false);
            JFrame frame = new JFrame("Armwrestling Tournament");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new TournamentDashboard());
            frame.setMinimumSize(new Dimension(870, 610));
            frame.setSize(1180, 748);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
