/**
 * Displays a scoreboard using sliders to show the highest scores dynamically, which extends {@link Component}.
 * This component is designed to visually represent the top user scores.
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
   * Initializes and sets up the UI components of the scoreboard, including sliders and a title label.
   * The method ensures that the UI components are created only once and updates the display.
   */
  @Override
    public void drawComponent() {
    super.drawComponent();
    ControlP5 cp5 = Application.getInstance().getControlP5();

    if (sliders == null) {
      sliders = new Slider[3];
      for (int i = 0; i < sliders.length; i++) {
        sliders[i] = cp5.addSlider("Bar " + (i + 1))
          .setPosition(super.getX()+20, super.getY() +50+ i * 50)
          .setSize(150, 20)
          .lock()
          .hide();
      }
    }
    if (title == null) {
      title = cp5.addTextlabel("scoreBoardLblTitle")
        .setText("Best players by total score")
        .setPosition(super.getX()+15, super.getY()+20)
        .setColor(255);
    }

    update();
  }
  /**
   * Updates the sliders with current high scores. This method fetches the top user scores and updates the sliders accordingly.
   * It also formats and sends the current top scores to clients.
   */
  public void update() {
    super.drawOver();

    User[] highestScoringUsers = Application.getInstance().getUserList().getUsersWithHighestScores(sliders.length);

    if (highestScoringUsers != null) {
      // Ensure we do not exceed the actual number of highest scoring users returned
      int numUsersToUpdate = Math.min(sliders.length, highestScoringUsers.length);
      String scoreListForClients = "";

      for (int i = 0; i < numUsersToUpdate; i++) {
        // Check if each user element is not null to avoid NullPointerException
        if (highestScoringUsers[i] != null) {
          scoreListForClients += ";;;"+highestScoringUsers[i].getUsername()+";;;" + highestScoringUsers[i].getScore();
          sliders[i].setRange(0, max(highestScoringUsers[0].getScore(), 1)); // set range of all sliders to the highest score (first user)
          sliders[i].setValue(highestScoringUsers[i].getScore());
          sliders[i].setLabel(highestScoringUsers[i].getUsername());
          // Align the label of the slider
          sliders[i].getCaptionLabel().align(ControlP5.LEFT, ControlP5.BOTTOM_OUTSIDE).setPaddingX(5);
          sliders[i].show();
        }
      }
      //send current topscores to client:
      Application.getInstance().getServerSide().writeToClients("Topscores"+scoreListForClients);
    }
  }
}
