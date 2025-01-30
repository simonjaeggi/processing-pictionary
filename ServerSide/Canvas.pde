/**
 * Extends {@link Component} to represent a drawable canvas area.
 * Handles a WordPicker, ColorPicker and StrokeSizePicker for the drawing specifics.
 * Sends drawing updates to clients.
 */

public class Canvas extends Component {
  private ColorPicker cp;
  private StrokeSizePicker sp;
  private WordPicker wp;
  private boolean allowDrawing;
  private Textfield timer;
  /**
   * Constructs a new Canvas object with specified dimensions, colors, and border styling.
   * It initializes the component with a background and border color, dimensions, and border radius,
   * and draws the initial state of the component.
   *
   * @param x the x-coordinate of the canvas.
   * @param y the y-coordinate of the canvas.
   * @param cWidth the width of the canvas.
   * @param cHeight the height of the canvas.
   * @param bgColor the background color of the canvas.
   * @param borderColor the color of the canvas border.
   * @param radius the radius of the rounded corners of the border.
   */

  public Canvas(int x, int y, int cWidth, int cHeight, Color bgColor, Color borderColor, int radius) {
    super(x, y, cWidth, cHeight, bgColor, borderColor, radius);
    drawComponent();
    Color customBlue =new Color(174, 198, 207);

    wp = new WordPicker(super.getX()+super.getWidth()-315, super.getY()+15, 300, 60, customBlue, customBlue, 0);
  }

  /**
   * Draws and initializes the canvas.
   * Overrides the {@code drawComponent()} method from the superclass.
   * Sets up a ColorPicker and a StrokeSizePicker along with a timer textfield for displaying time.
   */
  @Override
    public void drawComponent() {
    super.drawComponent();
    ControlP5 cp5 = Application.getInstance().getControlP5();
    

    cp = new ColorPicker(super.getX()+15, super.getY()+10, 40, 330);
    sp = new StrokeSizePicker(super.getX()+50, super.getY()+10, 100, 40, Color.WHITE, Color.WHITE, 10);

    if (timer == null) {
      timer = cp5.addTextfield("canvasTimer")
        .setText("0")
        .setLabel("Timer")
        .setPosition(super.getX()+super.getWidth()-40, super.getY()+100)
        .setColor(0)
        .setSize(20, 20)
        .setColorBackground(color(255))  // Set the background to white
        .setColorForeground(color(255))  // set border color when not focused
        .setColorActive(color(255))  // set border color when focused
        .setColorValue(color(0))  // Set the text color to black
        .lock();  // Lock the TextField to prevent user editing

      timer.getCaptionLabel()
        .setColor(0)
        .align(ControlP5.RIGHT, ControlP5.TOP_OUTSIDE)
        .setPaddingX(0)
        .setPaddingY(0);
    }
  }

  /**
   * Updates the state and behavior of the drawing canvas based on user interactions and current settings.
   * This method checks whether the user is authorized and if drawing is allowed. It handles mouse interactions
   * for drawing lines with specific stroke sizes and colors. It also manages network communication to broadcast
   * drawing data to other clients and provides user feedback when drawing is not allowed.
   */
  public void update() {
    //println(mouseX, mouseY, pmouseX, pmouseY);
    
    if (Application.getInstance().getUserList().getCurrentUser() != null) {
      if (isClicked()&&allowDrawing) {
        cp.updatePosition();
        sp.updateStrokeSize();
        int strokeColor = cp.getColor();
        int strokeSize = sp.getStrokeSize();

        //only send new coordinates to client if the mouse moves and is not interacting with the controling elements.
        if (!cp.isClicked() && !sp.isClicked() && (pmouseX != mouseX || pmouseY != mouseY)) {
          //we send the absolute coordinates with 0,0 at the top left of the canvas
          Application.getInstance().getServerSide().writeToClients("Draw;;;" + (pmouseX-super.getX()) + ";;;" + (pmouseY-super.getY()) +
            ";;;" + (mouseX-super.getX()) + ";;;" + (mouseY-super.getY()) + ";;;" + strokeColor + ";;;" + strokeSize);
          stroke(strokeColor);
          strokeWeight(strokeSize);
          line(mouseX, mouseY, pmouseX, pmouseY);
        }
      } else if (isClicked()&&!allowDrawing&&!wp.isClicked()) {
        Application.getInstance().getServerSide().showMessage("Make sure to start the game before drawing.", true);
      }
    } else if (Application.getInstance().getUserList().getCurrentUser() == null && isClicked()) {
      Application.getInstance().getServerSide().showMessage("You need to login before you can draw!", true);
    }
  }


  /**
   * Retrieves the WordPicker instance associated with the canvas.
   * The {@code WordPicker} is responsible for enabling the selection of words within the application.
   *
   * @return the WordPicker instance.
   */
  public WordPicker getWordPicker() {
    return wp;
  }

  /**
   * Clears the canvas by re-drawing the initial state of the component.
   */
  public void clear() {
    drawComponent();
  }

  /**
   * Enables or disables the drawing capability based on the given boolean flag.
   * When drawing is disabled, any interactions that would typically result in drawing
   * are ignored, as handeled in update().
   *
   * @param allowance true to allow drawing, false to prevent it.
   */
  public void allowDrawing(boolean allowance) {
    allowDrawing = allowance;
  }

  /**
   * Sets the countdown timer's remaining time. This method updates the visual display of the timer
   * within the component to reflect the number of seconds passed since the start of the game.
   *
   * @param seconds the number of seconds to be set on the timer display.
   */
  public void setTimerSeconds(long seconds) {
    timer.setText("" + seconds);
  }
}
