import processing.net.*; //<>//
import controlP5.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.awt.Color;
import java.awt.Color;
import processing.sound.*;
import java.awt.Color;
import controlP5.ControlP5;
import controlP5.Slider;
import controlP5.Textlabel;
import java.awt.Color;
import controlP5.ControlP5;
import controlP5.Textfield;
import controlP5.Textlabel;
import java.util.ArrayList;

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

/** Processing Sketch ClientSide */
public class ClientSide {

private Login login;
private Application appInstance;
private MessagePane messagePane;
private Canvas canvas;
private Chat chat;
private UserPane userPane;
private ScoreBoard scoreBoard;
private boolean gameOver;
private final String SERVERADDRESS = "127.0.0.1";
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

    // Iterate over all matches and print the captured group
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
/**
 * Singleton Static class that holds all the objects that need to be accessible from everywhere.
 * This class ensures that there is only one instance of each component within the application.
 */
public static class Application {
  private static Application instance;
  private ControlP5 cp5;
  private Client c;
  private Session session;
  private ClientSide clientSide;

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
    return instance;
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
  * Sets the Client instance for the application.
  * @param c the Client instance to be used by the application.
  */
  public void setClient(Client c) {
    this.c = c;
  }
  
  /**
  * Retrieves the current Client instance used by the application.
  * @return the Client instance currently set in the application.
  */
  public Client getClient() {
    return c;
  }
  
  /**
  * Sets the Session instance for the application.
  * @param session the Session instance to be used by the application.
  */
  public void setSession(Session session) {
    this.session = session;
  }
  
  /**
  * Retrieves the current Session instance used by the application.
  * @return the Session instance currently set in the application.
  */
  public Session getSession() {
    return session;
  }
  
  /**
  * Retrieves the current ClientSide instance used by the application.
  * @return the ClientSide instance currently set in the application.
  */
  public ClientSide getClientSide(){
    return clientSide;
  }
  
  /**
  * Sets the ClientSide instance for the application.
  * @param clientSide the ClientSide instance to be used by the application.
  */
  public void setClientSide(ClientSide clientSide){
    this.clientSide = clientSide;
  }
}/**
 * Extends {@link Component} to represent a drawable canvas area where the drawing from the server is mirrored.
 * Provides functionalities to update the drawing and clear the canvas.
 * Handles incoming server messages that contain drawing information.
 */
public class Canvas extends Component {
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
    }

    /**
     * Updates the canvas drawing based on user input. It processes the input string to extract
     * drawing commands and coordinates, then performs the drawing operation on the canvas.
     * 
     * @param input the input string containing the drawing commands and parameters formatted as "Draw;;;mouseX;;;mouseY;;;pMouseX;;;pMouseY;;;int ;;;strokeSize".
     */
    public void update(String input) {
        String[] messageParts = input.split(";;;");
        int mouseXRec = Integer.parseInt(messageParts[1]) + super.getX();
        int mouseYRec = Integer.parseInt(messageParts[2]) + super.getY();
        int pMouseXRec = Integer.parseInt(messageParts[3]) + super.getX();
        int pMouseYRec = Integer.parseInt(messageParts[4]) + super.getY();
        int colorRec = Integer.parseInt(messageParts[5]);
        int strokeSizeRec = Integer.parseInt(messageParts[6]);

        stroke(colorRec);
        strokeWeight(strokeSizeRec);
        line(mouseXRec, mouseYRec, pMouseXRec, pMouseYRec);
    }

    /**
     * Clears the canvas by re-drawing the initial state of the component.
     */
    public void clear() {
        drawComponent();
    }
}/**
 * Represents a chat component in a graphical user interface, extending {@link Component}.
 * This class creates and manages a chat interface.
 * It handles user interactions for sending messages and updating chat history, as well as 
 * sending and receiving chat messages to and from the server.
 */
public class Chat extends Component {
  private Button send;
  private Textfield messageTextfield;
  private Textarea chatHistory;
  private boolean isChatAllowed;

