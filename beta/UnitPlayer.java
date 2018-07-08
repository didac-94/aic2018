package beta;

import aic2018.*;

public class UnitPlayer {

    public void run(UnitController uc) {

        Data data = new Data(uc);
        Worker worker = new Worker(uc, data);
        Barracks barracks = new Barracks(uc, data);
        AttackUnit attackUnit = new AttackUnit(uc, data);

        while (true) {

            data.Update();

			if (uc.getType() == UnitType.WORKER) {
			    worker.run();
            } else if (uc.getType() == UnitType.BARRACKS) {
                barracks.run();
            } else {
                attackUnit.run();
            } /*else if (uc.getType() == UnitType.WARRIOR){
                Warrior warrior = new Warrior(uc, data);
                warrior.run();
            } else if (uc.getType() == UnitType.ARCHER) {
                Archer archer = new Archer(uc, data);
                archer.run();
            } else if (uc.getType() == UnitType.KNIGHT) {
                Knight knight = new Knight(uc, data);
                knight.run();
            } else if (uc.getType() == UnitType.BALLISTA) {
                Ballista ballista = new Ballista(uc, data);
                ballista.run();
            }*/

            uc.yield(); //End of turn
        }

    }
}
