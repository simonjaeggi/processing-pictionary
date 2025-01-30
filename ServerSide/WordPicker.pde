/**
 * A component that handles the display and interaction of word choices, extends {@link Component}.
 * It allows players to select words from displayed options and displays the selected word.
 */
public class WordPicker extends Component {
  private Button word0;
  private Button word1;
  private Button word2;
  private Button wordLabel;
  private Button restartGame;
  private ArrayList<String[]> wordSelection;
  /**
   * Constructs a WordPicker component with specified position, dimensions, and styling.
   *
   * @param x the x-coordinate of the component.
   * @param y the y-coordinate of the component.
   * @param cWidth the width of the component.
   * @param cHeight the height of the component.
   * @param bgColor the background color of the component.
   * @param borderColor the border color of the component.
   * @param radius the corner radius of the component.
   */
  public WordPicker(int x, int y, int cWidth, int cHeight, Color bgColor, Color borderColor, int radius) {
    super(x, y, cWidth, cHeight, bgColor, borderColor, radius);
    drawComponent();
  }

  /**
   * Initializes the visual components of the word picker.
   * Removes previous buttons from CP5 component before creating them anew.
   */
  @Override
    public void drawComponent() {
    super.drawComponent();
    ControlP5 cp5 = Application.getInstance().getControlP5();

    if (cp5.get("wordPickerBtnWord0") != null) {
      cp5.remove("wordPickerBtnWord0");
    }
    if (cp5.get("wordPickerBtnWord1") != null) {
      cp5.remove("wordPickerBtnWord1");
    }
    if (cp5.get("wordPickerBtnWord2") != null) {
      cp5.remove("wordPickerBtnWord2");
    }
    if (cp5.get("wordPickerWordLabel") != null) {
      cp5.remove("wordPickerWordLabel");
    }
    if (cp5.get("wordPickerBtnRestartGame") != null) {
      cp5.remove("wordPickerBtnRestartGame");
    }
    word0 = cp5.addButton("wordPickerBtnWord0")
      .setLabel("")
      .setPosition(super.getX()+10, super.getY()+10)
      .setSize(80, 40)
      .hide();

    word1 = cp5.addButton("wordPickerBtnWord1")
      .setLabel("")
      .setPosition(super.getX()+110, super.getY()+10)
      .setSize(80, 40)
      .hide();

    word2 = cp5.addButton("wordPickerBtnWord2")
      .setLabel("")
      .setPosition(super.getX()+210, super.getY()+10)
      .setSize(80, 40)
      .hide();

    wordLabel =  cp5.addButton("wordPickerWordLabel")
      .setLabel("")
      .setPosition(super.getX(), super.getY())
      .setSize(super.getWidth(), super.getHeight())
      .hide()
      .lock()
      .setFont(createFont("SansSerif", 20));

    restartGame = cp5.addButton("wordPickerBtnRestartGame")
      .setLabel("Restart Game")
      .setPosition(super.getX(), super.getY())
      .setSize(super.getWidth(), super.getHeight())
      .hide()
      .setFont(createFont("SansSerif", 20));
  }

  /**
   * Displays a selection of words to the user.
   */
  public void showSelection() {
    wordSelection = Application.getInstance().getWordList().getWordSelection();
    restartGame.hide();
    super.drawOver();
    word0.setLabel(wordSelection.get(0)[1]);
    word1.setLabel(wordSelection.get(1)[1]);
    word2.setLabel(wordSelection.get(2)[1]);
    word0.show();
    word1.show();
    word2.show();
  }

  /**
   * Handles the selection of a word by a user, updates the current word, and starts the game.
   *
   * @param wordSelectionIndex The index of the selected word in the wordSelection list.
   */
  public void selectWord(int wordSelectionIndex) {
    String[] currentWord = wordSelection.get(wordSelectionIndex);
    Application.getInstance().getWordList().setCurrentWord(currentWord);
    word0.hide();
    word1.hide();
    word2.hide();
    Application.getInstance().getServerSide().startGame();
    super.drawOver();
    wordLabel.setLabel(Application.getInstance().getWordList().getCurrentWord()[1]);
    wordLabel.show();
  }

  /**
   * Displays the button to restart the game.
   */
  public void showRestartButton() {
    wordLabel.hide();
    restartGame.show();
  }
}
