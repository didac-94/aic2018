package alpha;

import aic2018.*;

public class Worker {

    UnitController uc;
    UsefulInfo data;


    public Worker(UnitController _uc) {
        uc = _uc;
        data = new UsefulInfo(uc);
    }

    public void run() {
        //Make a single barracks
        int randIndex = (int)(Math.random()*8);
        if (data.nBarracks < 1) {
            if (uc.canSpawn(data.dirs[randIndex], UnitType.BARRACKS)) {
                uc.spawn(data.dirs[randIndex], UnitType.BARRACKS);
                uc.write(data.nBarracksIndex, data.nBarracks + 1);
            }
        }

        //Move in a random direction
        int dirIndex = (int)(Math.random()*8);
        if (uc.canMove(data.dirs[dirIndex])) uc.move(data.dirs[dirIndex]);

        //Attack the first enemy you encounter
        UnitInfo[] enemies = uc.senseUnits(data.enemy);
        for (UnitInfo unit : enemies){
            if (uc.canAttack(unit)) uc.attack(unit);
        }

        //Code for petit tree creation

        //Code for wood chopping

    }

}
