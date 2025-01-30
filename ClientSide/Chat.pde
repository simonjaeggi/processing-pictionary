/**
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
   * @param bgColor the background color of the chat component.
   * @param borderColor the border color of the chat component.
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
