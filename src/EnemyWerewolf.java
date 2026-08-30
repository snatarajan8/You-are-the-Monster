public class EnemyWerewolf extends WalkerEnemy {

    public static final int WOLF_WIDTH = 40;
    public static final int WOLF_HEIGHT = 40;

    public EnemyWerewolf(Game game, int x, int y, int patrolArea) {
        super(game, x, y, WOLF_WIDTH, WOLF_HEIGHT, 60, 10, 3, 6, patrolArea, 260);
        attackRange = 44;
        loadAnim("resources/werewolf", 6, 3, "FACE");
    }
}
