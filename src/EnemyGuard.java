public class EnemyGuard extends WalkerEnemy {

    public EnemyGuard(Game game, int x, int y, int patrolArea) {
        super(game, x, y, 28, 44, 50, 12, 3, 5, patrolArea, 210);
        attackRange = 46;
        loadAnim("resources/player", 6, 3, "FALL");
        tint(-0.08, 0.15, -0.05); // steel-blue livery
    }
}
