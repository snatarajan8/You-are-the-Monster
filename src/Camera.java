import java.awt.Rectangle;

public class Camera {
    private int x;
    private int y;
    private int viewportWidth;
    private int viewportHeight;
    private int levelWidth;
    private int levelHeight;
    private static final int SCROLL_MARGIN = 200;
    private static final int SCROLL_SPEED = 5;

    public Camera(int x, int y, int viewportWidth, int viewportHeight) {
        this.x = x;
        this.y = y;
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.levelWidth = 1280; // Default level width
        this.levelHeight = 704; // Default level height
    }

    public void update(Rectangle playerRect) {
        // Calculate target position (center on player)
        int targetX = (int) playerRect.getX() - viewportWidth / 2;
        int targetY = (int) playerRect.getY() - viewportHeight / 2;

        // Smooth camera follow
        x += (targetX - x) / 10;
        y += (targetY - y) / 10;

        // Clamp to level bounds
        x = Math.max(0, Math.min(x, levelWidth - viewportWidth));
        y = Math.max(0, Math.min(y, levelHeight - viewportHeight));
    }

    public void setLevelSize(int width, int height) {
        this.levelWidth = width;
        this.levelHeight = height;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getViewportWidth() {
        return viewportWidth;
    }

    public int getViewportHeight() {
        return viewportHeight;
    }

    public boolean isVisible(Rectangle rect) {
        return rect.getX() + rect.getWidth() > x && 
               rect.getX() < x + viewportWidth &&
               rect.getY() + rect.getHeight() > y && 
               rect.getY() < y + viewportHeight;
    }
}
