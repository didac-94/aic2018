package padrosalpha;

import aic2018.*;

public class UnitPlayer {

    public void run(UnitController uc) {

        Data data = new Data(uc);
        Worker worker = new Worker(uc, data);
        Barracks barracks = new Barracks(uc, data);
        AttackUnit attackUnit = new AttackUnit(uc, data);

        while (true) {

            data.Update();

            // If found, pick up victory points
            // No matter what unit type you are
            VictoryPointsInfo[] VPs = uc.senseVPs(data.ADJ_RADIUS);
            for (VictoryPointsInfo VP : VPs) {
                Direction toVP = uc.getLocation().directionTo(VP.location);
                if (uc.canGatherVPs(toVP)) uc.gatherVPs(toVP);
            }

            // If we are rich buy as much VPs as possible
            while (uc.getResources() > 1500) {
                if (uc.canBuyVP(1)) uc.buyVP(1);
            }

            if (uc.read(data.alphaChannel) == 0)
                uc.write(data.alphaChannel, data.DEFAULT_ALPHA_CODE);

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
