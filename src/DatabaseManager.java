import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 資料庫管理，將 vocabulary.db 存放於 JAR 所在資料夾下的 .VocLearningTool 子資料夾
 * 確保無論以 CLI 或雙擊 JAR 運行，皆在同一位置讀寫，並具有寫入權限。
 */
public class DatabaseManager {
    private static final String DB_DIR_NAME = ".VocLearningTool";
    private static final String DB_FILENAME = "vocabulary.db";

    /**
     * 取得並建立存放資料庫的資料夾 (位於 jar 同目錄下)
     */
    private Path getDbDir() {
        try {
            // 解析目前執行 jar 所在位置
            Path jarPath = Paths.get(
                DatabaseManager.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
            );
            Path jarDir = jarPath.getParent();
            Path dbDir = jarDir.resolve(DB_DIR_NAME);
            if (Files.notExists(dbDir)) {
                Files.createDirectories(dbDir);
            }
            return dbDir;
        } catch (Exception e) {
            throw new RuntimeException("無法建立或存取資料庫資料夾", e);
        }
    }

    /**
     * 回傳資料庫檔案完整路徑
     */
    private String getDbPath() {
        return getDbDir().resolve(DB_FILENAME).toString();
    }

    /**
     * 建構 JDBC URL
     */
    private String getJdbcUrl() {
        return "jdbc:sqlite:" + getDbPath();
    }

    /**
     * 初始化資料表（若不存在則建立），並設定初始進度
     */
    public void initializeDatabase() {
        String url = getJdbcUrl();
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS progress (" +
                         "id INTEGER PRIMARY KEY, current_index INTEGER NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS marked_words (" +
                         "id INTEGER PRIMARY KEY, word TEXT NOT NULL, meaning TEXT, " +
                         "part_of_speech TEXT, category TEXT, importance INTEGER)");

            stmt.execute("INSERT OR IGNORE INTO progress (id, current_index) VALUES (1, 0)");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 載入當前進度
     */
    public int loadProgress() {
        String url = getJdbcUrl();
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT current_index FROM progress WHERE id = 1")) {
            if (rs.next()) {
                return rs.getInt("current_index");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 載入所有標記單字
     */
    public List<Word> loadMarkedWords() {
        List<Word> markedWords = new ArrayList<>();
        String url = getJdbcUrl();
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT word, meaning, part_of_speech, category, importance FROM marked_words")) {
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

    /**
     * 保存進度與標記單字
     */
    public void saveProgress(int currentIndex, List<Word> markedWords) {
        String url = getJdbcUrl();
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {

            stmt.execute("UPDATE progress SET current_index = " + currentIndex + " WHERE id = 1");
            stmt.execute("DELETE FROM marked_words");
            for (Word word : markedWords) {
                stmt.execute(String.format(
                    "INSERT INTO marked_words (word, meaning, part_of_speech, category, importance) VALUES ('%s','%s','%s','%s',%d)",
                    word.getWord(), word.getMeaning(),
                    word.getPartOfSpeech(), word.getCategory(), word.getImportance()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
