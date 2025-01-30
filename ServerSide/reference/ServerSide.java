import processing.net.*;
import controlP5.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import controlP5.Textarea;
import processing.core.PApplet;
import java.awt.Color;
import processing.sound.*;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import de.bezier.data.sql.*;
import java.util.HashSet;

import processing.core.*;
import processing.data.*;
import processing.event.*;
import processing.opengl.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.File;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/** Processing Sketch ServerSide */
public class ServerSide {

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
/**
 * Singleton Static class that holds all the objects that need to be accessible from everywhere.
 * This class ensures that there is only one instance of each component within the application.
 */

public static class Application {
  private static Application instance;
  private ControlP5 cp5;
  private Server server;
  private ServerSide serverSide;
  private UserList userList;
  private WordList wordList;

  /**
   * Static initializer that runs once to initialize exactly one instance of the Application class.
   */
  static {
    instance = new Application();
  }

  /**
   * Private constructor to prevent instantiation from other classes, ensuring the singleton pattern.
   */
  private Application() {
  }

  /**
   * Provides access to the singleton instance of the Application class.
   * @return the single instance of Application
   */
  public static Application getInstance() {
    return Application.instance;
  }

  /**
   * Sets the ControlP5 instance for the application.
   * @param cp5 the ControlP5 instance to be used by the application.
   */
  public void setControlP5(ControlP5 cp5) {
    this.cp5 = cp5;
  }

  /**
   * Retrieves the current instance of ControlP5 used by the application.
   * @return the ControlP5 instance currently set in the application.
   */
  public ControlP5 getControlP5() {
    return cp5;
  }

  /**
   * Sets the Server instance for the application.
   * @param server the Server instance to be used by the application.
   */
  public void setServer(Server server) {
    this.server = server;
  }

  /**
   * Retrieves the current Server instance used by the application.
   * @return the Server instance currently set in the application.
   */
  public Server getServer() {
    return server;
  }

  /**
   * Retrieves the current ServerSide instance used by the application.
   * @return the ServerSide instance currently set in the application.
   */
  public ServerSide getServerSide() {
    return serverSide;
  }

  /**
   * Sets the ServerSide instance for the application.
   * @param serverSide the ServerSide instance to be used by the application.
   */
  public void setServerSide(ServerSide serverSide) {
    this.serverSide = serverSide;
  }

  /**
   * Sets the UserList instance for the application.
   * @param userList the UserList instance to be used by the application.
   */
  public void setUserList(UserList userList) {
    this.userList = userList;
  }
  /**
   * Retrieves the current UserList instance used by the application.
   * @return the UserList instance currently set in the application.
   */
  public UserList getUserList() {
    return userList;
  }
  /**
   * Sets the WordList instance for the application.
   * @param wordList the WordList instance to be used by the application.
   */
  public void setWordList(WordList wordList) {
    this.wordList = wordList;
  }

  /**
   * Retrieves the current WordList instance used by the application.
   * @return the WordList instance currently set in the application.
   */
  public WordList getWordList() {
    return this.wordList;
  }
}/**
 * Extends {@link Component} to represent a drawable canvas area.
 * Handles a WordPicker, ColorPicker and StrokeSizePicker for the drawing specifics.
 * Sends drawing updates to clients.
 */

public class Canvas extends Component {
  private ColorPicker cp;
  private StrokeSizePicker sp;
  private WordPicker wp;
  private boolean allowDrawing;
  private Textfield timer;
  /**
   * Constructs a new Canvas object with specified dimensions, colors, and border styling.
   * It initializes the component with a background and border int , dimensions, and border radius,
   * and draws the initial state of the component.
   *
   * @param x the x-coordinate of the canvas.
   * @param y the y-coordinate of the canvas.
   * @param cWidth the width of the canvas.
   * @param cHeight the height of the canvas.
   * @param bgColor the background int of the canvas.
   * @param borderColor the int of the canvas border.
   * @param radius the radius of the rounded corners of the border.
   */

  public Canvas(int x, int y, int cWidth, int cHeight, Color bgColor, Color borderColor, int radius) {
    super(x, y, cWidth, cHeight, bgColor, borderColor, radius);
    drawComponent();
    Color customBlue =new Color(174, 198, 207);

    wp = new WordPicker(super.getX()+super.getWidth()-315, super.getY()+15, 300, 60, customBlue, customBlue, 0);
  }

  /**
   * Draws and initializes the canvas.
   * Overrides the {@code drawComponent()} method from the superclass.
   * Sets up a ColorPicker and a StrokeSizePicker along with a timer textfield for displaying time.
   */
  @Override
    public void drawComponent() {
    super.drawComponent();
    ControlP5 cp5 = Application.getInstance().getControlP5();


    cp = new ColorPicker(super.getX()+15, super.getY()+10, 40, 330);
    sp = new StrokeSizePicker(super.getX()+50, super.getY()+10, 100, 40, Color.WHITE, Color.WHITE, 10);

    if (timer == null) {
      timer = cp5.addTextfield("canvasTimer")
        .setText("0")
        .setLabel("Timer")
        .setPosition(super.getX()+super.getWidth()-40, super.getY()+100)
        .setColor(0)
        .setSize(20, 20)
        .setColorBackground(color(255))  // Set the background to white
        .setColorForeground(color(255))  // set border int when not focused
        .setColorActive(color(255))  // set border int when focused
        .setColorValue(color(0))  // Set the text int to black
        .lock();  // Lock the TextField to prevent user editing

      timer.getCaptionLabel()
        .setColor(0)
        .align(ControlP5.RIGHT, ControlP5.TOP_OUTSIDE)
        .setPaddingX(0)
        .setPaddingY(0);
    }
  }

