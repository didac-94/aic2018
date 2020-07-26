package padrosalpha;

import aic2018.*;

public class Archer extends AttackUnit {

    UnitController uc;
    Data data;

    public Archer(UnitController _uc, Data _data) {
        uc = _uc;
        data = _data;
    }

    public void run() {

        //Move in a random direction
        int dirIndex = (int)(Math.random()*8);
        if (uc.canMove(data.dirs[dirIndex])) uc.move(data.dirs[dirIndex]);

        //Attack the first target you see
        UnitInfo[] enemies = uc.senseUnits(data.enemy);
        for (UnitInfo unit : enemies){
            if (uc.canAttack(unit)) uc.attack(unit);
        }

    }

}