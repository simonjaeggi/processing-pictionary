import processing.core.PApplet;

/**
 * Represents a color picker component, extending the {@link Component} class.
 * This component allows users to select a color from a predefined set of color options displayed as circles.
 */
public class ColorPicker extends Component {

    private int currentPosX;
    private int currentPosY;

    /**
     * Constructs a ColorPicker component with specified location and dimensions.
     * Initializes the component and sets up the initial drawing state.
     *
     * @param x the x-coordinate of the color picker.
     * @param y the y-coordinate of the color picker.
     * @param cWidth the width of the color picker.
     * @param cHeight the height of the color picker.
     */
    public ColorPicker(int x, int y, int cWidth, int cHeight) {
        super(x, y, cWidth, cHeight);
        drawComponent();
    }

    /**
     * Draws the color picker component on the screen. Defines the visual layout of the color picker,
     * drawing a series of colored circles each representing a selectable color.
     */
    @Override
    public void drawComponent() {
        noStroke();
        // Draw color circles
        int spacing = 30; // Vertical spacing between circles
        int radius = 20; // Radius of each color circle
        int[] colors = new int[] {  // RGB values of the colors
            color(193, 193, 193), // gray
            color(219, 53, 37),   // red
            color(238, 122, 49),  // orange
            color(250, 229, 76),  // yellow
            color(92, 201, 58),   // green
            color(81, 175, 249),  // light blue
            color(35, 31, 203),   // blue
            color(149, 29, 179),  // purple
            color(209, 112, 165), // pink
            color(243, 176, 147), // peach
            color(130, 42, 42)    // brown
        };

        for (int i = 0; i < colors.length; i++) {
            fill(colors[i]);
            circle(super.getX() + 10, super.getY() + 20 + i * spacing, radius);
        }
    }

    /**
     * Updates the position of the color selection based on user interaction.
     * This method can be called to handle user clicks.
     *
     * @return true if the color picker's position was updated, false otherwise.
     */
    public boolean updatePosition() {
        if (isClicked()) {
            drawComponent();
            currentPosX = mouseX;
            currentPosY = mouseY;
            return true;
        }
        return false;
    }

    /**
     * Retrieves the color at the current selected position within the color picker.
     * This method should be used after a call to {@code updatePosition()} confirms a color selection.
     *
     * @return the color at the current position as an integer.
     */
    public int getColor() {
        int pixelColor = get(currentPosX, currentPosY);
        return pixelColor;
    }
}
