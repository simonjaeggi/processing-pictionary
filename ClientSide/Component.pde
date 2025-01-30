import java.awt.Color;

/**
 * Represents a basic graphical component for a user interface.
 * This class provides a flexible foundation for drawing rectangular components with customizable
 * properties such as color, border, and dimensions.
 */
public class Component {
  private int x;
  private int y;
  private int cWidth;
  private int cHeight;
  private int radius;
  private Color bgColor;
  private Color borderColor;
  private int strokeWeight;

  /**
   * Constructs a component with specified location, width, and height.
   * Initializes the component with default colors and no border radius.
   *
   * @param x the x-coordinate of the component
   * @param y the y-coordinate of the component
   * @param cWidth the width of the component
   * @param cHeight the height of the component
   */
  public Component(int x, int y, int cWidth, int cHeight) {
    this(x, y, cWidth, cHeight, 0, 1);
  }

  /**
   * Constructs a component with specified location, dimensions, and corner radius.
   * Initializes the component with default colors.
   *
   * @param x the x-coordinate of the component
   * @param y the y-coordinate of the component
   * @param cWidth the width of the component
   * @param cHeight the height of the component
   * @param radius the border radius of the component corners
   */
  public Component(int x, int y, int cWidth, int cHeight, int radius) {
    this(x, y, cWidth, cHeight, Color.BLACK, Color.BLACK, radius);
  }

  /**
   * Constructs a component with specified location, dimensions, corner radius, and stroke weight.
   * Initializes the component with default colors.
   *
   * @param x the x-coordinate of the component
   * @param y the y-coordinate of the component
   * @param cWidth the width of the component
   * @param cHeight the height of the component
   * @param radius the border radius of the component corners
   * @param strokeWeight the stroke weight for the border of the component
   */
  public Component(int x, int y, int cWidth, int cHeight, int radius, int strokeWeight) {
    this.x = x;
    this.y = y;
    this.cWidth = cWidth;
    this.cHeight = cHeight;
    this.radius = radius;
    this.strokeWeight = strokeWeight;
    this.bgColor = Color.BLACK;
    this.borderColor = Color.BLACK;
  }

  /**
   * Constructs a component with specified location, dimensions, colors, and corner radius.
   *
   * @param x the x-coordinate of the component
   * @param y the y-coordinate of the component
   * @param cWidth the width of the component
   * @param cHeight the height of the component
   * @param bgColor the background color of the component
   * @param borderColor the border color of the component
   * @param radius the border radius of the component corners
   */
  public Component(int x, int y, int cWidth, int cHeight, Color bgColor, Color borderColor, int radius) {
    this.x = x;
    this.y = y;
    this.cWidth = cWidth;
    this.cHeight = cHeight;
    this.radius = radius;
    this.bgColor = bgColor;
    this.borderColor = borderColor;
    this.strokeWeight = 1;
  }

  /**
   * Draws the component with its current attributes.
   */
  public void drawComponent() {
    strokeWeight(strokeWeight);
    fill(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue());
    stroke(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue());
    rect(x, y, cWidth, cHeight, radius);
  }

  /**
   * Determines if the component is being clicked.
   *
   * @return true if the component is currently clicked, false otherwise.
   */
  public boolean isClicked() {
    return mousePressed && mouseX > x && mouseX < x + cWidth && mouseY > y && mouseY < y + cHeight;
  }

  /**
   * Draws a rectangle over the component using the current background color.
   */
  public void drawOver() {
    strokeWeight(strokeWeight);
    fill(bgColor.getRGB());
    stroke(bgColor.getRGB());
    rect(x-strokeWeight, y-strokeWeight, cWidth+2*strokeWeight, cHeight+2*strokeWeight);
  }

  /**
   * Draws a rectangle over the component using a specified background color.
   * @param bgColor the background color used for drawing the rectangle.
   
   */
  public void drawOver(int bgColor) {
    fill(bgColor);
    stroke(bgColor);
    rect(x-strokeWeight, y-strokeWeight, cWidth+2*strokeWeight, cHeight+2*strokeWeight);
  }

  /**
   * Returns the x-coordinate of the component.
   *
   * @return the x-coordinate of this component
   */
  public int getX() {
    return x;
  }

  /**
   * Returns the y-coordinate of the component.
   *
   * @return the y-coordinate of this component
   */
  public int getY() {
    return y;
  }

  /**
   * Returns the height of the component.
   *
   * @return the height of this component
   */
  public int getHeight() {
    return cHeight;
  }

  /**
   * Returns the width of the component.
   *
   * @return the width of this component
   */
  public int getWidth() {
    return cWidth;
  }

  /**
   * Returns the radius of the component's corners.
   *
   * @return the corner radius of this component
   */
  public int getRadius() {
    return radius;
  }

  /**
   * Returns the background color of the component.
   *
   * @return the current background color of this component
   */
  public Color getBgColor() {
    return bgColor;
  }

  /**
   * Returns the border color of the component.
   *
   * @return the current border color of this component
   */
  public Color getBorderColor() {
    return borderColor;
  }

  /**
   * Sets the background color of the component.
   * Does not redraw the component with the new color.
   *
   * @param bgColor the new background color to set for this component.
   */
  public void setBgColor(Color bgColor) {
    this.bgColor = bgColor;
  }

  /**
   * Sets the border color of the component.
   * Does not redraw the component with the new color.
   *
   * @param borderColor the new border color to set for this component
   */
  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }
}
