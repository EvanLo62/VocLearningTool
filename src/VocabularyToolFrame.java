import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VocabularyToolFrame extends JFrame {
    private List<Word> vocabularyList; // 單字列表
    private List<Word> filteredWords; // 篩選後的單字列表
    private List<Word> markedWords; // 已標記的單字
    private int currentIndex; // 當前單字索引

    private JLabel wordLabel, meaningLabel; // 單字與意思顯示
    private JLabel progressLabel; // 顯示進度
    private DatabaseManager dbManager;

    public VocabularyToolFrame() {
        setTitle("單字學習工具");
        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 視窗置中

        dbManager = new DatabaseManager();
        dbManager.initializeDatabase();

        vocabularyList = new ArrayList<>();
        markedWords = dbManager.loadMarkedWords();
        currentIndex = dbManager.loadProgress();
        filteredWords = new ArrayList<>();

        // 從 CSV 文件讀取單字
        loadVocabularyFromCSV("vocabulary.csv");

        // 預設篩選全部單字
        filteredWords.addAll(vocabularyList);

        // 添加菜單
        setJMenuBar(createMenuBar());

        // 添加主要內容
        add(createMainUI());

        // 更新顯示進度與內容
        updateWordDisplay();

        // 儲存進度的關閉事件
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                dbManager.saveProgress(currentIndex, markedWords);
            }
        });
    }

    private JPanel createMainUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 248, 255));

        // 左上角進度條
        progressLabel = new JLabel("當前單字：1/" + filteredWords.size());
        progressLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        progressLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 添加內距
        mainPanel.add(progressLabel, BorderLayout.NORTH);

        // 中間顯示區
        JPanel displayPanel = new JPanel();
        displayPanel.setLayout(new BoxLayout(displayPanel, BoxLayout.Y_AXIS));
        displayPanel.setBackground(mainPanel.getBackground());
        displayPanel.setBorder(BorderFactory.createTitledBorder("單字區"));

        wordLabel = new JLabel("單字: " + filteredWords.get(currentIndex).getWord(), JLabel.CENTER);
        wordLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 28));
        wordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        meaningLabel = new JLabel("點擊查看意思", JLabel.CENTER);
        meaningLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 18));
        meaningLabel.setForeground(Color.GRAY);
        meaningLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 點擊後顯示意思
        meaningLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                Word currentWord = filteredWords.get(currentIndex);
                meaningLabel.setText("意思: " + currentWord.getMeaning() + " | 詞性: " + currentWord.getPartOfSpeech());
                meaningLabel.setForeground(Color.BLACK);
            }
        });

        displayPanel.add(wordLabel);
        displayPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        displayPanel.add(meaningLabel);

        // 下方按鈕區
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(mainPanel.getBackground());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // 添加內距
        JButton prevButton = createCustomButton("上一個");
        JButton nextButton = createCustomButton("下一個");
        JButton markButton = createCustomButton("標記單字");
        JButton historyButton = createCustomButton("歷史紀錄");

        prevButton.addActionListener(e -> showPreviousWord());
        nextButton.addActionListener(e -> showNextWord());
        markButton.addActionListener(e -> markCurrentWord());
        historyButton.addActionListener(e -> showMarkedWords());

        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);
        buttonPanel.add(markButton);
        buttonPanel.add(historyButton);

        buttonPanel.setPreferredSize(new Dimension(mainPanel.getWidth(), 60));

        // 組裝主要面板
        mainPanel.add(displayPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
    
        // 功能區選單
        JMenu functionalityMenu = new JMenu("功能區");
        JMenuItem viewMarkedWordsItem = new JMenuItem("查看標記單字");
        JMenuItem filterWordsItem = new JMenuItem("篩選單字");
        JMenuItem resetWordsItem = new JMenuItem("還原初始設定");
    
        // 查看標記單字
        viewMarkedWordsItem.addActionListener(e -> showMarkedWords());
    
        // 篩選單字
        filterWordsItem.addActionListener(e -> filterWords());
    
        // 還原初始設定
        resetWordsItem.addActionListener(e -> resetToInitialSettings());
    
        functionalityMenu.add(viewMarkedWordsItem);
        functionalityMenu.add(filterWordsItem);
        functionalityMenu.add(resetWordsItem);
    
        // 檢視選單
        JMenu viewMenu = new JMenu("檢視");
        // TODO: 添加檢視相關功能
    
        // 其他功能選單
        JMenu otherFunctionsMenu = new JMenu("其他功能");
        // TODO: 添加其他功能相關選項
    
        // 添加到菜單列
        menuBar.add(functionalityMenu);
        menuBar.add(viewMenu);
        menuBar.add(otherFunctionsMenu);
    
        return menuBar;
    }
    
    private void resetToInitialSettings() {
        // 還原為初始的單字列表
        filteredWords = new ArrayList<>(vocabularyList);
        currentIndex = 0;
    
        // 更新顯示
        updateWordDisplay();
        progressLabel.setText("當前單字：1/" + filteredWords.size());
    
        JOptionPane.showMessageDialog(this, "已還原到初始設定！", "提示", JOptionPane.INFORMATION_MESSAGE);
    }
    

    private void filterWords() {
        JTextField startLetterField = new JTextField();
        JComboBox<String> partOfSpeechComboBox = new JComboBox<>(new String[]{"名詞", "動詞", "形容詞"});
        JTextField limitField = new JTextField();
    
        Object[] message = {
            "輸入開頭字母 (A-Z):", startLetterField,
            "選擇詞性:", partOfSpeechComboBox,
            "輸入要學習的單字數量:", limitField
        };
    
        int option = JOptionPane.showConfirmDialog(this, message, "篩選條件", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String startLetter = startLetterField.getText().trim().toUpperCase();
            String partOfSpeech = partOfSpeechComboBox.getSelectedItem().toString();
            int limit;
    
            // 驗證開頭字母是否為 A-Z
            if (!startLetter.matches("[A-Z]")) {
                JOptionPane.showMessageDialog(this, "開頭字母必須是 A-Z 的字母！", "錯誤", JOptionPane.ERROR_MESSAGE);
                return;
            }
    
            try {
                limit = Integer.parseInt(limitField.getText().trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "數量必須是數字！", "錯誤", JOptionPane.ERROR_MESSAGE);
                return;
            }
    
            // 篩選單字
            filteredWords = vocabularyList.stream()
                    .filter(word -> word.getWord().toUpperCase().startsWith(startLetter))
                    .filter(word -> word.getPartOfSpeech().equals(partOfSpeech))
                    .limit(limit)
                    .collect(Collectors.toList());
    
            if (filteredWords.isEmpty()) {
                JOptionPane.showMessageDialog(this, "未找到符合條件的單字！", "提示", JOptionPane.INFORMATION_MESSAGE);
            } else {
                currentIndex = 0;
                updateWordDisplay();
                progressLabel.setText("當前單字：1/" + filteredWords.size());
            }
        }
    }
    

    private void showPreviousWord() {
        currentIndex = (currentIndex - 1 + filteredWords.size()) % filteredWords.size();
        updateWordDisplay();
    }

    private void showNextWord() {
        currentIndex = (currentIndex + 1) % filteredWords.size();
        updateWordDisplay();
    }

    private void markCurrentWord() {
        Word currentWord = filteredWords.get(currentIndex);
        if (!markedWords.contains(currentWord)) {
            markedWords.add(currentWord);
            JOptionPane.showMessageDialog(this, "已標記單字：" + currentWord.getWord());
        } else {
            JOptionPane.showMessageDialog(this, "該單字已標記過！");
        }
    }

    private void showMarkedWords() {
        if (markedWords.isEmpty()) {
            JOptionPane.showMessageDialog(this, "尚未標記任何單字！");
            return;
        }

        StringBuilder markedWordInfo = new StringBuilder("已標記的單字：\n");
        for (Word word : markedWords) {
            markedWordInfo.append("單字: ").append(word.getWord())
                    .append(", 意思: ").append(word.getMeaning())
                    .append(", 詞性: ").append(word.getPartOfSpeech())
                    .append(", 分類: ").append(word.getCategory()).append("\n");
        }

        JOptionPane.showMessageDialog(this, markedWordInfo.toString(), "歷史紀錄", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateWordDisplay() {
        if (filteredWords.isEmpty()) {
            wordLabel.setText("無符合條件的單字");
            meaningLabel.setText("");
            progressLabel.setText("當前單字：0/0");
        } else {
            Word currentWord = filteredWords.get(currentIndex);
            wordLabel.setText("單字: " + currentWord.getWord());
            meaningLabel.setText("點擊查看意思");
            meaningLabel.setForeground(Color.GRAY);
            progressLabel.setText("當前單字：" + (currentIndex + 1) + "/" + filteredWords.size());
        }
    }

    private JButton createCustomButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
        button.setBackground(new Color(70, 130, 180)); // 深藍色
        button.setForeground(Color.BLACK); // 黑色字體
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }

    private void loadVocabularyFromCSV(String filePath) {
        try (BufferedReader br = new BufferedReader(
            new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            String line = br.readLine(); // 跳過第一行

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) { // 確保每一行有 5 個欄位
                    String word = parts[0];
                    String meaning = parts[1];
                    String partOfSpeech = parts[2];
                    String category = parts[3];
                    int difficulty = Integer.parseInt(parts[4]); // 確保數字格式正確
                    vocabularyList.add(new Word(word, meaning, partOfSpeech, category, difficulty, false));
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "讀取單字檔失敗：" + e.getMessage(),
                    "錯誤", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "檔案內容格式錯誤，請檢查數字格式！",
                    "錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }
}