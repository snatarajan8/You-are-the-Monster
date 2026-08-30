public class EnemyZombie extends WalkerEnemy {

    public EnemyZombie(Game game, int x, int y, int patrolArea) {
        super(game, x, y, 28, 44, 45, 8, 1, 2, patrolArea, 150);
        loadAnim("resources/player", 6, 3, "FALL");
        tint(0.35, -0.15, -0.18); // rotting green
    }
}
