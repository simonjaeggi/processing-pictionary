import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A component that manages and displays a list of connected users, extends {@link Component}.
 * Manages the connection status of both server and client users by listening to heartbeats
 * and aging each user until they expire and are deleted.
 */
public class UserPane extends Component {
  private HashMap<String, Integer> users;
  private int maxHeartbeatAge;
  private Textfield[] userLabels;
  private Textlabel title;
  private int MAXUSERLABELS = 9;

  /**
   * Constructs a UserPane component with specified location, size, and visual attributes.
   *
   * @param x the x-coordinate of the user pane.
   * @param y the y-coordinate of the user pane.
   * @param cWidth the width of the user pane.
   * @param cHeight the height of the user pane.
   * @param bgColor the background color of the user pane.
   * @param borderColor the border color of the user pane.
   * @param radius the radius of the corners of the user pane.
   */
  public UserPane(int x, int y, int cWidth, int cHeight, Color bgColor, Color borderColor, int radius) {
    super(x, y, cWidth, cHeight, bgColor, borderColor, radius);
    this.maxHeartbeatAge = 100;
    users = new HashMap<>();
    userLabels = new Textfield[MAXUSERLABELS];

    drawComponent();
  }




  /**
   * Initializes the visual components of the user pane.
   */
  @Override
    public void drawComponent() {
    super.drawComponent();
    ControlP5 cp5 = Application.getInstance().getControlP5();
    if (title == null) {
      title = cp5.addTextlabel("userPaneLblTitle")
        .setText("")
        .setPosition(super.getX()+20, super.getY()+20)
        .setColor(255);
    }
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
   * Updates the heartbeat counter for a user. If the user does not exist, they are added.
   *
   * @param input A string input typically expected as "UserHeartbeat;;;username".
   */
  public void updateUserHeartbeat(String input) {
    String[] messageParts = input.split(";;;");
    String username = messageParts[1];

    users.put(username, 0);
  }

  /**
   * Updates the display of currently connected users. This includes aging heartbeats,
   * removing expired user entries, and updating the display of active users.
   * Sends the currently connected users to the clients.
   */
  public void update() {
    if (users.isEmpty()) {
      title.setText("No connected users.");
    } else {
      title.setText("Currently connected users:");
    }
    //hide labels and remove text
    for (Textfield tf : userLabels) {
      tf.hide();
      tf.setText("");
    }

    super.drawOver();


    //Age user
    for (String key : users.keySet()) {
      users.put(key, users.get(key) + 1);
    }

    // Use an iterator to safely remove old users
    Iterator<Map.Entry<String, Integer>> iterator = users.entrySet().iterator();

    int currentIteration = 0;

    //String that is sent to the clients to update their userlist
    String userListForClients = "";

    while (iterator.hasNext() && currentIteration < MAXUSERLABELS) {
      Map.Entry<String, Integer> entry = iterator.next();
      String key = entry.getKey();
      Integer value = entry.getValue();

      if (value >= maxHeartbeatAge) {
        iterator.remove();
      } else {
        String userDisplayName = key.length() > 10 ? key.substring(0, 10) : key;

        userListForClients += ";;;"+key;
        userLabels[currentIteration].setText(userDisplayName);
        userLabels[currentIteration].show();
      }

      currentIteration++;
    }

    //send current userlist to client:
    Application.getInstance().getServerSide().writeToClients("ConnectedUsers"+userListForClients);
  }
}
