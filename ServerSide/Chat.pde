import controlP5.Textarea;

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
   * @param bgColor the background color of the chat component.
   * @param borderColor the border color of the chat component.
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
        .setColorBackground(color(255))  // Set background color to white
        .setColorForeground(color(255))  // Set foreground color to white
        .setColor(color(0))              // Set text color to black
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
    //println(input);  // Debug output to console
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
