public class Word {
    private String word;      // 單字
    private String meaning;   // 意思
    private String partOfSpeech; // 詞性
    private String category;  // 分類
    private int difficulty;   // 難度（可選）
    private boolean learned;  // 是否已學習

    public Word(String word, String meaning, String partOfSpeech, String category, int difficulty, boolean learned) {
        this.word = word;
        this.meaning = meaning;
        this.partOfSpeech = partOfSpeech;
        this.category = category;
        this.difficulty = difficulty;
        this.learned = learned;
    }

    public String getWord() {
        return word;
    }

    public String getMeaning() {
        return meaning;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public String getCategory() {
        return category;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public boolean getLearned() {
        return learned;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public void setLearned(boolean learned) {
        this.learned = learned;
    }
}
