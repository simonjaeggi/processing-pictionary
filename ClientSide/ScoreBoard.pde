import java.awt.Color;
import controlP5.ControlP5;
import controlP5.Slider;
import controlP5.Textlabel;

/**
 * Represents a scoreboard component in a graphical user interface, which extends {@link Component}.
 * This class displays the alltime highscores of players dynamically using sliders to represent the scores visually.
 * It also handles receiving the scores from the server.
 */
public class ScoreBoard extends Component {

  private Slider[] sliders;
  private Textlabel title;

  /**
   * Constructs a ScoreBoard with specified parameters and initializes its components.
   *
   * @param x the x-coordinate of the scoreboard
   * @param y the y-coordinate of the scoreboard
   * @param cWidth the width of the scoreboard
   * @param cHeight the height of the scoreboard
   * @param bgColor the background color of the scoreboard
   * @param borderColor the border color of the scoreboard
   * @param radius the corner radius of the scoreboard
   */
  public ScoreBoard(int x, int y, int cWidth, int cHeight, Color bgColor, Color borderColor, int radius) {
    super(x, y, cWidth, cHeight, bgColor, borderColor, radius);
    drawComponent();
  }

  /**
   * Initializes and lays out the scoreboard's UI elements, including sliders for scores and a title label.
   */
  @Override
    public void drawComponent() {
    super.drawComponent();
    ControlP5 cp5 = Application.getInstance().getControlP5();

    if (sliders == null) {
      sliders = new Slider[3];
      for (int i = 0; i < sliders.length; i++) {
        sliders[i] = cp5.addSlider("Bar " + (i + 1))
          .setPosition(super.getX() + 20, super.getY() + 50 + i * 50)
          .setSize(150, 20)
          .lock()
          .hide();
      }
    }
    if (title == null) {
      title = cp5.addTextlabel("scoreBoardLblTitle")
        .setText("Best players by total score")
        .setPosition(super.getX() + 15, super.getY() + 20)
        .setColor(255);
    }
  }

  /**
   * Updates the sliders based on incoming score data. This method parses the input, updates the sliders, and sets their labels accordingly.
   *
   * @param input a semicolon-separated string containing usernames and their corresponding scores formatted as "Topscores;;;user1;;;score1..."
   */
  public void update(String input) {
    super.drawOver();
    String[] messageParts = input.split(";;;");

    ArrayList<User> highestScoringUsers = new ArrayList<>();
    for (int i = 1; i < messageParts.length; i += 2) {
      String username = messageParts[i];
      int score = Integer.parseInt(messageParts[i + 1]);
      highestScoringUsers.add(new User(username, 0, score));
    }

    // Ensure we do not exceed the actual number of highest scoring users returned
    int numUsersToUpdate = Math.min(sliders.length, highestScoringUsers.size());

    for (int i = 0; i < numUsersToUpdate; i++) {
      if (highestScoringUsers.get(i) != null) {
        // set range of all sliders to the highest score, if there is no higher score than 0, we set a range from 0 to 1, so that the sliders can be displayed correctly.
        sliders[i].setRange(0, Math.max(highestScoringUsers.get(0).getScore(), 1));
        sliders[i].setValue(highestScoringUsers.get(i).getScore());
        sliders[i].setLabel(highestScoringUsers.get(i).getUsername());
        sliders[i].getCaptionLabel().align(ControlP5.LEFT, ControlP5.BOTTOM_OUTSIDE).setPaddingX(5);
        sliders[i].show();
      }
    }
  }
}