  /**
   * Updates the state and behavior of the drawing canvas based on user interactions and current settings.
   * This method checks whether the user is authorized and if drawing is allowed. It handles mouse interactions
   * for drawing lines with specific stroke sizes and colors. It also manages network communication to broadcast
   * drawing data to other clients and provides user feedback when drawing is not allowed.
   */
  public void update() {
    if (Application.getInstance().getUserList().getCurrentUser() != null) {
      if (isClicked()&&allowDrawing) {
        cp.updatePosition();
        sp.updateStrokeSize();
        int strokeColor = cp.getColor();
        int strokeSize = sp.getStrokeSize();

        if (!cp.isClicked() && !sp.isClicked()) {
          //we send the absolute coordinates with 0,0 at the top left of the canvas
          Application.getInstance().getServerSide().writeToClients("Draw;;;" + (pmouseX-super.getX()) + ";;;" + (pmouseY-super.getY()) +
            ";;;" + (mouseX-super.getX()) + ";;;" + (mouseY-super.getY()) + ";;;" + strokeColor + ";;;" + strokeSize);
          stroke(strokeColor);
          strokeWeight(strokeSize);
          line(mouseX, mouseY, pmouseX, pmouseY);
        }
      } else if (isClicked()&&!allowDrawing&&!wp.isClicked()) {
        Application.getInstance().getServerSide().showMessage("Make sure to restart the game before drawing.", true);
      }
    } else if (Application.getInstance().getUserList().getCurrentUser() == null && isClicked()) {
      Application.getInstance().getServerSide().showMessage("You need to login before you can draw!", true);
    }
  }


  /**
   * Retrieves the WordPicker instance associated with the canvas.
   * The {@code WordPicker} is responsible for enabling the selection of words within the application.
   *
   * @return the WordPicker instance.
   */
  public WordPicker getWordPicker() {
    return wp;
  }

  /**
   * Clears the canvas by re-drawing the initial state of the component.
   */
  public void clear() {
    drawComponent();
  }

  /**
   * Enables or disables the drawing capability based on the given boolean flag.
   * When drawing is disabled, any interactions that would typically result in drawing
   * are ignored, as handeled in update().
   *
   * @param allowance true to allow drawing, false to prevent it.
   */
  public void allowDrawing(boolean allowance) {
    allowDrawing = allowance;
  }

  /**
   * Sets the countdown timer's remaining time. This method updates the visual display of the timer
   * within the component to reflect the number of seconds passed since the start of the game.
   *
   * @param seconds the number of seconds to be set on the timer display.
   */
  public void setTimerSeconds(long seconds) {
    timer.setText("" + seconds);
  }
}
/**
 * Represents a chat component in a graphical user interface, extending {@link Component}.
 * Handles chat interactions, displaying chat history, and processing incoming chat messages.
 */
public class Chat extends Component {
  private Textarea chatHistory;

  /**
   * Constructs a Chat component with specified location, dimensions, and visual styling.
   * Initializes the chat area where chat messages will be displayed.
   *
   * @param x the x-coordinate of the chat component.
   * @param y the y-coordinate of the chat component.
   * @param cWidth the width of the chat component.
   * @param cHeight the height of the chat component.
   * @param bgColor the background int of the chat component.
   * @param borderColor the border int of the chat component.
   * @param radius the border radius of the chat component.
   */
  public Chat(int x, int y, int cWidth, int cHeight, Color bgColor, Color borderColor, int radius) {
    super(x, y, cWidth, cHeight, bgColor, borderColor, radius);
    drawComponent();
  }

  /**
   * Initializes and lays out the chat component's UI elements, specifically the chat history textarea.
   */
  @Override
  public void drawComponent() {
    super.drawComponent();
    ControlP5 cp5 = Application.getInstance().getControlP5();
    if (chatHistory == null) {
      chatHistory = cp5.addTextarea("chatHistory")
        .setPosition(super.getX() + 20, super.getY() + 20)
        .setSize(super.getWidth() - 40, super.getHeight() - 40)
        .setLineHeight(14)
        .setColorBackground(color(255))  // Set background int to white
        .setColorForeground(color(255))  // Set foreground int to white
        .setColor(color(0))              // Set text int to black
        .setText("")
        .setFont(createFont("Arial", 12));  // Set the font to Arial size 12
    }
  }

  /**
   * Processes and updates the chat history with a new incoming message.
   * Validates each new message to check if the word was guessed and ends the game if the correct word is detected.
   * Each incoming message is forwarded to all clients.
   *
   * @param input the raw input string received, formatted as "Chat;;;username;;;message"
   */
  public void update(String input) {
    println(input);  // Debug output to console
    String[] messageParts = input.split(";;;");
    String username = messageParts[1];
    String message = messageParts[2];
    addMessageToChat(username + ": " + message);
    Application.getInstance().getServerSide().writeToClients(input);

    // Check if the message contains a word that ends the game
    if (Application.getInstance().getWordList().validateWord(message)) {
      Application.getInstance().getServerSide().endGame(username);
    }
  }

  /**
   * Adds a message to the chat history.
   *
   * @param message the message to be added to the chat history.
   */
  private void addMessageToChat(String message) {
    chatHistory.setText(chatHistory.getStringValue() + message + "\n");
  }
}

/**
 * Represents a int picker component, extending the {@link Component} class.
 * This component allows users to select a int from a predefined set of int options displayed as circles.
 */
public class ColorPicker extends Component {

    private int currentPosX;
    private int currentPosY;

