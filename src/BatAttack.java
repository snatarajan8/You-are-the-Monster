import java.util.Set;

public class BatAttack extends CollisionUnit{
    private int directionx;
    private int directiony;
    private final int damage = 5;
    protected Set<CollisionUnit> units;

    public BatAttack(Game game, int x, int y, int height, int width, int velocityx, int velocityy) {
        super(x, y, height, width);
        this.directionx = velocityx;
        this.directiony = velocityy;
        units = game.getCollisionSet();
    }

    public void update() {
        rectangle.setLocation((int) rectangle.getX() + directionx, (int) rectangle.getY() + directiony);
        for (CollisionUnit unit : units) {
            if (unit != this) {
                if (rectangle.intersects(unit.getRectangle())) {
                    if (unit instanceof Player) {
                        ((Player) unit).damageHealth(damage);
                    }
                    units.remove(this);
                }
            }
        }
    }
}
