public class EnemySkeleton extends WalkerEnemy {

    public EnemySkeleton(Game game, int x, int y, int patrolArea) {
        super(game, x, y, 24, 40, 40, 8, 2, 4, patrolArea, 190);
        pausesAtEnds = true;
        loadAnim("resources/skeleton", 6, 3, "FALL");
        tint(0.0, -0.85, 0.35); // bleach the palette-swapped body toward bone
    }
}
