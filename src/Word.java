public class Word {
    private String word;      // 單字
    private String meaning;   // 意思
    private String partOfSpeech; // 詞性
    private String category;  // 分類
    private int importance;   // 重要性
    private boolean isMarked = false; // 是否已標註

    public Word(String word, String meaning, String partOfSpeech, String category, int difficulty, boolean isMarked) {
        this.word = word;
        this.meaning = meaning;
        this.partOfSpeech = partOfSpeech;
        this.category = category;
        this.importance = difficulty;
        this.isMarked = isMarked;
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

    public int getImportance() {
        return importance;
    }

    public String getImportantStars() {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < importance; i++) {
            stars.append("★");
        }
        return stars.toString();
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

    public void setImportance(int difficulty) {
        this.importance = difficulty;
    }

    public boolean isMarked() {
        return isMarked;
    }

    public void setMarked(boolean marked) {
        isMarked = marked;
    }
}
