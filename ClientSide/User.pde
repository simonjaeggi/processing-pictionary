/**
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
