package betav1;

import aic2018.*;

public class UnitPlayer {

    public void run(UnitController uc) {

        if (uc.getType() == UnitType.WORKER) {
            Worker worker = new Worker(uc);
            while (true) {
                worker.run();
                uc.yield(); //End of turn
            }
        } else if (uc.getType() == UnitType.BARRACKS) {
            Barracks barracks = new Barracks(uc);
            while (true) {
                barracks.run();
                uc.yield(); //End of turn
            }
        } else {
            AttackUnit attackUnit = new AttackUnit(uc);
            while (true) {
                attackUnit.run();
                uc.yield(); //End of turn
            }
        }

    }
}
