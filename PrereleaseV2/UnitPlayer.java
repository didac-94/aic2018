package PrereleaseV2;

import aic2018.UnitController;
import aic2018.UnitType;

public class UnitPlayer {

    public void run(UnitController uc) {

        if (uc.getType() == UnitType.WORKER) {
            Worker worker = new Worker(uc);
            while (true) {
                worker.run();
                uc.yield();
            }
        } else if (uc.getType() == UnitType.BARRACKS) {
            Barracks barracks = new Barracks(uc);
            while (true) {
                barracks.run();
                uc.yield();
            }
        } else if (uc.getType() == UnitType.WARRIOR){
            Warrior warrior = new Warrior(uc);
            while (true) {
                warrior.run();
                uc.yield();
            }
        } else if (uc.getType() == UnitType.KNIGHT) {
            Knight knight = new Knight(uc);
            while (true) {
                knight.run();
                uc.yield();
            }
        } else if (uc.getType() == UnitType.ARCHER) {
            Archer archer = new Archer(uc);
            while (true) {
                archer.run();
                uc.yield();
            }
        } else if (uc.getType() == UnitType.BALLISTA) {
            Ballista ballista = new Ballista(uc);
            while (true) {
                ballista.run();
                uc.yield();
            }
        }

    }
}
