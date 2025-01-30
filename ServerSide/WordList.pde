import de.bezier.data.sql.*;
import java.util.HashSet;
/**
 * Manages a list of words, including loading them from a database, and provides functionality for word validation and selection.
 */
public class WordList {
  private ArrayList<String[]> words;
  private SQLite db;
  private String[] currentWord;

  /**
   * Constructs a WordList, initializing the database connection and loading words from the specified data source.
   *
   * @param dataSource The path or identifier for the data source used by SQLite.
   */
  public WordList(String dataSource) {
    db = new SQLite(Application.getInstance().getServerSide(), dataSource);
    words = new ArrayList<>();
    loadWordsFromDB();
  }

  /**
   * Loads words from the database into the words list. Each word entry is a string array representing the word in different languages.
   */
  public void loadWordsFromDB() {
    try {
      if ( db.connect()) {
        print("db connected");
        db.query( "SELECT * FROM words" );
        while (db.next()) {
          String wordGer = db.getString("wordGer");
          String wordEng = db.getString("wordEng");
          String wordUkr = db.getString("wordUkr");
          //String wordChi = db.getString("wordChi"); //chinese characters are not displayed correctly, so we dont load them.
          String wordMon = db.getString("wordMon");
          String wordSpa = db.getString("wordSpa");
          String wordPor = db.getString("wordPor");
          String wordKen = db.getString("wordKen");
          String wordNig = db.getString("wordNig");
          String wordFre = db.getString("wordFre");
          String[] word = {wordGer, wordEng, wordUkr, wordMon, wordSpa, wordPor, wordKen, wordNig, wordFre};
          words.add(word);
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }


  /**
   * Validates if the given word matches any word in the currentWord array.
   *
   * @param word The word to validate.
   * @return true if the word matches any language version of the current word, false otherwise.
   */
  public boolean validateWord(String word) {
    if (currentWord != null) {
      for (int i=0; i<currentWord.length; i++) {
        if (currentWord[i].toLowerCase().equals(word.toLowerCase())) {
          return true;
        }
      }
    }
    return false;
  }


  /**
   * Randomly selects a set of 3 unique words from the list.
   *
   * @return An ArrayList of String arrays, each containing word representations in different languages.
   */
  public ArrayList<String[]> getWordSelection() {
    HashSet<String[]> uniqueSelection = new HashSet<>();

    //easy way to ensure that 3 different words are returned is to just try and add it to the set until it is long enough
    while (uniqueSelection.size() < 3) {

      uniqueSelection.add(words.get((int)random(words.size())));
    }
    ArrayList<String[]> wordSelection = new ArrayList(uniqueSelection);

    return wordSelection;
  }

  /**
   * Sets the currently active word array.
   *
   * @param word An array of strings representing the same word in different languages.
   */
  public void setCurrentWord(String[] word) {
    currentWord = word;
  }

  /**
   * Retrieves the currently active word array.
   *
   * @return An array of strings representing the current word in different languages.
   */
  public String[] getCurrentWord() {
    return currentWord;
  }
}
