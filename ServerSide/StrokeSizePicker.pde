
/**
 * A component for selecting stroke sizes visually, extends {@link Component}. Users can select different stroke sizes
 * by clicking on visually represented circles of varying sizes.
 */
public class StrokeSizePicker extends Component {
  private int strokeSize;
  /**
   * Constructs a StrokeSizePicker component with specified position, dimensions, and visual style.
   * Initializes the component with a default stroke size.
   *
   * @param x the x-coordinate of the picker.
   * @param y the y-coordinate of the picker.
   * @param cWidth the width of the picker.
   * @param cHeight the height of the picker.
   * @param bgColor the background color of the picker.
   * @param borderColor the border color of the picker.
   * @param radius the corner radius of the picker's border.
   */
  public StrokeSizePicker(int x, int y, int cWidth, int cHeight, Color bgColor, Color borderColor, int radius) {
    super(x, y, cWidth, cHeight, bgColor, borderColor, radius);
    drawComponent();
    strokeSize = 2;
  }
  /**
   * Draws the component on the screen, displaying circles that represent different stroke sizes.
   * Larger circles represent larger stroke sizes.
   */
  @Override
    public void drawComponent() {
    super.drawComponent();
    fill(0);
    noStroke();
    circle(super.getX()+20, super.getY()+20, 15);
    circle(super.getX()+40, super.getY()+20, 10);
    circle(super.getX()+55, super.getY()+20, 5);
  }
  /**
   * Updates the stroke size based on user interaction. If a circle is clicked, the stroke size
   * corresponding to that circle's diameter is selected.
   *
   * @return true if the stroke size was updated, false otherwise.
   */
  public boolean updateStrokeSize() {
    if (isClicked()) {
      float distanceToCircle1 = dist(mouseX, mouseY, super.getX() + 20, super.getY() + 20);
      float distanceToCircle2 = dist(mouseX, mouseY, super.getX() + 40, super.getY() + 20);
      float distanceToCircle3 = dist(mouseX, mouseY, super.getX() + 55, super.getY() + 20);

      // Check if the mouse is within the radius of any circle
      if (distanceToCircle1 <= 15) {
        strokeSize = 15;
        return true;
      } else if (distanceToCircle2 <= 10) {
        strokeSize = 10;
        return true;
      } else if (distanceToCircle3 <= 5) {
        strokeSize = 5;
        return true;
      }
    }
    return false;
  }
  /**
   * Returns the currently selected stroke size.
   *
   * @return the stroke size.
   */
  public int getStrokeSize() {
    return strokeSize;
  }
}