    /**
     * Constructs a ColorPicker component with specified location and dimensions.
     * Initializes the component and sets up the initial drawing state.
     *
     * @param x the x-coordinate of the int picker.
     * @param y the y-coordinate of the int picker.
     * @param cWidth the width of the int picker.
     * @param cHeight the height of the int picker.
     */
    public ColorPicker(int x, int y, int cWidth, int cHeight) {
        super(x, y, cWidth, cHeight);
        drawComponent();
    }

    /**
     * Draws the int picker component on the screen. Defines the visual layout of the int picker,
     * drawing a series of colored circles each representing a selectable int .
     */
    @Override
    public void drawComponent() {
        noStroke();
        // Draw int circles
        int spacing = 30; // Vertical spacing between circles
        int radius = 20; // Radius of each int circle
        int[] colors = new int[] {  // RGB values of the colors
            color(193, 193, 193), // gray
            color(219, 53, 37),   // red
            color(238, 122, 49),  // orange
            color(250, 229, 76),  // yellow
            color(92, 201, 58),   // green
            color(81, 175, 249),  // light blue
            color(35, 31, 203),   // blue
            color(149, 29, 179),  // purple
            color(209, 112, 165), // pink
            color(243, 176, 147), // peach
            color(130, 42, 42)    // brown
        };

        for (int i = 0; i < colors.length; i++) {
            fill(colors[i]);
            circle(super.getX() + 10, super.getY() + 20 + i * spacing, radius);
        }
    }

    /**
     * Updates the position of the int selection based on user interaction.
     * This method can be called to handle user clicks.
     *
     * @return true if the int picker's position was updated, false otherwise.
     */
    public boolean updatePosition() {
        if (isClicked()) {
            drawComponent();
            currentPosX = mouseX;
            currentPosY = mouseY;
            return true;
        }
        return false;
    }

    /**
     * Retrieves the int at the current selected position within the int picker.
     * This method should be used after a call to {@code updatePosition()} confirms a int selection.
     *
     * @return the int at the current position as an integer.
     */
    public int getColor() {
        int pixelColor = get(currentPosX, currentPosY);
        return pixelColor;
    }
}

/**
 * Represents a basic graphical component for a user interface.
 * This class provides a flexible foundation for drawing rectangular components with customizable
 * properties such as int , border, and dimensions.
 */
public class Component {
  private int x;
  private int y;
  private int cWidth;
  private int cHeight;
  private int radius;
  private Color bgColor;
  private Color borderColor;
  private int strokeWeight;

  /**
   * Constructs a component with specified location, width, and height.
   * Initializes the component with default colors and no border radius.
   *
   * @param x the x-coordinate of the component
   * @param y the y-coordinate of the component
   * @param cWidth the width of the component
   * @param cHeight the height of the component
   */
  public Component(int x, int y, int cWidth, int cHeight) {
    this(x, y, cWidth, cHeight, 0, 1);
  }

  /**
   * Constructs a component with specified location, dimensions, and corner radius.
   * Initializes the component with default colors.
   *
   * @param x the x-coordinate of the component
   * @param y the y-coordinate of the component
   * @param cWidth the width of the component
   * @param cHeight the height of the component
   * @param radius the border radius of the component corners
   */
  public Component(int x, int y, int cWidth, int cHeight, int radius) {
    this(x, y, cWidth, cHeight, Color.BLACK, Color.BLACK, radius);
  }

  /**
   * Constructs a component with specified location, dimensions, corner radius, and stroke weight.
   * Initializes the component with default colors.
   *
   * @param x the x-coordinate of the component
   * @param y the y-coordinate of the component
   * @param cWidth the width of the component
   * @param cHeight the height of the component
   * @param radius the border radius of the component corners
   * @param strokeWeight the stroke weight for the border of the component
   */
  public Component(int x, int y, int cWidth, int cHeight, int radius, int strokeWeight) {
    this.x = x;
    this.y = y;
    this.cWidth = cWidth;
    this.cHeight = cHeight;
    this.radius = radius;
    this.strokeWeight = strokeWeight;
    this.bgColor = Color.BLACK;
    this.borderColor = Color.BLACK;
  }

  /**
   * Constructs a component with specified location, dimensions, colors, and corner radius.
   *
   * @param x the x-coordinate of the component
   * @param y the y-coordinate of the component
   * @param cWidth the width of the component
   * @param cHeight the height of the component
   * @param bgColor the background int of the component
   * @param borderColor the border int of the component
   * @param radius the border radius of the component corners
   */
  public Component(int x, int y, int cWidth, int cHeight, Color bgColor, Color borderColor, int radius) {
    this.x = x;
    this.y = y;
    this.cWidth = cWidth;
    this.cHeight = cHeight;
    this.radius = radius;
    this.bgColor = bgColor;
    this.borderColor = borderColor;
    this.strokeWeight = 1;
  }

  /**
   * Draws the component with its current attributes.
   */
  public void drawComponent() {
    strokeWeight(strokeWeight);
    fill(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue());
    stroke(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue());
    rect(x, y, cWidth, cHeight, radius);
  }

  /**
   * Determines if the component is being clicked.
   *
   * @return true if the component is currently clicked, false otherwise.
   */
  public boolean isClicked() {
    return mousePressed && mouseX > x && mouseX < x + cWidth && mouseY > y && mouseY < y + cHeight;
  }

  /**
   * Draws a rectangle over the component using the current background int .
   */
  public void drawOver() {
    strokeWeight(strokeWeight);
    fill(bgColor.getRGB());
    stroke(bgColor.getRGB());
    rect(x-strokeWeight, y-strokeWeight, cWidth+2*strokeWeight, cHeight+2*strokeWeight);
  }

  /**
   * Draws a rectangle over the component using a specified background int .
   * @param bgColor the background int used for drawing the rectangle.
   
   */
  public void drawOver(int bgColor) {
    fill(bgColor);
    stroke(bgColor);
    rect(x-strokeWeight, y-strokeWeight, cWidth+2*strokeWeight, cHeight+2*strokeWeight);
  }

