/**
 * Extends {@link Component} to represent a drawable canvas area where the drawing from the server is mirrored.
 * Provides functionalities to update the drawing and clear the canvas.
 * Handles incoming server messages that contain drawing information.
 */
public class Canvas extends Component {
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
    }

    /**
     * Updates the canvas drawing based on user input. It processes the input string to extract
     * drawing commands and coordinates, then performs the drawing operation on the canvas.
     * 
     * @param input the input string containing the drawing commands and parameters formatted as "Draw;;;mouseX;;;mouseY;;;pMouseX;;;pMouseY;;;color;;;strokeSize".
     */
    public void update(String input) {
        String[] messageParts = input.split(";;;");
        int mouseXRec = Integer.parseInt(messageParts[1]) + super.getX();
        int mouseYRec = Integer.parseInt(messageParts[2]) + super.getY();
        int pMouseXRec = Integer.parseInt(messageParts[3]) + super.getX();
        int pMouseYRec = Integer.parseInt(messageParts[4]) + super.getY();
        int colorRec = Integer.parseInt(messageParts[5]);
        int strokeSizeRec = Integer.parseInt(messageParts[6]);

        stroke(colorRec);
        strokeWeight(strokeSizeRec);
        line(mouseXRec, mouseYRec, pMouseXRec, pMouseYRec);
    }

    /**
     * Clears the canvas by re-drawing the initial state of the component.
     */
    public void clear() {
        drawComponent();
    }
}
