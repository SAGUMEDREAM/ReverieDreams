package cc.thonly.reverie_dreams.fabric.window;

import lombok.AllArgsConstructor;

import javax.swing.*;

@AllArgsConstructor
public class SwingNoticeWindow {

    private final String text;

    public void show() {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            JOptionPane optionPane = new JOptionPane(
                    text,
                    JOptionPane.WARNING_MESSAGE,
                    JOptionPane.DEFAULT_OPTION
            );

            JDialog dialog = optionPane.createDialog("Reverie Dreams");
            dialog.setAlwaysOnTop(true);
            dialog.setModal(true);

            dialog.setLocationRelativeTo(null);

            dialog.setVisible(true);
        });
    }
}