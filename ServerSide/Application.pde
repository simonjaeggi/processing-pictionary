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
}