  /**
   * Returns the x-coordinate of the component.
   *
   * @return the x-coordinate of this component
   */
  public int getX() {
    return x;
  }

  /**
   * Returns the y-coordinate of the component.
   *
   * @return the y-coordinate of this component
   */
  public int getY() {
    return y;
  }

  /**
   * Returns the height of the component.
   *
   * @return the height of this component
   */
  public int getHeight() {
    return cHeight;
  }

  /**
   * Returns the width of the component.
   *
   * @return the width of this component
   */
  public int getWidth() {
    return cWidth;
  }

  /**
   * Returns the radius of the component's corners.
   *
   * @return the corner radius of this component
   */
  public int getRadius() {
    return radius;
  }

  /**
   * Returns the background int of the component.
   *
   * @return the current background int of this component
   */
  public Color getBgColor() {
    return bgColor;
  }

  /**
   * Returns the border int of the component.
   *
   * @return the current border int of this component
   */
  public Color getBorderColor() {
    return borderColor;
  }

  /**
   * Sets the background int of the component.
   * Does not redraw the component with the new int .
   *
   * @param bgColor the new background int to set for this component.
   */
  public void setBgColor(Color bgColor) {
    this.bgColor = bgColor;
  }

  /**
   * Sets the border int of the component.
   * Does not redraw the component with the new int .
   *
   * @param borderColor the new border int to set for this component
   */
  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }
}
/**
 * Custom exception for handling scenarios where an attempt is made to create or register a user that already exists.
 * This class extends {@link Exception} and provides a constructor that allows for specifying an error message.
 */
public class DuplicateUserException extends Exception {

    /**
     * Constructs a new {@code DuplicateUserException} with the specified detail message.
     *
     * @param message the detail message.
     */
    public DuplicateUserException(String message) {
        super(message);
    }
}/**
 * Manages the user interface for login and registration activities.
 * This class extends {@link Component}.
 * and labels to display current user information and statistics.
 */

public class Login extends Component {
  private Textfield username;
  private Textfield password;
  private Button login;
  private Button register;
  private Textlabel loggedInUserTitle;
  private Textlabel loggedInUser;
  private Textlabel currentScoreTitle;
  private Textlabel currentScore;
  private Textlabel currentRankTitle;
  private Textlabel currentRank;

  /**
   * Constructs a new Login component with specified location, size, and int attributes.
   * @param x the x-coordinate of the component.
   * @param y the y-coordinate of the component.
   * @param cWidth the width of the component.
   * @param cHeight the height of the component.
   * @param bgColor the background int of the component.
   * @param borderColor the border int of the component.
   * @param radius the radius of the component's corners.
   */
  public Login(int x, int y, int cWidth, int cHeight, Color bgColor, Color borderColor, int radius) {
    super(x, y, cWidth, cHeight, bgColor, borderColor, radius);
    drawComponent();
  }
  /**
   * Initializes and sets up UI components including text fields for username and password,
   * login and register buttons, and various labels for displaying user information and game statistics.
   */
  @Override
    public void drawComponent() {
    super.drawComponent();

    ControlP5 cp5 = Application.getInstance().getControlP5();
    username = cp5.addTextfield("Username")
      .setPosition(super.getX()+20, super.getY()+20)
      .setSize(round(super.getWidth()*0.8), 30);

    password = cp5.addTextfield("Password")
      .setPosition(super.getX()+20, super.getY()+80)
      .setSize(round(super.getWidth()*0.8), 30)
      .setColor(color(255, 0, 0));

    login = cp5.addButton("loginBtnLogin")
      .setLabel("Login")
      .setPosition(super.getX()+20, super.getY()+150)
      .setSize(50, 20);

    register = cp5.addButton("loginBtnRegister")
      .setLabel("Register")
      .setPosition(super.getX()+120, super.getY()+150)
      .setSize(50, 20);

    loggedInUserTitle = cp5.addTextlabel("loginLblLoggedInUserTitle")
      .setText("Currently logged in as:")
      .setPosition(super.getX()+20, super.getY()+20)
      .setSize(50, 20)
      .hide();

    loggedInUser = cp5.addTextlabel("loginLblLoggedInUser")
      .setText("")
      .setPosition(super.getX()+20, super.getY()+35)
      .setSize(50, 20)
      .hide();

    currentScoreTitle = cp5.addTextlabel("loginLblCurrentScoreTitle")
      .setText("Your current total score:")
      .setPosition(super.getX()+20, super.getY()+100)
      .setSize(100, 20)
      .hide();

    currentScore = cp5.addTextlabel("loginLblCurrentScore")
      .setText("")
      .setPosition(super.getX()+20, super.getY()+115)
      .setSize(100, 20)
      .hide();

    currentRankTitle = cp5.addTextlabel("loginLblCurrentRankTitle")
      .setText("Your current rank:")
      .setPosition(super.getX()+20, super.getY()+140)
      .setSize(100, 20)
      .hide();

    currentRank = cp5.addTextlabel("loginLblCurrentRank")
      .setText("")
      .setPosition(super.getX()+20, super.getY()+155)
      .setSize(100, 20)
      .hide();
  }

