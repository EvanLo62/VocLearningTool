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

    private JComboBox<String> letterComboBox;
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

        // 同步已標記單字
        syncMarkedWords();

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
        
        // 上方面板 (包含字首選單和進度條)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(mainPanel.getBackground());

        // 字首單字導引下拉選單
        letterComboBox = new JComboBox<>();
        for (char c = 'A'; c <= 'Z'; c++) {
            letterComboBox.addItem(String.valueOf(c));
        }
        letterComboBox.addActionListener(e -> onLetterSelected((String) letterComboBox.getSelectedItem()));
        headerPanel.add(letterComboBox, BorderLayout.EAST); // 放在右上角

        // 左上角進度條
        progressLabel = new JLabel("當前單字：1/" + filteredWords.size());
        progressLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        progressLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 添加內距
        headerPanel.add(progressLabel, BorderLayout.WEST); // 放在左上角

        mainPanel.add(headerPanel, BorderLayout.NORTH);

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
                meaningLabel.setText(
                    "意思: " + currentWord.getMeaning() +
                    "  |  詞性: " + currentWord.getPartOfSpeech() +
                    "  |  重要性: " + currentWord.getImportantStars()
                  );
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
        JButton firstButton = createCustomButton("|＜");
        JButton prevButton = createCustomButton("＜");
        JButton nextButton = createCustomButton("＞");
        JButton lastButton = createCustomButton("＞|");
        JButton markButton = createCustomButton("標記單字");
        // JButton quizModeButton = createCustomButton("測驗模式");

        firstButton.addActionListener(e -> showFirstWord());
        prevButton.addActionListener(e -> showPreviousWord());
        nextButton.addActionListener(e -> showNextWord());
        lastButton.addActionListener(e -> showLastWord());
        markButton.addActionListener(e -> markCurrentWord());
        // quizModeButton.addActionListener(e -> enterQuizMode());

        buttonPanel.add(firstButton);
        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);
        buttonPanel.add(lastButton);
        buttonPanel.add(markButton);
        // buttonPanel.add(quizModeButton);

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
        JMenuItem filterWordsItem = new JMenuItem("篩選單字");
        JMenuItem resetWordsItem = new JMenuItem("重置列表");
        JMenuItem searchWordItem = new JMenuItem("搜尋特定單字");
    
    
        // 篩選單字
        filterWordsItem.addActionListener(e -> filterWords());

        // 搜尋特定單字
        searchWordItem.addActionListener(e -> searchSpecificWord());
    
        // 還原初始設定(重置)
        resetWordsItem.addActionListener(e -> resetToInitialSettings());
    
        functionalityMenu.add(filterWordsItem);
        functionalityMenu.add(searchWordItem);
        functionalityMenu.add(resetWordsItem);
    
        // 檢視選單
        JMenu viewMenu = new JMenu("檢視");
        JMenuItem viewMarkedWordsItem = new JMenuItem("查看標記單字");
    
        // 查看標記單字
        viewMarkedWordsItem.addActionListener(e -> showMarkedWords());
        
        viewMenu.add(viewMarkedWordsItem);


        // 其他功能選單
        JMenu quizModeMenu = new JMenu("測驗");
        JMenuItem quizModeItem = new JMenuItem("進入測驗模式");
        JMenuItem quizModeForMarkedVoc = new JMenuItem("測驗標記單字");
        
        // 進入測驗模式的事件
        quizModeItem.addActionListener(e -> enterQuizMode());
        quizModeForMarkedVoc.addActionListener(e -> enterMarkedVocQuizMode());

        quizModeMenu.add(quizModeItem);
        quizModeMenu.add(quizModeForMarkedVoc);

        // 添加到菜單列
        menuBar.add(functionalityMenu);
        menuBar.add(viewMenu);
        menuBar.add(quizModeMenu);
    
        return menuBar;
    }
    
    private void resetToInitialSettings() {
        // 還原為初始的單字列表
        filteredWords = new ArrayList<>(vocabularyList);
        currentIndex = 0;
        
        letterComboBox.setVisible(true);

        // 更新顯示
        updateWordDisplay();
        progressLabel.setText("當前單字：1/" + filteredWords.size());
    
        JOptionPane.showMessageDialog(this, "已還原到原始列表！", "提示", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void onLetterSelected(String selectedLetter) {
    
        currentIndex = findFirstWordIndexByLetter(selectedLetter.charAt(0));
    
        updateWordDisplay();
    }
    
    private int findFirstWordIndexByLetter(char letter) {
        for (int i = 0; i < vocabularyList.size(); i++) {
            if (vocabularyList.get(i).getWord().toUpperCase().startsWith(String.valueOf(letter))) {
                return i;
            }
        }
    
        JOptionPane.showMessageDialog(this, "未找到以 " + letter + " 開頭的單字！", "提示", JOptionPane.INFORMATION_MESSAGE);
        return currentIndex;
    }

    private void filterWords() {
        // 創建字首範圍下拉框
        JComboBox<String> startLetterComboBox = new JComboBox<>();
        startLetterComboBox.addItem("全部");
        for (char c = 'A'; c <= 'Z'; c++) {
            startLetterComboBox.addItem(String.valueOf(c));
        }
    
        // 創建詞性下拉框
        JComboBox<String> partOfSpeechComboBox = new JComboBox<>(new String[]{"全部", "名詞", "動詞", "形容詞", "副詞"});
    
        // 數量輸入框
        JTextField limitField = new JTextField();
    
        Object[] message = {
            "選擇字首範圍:", startLetterComboBox,
            "選擇詞性:", partOfSpeechComboBox,
            "輸入要學習的單字數量:", limitField
        };
    
        int option = JOptionPane.showConfirmDialog(this, message, "篩選條件", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            // 獲取使用者選擇的值
            String selectedLetter = startLetterComboBox.getSelectedItem().toString();
            String selectedPartOfSpeech = partOfSpeechComboBox.getSelectedItem().toString();
            int limit;
    
            // 驗證數量輸入是否為數字
            try {
                limit = Integer.parseInt(limitField.getText().trim());
                if (limit <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "數量必須是正整數！", "錯誤", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 篩選單字
            filteredWords = vocabularyList.stream()
                    .filter(word -> selectedLetter.equals("全部") || word.getWord().toUpperCase().startsWith(selectedLetter))
                    .filter(word -> selectedPartOfSpeech.equals("全部") || word.getPartOfSpeech().equals(selectedPartOfSpeech))
                    .limit(limit)
                    .collect(Collectors.toList());
    
            if (filteredWords.isEmpty()) {
                JOptionPane.showMessageDialog(this, "未找到符合條件的單字！", "提示", JOptionPane.INFORMATION_MESSAGE);
            } else {
                letterComboBox.setVisible(false);
                currentIndex = 0;
                updateWordDisplay();
                progressLabel.setText("當前單字：1/" + filteredWords.size());
            }
        }
    }
    
    private void showFirstWord() {
        currentIndex = 0;
        updateWordDisplay();
    }

    private void showPreviousWord() {
        currentIndex = (currentIndex - 1 + filteredWords.size()) % filteredWords.size();
        updateWordDisplay();
    }

    private void showNextWord() {
        currentIndex = (currentIndex + 1) % filteredWords.size();
        updateWordDisplay();
    }

    private void showLastWord() {
        currentIndex = filteredWords.size() - 1;
        updateWordDisplay();
    }    

    private void markCurrentWord() {
        Word currentWord = filteredWords.get(currentIndex);
    
        if (currentWord.isMarked()) {
            JOptionPane.showMessageDialog(this, "該單字已標記過！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
    
        currentWord.setMarked(true);
        markedWords.add(currentWord);
        updateWordDisplay(); // 標記後立即更新樣式
        JOptionPane.showMessageDialog(this, "已標記單字：" + currentWord.getWord(), "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showMarkedWords() {
        DefaultListModel<String> listModel = new DefaultListModel<>();
        if (markedWords.isEmpty()) {
            listModel.addElement("目前無標記單字");
        } else {
            for (Word word : markedWords) {
                listModel.addElement("單字: " + word.getWord() + " | 字義: " + word.getMeaning() +
                                     " | 詞性: " + word.getPartOfSpeech() + 
                                     " | 分類: " + word.getCategory() + 
                                     " | 重要性: " + word.getImportance());
            }
        }
    
        JList<String> markedWordList = new JList<>(listModel);
        markedWordList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(markedWordList);
    
        // 清除選定按鈕
        JButton deleteButton = new JButton("清除選定單字");
        deleteButton.addActionListener(e -> {
            int selectedIndex = markedWordList.getSelectedIndex();
            if (selectedIndex != -1 && !listModel.getElementAt(selectedIndex).equals("目前無標記單字")) {
                // 從 `markedWords` 列表中移除對應的單字
                Word removedWord = markedWords.remove(selectedIndex);
        
                // 將 `vocabularyList` 中的該單字標記狀態更新為未標記
                for (Word word : vocabularyList) {
                    if (word.getWord().equals(removedWord.getWord())) {
                        word.setMarked(false);
                        break;
                    }
                }
                
                // 從列表模型中移除所選項目
                listModel.remove(selectedIndex);

                // 更新當前顯示的單字樣式
                if (!filteredWords.isEmpty() && filteredWords.get(currentIndex).getWord().equals(removedWord.getWord())) {
                    updateWordDisplay(); // 即時刷新顯示
                }
        
                if (listModel.isEmpty()) {
                    // 當列表清空後，顯示「目前無標記單字」
                    listModel.addElement("目前無標記單字");
                    markedWordList.setEnabled(false); // 禁用列表
                }
        
                JOptionPane.showMessageDialog(this, "已清除所選單字！", "提示", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "請先選擇要清除的單字！", "提示", JOptionPane.WARNING_MESSAGE);
            }
        });
        
    
        // 清空所有按鈕
        JButton clearAllButton = new JButton("清空所有單字");
        clearAllButton.addActionListener(e -> {
            if (markedWords.isEmpty()) {
                JOptionPane.showMessageDialog(this, "目前無標記單字！", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
        
            int confirm = JOptionPane.showConfirmDialog(this, "確定要清空所有標記單字嗎？", "確認", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                // 將所有標記的單字設為未標記
                for (Word markedWord : markedWords) {
                    for (Word word : vocabularyList) {
                        if (word.getWord().equals(markedWord.getWord())) {
                            word.setMarked(false);
                            break;
                        }
                    }
                }
        
                // 清空標記列表
                markedWords.clear();
                listModel.clear();
                listModel.addElement("目前無標記單字");
                markedWordList.setEnabled(false); // 禁用列表

                // 即時刷新當前顯示的單字樣式
                updateWordDisplay();

                JOptionPane.showMessageDialog(this, "所有標記單字已清空！", "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    
        // 匯出按鈕
        JButton exportButton = new JButton("匯出單字");
        exportButton.addActionListener(e -> {
            if (markedWords.isEmpty()) {
                JOptionPane.showMessageDialog(this, "無標記單字可匯出！", "提示", JOptionPane.WARNING_MESSAGE);
            } else {
                exportMarkedWords();
            }
        });
    
        // 將文本框與按鈕放到對話框中
        JPanel panel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearAllButton);
        buttonPanel.add(exportButton);
    
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
    
        JOptionPane.showMessageDialog(this, panel, "查看標記單字", JOptionPane.INFORMATION_MESSAGE);
    }    

    private void exportMarkedWords() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY); // 僅允許選擇檔案
        fileChooser.setSelectedFile(new java.io.File("marked_words.csv")); // 預設檔案名
    
        // 僅允許 CSV 檔案類型
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));
    
        int option = fileChooser.showSaveDialog(this);
    
        if (option == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
    
            // 確保檔案名以 ".csv" 結尾
            if (!filePath.toLowerCase().endsWith(".csv")) {
                filePath += ".csv";
            }
    
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(filePath)) {
                // 寫入 BOM 標記
                fos.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
    
                // 構建 CSV 內容
                StringBuilder csvContent = new StringBuilder("單字,字義,詞性,分類,重要性\n");
                for (Word word : markedWords) {
                    csvContent.append(word.getWord()).append(",")
                              .append(word.getMeaning()).append(",")
                              .append(word.getPartOfSpeech()).append(",")
                              .append(word.getCategory()).append(",")
                              .append(word.getImportance()).append("\n");
                }
    
                // 寫入 CSV 內容
                fos.write(csvContent.toString().getBytes(StandardCharsets.UTF_8));
                JOptionPane.showMessageDialog(this, "匯出成功！\n檔案路徑：" + filePath, "成功", JOptionPane.INFORMATION_MESSAGE);
    
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "匯出失敗：" + e.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void searchSpecificWord() {
        String wordToSearch = JOptionPane.showInputDialog(this, "請輸入要搜尋的單字:", "搜尋特定單字", JOptionPane.PLAIN_MESSAGE);

        if (wordToSearch != null && !wordToSearch.trim().isEmpty()) {
            String searchKey = wordToSearch.trim().toLowerCase();
            boolean foundInFiltered = false;
            boolean foundInFullList = false;
    
            // 先在篩選後的列表中搜尋
            for (int i = 0; i < filteredWords.size(); i++) {
                Word word = filteredWords.get(i);
                if (word.getWord().equalsIgnoreCase(searchKey)) {
                    currentIndex = i; // 更新索引至篩選範圍內的位置
                    updateWordDisplay();
                    progressLabel.setText("當前單字：" + (currentIndex + 1) + "/" + filteredWords.size());
                    foundInFiltered = true;
                    break;
                }
            }
    
            // 若篩選範圍內未找到，則在完整列表中搜尋
            if (!foundInFiltered) {
                for (int i = 0; i < vocabularyList.size(); i++) {
                    Word word = vocabularyList.get(i);
                    if (word.getWord().equalsIgnoreCase(searchKey)) {
                        // 恢復完整列表並更新索引至完整列表中的位置
                        filteredWords = new ArrayList<>(vocabularyList);
                        currentIndex = i;
                        letterComboBox.setVisible(true);
                        updateWordDisplay();
                        progressLabel.setText("當前單字：" + (currentIndex + 1) + "/" + filteredWords.size());
                        foundInFullList = true;
                        JOptionPane.showMessageDialog(this, "此單字不在目前篩選範圍內，已恢復至完整列表！", "提示", JOptionPane.INFORMATION_MESSAGE);
                        break;
                    }
                }
            }
    
            // 若完整列表中也未找到
            if (!foundInFiltered && !foundInFullList) {
                JOptionPane.showMessageDialog(this, "未找到指定的單字！", "搜尋結果", JOptionPane.INFORMATION_MESSAGE);
            }
        }
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

            // 判斷單字是否被標記，改變字樣
            if (currentWord.isMarked()) {
                wordLabel.setForeground(Color.RED); // 標記單字顯示紅色
                wordLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 28));
            } else {
                wordLabel.setForeground(Color.BLACK); // 未標記單字顯示黑色
                wordLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 28));
            }

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

    // 進入測驗模式
    public void enterQuizMode() {
        dbManager.saveProgress(currentIndex, markedWords);
        dispose();
        QuizModeFrame quizMode = new QuizModeFrame();
        if (quizMode.isDisplayable()) { // 確保只有需要顯示時才執行
            SwingUtilities.invokeLater(() -> quizMode.setVisible(true));
        }
    }

    // 進入測驗標記單字模式
    public void enterMarkedVocQuizMode() {
        dbManager.saveProgress(currentIndex, markedWords);
        dispose();
        
        MarkedVocQuizFrame quizMode = new MarkedVocQuizFrame(markedWords);
        if (quizMode.isDisplayable()) {
            SwingUtilities.invokeLater(() -> quizMode.setVisible(true));
        }
    }
    
    private void syncMarkedWords() {
        for (Word markedWord : markedWords) {
            for (Word word : vocabularyList) {
                if (word.getWord().equals(markedWord.getWord())) {
                    word.setMarked(true); // 同步標記狀態
                    break;
                }
            }
        }
    }

    public void reloadMarkedWords() {
        // 確保標記的單字從內存或資料庫中重新同步
        markedWords = dbManager.loadMarkedWords(); // 從資料庫重新載入
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
                    int importance = Integer.parseInt(parts[4]); // 確保數字格式正確
                    vocabularyList.add(new Word(word, meaning, partOfSpeech, category, importance, false));
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
