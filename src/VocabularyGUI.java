import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class VocabularyGUI {
    public static void main(String[] args) {
        // 使用 SwingUtilities.invokeLater 確保在 EDT 執行
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new VocabularyToolFrame().setVisible(true);
        });
    }
}
