package alpha;

import aic2018.*;

public class UnitPlayer {

    public void run(UnitController uc) {

        while (true) {

			if (uc.getType() == UnitType.WORKER) {
			    Worker worker = new Worker(uc);
			    worker.run();
            } else if (uc.getType() == UnitType.BARRACKS) {
                Barracks barracks = new Barracks(uc);
                barracks.run();
            } else {
                AttackUnit attackUnit = new AttackUnit(uc);
                attackUnit.run();
            } /*else if (uc.getType() == UnitType.WARRIOR){
                Warrior warrior = new Warrior(uc);
                warrior.run();
            } else if (uc.getType() == UnitType.ARCHER) {
                Archer archer = new Archer(uc);
                archer.run();
            } else if (uc.getType() == UnitType.KNIGHT) {
                Knight knight = new Knight(uc);
                knight.run();
            } else if (uc.getType() == UnitType.BALLISTA) {
                Ballista ballista = new Ballista(uc);
                ballista.run();
            }*/

            uc.yield(); //End of turn
        }

    }
}
