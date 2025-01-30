import java.awt.Color;
import controlP5.ControlP5;
import controlP5.Textfield;
import controlP5.Textlabel;
import java.util.ArrayList;

/**
 * Represents a user pane component, which extends {@link Component}.
 * Displays a list of currently connected users, based on the information provided by the server.
 *
 */
public class UserPane extends Component {
  private Textfield[] userLabels;
  private Textlabel title;
  private int MAXUSERLABELS = 9;


  /**
   * Constructs a UserPane with specified parameters and initializes its components.
   *
   * @param x the x-coordinate of the user pane
   * @param y the y-coordinate of the user pane
   * @param cWidth the width of the user pane
   * @param cHeight the height of the user pane
   * @param bgColor the background color of the user pane
   * @param borderColor the border color of the user pane
   * @param radius the corner radius of the user pane
   */
  public UserPane(int x, int y, int cWidth, int cHeight, Color bgColor, Color borderColor, int radius) {
    super(x, y, cWidth, cHeight, bgColor, borderColor, radius);
    userLabels = new Textfield[MAXUSERLABELS];
    drawComponent();
  }

  /**
   * Initializes and lays out the user pane's UI elements, including a title label for displaying user connection status.
   */
  @Override
    public void drawComponent() {
    super.drawComponent();
    ControlP5 cp5 = Application.getInstance().getControlP5();
    title = cp5.addTextlabel("userPaneLblTitle")
      .setText("")
      .setPosition(super.getX() + 20, super.getY() + 20)
      .setColor(255);
    int labelWidth = 60;
    for (int i = 0; i < MAXUSERLABELS; i++) {
      Textfield tf = cp5.addTextfield("userPaneLbl" + i)
        .setPosition(super.getX() + 25 + i * (labelWidth + 5), super.getY() + 40)
        .setSize(labelWidth, 20)
        .setLabel("")
        .setColor(255)
        .lock()
        .hide(); // Start hidden
      userLabels[i] = tf;
    }
  }

  /**
   * Sends a heartbeat signal for the currently logged-in user to the server.
   */
  public void sendHeartbeat() {
    User currentUser = Application.getInstance().getSession().getCurrentUser();
    if (currentUser != null) {
      Application.getInstance().getClientSide().writeToServer("UserHeartbeat;;;" + currentUser.getUsername());
    }
  }

  /**
   * Updates the display of connected users based on a userlist from the server.
   *
   * @param input a semicolon-separated string containing usernames of connected users, formatted as "ConnectedUsers;;;username1;;;username2;;;...".
   */
  public void update(String input) {
    String[] messageParts = input.split(";;;");

    //hide labels and remove text
    for (Textfield tf : userLabels) {
      tf.hide();
      tf.setText("");
    }
    super.drawOver();

    if (messageParts.length < 2) {
      title.setText("No connected users.");
    } else {
      title.setText("Currently connected users:");

      for (int i = 1; i < messageParts.length; i++) {
        String userDisplayName = messageParts[i].length() > 10 ? messageParts[i].substring(0, 10) : messageParts[i];
        if (i<=MAXUSERLABELS) {
          userLabels[i-1].setText(userDisplayName);
          userLabels[i-1].show();
        }
      }
    }
  }
}
