import processing.net.*;  //<>//
import controlP5.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

private Login login;
private Application appInstance;
private MessagePane messagePane;
private Canvas canvas;
private Chat chat;
private UserPane userPane;
private ScoreBoard scoreBoard;
private boolean gameOver;
private final String SERVERADDRESS = "localhost";
private final int PORT = 12345;

/**
 * Sets the properties of the GUI, initializes the different GUI components and
 * establishes the server connection.
 */
void setup() {
  size(1000, 900);
  background(0);   
  stroke(0);
  Color customRed = new Color(255, 102, 102);
  Color customDarkRed = new Color (139, 0, 0);
  Color customBlue =new Color(174, 198, 207);
  Color customGreen = new Color(119, 221, 119);
  Color customDarkGreen = new Color(0, 100, 0);
  Color customDarkBlue = new Color(0, 0, 139);


  appInstance = Application.getInstance();
  appInstance.setControlP5(new ControlP5(this));
  appInstance.setSession(new Session());
  appInstance.setClientSide(this);

  chat = new Chat(700, 200, 300, 700, customBlue, customDarkBlue, 0);
  userPane = new UserPane(200, 125, 600, 70, Color.BLACK, Color.BLACK, 0);
  login = new Login(0, 0, 200, 200);
  messagePane = new MessagePane(300, 20, 400, 50, 10, this, customRed, customDarkRed, customGreen, customDarkGreen, 3);
  canvas = new Canvas(0, 200, 700, 700, Color.WHITE, customDarkBlue, 0);
  scoreBoard = new ScoreBoard(800, 0, 200, 200, Color.BLACK, Color.BLACK, 0);
  connectToServer();
}
/**
 * Main draw loop of the game.
 * Updates current UI and responds to incoming server messages.
 * Checks for server connectivity and sends heartbeats to signalise active user.
 */
void draw() {
  Client client = Application.getInstance().getClient();
  if (client.active()) {
    userPane.sendHeartbeat();
    cursor(ARROW);
    handleIncomingMessages();
  } else {
    //we need to disable the sound notification, otherwise it will be repeated over and over again.
    messagePane.setSoundEnabled(false);
    cursor(WAIT);
    connectToServer();
    messagePane.setSoundEnabled(true);
  }
}
/**
 * Handles incoming messages from the server and redirects them to the corresponding methods.
 */
public void handleIncomingMessages() {
  Client client = Application.getInstance().getClient();

  if (client.available()>0) {
    String rawMessage = client.readString();

    //make sure that only one message is processed at a time
    String pattern = "!MSG(.*?)MSG\\?";
    Pattern regex = Pattern.compile(pattern);
    Matcher matcher = regex.matcher(rawMessage);

    while (matcher.find()) {
      String message = matcher.group(1); // Get the content in the first capturing group
      //println("Received: " + message);
      if (message.startsWith("Chat")) {
        chat.update(message);
      } else if (message.startsWith("Draw")) {
        canvas.update(message);
      } else if (message.startsWith("UserLoginResponse")) {
        login.loginVerification(message);
      } else if (message.startsWith("UserCreatedResponse")) {
        login.registrationVerification(message);
      } else if (message.startsWith("Winner")) {
        gameOver(message);
      } else if (message.startsWith("ConnectedUsers")) {
        userPane.update(message);
      } else if (message.startsWith("Gamestart")) {
        canvas.clear();
        showMessage("New Game has started. Guess away!", false);
      } else if (message.startsWith("Topscores")) {
        scoreBoard.update(message);
      } else if (message.startsWith("UserStats")) {
        login.updateUserStats(message);
      }
    }
  }
}
/**
 * Handles the end-of-game logic by processing the game over message received from the server.
 * @param input The game over message received from the server, expected as "GameOver;;;username"
 */
private void gameOver(String input) {
  String[] messageParts = input.split(";;;");
  String username = messageParts[1];
  User currentUser = Application.getInstance().getSession().getCurrentUser();
  if (username.equals(currentUser.getUsername())) {
    //Display message you won
    showMessage("Yay, well done you won and earned some points!!", false);
  } else {
    showMessage("Awwww " + username + " was faster than you. Good luck next time!", true);
  }
  gameOver = true;
  writeToServer("RequestUserStats;;;"+currentUser.getUsername());
}
/**
 * Checks if the game has ended.
 * @return true if the game is over, false otherwise.
 */
public boolean isGameOver() {
  return gameOver;
}

/**
 * Enables or disables chat interaction based on the provided flag.
 * @param allowance true to allow chat interaction, false to disable it.
 */
public void allowChatInteraction(boolean allowance) {
  chat.allowChat(allowance);
}

/**
 * Displays a message to the user.
 *
 * @param message The message to be displayed.
 * @param isCritical Indicates if the message is critical.
 */
public void showMessage(String message, boolean isCritical) {
  messagePane.showMessage(message, isCritical);
}

/**
 * Attempts to connect to the server.
 * Notifies the user about the connection status through UI messages.
 */
public void connectToServer() {
  try {
    Client client = new Client(this, SERVERADDRESS, PORT);
    Application.getInstance().setClient(client);

    if (!client.active()) {
      messagePane.showMessage("Client not active, no connection to server!", true);
    } else {
      messagePane.showMessage("Connected to server successfully!", false);
    }
  }
  catch(Exception e) {
    messagePane.showMessage("Exception while connecting to server, connection failed!", true);
  }
}

/**
 * Handles control events triggered by the user interface, such as button clicks.
 * This method routes the actions based on the source of the event, identified by the controller's name.
 *
 * @param theEvent The control event captured by the system.
 */
public void controlEvent(ControlEvent theEvent) {
  String controllerName = theEvent.getController().getName();
  if (controllerName.equals("loginBtnRegister")) {
    login.register();
  } else if (controllerName.equals("loginBtnLogin")) {
    login.login();
  } else if (controllerName.equals("messageBtnClose")) {
    messagePane.closeMessage();
  } else if (controllerName.equals("chatBtnSend")) {
    chat.send();
  }
}

/**
 * Sends a formatted message to the server in a specific format.
 *
 * @param message The raw message to be sent to the server.
 */
public void writeToServer(String message) {
  Application.getInstance().getClient().write("!MSG" + message + "MSG?");
}

/**
 * Handles key press events.
 */
public void keyPressed() {
  if (key == ENTER && chat.isChatAllowed()) {
    chat.send();
  }
}
