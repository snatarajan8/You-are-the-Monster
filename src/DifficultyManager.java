public class DifficultyManager {
    public enum Difficulty {
        EASY,
        NORMAL,
        HARD,
        NIGHTMARE
    }

    private Difficulty currentDifficulty;
    private float healthMultiplier;
    private float damageMultiplier;
    private float pickupMultiplier;
    private float enemyAggressionMultiplier;
    private float enemyHealthMultiplier;

    public DifficultyManager() {
        this.currentDifficulty = Difficulty.NORMAL;
        applyDifficulty(Difficulty.NORMAL);
    }

    public void setDifficulty(Difficulty difficulty) {
        this.currentDifficulty = difficulty;
        applyDifficulty(difficulty);
    }

    private void applyDifficulty(Difficulty difficulty) {
        switch (difficulty) {
            case EASY:
                healthMultiplier = 2.0f;
                damageMultiplier = 0.5f;
                pickupMultiplier = 1.5f;
                enemyAggressionMultiplier = 0.7f;
                enemyHealthMultiplier = 0.8f;
                break;
            case NORMAL:
                healthMultiplier = 1.0f;
                damageMultiplier = 1.0f;
                pickupMultiplier = 1.0f;
                enemyAggressionMultiplier = 1.0f;
                enemyHealthMultiplier = 1.0f;
                break;
            case HARD:
                healthMultiplier = 0.5f;
                damageMultiplier = 2.0f;
                pickupMultiplier = 0.7f;
                enemyAggressionMultiplier = 1.5f;
                enemyHealthMultiplier = 1.5f;
                break;
            case NIGHTMARE:
                healthMultiplier = 0.25f;
                damageMultiplier = 4.0f;
                pickupMultiplier = 0.3f;
                enemyAggressionMultiplier = 2.0f;
                enemyHealthMultiplier = 2.0f;
                break;
        }
    }

    public Difficulty getCurrentDifficulty() {
        return currentDifficulty;
    }

    public float getHealthMultiplier() {
        return healthMultiplier;
    }

    public float getDamageMultiplier() {
        return damageMultiplier;
    }

    public float getPickupMultiplier() {
        return pickupMultiplier;
    }

    public float getEnemyAggressionMultiplier() {
        return enemyAggressionMultiplier;
    }

    public float getEnemyHealthMultiplier() {
        return enemyHealthMultiplier;
    }

    public int getScaledHealth(int baseHealth) {
        return (int) (baseHealth * healthMultiplier);
    }

    public int getScaledDamage(int baseDamage) {
        return (int) (baseDamage * damageMultiplier);
    }

    public int getScaledEnemyHealth(int baseHealth) {
        return (int) (baseHealth * enemyHealthMultiplier);
    }

    public String getDifficultyName() {
        switch (currentDifficulty) {
            case EASY: return "Easy";
            case NORMAL: return "Normal";
            case HARD: return "Hard";
            case NIGHTMARE: return "Nightmare";
            default: return "Unknown";
        }
    }
}
