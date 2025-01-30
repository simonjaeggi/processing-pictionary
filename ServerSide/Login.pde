/**
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
   * Constructs a new Login component with specified location, size, and color attributes.
   * @param x the x-coordinate of the component.
   * @param y the y-coordinate of the component.
   * @param cWidth the width of the component.
   * @param cHeight the height of the component.
   * @param bgColor the background color of the component.
   * @param borderColor the border color of the component.
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
    int communicationId = int(messageParts[1]);
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
    int communicationId = int(messageParts[1]);
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
