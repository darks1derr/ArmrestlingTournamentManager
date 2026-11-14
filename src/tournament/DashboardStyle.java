package tournament;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

// Colors and small Swing components shared across the dashboard.
final class DashboardStyle {
    static final Color PAPER = new Color(0xf5f3ee);
    static final Color WHITE = new Color(0xfffdfa);
    static final Color INK = new Color(0x231f1d);
    static final Color MUTED = new Color(0x6b625b);
    static final Color LINE = new Color(0xe3ddd4);
    static final Color ORANGE = new Color(0xa63d20);
    static final Color DARK = new Color(0x241f1c);
    static final Color SIDE_TEXT = new Color(0xf7e9de);
    static final Color SIDE_ACTIVE = new Color(0x553127);
    static final Color GREEN = new Color(0x28614a);
    static final String FONT = "SansSerif";
    static final Color SIDE_MUTED = new Color(0xd6b9a6);

    private DashboardStyle() {}

    static JComponent empty(String message) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(WHITE);
        panel.setBorder(cardBorder(15));
        JLabel center = label(message, 15, false, MUTED);
        center.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(center);
        return panel;
    }

    static JScrollPane scroll(JComponent content, Color background) {
        JScrollPane pane = new JScrollPane(content);
        pane.getViewport().setBackground(background);
        pane.setBorder(BorderFactory.createEmptyBorder());
        pane.getVerticalScrollBar().setUnitIncrement(22);
        pane.getHorizontalScrollBar().setUnitIncrement(22);
        return pane;
    }

    static JPanel toolbar() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        return actions;
    }

    static JPanel stack(Color color) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(color);
        return panel;
    }

    static JLabel label(String value, int size, boolean bold, Color color) {
        JLabel label = new JLabel();
        label.putClientProperty("html.disable", Boolean.TRUE);
        label.setText(value);
        label.setForeground(color);
        label.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        label.setFont(new Font(FONT, bold ? Font.BOLD : Font.PLAIN, size));
        return label;
    }

    static JButton sideButton(String text, boolean selected, Runnable action) {
        JButton button = new FlatButton(text, selected ? SIDE_ACTIVE : DARK, SIDE_TEXT, action);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
        return button;
    }

    static JButton button(String text, boolean primary, Runnable action) {
        return new FlatButton(text, primary ? ORANGE : new Color(0xece6dc),
                primary ? Color.WHITE : INK, action);
    }

    static Border padding(int top, int left, int bottom, int right) {
        return BorderFactory.createEmptyBorder(top, left, bottom, right);
    }

    static Border cardBorder(int space) {
        return BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE, 1, true),
                padding(space, space, space, space));
    }

    private static final class FlatButton extends JButton {
        private final Color fill;

        FlatButton(String text, Color fill, Color foreground, Runnable action) {
            super(text);
            this.fill = fill;
            setForeground(foreground);
            setFont(new Font(FONT, Font.BOLD, 13));
            setBorder(padding(10, 13, 10, 13));
            setAlignmentX(JComponent.LEFT_ALIGNMENT);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addActionListener(event -> action.run());
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D layer = (Graphics2D) graphics.create();
            layer.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color background = fill;
            if (!isEnabled()) {
                background = LINE;
            } else if (getModel().isRollover()) {
                background = fill.darker();
            }
            layer.setColor(background);
            layer.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            if (isFocusOwner()) {
                layer.setColor(getForeground());
                layer.drawRoundRect(3, 3, getWidth() - 7, getHeight() - 7, 9, 9);
            }
            layer.dispose();
            super.paintComponent(graphics);
        }
    }
}
