import java.awt.Color;

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
      int communicationIDInput = int(messageParts[1]);
      String usernameInput = messageParts[2];
      int scoreInput = int(messageParts[3]);
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
    int score = int(messageParts[2]);
    int rank = int(messageParts[3]);

    if (Application.getInstance().getSession().getCurrentUser().getUsername().equals(username)) {
      super.drawOver();
      loggedInUser.setText(username);
      currentScore.setText(""+score);
      currentRank.setText(""+rank);
    }
  }
}
