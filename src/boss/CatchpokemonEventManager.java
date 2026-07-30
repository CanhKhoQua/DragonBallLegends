package boss;



public class CatchpokemonEventManager extends BossManager {

    private static CatchpokemonEventManager instance;

    public static CatchpokemonEventManager gI() {
        if (instance == null) {
            instance = new CatchpokemonEventManager();
        }
        return instance;
    }

}
