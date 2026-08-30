public class EnemyKnight extends WalkerEnemy {

    public EnemyKnight(Game game, int x, int y, int patrolArea) {
        super(game, x, y, 32, 46, 80, 15, 2, 3, patrolArea, 220);
        attackRange = 52;
        attackCooldown = 60;
        loadAnim("resources/player", 6, 3, "FALL");
        tint(-0.02, -0.6, -0.32); // dark iron plate
    }
}
