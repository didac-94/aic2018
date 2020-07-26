package betav4;

import aic2018.UnitController;
import aic2018.UnitType;

public class UnitPlayer {

    public void run(UnitController uc) {

        UnitType type = uc.getType();

        if (type == UnitType.WORKER) {
            Worker worker = new Worker(uc);
            while (true) {
                worker.run();
                uc.yield();
            }
        } else if (type == UnitType.BARRACKS) {
            Barracks barracks = new Barracks(uc);
            while (true) {
                barracks.run();
                uc.yield();
            }
        } else if (type == UnitType.WARRIOR){
            Warrior warrior = new Warrior(uc);
            while (true) {
                warrior.run();
                uc.yield();
            }
        } else if (type == UnitType.KNIGHT) {
            Knight knight = new Knight(uc);
            while (true) {
                knight.run();
                uc.yield();
            }
        } else if (type == UnitType.ARCHER) {
            Archer archer = new Archer(uc);
            while (true) {
                archer.run();
                uc.yield();
            }
        } else if (type == UnitType.BALLISTA) {
            Ballista ballista = new Ballista(uc);
            while (true) {
                ballista.run();
                uc.yield();
            }
        }

    }
}
