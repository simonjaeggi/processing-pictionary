import processing.net.*;
import controlP5.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

private Application appInstance;
private Canvas canvas;
private Chat chat;
private UserPane userPane;
private Login login;
private MessagePane messagePane;
private ScoreBoard scoreBoard;
private long timestampGamestart;
private final int POINTSFORWORDGUESS = 200;
private final int MAXPOINTSFORDRAWING = 400;
private final int MINPOINTSFORDRAWING = 100;
private boolean gameOver;
private long currentGameDuration;

/**
 * Sets the properties of the GUI and initializes the different GUI components.
 */
void setup() {
  Color customRed = new Color(255, 102, 102);
  Color customDarkRed = new Color (139, 0, 0);
  Color customBlue =new Color(174, 198, 207);
  Color customGreen = new Color(119, 221, 119);
  Color customDarkGreen = new Color(0, 100, 0);
  Color customDarkBlue = new Color(0, 0, 139);

  appInstance = Application.getInstance();
  appInstance.setControlP5(new ControlP5(this));
  appInstance.setServer(new Server(this, 12345));
  appInstance.setServerSide(this);
  appInstance.setUserList(new UserList("data/data.sqlite"));
  appInstance.setWordList(new WordList("data/data.sqlite"));


  size(1000, 900);
  background(0);
  stroke(0);

  chat = new Chat(700, 200, 300, 700, customBlue, customDarkBlue, 0);
  canvas = new Canvas(0, 200, 700, 700, Color.WHITE, Color.WHITE, 0);
  login = new Login(0, 0, 200, 200, Color.BLACK, Color.BLACK, 0);
  userPane = new UserPane(200, 125, 600, 75, Color.BLACK, Color.BLACK, 0);
  messagePane = new MessagePane(300, 20, 400, 50, 10, this, customRed, customDarkRed, customGreen, customDarkGreen, 3);
  scoreBoard = new ScoreBoard(800, 0, 200, 200, Color.BLACK, Color.BLACK, 0);
  gameOver = true;
}

/**
 * Main draw loop of the game.
 * Updates current UI and responds to incoming client messages.
 * Checks for game status and handles serverside user heartbeat.
 */
void draw() {
  userPane.update();
  scoreBoard.update();
  canvas.update();
  handleIncomingMessages();

  if (!gameOver) {
    currentGameDuration =  (System.currentTimeMillis() - timestampGamestart) / 1000;
    canvas.setTimerSeconds(currentGameDuration);
  }
  if (Application.getInstance().getUserList().getCurrentUser() != null) {
    userPane.updateUserHeartbeat("UserHeartbeat;;;"+Application.getInstance().getUserList().getCurrentUser().getUsername());
  }
}

/**
 * Handles incoming messages from the clients and redirects them to the corresponding methods.
 */
public void handleIncomingMessages() {
  Server server = appInstance.getServer();
  Client client = server.available();


  if (client != null) {
    String rawMessage = client.readString();

    //make sure that only one message is processed at a time
    String pattern = "!MSG(.*?)MSG\\?";
    Pattern regex = Pattern.compile(pattern);
    Matcher matcher = regex.matcher(rawMessage);

    // Iterate over all matches and print the captured group
    while (matcher.find()) {
      String message = matcher.group(1); // Get the content in the first capturing group
      //println("Received: " + message);
      if (message.startsWith("Chat")) {
        chat.update(message);
      } else if (message.startsWith("ValidateUserLogin")) {
        login.clientLogin(message);
      } else if (message.startsWith("CreateUser")) {
        login.clientRegistration(message);
      } else if (message.startsWith("UserHeartbeat")) {
        userPane.updateUserHeartbeat(message);
      } else if (message.startsWith("RequestUserStats")) {
        login.updateClientUserStats(message);
      }
    }
  }
}
/**
 * Sends a formatted message to all clients in a specific format.
 *
 * @param message The raw message to be sent to the server.
 */
public void writeToClients(String message) {
  //println("Sending: " + message);
  Application.getInstance().getServer().write("!MSG"+message+"MSG?");
}

/**
 * Starts a new game and resets canvas.
 */
public void startGame() {
  canvas.clear();
  gameOver = false;
  timestampGamestart = System.currentTimeMillis();
  writeToClients("Gamestart");
  canvas.allowDrawing(true);
}

/**
 * Ends the current game and stops canvas inputs. Attributes points to the drawing user based on duration of game
 * and a fixed amount to the guessing user. Updates the user stats and sends a message to the clients, telling them
 * who won the game. Updates the canvas to show the restart button.
 * @param username The username of the user that guessed the word correctly.
 */
public void endGame(String username) {
  gameOver = true;
  canvas.allowDrawing(false);
  UserList userList = Application.getInstance().getUserList();

  // Retrieve the winning user using the provided username
  User winningUser = userList.getUserByUsername(username);

  if (winningUser != null) {
    userList.awardPointsToUser(winningUser, POINTSFORWORDGUESS);
    // Notify user of points
    showMessage(POINTSFORWORDGUESS + " points awarded to the guessing user: " + winningUser.getUsername(), false);
  } else {
    showMessage("Could not accredit guessing user with points, user not found", true);
  }

  // Get the current user who is drawing
  User drawingUser = userList.getCurrentUser();

  if (drawingUser != null) {
    int pointsForDrawingUser;

    if (currentGameDuration <= 20) {
      pointsForDrawingUser = MAXPOINTSFORDRAWING;
    } else {
      // Calculate point reduction for each 20-second period beyond the first 20 seconds
      int extraTime = (int) currentGameDuration - 20;
      int reductions = 1+extraTime / 20;
      pointsForDrawingUser = MAXPOINTSFORDRAWING - (reductions * 50);
      pointsForDrawingUser = Math.max(pointsForDrawingUser, MINPOINTSFORDRAWING); // Ensure minimum points
    }

    userList.awardPointsToUser(drawingUser, pointsForDrawingUser);
    // Notify user of points
    showMessage("Game lasted " + currentGameDuration +" secconds. " + pointsForDrawingUser + " points awarded to the drawing user: " + drawingUser.getUsername(), false);
  } else {
    showMessage("Could not accredit drawing user with points, user not found", true);
  }
  writeToClients("Winner;;;"+username);
  canvas.getWordPicker().showRestartButton();
  login.updateUserStats();
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
 * Shows the wordpicker in the canvas.
 */
public void showWordPicker() {
  canvas.getWordPicker().showSelection();
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
  } else if (controllerName.startsWith("wordPickerBtnWord")) {
    String numberPart = controllerName.substring("wordPickerBtnWord".length());
    int num = Integer.parseInt(numberPart); // Convert the number part to an integer
    canvas.getWordPicker().selectWord(num);
  } else if (controllerName.equals("messageBtnClose")) {
    messagePane.closeMessage();
  } else if (controllerName.equals("wordPickerBtnRestartGame")) {
    canvas.getWordPicker().showSelection();
  }
}
