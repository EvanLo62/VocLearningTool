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
// 1. 匯出標記單字(獨立檔案) v
// 2. 清除標記單字(單一清除、全部清除) v
// 3. 新增測驗模式 v (還需檢查)
// 4. 新增回到第一個或至最後一個單字的按鈕(可選)
// 5. 支援使用者動態新增單字至檔案中 - 直接寫入.csv檔(可選)
// 6. 可考慮分檔