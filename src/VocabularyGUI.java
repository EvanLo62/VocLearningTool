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
// 4. 新增回到第一個或至最後一個單字的按鈕(可選) v
// 5. 支援使用者動態新增單字至檔案中 - 直接寫入.csv檔(可選)
// 6. 可考慮將程式碼分檔
// 7. 搜尋某個特定單字 v
// 8. 標記後的單字會更改樣式(顏色or粗體) v
// 9. 美化介面、將查看標記單字移至檢視菜單中 
// 10. 增加副詞 v 