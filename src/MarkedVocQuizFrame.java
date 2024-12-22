import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MarkedVocQuizFrame extends JFrame {
    private List<Word> vocabularyList;
    private List<Word> markedWords;
    private List<Word> quizWords;
    private int currentQuestionIndex;
    private int score;

    private JLabel questionLabel;
    private JRadioButton[] options;
    private ButtonGroup optionsGroup;
    private JButton nextButton, exitButton;
    private JLabel progressLabel;
    private int questionCount;

    public MarkedVocQuizFrame(List<Word> markedWords) {
        this.markedWords = new ArrayList<>(markedWords); // 創建標記單字的複本
        setTitle("標記單字測驗模式");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // 載入單字列表(供隨機選項內容使用)
        vocabularyList = new ArrayList<>();
        loadVocabulary("vocabulary.csv");

        if (!setupQuizSettings()) {
            dispose(); // 關閉測驗視窗
            new VocabularyToolFrame().setVisible(true); // 返回主頁面
            return;
        }

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel quizPanel = new JPanel();
        quizPanel.setLayout(new BoxLayout(quizPanel, BoxLayout.Y_AXIS));

        questionLabel = new JLabel("問題將顯示在此", JLabel.CENTER);
        questionLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
        questionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        progressLabel = new JLabel("1/" + questionCount, JLabel.RIGHT);
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
        startQuiz();
    }

    private boolean setupQuizSettings() {
        if (markedWords.isEmpty()) {
            JOptionPane.showMessageDialog(this, "目前無標記單字可用於測驗！", "錯誤", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        JDialog settingsDialog = new JDialog(this, "選擇測驗題數", true);
        settingsDialog.setSize(300, 150);
        settingsDialog.setLayout(new BorderLayout());
        settingsDialog.setLocationRelativeTo(this);

        JPanel inputPanel = new JPanel(new FlowLayout());

        JComboBox<Integer> questionAmountComboBox = new JComboBox<>();
        for (int i = 1; i <= markedWords.size(); i++) {
            questionAmountComboBox.addItem(i);
        }

        inputPanel.add(new JLabel("選擇測驗題數 (1-" + markedWords.size() + "):"));
        inputPanel.add(questionAmountComboBox);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton confirmButton = new JButton("確定");
        JButton cancelButton = new JButton("取消");
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        settingsDialog.add(inputPanel, BorderLayout.CENTER);
        settingsDialog.add(buttonPanel, BorderLayout.SOUTH);

        final boolean[] isConfirmed = {false};

        confirmButton.addActionListener(e -> {
            questionCount = (Integer) questionAmountComboBox.getSelectedItem();
            isConfirmed[0] = true;
            settingsDialog.dispose();
        });

        cancelButton.addActionListener(e -> {
            questionCount = 0;
            settingsDialog.dispose();
        });

        settingsDialog.setVisible(true);

        return isConfirmed[0];
    }

    private void startQuiz() {
        quizWords = new ArrayList<>(markedWords);
        Collections.shuffle(quizWords);
        quizWords = quizWords.subList(0, questionCount);

        currentQuestionIndex = 0;
        score = 0;
        nextQuestion();
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

        Collections.shuffle(optionsList);

        for (int i = 0; i < 4; i++) {
            options[i].setText(optionsList.get(i));
            options[i].setForeground(Color.BLACK);
        }

        optionsGroup.clearSelection();
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
