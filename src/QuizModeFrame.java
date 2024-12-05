import java.awt.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class QuizModeFrame extends JFrame {
    private List<Word> vocabularyList;
    private List<Word> quizWords;
    private int currentQuestionIndex;
    private int score;

    private JLabel questionLabel;
    private JRadioButton[] options;
    private ButtonGroup optionsGroup;
    private JButton nextButton, exitButton;

    private JLabel progressLabel; // 顯示題數進度的 Label

    private String startLetter;
    private String partOfSpeech;
    private int questionCount;

    public QuizModeFrame() {
        setTitle("測驗模式");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    
        // 載入單字列表
        vocabularyList = new ArrayList<>();
        loadVocabulary("vocabulary.csv");
        
        // 測驗範圍設定
        if (!setupQuizSettings()) {
            dispose(); // 關閉測驗視窗
            new VocabularyToolFrame().setVisible(true); // 返回主頁面
            return;
        }
        
        // 測驗初始化介面
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel quizPanel = new JPanel();
        quizPanel.setLayout(new BoxLayout(quizPanel, BoxLayout.Y_AXIS));
    
        questionLabel = new JLabel("測驗問題將顯示於此", JLabel.CENTER);
        questionLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
        questionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    
        progressLabel = new JLabel("1/10", JLabel.RIGHT); // 顯示進度
        progressLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
    
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(questionLabel, BorderLayout.CENTER);
        headerPanel.add(progressLabel, BorderLayout.EAST);
    
        options = new JRadioButton[4];
        optionsGroup = new ButtonGroup();
        for (int i = 0; i < 4; i++) {
            options[i] = new JRadioButton();
            options[i].setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
            optionsGroup.add(options[i]);
            quizPanel.add(options[i]);
        }
    
        nextButton = new JButton("下一題");
        nextButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        nextButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        nextButton.addActionListener(e -> checkAnswerAndProceed());
    
        exitButton = new JButton("退出測驗");
        exitButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.addActionListener(e -> {
            dispose();
            new VocabularyToolFrame().setVisible(true); // 返回主頁面
        });
    
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(nextButton);
        buttonPanel.add(exitButton);
    
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(quizPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    
        add(mainPanel);
    
        // 初始化測驗
        startQuiz();
    }
    

    private boolean setupQuizSettings() {
        JDialog settingsDialog = new JDialog(this, "設定測驗範圍", true);
        settingsDialog.setSize(400, 300);
        settingsDialog.setLayout(new BorderLayout());
        settingsDialog.setLocationRelativeTo(this);
    
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(3, 2, 10, 10));
    
        JComboBox<String> letterComboBox = new JComboBox<>();
        letterComboBox.addItem("全部");
        for (char c = 'A'; c <= 'Z'; c++) {
            letterComboBox.addItem(String.valueOf(c));
        }
    
        JComboBox<String> posComboBox = new JComboBox<>(new String[]{"全部", "名詞", "動詞", "形容詞"});
        JComboBox<Integer> questionAmountComboBox = new JComboBox<>();
        for (int i = 1; i <= 20; i++) {
            questionAmountComboBox.addItem(i);
        }
    
        inputPanel.add(new JLabel("字首範圍 (選擇):"));
        inputPanel.add(letterComboBox);
        inputPanel.add(new JLabel("詞性:"));
        inputPanel.add(posComboBox);
        inputPanel.add(new JLabel("題數 (1-20):"));
        inputPanel.add(questionAmountComboBox);
    
        // 確定與取消按鈕
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton confirmButton = new JButton("確定");
        JButton cancelButton = new JButton("取消");
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
    
        settingsDialog.add(inputPanel, BorderLayout.CENTER);
        settingsDialog.add(buttonPanel, BorderLayout.SOUTH);
    
        final boolean[] isConfirmed = {false}; // 用於記錄是否按下確定
    
        confirmButton.addActionListener(e -> {
            startLetter = letterComboBox.getSelectedItem().toString();
            partOfSpeech = posComboBox.getSelectedItem().toString();
            questionCount = (Integer) questionAmountComboBox.getSelectedItem();
    
            isConfirmed[0] = true; // 確定設定完成
            settingsDialog.dispose();
        });
    
        cancelButton.addActionListener(e -> {
            isConfirmed[0] = false; // 設定取消
            settingsDialog.dispose();
        });
    
        settingsDialog.setVisible(true);
    
        return isConfirmed[0]; // 根據設定是否被確定返回結果
    }
    
    

    private void startQuiz() {
        quizWords = new ArrayList<>();
        for (Word word : vocabularyList) {
            if ((startLetter.equals("全部") || word.getWord().toUpperCase().startsWith(startLetter)) &&
                    (partOfSpeech.equals("全部") || word.getPartOfSpeech().equals(partOfSpeech))) {
                quizWords.add(word);
            }
        }

        if (quizWords.size() > questionCount) {
            quizWords = quizWords.subList(0, questionCount);
        }

        currentQuestionIndex = 0;
        score = 0;

        if (quizWords.isEmpty()) {
            JOptionPane.showMessageDialog(this, "沒有符合條件的單字可測驗！", "錯誤", JOptionPane.ERROR_MESSAGE);
            new VocabularyToolFrame().setVisible(true);
            dispose();
        } else {
            nextQuestion();
        }
    }

    private void nextQuestion() {
        if (currentQuestionIndex >= quizWords.size()) {
            JOptionPane.showMessageDialog(this, "測驗結束！您的得分是：" + score + "/" + quizWords.size(),
                    "測驗結束", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new VocabularyToolFrame().setVisible(true);
            return;
        }

        Word currentWord = quizWords.get(currentQuestionIndex);
        questionLabel.setText("單字: " + currentWord.getWord());

        List<String> optionsList = new ArrayList<>();
        optionsList.add(currentWord.getMeaning());
        while (optionsList.size() < 4) {
            String randomMeaning = vocabularyList.get((int) (Math.random() * vocabularyList.size())).getMeaning();
            if (!optionsList.contains(randomMeaning)) {
                optionsList.add(randomMeaning);
            }
        }

        java.util.Collections.shuffle(optionsList);

        for (int i = 0; i < 4; i++) {
            options[i].setText(optionsList.get(i));
            options[i].setForeground(Color.BLACK);
        }

        optionsGroup.clearSelection();

        // 更新題數進度
        progressLabel.setText((currentQuestionIndex + 1) + "/" + quizWords.size());
    }

    private void checkAnswerAndProceed() {
        Word currentWord = quizWords.get(currentQuestionIndex);
        String correctAnswer = currentWord.getMeaning();

        for (int i = 0; i < 4; i++) {
            if (options[i].isSelected()) {
                if (options[i].getText().equals(correctAnswer)) {
                    options[i].setForeground(Color.GREEN);
                    score++;
                } else {
                    options[i].setForeground(Color.RED);
                }
            }
            if (options[i].getText().equals(correctAnswer)) {
                options[i].setForeground(Color.GREEN);
            }
        }

        currentQuestionIndex++;
        nextButton.setEnabled(false);
        Timer timer = new Timer(1500, e -> {
            nextButton.setEnabled(true);
            nextQuestion();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void loadVocabulary(String filePath) {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            String line = br.readLine();

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    String word = parts[0];
                    String meaning = parts[1];
                    String partOfSpeech = parts[2];
                    String category = parts[3];
                    int importance = Integer.parseInt(parts[4]);
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
