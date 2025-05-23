# 單字學習工具

一個簡單的單字學習應用程式，幫助使用者學習各種類別的單字。

## 功能特點
- 支援多種單字類別（食物、旅遊、生活、商業等，也包括檢定常見單字）
- 瀏覽模式：循序學習單字
- 測驗模式：隨機測驗或測驗標記單字
- 即時回饋
- 學習進度追蹤
- 標記單字並提供匯出功能

## 系統需求
- Java Runtime Environment (JRE) 8 或更高版本
- 支援中文顯示的作業系統

## 使用說明
1. 確保系統已安裝 Java
2. 下載並解壓縮程式檔案
3. 執行程式：
   - **Windows**：在命令提示字元切換到程式所在目錄後，輸入：
     ```bat
     java -cp "VocLearningTool.jar;lib/*" VocabularyGUI
     ```
   - **Linux/macOS**：在終端機切換到程式所在目錄後，輸入：
     ```bash
     java -cp 'VocLearningTool.jar:lib/*' VocabularyGUI
     ```
4. 在主選單選擇想要學習的單字類別
5. 選擇「瀏覽模式」或「測驗模式」開始學習

## 單字檔案格式
CSV 檔案格式，應包含以下欄位：
```
word, meaning, part_of_speech, category, importance
```
範例：
```
Abandon, 丟棄, 動詞, 多益, 3
Access, 通道/接近, 名詞, 托福, 2
```
