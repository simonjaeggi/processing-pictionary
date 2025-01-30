/**
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
