public class EnemySpider extends WalkerEnemy {

    public EnemySpider(Game game, int x, int y, int patrolArea) {
        super(game, x, y, 26, 18, 25, 6, 4, 6, patrolArea, 130);
        attackRange = 34;
        attackCooldown = 35;
        // Bat art only ships MOVELEFT/MOVERIGHT; reuse it for every state.
        animation = new SpriteAnimation();
        animation.addSequence(State.MOVELEFT, animArray("resources/Bat/MOVELEFT", 4), 260);
        animation.addSequence(State.MOVERIGHT, animArray("resources/Bat/MOVERIGHT", 4), 260);
        animation.addSequence(State.FACELEFT, animArray("resources/Bat/MOVELEFT", 1), 400);
        animation.addSequence(State.FACERIGHT, animArray("resources/Bat/MOVERIGHT", 1), 400);
        animation.addSequence(State.FALLLEFT, animArray("resources/Bat/MOVELEFT", 1), 400);
        animation.addSequence(State.FALLRIGHT, animArray("resources/Bat/MOVERIGHT", 1), 400);
        tint(-0.05, 0.1, -0.28); // darker than the bats
    }
}