  /**
   * Handles user registration by the server user. If the input is valid and the user does not already exist,
   * a new user is added to the system. The user is informed about the registration through status messages.
   */
  public void register() {
    String usernameInput = username.getText();
    String passwordInput = password.getText();
    print("reg");
    if (!usernameInput.equals("") && !passwordInput.equals("")) {
      long timestamp = System.currentTimeMillis();
      System.out.println("Timestamp in seconds: " + timestamp);
      try {
        Application.getInstance().getUserList().addUser(usernameInput, passwordInput, timestamp);
        Application.getInstance().getServerSide().showMessage("Registration successful. Please login.", false);
      }
      //handle what happens when user already exists
      catch(DuplicateUserException due) {
        Application.getInstance().getServerSide().showMessage("Username already exists. Please login or register using a different name.", true);
        due.printStackTrace();
      }
    } else {
      Application.getInstance().getServerSide().showMessage("Please make sure to enter a password and username.", true);
    }
  }

  /**
   * Handles user login by the client user. Validates user credentials and updates UI to reflect the logged-in state.
   */
  private void login() {
    String usernameInput = username.getText();
    String passwordInput = password.getText();
    User user;
    if (!usernameInput.equals("") && !passwordInput.equals("")) {
      if ((user = Application.getInstance().getUserList().getValidatedUser(usernameInput, passwordInput)) != null) {
        Application.getInstance().getUserList().setCurrentUser(user);
        username.setVisible(false);
        password.setVisible(false);
        login.setVisible(false);
        register.setVisible(false);
        Application.getInstance().getServerSide().showMessage("Login successful.", false);
        username.hide();
        password.hide();
        login.hide();
        register.hide();
        updateUserStats();
        currentRank.show();
        currentRankTitle.show();
        currentScore.show();
        currentScoreTitle.show();
        loggedInUser.show();
        loggedInUserTitle.show();
        Application.getInstance().getServerSide().showWordPicker();
      } else {
        Application.getInstance().getServerSide().showMessage("Login failed. Please check your username and password.", true);
      }
    } else {
      Application.getInstance().getServerSide().showMessage("Please make sure to enter a password and username.", true);
    }
  }
  /**
   * Handles client login requests and returns an answer to the requesting client with the login status and user information.
   * @param input the data received from the client, expected to be formatted as "ValidateUserLogin;;;communicationID;;;username;;;password".
   */
  public void clientLogin(String input) {
    String[] messageParts = input.split(";;;");
    //maybe check for parse errors
    int communicationId = PApplet.parseInt(messageParts[1]);
    String username = messageParts[2];
    String password = messageParts[3];
    User user;
    if ((user = Application.getInstance().getUserList().getValidatedUser(username, password)) != null) {
      Application.getInstance().getServerSide().writeToClients("UserLoginResponse;;;"+communicationId+";;;"+user.getUsername()+";;;"+user.getScore()+";;;"+user.getTimeCreated());
    } else {
      Application.getInstance().getServerSide().writeToClients("UserLoginResponse;;;"+communicationId+";;;"+"LoginFailed;;;0;;;0");
    }
  }

  /**
   * Handles client registration requests and returns an answer to the requesting client with the registration status.
   * @param input the data received from the client, expected to be formatted as "CreateUser;;;communicationID;;;username;;;password;;;timestamp".
   */
  public void clientRegistration(String input) {
    String[] messageParts = input.split(";;;");
    int communicationId = PApplet.parseInt(messageParts[1]);
    String username = messageParts[2];
    String password = messageParts[3];
    long timestamp =  Long.parseLong(messageParts[4]);

    try {
      Application.getInstance().getUserList().addUser(username, password, timestamp);
      Application.getInstance().getServerSide().writeToClients("UserCreatedResponse;;;"+communicationId+";;;"+"UserCreatedSuccessfully");
    }
    //handle what happens when user already exists
    catch(DuplicateUserException due) {
      Application.getInstance().getServerSide().writeToClients("UserCreatedResponse;;;"+communicationId+";;;"+"UserCreationFailed");
    }
  }

  /**
   * Updates and displays user statistics on the UI.
   */
  public void updateUserStats() {
    super.drawOver();
    User user = Application.getInstance().getUserList().getCurrentUser();
    loggedInUser.setText(user.getUsername());
    currentScore.setText(""+user.getScore());
    currentRank.setText(""+Application.getInstance().getUserList().getCurrentUserScorePlacement(user));
  }

  /**
   * Answers requests for updated user statistics for specific users.
   * @param input the updated user statistics data, expected to be formatted as "RequestUserStats;;;username"
   */
  public void updateClientUserStats(String input) {
    String[] messageParts = input.split(";;;");
    String username = messageParts[1];
    User user = Application.getInstance().getUserList().getUserByUsername(username);
    if (user != null) {
      Application.getInstance().getServerSide().writeToClients("UserStats;;;"+username+";;;"+user.getScore()+";;;"
        +Application.getInstance().getUserList().getCurrentUserScorePlacement(user));
    }
  }
}
/**
 * Represents a message pane component, which extends {@link Component}.
 * This class handles the display of informational and critical messages to the user, including audio notifications.
 */
public class MessagePane extends Component {
  private Button close;
  private Textlabel txtLabel;
  private String messageText;
  private SoundFile positiveNotification;
  private SoundFile negativeNotification;
  private boolean soundEnabled;
  private Color criticalMessageBgColor;
  private Color criticalMessageBorderColor;
  private Color positiveMessageBgColor;
  private Color positiveMessageBorderColor;

