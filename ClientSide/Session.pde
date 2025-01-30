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
}