  /**
   * Constructs a Chat component with specified location, dimensions, colors, and border styling.
   * It initializes the chat component's UI elements.
   *
   * @param x the x-coordinate of the chat component.
   * @param y the y-coordinate of the chat component.
   * @param cWidth the width of the chat component.
   * @param cHeight the height of the chat component.
   * @param bgColor the background int of the chat component.
   * @param borderColor the border int of the chat component.
   * @param radius the radius of the border corners.
   */
  public Chat(int x, int y, int cWidth, int cHeight, Color bgColor, Color borderColor, int radius) {
    super(x, y, cWidth, cHeight, bgColor, borderColor, radius);
    drawComponent();
  }

  /**
   * Initializes the UI components and draw the Chat.
   */
  @Override
  public void drawComponent() {
    super.drawComponent();
    ControlP5 cp5 = Application.getInstance().getControlP5();
    messageTextfield = cp5.addTextfield("chatMessageInput")
      .setPosition(super.getX()+20, super.getY()+super.getHeight()-50)
      .setSize(180, 30)
      .setLabel("")
      .setLock(true)
      .setText("Please login to participate");

    send = cp5.addButton("chatBtnSend")
      .setLabel("Send")
      .setPosition(super.getX()+super.getWidth()-80, super.getY()+super.getHeight()-50)
      .setSize(50, 30)
      .setLock(true);
    chatHistory = cp5.addTextarea("chatHistory")
      .setPosition(super.getX()+20, super.getY()+20)
      .setSize(super.getWidth()-40, super.getHeight()-100)
      .setLineHeight(14)
      .setColorBackground(color(255))
      .setColorForeground(color(255))
      .setColor(color(0))
      .setFont(createFont("Arial", 12));
  }

  /**
   * Sends the current message from the message input field to the server if the field is not empty.
   */
  public void send() {
    if (!messageTextfield.getText().equals("")) {
      String username = Application.getInstance().getSession().getCurrentUser().getUsername();
      Application.getInstance().getClientSide().writeToServer("Chat;;;"+username+";;;"+messageTextfield.getText());
      messageTextfield.setText("");
    }
  }

  /**
   * Updates the chat history area with a new message received from the server.
   * 
   * @param input the input string containing the message type, sender username, and message text.
   */
  public void update(String input) {
    String[] messageParts = input.split(";;;");
    String messageType = messageParts[0]; // Will be "Chat"
    String username = messageParts[1];
    String message = messageParts[2];
    addMessageToChat(username + ": " + message);
  }

  /**
   * Allows or disallows sending messages in the chat by disabling/enabling the UI components.
   * 
   * @param allowance true to enable chat functionalities, false to disable them.
   */
  public void allowChat(boolean allowance) {
    messageTextfield.setText(allowance ? "" : "Please login to participate");
    messageTextfield.setLock(!allowance);
    send.setLock(!allowance);
    isChatAllowed = allowance;
  }

  /**
   * Checks if chatting is currently allowed.
   * 
   * @return true if chat is allowed, otherwise false.
   */
  public boolean isChatAllowed() {
    return isChatAllowed;
  }

