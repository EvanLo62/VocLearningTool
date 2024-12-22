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
// 可新增的功能：
// 1. 支援使用者動態新增單字至檔案中 - 直接寫入.csv檔(可選)
// 2. 可考慮將程式碼分檔
// 3. 使用標記單字進行測驗