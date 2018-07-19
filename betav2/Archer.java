package betav2;

import aic2018.UnitController;
import aic2018.UnitInfo;

public class Archer extends AttackUnit {

    UnitController uc;
    Data data;

    public Archer(UnitController _uc) {
        uc = _uc;
        data = new Data(uc);
    }

    public void run() {

        data.Update();

        //Move in a random direction
        int dirIndex = (int)(Math.random()*8);
        if (uc.canMove(data.dirs[dirIndex])) uc.move(data.dirs[dirIndex]);

        //Attack the first target you see
        UnitInfo[] enemies = uc.senseUnits(data.enemyTeam);
        for (UnitInfo unit : enemies){
            if (uc.canAttack(unit)) uc.attack(unit);
        }

    }

}