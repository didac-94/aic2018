package alpha;

import aic2018.*;

public class Barracks {

    UnitController uc;
    Data data;
    Tools tools;


    public Barracks(UnitController _uc, Data _data) {
        uc = _uc;
        data = _data;
        tools = new Tools(data);
    }

    public void run() {

        //Make Warriors
        //int randIndex = (int)(Math.random()*4);
        //UnitType type = UnitType.values()[2+randIndex];
        //try to spawn a unit of the given type, if successful reset type.
        for (int i = 0; i < 8; ++i) if (uc.canSpawn(data.dirs[i], UnitType.WARRIOR)){
            uc.spawn(data.dirs[i], UnitType.WARRIOR);
        }

    }

}
