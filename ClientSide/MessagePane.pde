import processing.sound.*;

/**
 * Represents a message pane component, which extends {@link Component}.
 * This class handles the display of informational and critical messages to the user, including audio notifications.
 */
public class MessagePane extends Component {
  private Button close;
  private Textlabel txtLabel;
  private String messageText;
  private SoundFile positiveNotification;
  private SoundFile negativeNotification;
  private boolean soundEnabled;
  private Color criticalMessageBgColor;
  private Color criticalMessageBorderColor;
  private Color positiveMessageBgColor;
  private Color positiveMessageBorderColor;

  /**
   * Constructs a MessagePane with specified parameters and initializes its components.
   *
   * @param x the x-coordinate of the message pane
   * @param y the y-coordinate of the message pane
   * @param cWidth the width of the message pane
   * @param cHeight the height of the message pane
   * @param radius the corner radius of the message pane
   * @param pApplet the processing application instance, required for sound file loading
   * @param criticalMessageBgColor the background color for critical messages
   * @param criticalMessageBorderColor the border color for critical messages
   * @param positiveMessageBgColor the background color for non-critical messages
   * @param positiveMessageBorderColor the border color for non-critical messages
   * @param strokeWeight the stroke weight for the border of the pane
   */
  public MessagePane(int x, int y, int cWidth, int cHeight, int radius, PApplet pApplet, Color criticalMessageBgColor, Color criticalMessageBorderColor, Color positiveMessageBgColor, Color positiveMessageBorderColor, int strokeWeight) {
    super(x, y, cWidth, cHeight, radius, strokeWeight);
    messageText = "";
    initialiseComponents();
    positiveNotification = new SoundFile(pApplet, "data/positiveNotification.mp3");
    negativeNotification = new SoundFile(pApplet, "data/negativeNotification.mp3");
    soundEnabled = true;
    this.criticalMessageBgColor = criticalMessageBgColor;
    this.criticalMessageBorderColor = criticalMessageBorderColor;
    this.positiveMessageBgColor = positiveMessageBgColor;
    this.positiveMessageBorderColor = positiveMessageBorderColor;
  }

  /**
   * Initializes the UI components of the message pane, including a close button and a label for displaying messages.
   */
  public void initialiseComponents() {
    ControlP5 cp5 = Application.getInstance().getControlP5();
    stroke(0);
    close = cp5.addButton("messageBtnClose")
      .setLabel("x")
      .setPosition(super.getX() + super.getWidth() - 30, super.getY() + 15)
      .setSize(20, 20)
      .hide();
    txtLabel = cp5.addTextlabel("messageTextlabel")
      .setText(messageText)
      .setPosition(super.getX() + 20, super.getY() + 15)
      .setSize(super.getWidth() - 40, super.getHeight() - 30)
      .hide()
      .setColorValue(color(0, 0, 0));
  }

  /**
   * Hides the message pane and the close button, effectively clearing the message.
   */
  public void closeMessage() {
    close.hide();
    txtLabel.hide();
    super.drawOver(0);
  }

  /**
   * Displays a message on the message pane. 
   * Critical and non-critical messages are differentiated by the given colors, as well as the
   * optional audio messages, that can be disabled or enabled with the according setter method.
   *
   * @param messageText the text of the message to display
   * @param isCritical indicates whether the message is critical (true) or not (false)
   */
  public void showMessage(String messageText, boolean isCritical) {
    if (isCritical) {
      setBorderColor(criticalMessageBorderColor);
      setBgColor(criticalMessageBgColor);
      if (soundEnabled) {
        negativeNotification.play();
      }
    } else {
      setBorderColor(positiveMessageBorderColor);
      setBgColor(positiveMessageBgColor);
      if (soundEnabled) {
        positiveNotification.play();
      }
    }
    strokeWeight(3);
    super.drawComponent();
    this.messageText = messageText;
    txtLabel.setText(messageText);
    close.show();
    txtLabel.show();
  }

  /**
   * Enables or disables the sound notifications for messages.
   *
   * @param soundEnabled true to enable sound notifications, false to disable them
   */
  public void setSoundEnabled(boolean soundEnabled) {
    this.soundEnabled = soundEnabled;
  }
}
