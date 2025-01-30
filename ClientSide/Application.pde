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
}