  /**
   * Adds a new message to the chat history.
   * 
   * @param message the message to be added to the chat history.
   */
  private void addMessageToChat(String message) {
    chatHistory.setText(chatHistory.getStringValue() + message + "\n");
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
 * Represents a login component in a graphical user interface, which extends {@link Component}.
 * This class allows creating and managing a login interface.
 * It also handles the outgoing login and registration messages shared with and received from
 * the server.
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
   * Constructs a Login component with specified location and dimensions.
   * Initializes the login interface UI elements.
   *
   * @param x the x-coordinate of the login component.
   * @param y the y-coordinate of the login component.
   * @param cWidth the width of the login component
   * @param cHeight the height of the login component
   */
  public Login(int x, int y, int cWidth, int cHeight) {
    super(x, y, cWidth, cHeight);
    drawComponent();
  }

  /**
   * Initializes and lays out the login component's UI elements including text fields for username and password,
   * login and register buttons, and a status label.
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
   * Handles user registration by sending credentials to the server if the username and password fields are not empty.
   */
  public void register() {
    if (!username.getText().equals("") && !password.getText().equals("")) {
      int communicationID = Application.getInstance().getSession().getCommunicationID();
      long timestamp = System.currentTimeMillis();

      Application.getInstance().getClientSide().writeToServer("CreateUser;;;"+communicationID+";;;"+username.getText()
        +";;;"+ password.getText()+";;;"+ timestamp);
    } else {
      Application.getInstance().getClientSide().showMessage("Please make sure to enter a password and username.", true);
    }
  }

  /**
   * Processes the server's response to a registration request.
   *
   * @param input the response from the server containing the registration status
   */
  public void registrationVerification(String input) {
    try {
      int communicationID = Application.getInstance().getSession().getCommunicationID();
      String[] messageParts = input.split(";;;");
      int communicationIDInput = Integer.parseInt(messageParts[1]);
      String userCreatedStatus = messageParts[2];

      if (communicationIDInput == communicationID) {
        if (userCreatedStatus.equals("UserCreationFailed")) {
          Application.getInstance().getClientSide().showMessage("User registration failed, please try again using a different username!", true);
        } else if (userCreatedStatus.equals("UserCreatedSuccessfully")) {
          Application.getInstance().getClientSide().showMessage("Registration successful. Please login.", false);
        }
      }
    }
    catch(NumberFormatException nfe) {
      // Log error or handle exception
    }
  }

  /**
   * Sends login credentials to the server for verification.
   */
  private void login() {
    if (!username.getText().equals("") && !password.getText().equals("")) {
      int communicationID = Application.getInstance().getSession().getCommunicationID();
      Application.getInstance().getClientSide().writeToServer("ValidateUserLogin;;;"+communicationID+";;;"+username.getText() +";;;"+ password.getText());
    } else {
      Application.getInstance().getClientSide().showMessage("Please make sure to enter a password and username.", true);
    }
  }
  //method that receives login verification.
  private void loginVerification(String input) {
    try {
      int communicationID = Application.getInstance().getSession().getCommunicationID();
      String[] messageParts = input.split(";;;");
      int communicationIDInput = PApplet.parseInt(messageParts[1]);
      String usernameInput = messageParts[2];
      int scoreInput = PApplet.parseInt(messageParts[3]);
      long timeCreatedInput = Long.parseLong(messageParts[4]);

      //check if correct conversation between client and user is being listened to
      if (communicationIDInput == communicationID) {
        if (usernameInput.equals("LoginFailed")) {
          Application.getInstance().getClientSide().showMessage("Username or password wrong, try again or register first.", true);
        } else {
          Application.getInstance().getClientSide().showMessage("Login successful.", false);
          User currentUser = new User(usernameInput, timeCreatedInput, scoreInput);
          Application.getInstance().getSession().setCurrentUser(currentUser);
          username.hide();
          password.hide();
          login.hide();
          register.hide();
          loggedInUser.setText(currentUser.getUsername());
          super.drawOver();
          loggedInUser.show();
          loggedInUserTitle.show();
          currentScore.show();
          currentScoreTitle.show();
          currentRank.show();
          currentRankTitle.show();
          Application.getInstance().getClientSide().allowChatInteraction(true);
          Application.getInstance().getClientSide().writeToServer("RequestUserStats;;;"+currentUser.getUsername());
        }
      }
    }
    catch(NumberFormatException nfe) {
      //user not created because of problem with parsing parameters to numbers.
    }
  }

  /**
   * Updates the user statistics displayed in the UI, such as score and rank.
   *
   * @param input A formatted string containing the user's statistics, expected to be formatted as "UserStats;;;username;;;score;;;rank"
   */
  public void updateUserStats(String input) {
    String[] messageParts = input.split(";;;");
    String username = messageParts[1];
    int score = PApplet.parseInt(messageParts[2]);
    int rank = PApplet.parseInt(messageParts[3]);

    if (Application.getInstance().getSession().getCurrentUser().getUsername().equals(username)) {
      super.drawOver();
      loggedInUser.setText(username);
      currentScore.setText(""+score);
      currentRank.setText(""+rank);
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
   * @param bgColor the background int of the scoreboard
   * @param borderColor the border int of the scoreboard
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
/**
 * Manages user session details including the current user's information and a unique communication ID.
 * This class is responsible for handling the user's login status and session-specific identifiers that
 * facilitate communication identification among other clients with a server.
 */
public class Session {
  private User currentUser;
  private int communicationID;

  /**
   * Constructs a Session object with a randomly generated communication ID.
   * The communication ID is used to uniquely identify the session in network communications.
   */
  public Session() {
    communicationID = (int) random(1000000, 10000000);
  }

  /**
   * Sets the current user of the session.
   * This method updates the session's user information.
   * 
   * @param user the User object representing the logged-in user.
   */
  public void setCurrentUser(User user) {
    currentUser = user;
  }

  /**
   * Returns the currently logged-in user.
   * This method retrieves the user information stored in the session.
   * 
   * @return the current user as a User object. If no user is logged in, this will return null.
   */
  public User getCurrentUser() {
    return currentUser;
  }

  /**
   * Retrieves the communication ID of the session.
   * The communication ID is used to identify the session uniquely across different network messages.
   * 
   * @return the communication ID as an integer.
   */
  public int getCommunicationID(){
    return communicationID;
  }
}/**
 * Represents a local user during system runtime.
 */
public class User {
  private String username;
  private int score;
  private long timeCreated;

  /**
   * Constructs a User object with specified username, creation time, and score.
   *
   * @param username the username of the user.
   * @param timeCreated the timestamp (in milliseconds) when the user was created.
   * @param score the initial score of the user, which can be used in applications where user scoring is relevant.
   */
  public User(String username, long timeCreated, int score) {
    this.username = username;
    this.timeCreated = timeCreated;
    this.score = score;
  }

  /**
   * Returns the score of the user.
   *
   * @return the current score of the user.
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
   * Returns the time the user was created.
   *
   * @return the timestamp (in milliseconds) representing when the user was created.
   */
  public long getTimeCreated() {
    return timeCreated;
  }
}
/**
 * Represents a user pane component, which extends {@link Component}.
 * Displays a list of currently connected users, based on the information provided by the server.
 * 
 */
public class UserPane extends Component {
  private ArrayList<Textfield> userLabels;
  private Textlabel title;

  /**
   * Constructs a UserPane with specified parameters and initializes its components.
   *
   * @param x the x-coordinate of the user pane
   * @param y the y-coordinate of the user pane
   * @param cWidth the width of the user pane
   * @param cHeight the height of the user pane
   * @param bgColor the background int of the user pane
   * @param borderColor the border int of the user pane
   * @param radius the corner radius of the user pane
   */
  public UserPane(int x, int y, int cWidth, int cHeight, Color bgColor, Color borderColor, int radius) {
    super(x, y, cWidth, cHeight, bgColor, borderColor, radius);
    userLabels = new ArrayList<>();
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
    ControlP5 cp5 = Application.getInstance().getControlP5();
    String[] messageParts = input.split(";;;");

    // Remove existing user labels
    userLabels.forEach(Textfield::remove);
    userLabels.clear();
    super.drawOver();

    if (messageParts.length < 2) {
      title.setText("No connected users.");
    } else {
      title.setText("Currently connected users:");

      int labelWidth = 60;
      int moveRight = 0;
      for (int i = 1; i < messageParts.length; i++) {
        String userDisplayName = messageParts[i].length() > 10 ? messageParts[i].substring(0, 10) : messageParts[i];

        if (super.getX() + moveRight + labelWidth < width) {
          Textfield currentLabel = cp5.addTextfield(messageParts[i])
            .setText(userDisplayName)
            .setLabel("")
            .setPosition(super.getX() + 25 + moveRight, super.getY() + 40)
            .setSize(labelWidth, 20)
            .setColor(255)
            .lock();

          userLabels.add(currentLabel);
          moveRight += labelWidth + 5;
        }
      }
    }
  }
}

}