  /**
   * Constructs a MessagePane with specified parameters and initializes its components.
   *
   * @param x the x-coordinate of the message pane
   * @param y the y-coordinate of the message pane
   * @param cWidth the width of the message pane
   * @param cHeight the height of the message pane
   * @param radius the corner radius of the message pane
   * @param pApplet the processing application instance, required for sound file loading
   * @param criticalMessageBgColor the background int for critical messages
   * @param criticalMessageBorderColor the border int for critical messages
   * @param positiveMessageBgColor the background int for non-critical messages
   * @param positiveMessageBorderColor the border int for non-critical messages
   * @param strokeWeight the stroke weight for the border of the pane
   */
  public MessagePane(int x, int y, int cWidth, int cHeight, int radius, PApplet pApplet, Color criticalMessageBgColor, Color criticalMessageBorderColor, Color positiveMessageBgColor, Color positiveMessageBorderColor, int strokeWeight) {
    super(x, y, cWidth, cHeight, radius, strokeWeight);
    messageText = "";
    initialiseComponents();
    positiveNotification = new SoundFile(pApplet, "data/positiveNotification.mp3");
    negativeNotification = new SoundFile(pApplet, "data/negativeNotification.mp3");
    soundEnabled = true;
    this.criticalMessageBgColor = criticalMessageBgColor;
    this.criticalMessageBorderColor = criticalMessageBorderColor;
    this.positiveMessageBgColor = positiveMessageBgColor;
    this.positiveMessageBorderColor = positiveMessageBorderColor;
  }

  /**
   * Initializes the UI components of the message pane, including a close button and a label for displaying messages.
   */
  public void initialiseComponents() {
    ControlP5 cp5 = Application.getInstance().getControlP5();
    stroke(0);
    close = cp5.addButton("messageBtnClose")
      .setLabel("x")
      .setPosition(super.getX() + super.getWidth() - 30, super.getY() + 15)
      .setSize(20, 20)
      .hide();
    txtLabel = cp5.addTextlabel("messageTextlabel")
      .setText(messageText)
      .setPosition(super.getX() + 20, super.getY() + 15)
      .setSize(super.getWidth() - 40, super.getHeight() - 30)
      .hide()
      .setColorValue(color(0, 0, 0));
  }

  /**
   * Hides the message pane and the close button, effectively clearing the message.
   */
  public void closeMessage() {
    close.hide();
    txtLabel.hide();
    super.drawOver(0);
  }

  /**
   * Displays a message on the message pane. 
   * Critical and non-critical messages are differentiated by the given colors, as well as the
   * optional audio messages, that can be disabled or enabled with the according setter method.
   *
   * @param messageText the text of the message to display
   * @param isCritical indicates whether the message is critical (true) or not (false)
   */
  public void showMessage(String messageText, boolean isCritical) {
    if (isCritical) {
      setBorderColor(criticalMessageBorderColor);
      setBgColor(criticalMessageBgColor);
      if (soundEnabled) {
        negativeNotification.play();
      }
    } else {
      setBorderColor(positiveMessageBorderColor);
      setBgColor(positiveMessageBgColor);
      if (soundEnabled) {
        positiveNotification.play();
      }
    }
    strokeWeight(3);
    super.drawComponent();
    this.messageText = messageText;
    txtLabel.setText(messageText);
    close.show();
    txtLabel.show();
  }

  /**
   * Enables or disables the sound notifications for messages.
   *
   * @param soundEnabled true to enable sound notifications, false to disable them
   */
  public void setSoundEnabled(boolean soundEnabled) {
    this.soundEnabled = soundEnabled;
  }
}
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
   * @param bgColor the background int of the scoreboard
   * @param borderColor the border int of the scoreboard
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
/**
 * A component for selecting stroke sizes visually, extends {@link Component}. Users can select different stroke sizes
 * by clicking on visually represented circles of varying sizes.
 */
public class StrokeSizePicker extends Component {
  private int strokeSize;
  /**
   * Constructs a StrokeSizePicker component with specified position, dimensions, and visual style.
   * Initializes the component with a default stroke size.
   *
   * @param x the x-coordinate of the picker.
   * @param y the y-coordinate of the picker.
   * @param cWidth the width of the picker.
   * @param cHeight the height of the picker.
   * @param bgColor the background int of the picker.
   * @param borderColor the border int of the picker.
   * @param radius the corner radius of the picker's border.
   */
  public StrokeSizePicker(int x, int y, int cWidth, int cHeight, Color bgColor, Color borderColor, int radius) {
    super(x, y, cWidth, cHeight, bgColor, borderColor, radius);
    drawComponent();
    strokeSize = 2;
  }
  /**
   * Draws the component on the screen, displaying circles that represent different stroke sizes.
   * Larger circles represent larger stroke sizes.
   */
  @Override
    public void drawComponent() {
    super.drawComponent();
    fill(0);
    noStroke();
    circle(super.getX()+20, super.getY()+20, 15);
    circle(super.getX()+40, super.getY()+20, 10);
    circle(super.getX()+55, super.getY()+20, 5);
  }
  /**
   * Updates the stroke size based on user interaction. If a circle is clicked, the stroke size
   * corresponding to that circle's diameter is selected.
   *
   * @return true if the stroke size was updated, false otherwise.
   */
  public boolean updateStrokeSize() {
    if (isClicked()) {
      float distanceToCircle1 = dist(mouseX, mouseY, super.getX() + 20, super.getY() + 20);
      float distanceToCircle2 = dist(mouseX, mouseY, super.getX() + 40, super.getY() + 20);
      float distanceToCircle3 = dist(mouseX, mouseY, super.getX() + 55, super.getY() + 20);

      // Check if the mouse is within the radius of any circle
      if (distanceToCircle1 <= 15) {
        strokeSize = 15;
        return true;
      } else if (distanceToCircle2 <= 10) {
        strokeSize = 10;
        return true;
      } else if (distanceToCircle3 <= 5) {
        strokeSize = 5;
        return true;
      }
    }
    return false;
  }
  /**
   * Returns the currently selected stroke size.
   *
   * @return the stroke size.
   */
  public int getStrokeSize() {
    return strokeSize;
  }
}/**
 * Represents a local user during system runtime.
 */

