import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;

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
