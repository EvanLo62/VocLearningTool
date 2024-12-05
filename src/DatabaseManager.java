import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// import javax.swing.JOptionPane;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:vocabulary.db";

    public void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // 創建進度表
            String createProgressTable = "CREATE TABLE IF NOT EXISTS progress (" +
                    "id INTEGER PRIMARY KEY, " +
                    "current_index INTEGER NOT NULL)";
            stmt.execute(createProgressTable);

            // 創建標記單字表
            String createMarkedWordsTable = "CREATE TABLE IF NOT EXISTS marked_words (" +
                    "id INTEGER PRIMARY KEY, " +
                    "word TEXT NOT NULL, " +
                    "meaning TEXT, " +
                    "part_of_speech TEXT, " +
                    "category TEXT, " +
                    "importance INTEGER)";
            stmt.execute(createMarkedWordsTable);

            // 初始化進度數據
            String insertInitialProgress = "INSERT OR IGNORE INTO progress (id, current_index) VALUES (1, 0)";
            stmt.execute(insertInitialProgress);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // public List<Word> loadVocabularyFromDatabase() {
    //     List<Word> vocabularyList = new ArrayList<>();
    //     String query = "SELECT word, meaning, part_of_speech, category, importance FROM marked_words";

    //     try (Connection conn = DriverManager.getConnection(DB_URL);
    //         Statement stmt = conn.createStatement();
    //         ResultSet rs = stmt.executeQuery(query)) {

    //         while (rs.next()) {
    //             String word = rs.getString("word");
    //             String meaning = rs.getString("meaning");
    //             String partOfSpeech = rs.getString("part_of_speech");
    //             String category = rs.getString("category");
    //             int importance = rs.getInt("importance");

    //             vocabularyList.add(new Word(word, meaning, partOfSpeech, category, importance, false));
    //         }
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //         JOptionPane.showMessageDialog(null, "從資料庫載入單字失敗：" + e.getMessage(),
    //                 "錯誤", JOptionPane.ERROR_MESSAGE);
    //     }

    //     return vocabularyList;
    // }


    public int loadProgress() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT current_index FROM progress WHERE id = 1")) {

            if (rs.next()) {
                return rs.getInt("current_index");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // 默認從第一個單字開始
    }

    public List<Word> loadMarkedWords() {
        List<Word> markedWords = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT word, meaning, part_of_speech, category, importance FROM marked_words")) {

            while (rs.next()) {
                markedWords.add(new Word(
                        rs.getString("word"),
                        rs.getString("meaning"),
                        rs.getString("part_of_speech"),
                        rs.getString("category"),
                        rs.getInt("importance"),
                        false
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return markedWords;
    }

    public void saveProgress(int currentIndex, List<Word> markedWords) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // 儲存當前進度
            String updateProgress = "UPDATE progress SET current_index = " + currentIndex + " WHERE id = 1";
            stmt.execute(updateProgress);

            // 清空已標記單字表
            stmt.execute("DELETE FROM marked_words");

            // 插入標記的單字
            for (Word word : markedWords) {
                String insertMarkedWord = String.format(
                        "INSERT INTO marked_words (word, meaning, part_of_speech, category, importance) " +
                                "VALUES ('%s', '%s', '%s', '%s', %d)",
                        word.getWord(), word.getMeaning(), word.getPartOfSpeech(),
                        word.getCategory(), word.getImportance());
                stmt.execute(insertMarkedWord);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