public class User {
  private String username;
  private String securePassword;
  private int score;
  private long timeCreated;
  /**
   * Constructs a new User object with initial settings for username, password, and account creation time.
   * Initializes the score to zero.
   *
   * @param username the username of the user.
   * @param securePassword the user's password, expected to be stored in a secure hashed format.
   * @param timeCreated the timestamp representing when the user account was created.
   */
  public User(String username, String securePassword, long timeCreated) {
    this.username = username;
    this.securePassword = securePassword;
    this.timeCreated = timeCreated;
    this.score = 0;
  }

  /**
   * Constructs a new User object with specified username, password, creation time, and score.
   * This constructor is used when loading existing user data.
   *
   * @param username the username of the user.
   * @param securePassword the user's password, expected to be stored in a secure hashed format.
   * @param timeCreated the timestamp representing when the user account was created.
   * @param score the initial score of the user, as retrieved from storage or another source.
   */
  public User(String username, String securePassword, long timeCreated, int score) {
    this.username = username;
    this.securePassword = securePassword;
    this.timeCreated = timeCreated;
    this.score = score;
  }
  /**
   * Returns the current score of the user.
   *
   * @return the score of the user.
   */
  public int getScore() {
    return score;
  }
  /**
   * Sets the score of the user.
   *
   * @param score the new score to be set for the user.
   */
  public void setScore(int score) {
    this.score = score;
  }
  /**
   * Returns the username of the user.
   *
   * @return the username of the user.
   */
  public String getUsername() {
    return username;
  }
  /**
   * Returns the securely stored password of the user.
   * Note: The password should be stored in a salted hash format to ensure security.
   *
   * @return the secure password of the user.
   */
  public String getSecurePassword() {
    return securePassword;
  }

  /**
   * Returns the timestamp indicating when the user's account was created in ms.
   *
   * @return the creation time of the user account, represented as a long integer timestamp in ms.
   */
  public long getTimeCreated() {
    return timeCreated;
  }
}
/**
 * Manages a list of users including operations to add, retrieve, and update users in the system
 * and their corresponding data in the database.
 */
public class UserList {
  private ArrayList<User> users;
  private SQLite db;
  private User currentUser;

  /**
   * Initializes a new UserList and loads existing users from the database.
   *
   * @param dataSource the database source path.
   */
  public UserList(String dataSource) {
    users = new ArrayList();
    db = new SQLite(Application.getInstance().getServerSide(), dataSource);
    loadUsersFromDB();
  }


  /**
   * Loads user data from the database and populates the users list.
   */
  private void loadUsersFromDB() {
    try {
      if ( db.connect() ) {
        db.query( "SELECT * FROM users" );
        while (db.next()) {
          String username = db.getString("username");
          String securePassword = db.getString("password");
          long timeCreated = db.getLong("userCreatedTimestamp");
          int score = db.getInt("score");
          users.add(new User(username, securePassword, timeCreated, score));
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Adds a user to the system and database. The cleartext password is salted with the timestamp
   * and hashed before being stored in the user object and the database.
   * Throws a DuplicateUserExecption if the user already exists.
   *
   * @param username the user's username.
   * @param password the user's password.
   * @param timestamp the creation timestamp for the user.
   * @throws DuplicateUserException if the username already exists.
   */
  private void addUser (String username, String password, long timestamp) throws DuplicateUserException {
    String saltedPassword = password + timestamp;
    String saltedHashedPassword;
    User user = null;

    try {
      MessageDigest digest = MessageDigest.getInstance("SHA3-256");
      byte[] hashbytes = digest.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));
      saltedHashedPassword = bytesToHex(hashbytes);
      user = new User(username, saltedHashedPassword, timestamp);
    }
    catch(NoSuchAlgorithmException nsae) {
      nsae.printStackTrace();
    }
    if (user!=null) {
      if (isUserRegistered(user.getUsername())) {
        throw new DuplicateUserException("User '" + user.getUsername() + "' already exists.");
      } else {
        users.add(user);
        try {
          if ( db.connect() ) {
            db.query("INSERT INTO users (username, password, userCreatedTimestamp, score) " +
              "VALUES ('" + user.getUsername() + "', '" + user.getSecurePassword() + "', '" + user.getTimeCreated() + "', 0)");
          }
        }
        //this is necessary to catch an sqlexception. sqlexception is not thrown by sqlite lib, so general exception needs to be caught.
        //problems could be write errors, db connection failure, etc.
        catch(Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Retrieves an array of users sorted by highest scores.
   *
   * @param amount the number of top users to retrieve.
   * @return an array of top-scoring users.
   */
  public User[] getUsersWithHighestScores(int amount) {
    // Sort users by score in descending order
    Collections.sort(users, Comparator.comparing(User::getScore).reversed());

    //make sure not to try and return more users than the users list contains
    int returnSize = Math.min(amount, users.size());
    User[] highestScoringUsers = new User[returnSize];
    for (int i=0; i<returnSize; i++) {
      highestScoringUsers[i] = users.get(i);
    }
    return highestScoringUsers;
  }

  /**
   * Returns the ranking position of a user based on their score.
   *
   * @param user the user whose ranking is to be determined.
   * @return the ranking position as an integer. Returns -1 if the user was not found.
   */
  public int getCurrentUserScorePlacement(User user) {
    // Sort users by score in descending order
    Collections.sort(users, Comparator.comparing(User::getScore).reversed());

    // Find the index of the specified user in the sorted list
    for (int i = 0; i < users.size(); i++) {
      if (users.get(i).equals(user)) {
        return i + 1; // Return rank (1-based index)
      }
    }

    return -1; // User not found in the list, return -1
  }

  /**
   * Awards points to a user and updates their score in the database.
   *
   * @param user the user to award points to.
   * @param points the number of points to award.
   */
  public void awardPointsToUser(User user, int points) {
    user.setScore(user.getScore() + points);
    updateUserinDB(user);
  }

  /**
   * Checks if a username is already registered in the system.
   *
   * @param username the username to check.
   * @return true if the username is already registered, false otherwise.
   */
  public boolean isUserRegistered(String username) {
    for (User user : users) {
      if (user.getUsername().equals(username)) {
        return true; // User found
      }
    }
    return false; // User not found
  }

  /**
   * Retrieves a user by their username.
   *
   * @param username the username of the user to retrieve.
   * @return the User object if found, null otherwise.
   */
  public User getUserByUsername(String username) {
    for (User user : users) {
      if (user.getUsername().equals(username)) {
        return user; // User found
      }
    }
    return null; // User not found
  }

  /**
   * Validates a user's login credentials.
   *
   * @param username the username to validate.
   * @param password the cleartext password to validate.
   * @return the User object if credentials are valid, null otherwise.
   */
  public User getValidatedUser(String username, String password) {
    User user;
    //if user is found
    if ((user = getUserByUsername(username)) != null) {
      String saltedPassword = password + user.getTimeCreated();
      String saltedHashedPassword;
      try {
        MessageDigest digest = MessageDigest.getInstance("SHA3-256");
        byte[] hashbytes = digest.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));
        saltedHashedPassword = bytesToHex(hashbytes);
        //if password is the same as in found user object
        if (user.getSecurePassword().equals(saltedHashedPassword)) {
          return user;
        } else {
          return null;
        }
      }//if program runs into problem, user credential is deemed invalid and null is returned
      catch(NoSuchAlgorithmException nsae) {
        nsae.printStackTrace();
        return null;
      }
    } else {
      return null;
    }
  }

  /**
   * Converts a byte array to a hexadecimal string.
   * Source:  https://www.baeldung.com/sha-256-hashing-java
   * @param hash the byte array to convert.
   * @return the corresponding hexadecimal string.
   */
  private String bytesToHex(byte[] hash) {
    StringBuilder hexString = new StringBuilder(2 * hash.length);
    for (int i = 0; i < hash.length; i++) {
      String hex = Integer.toHexString(0xff & hash[i]);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }

  /**
   * Sets the current user of the application.
   *
   * @param user the user to set as current.
   */
  public void setCurrentUser(User user) {
    currentUser = user;
  }

  /**
   * Returns the current user of the application.
   *
   * @return the current user.
   */
  public User getCurrentUser() {
    return currentUser;
  }

  /**
   * Updates user information in the database, specifically the user's password and score.
   * 
   * @param user the user whose information is to be updated.
   */
  private void updateUserinDB(User user) {
    try {
      if (db.connect() ) {
        db.query("UPDATE users SET password = '"+user.getSecurePassword()+"', score = "+user.getScore()+" WHERE username = '"+user.getUsername()+"';");
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}

/**
 * A component that manages and displays a list of connected users, extends {@link Component}.
 * Manages the connection status of both server and client users by listening to heartbeats
 * and aging each user until they expire and are deleted.
 */
public class UserPane extends Component {
  private HashMap<String, Integer> users;
  private int maxHeartbeatAge;
  private ArrayList<Textfield> userLabels;
  private Textlabel title;


  /**
   * Constructs a UserPane component with specified location, size, and visual attributes.
   *
   * @param x the x-coordinate of the user pane.
   * @param y the y-coordinate of the user pane.
   * @param cWidth the width of the user pane.
   * @param cHeight the height of the user pane.
   * @param bgColor the background int of the user pane.
   * @param borderColor the border int of the user pane.
   * @param radius the radius of the corners of the user pane.
   */
  public UserPane(int x, int y, int cWidth, int cHeight, Color bgColor, Color borderColor, int radius) {
    super(x, y, cWidth, cHeight, bgColor, borderColor, radius);
    this.maxHeartbeatAge = 100;
    users = new HashMap<>();
    userLabels = new ArrayList<>();
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
    ControlP5 cp5 = Application.getInstance().getControlP5();
    if (users.isEmpty()) {
      title.setText("No connected users.");
    } else {
      title.setText("Currently connected users:");
    }

    // Remove existing user labels
    userLabels.forEach(Textfield::remove);
    userLabels.clear();
    super.drawOver();

    //Age user
    for (String key : users.keySet()) {
      users.put(key, users.get(key) + 1);
    }

    // Use an iterator to safely remove old users
    Iterator<Map.Entry<String, Integer>> iterator = users.entrySet().iterator();
    int moveRight = 0;
    int labelWidth = 60;

    //String that is sent to the clients to update their userlist
    String userListForClients = "";

    while (iterator.hasNext()) {
      Map.Entry<String, Integer> entry = iterator.next();
      String key = entry.getKey();
      Integer value = entry.getValue();

      if (value >= maxHeartbeatAge) {
        iterator.remove();
      } else {
        String userDisplayName = key.length() > 10 ? key.substring(0, 10) : key;

        userListForClients += ";;;"+key;

        if (super.getX() + moveRight + labelWidth < width) {
          Textfield currentLabel = cp5.addTextfield(key)
            .setText(userDisplayName)
            .setLabel("")
            .setPosition(super.getX()+25 + moveRight, super.getY()+40)
            .setSize(labelWidth, 20)
            .setColor(255)
            .lock();
          ;
          userLabels.add(currentLabel);
          moveRight += labelWidth+5;
        }
      }
    }

    //send current userlist to client:
    Application.getInstance().getServerSide().writeToClients("ConnectedUsers"+userListForClients);
  }
}
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
   * @param bgColor the background int of the component.
   * @param borderColor the border int of the component.
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
}

