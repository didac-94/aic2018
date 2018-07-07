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
        for (int i = 0; i < 8; ++i) {
            if (uc.getRound() % 3 == 0) {

                if(uc.getRound() % 113 == 0){
                    if (uc.canSpawn(data.dirs[i], UnitType.BALLISTA)) {
                        uc.spawn(data.dirs[i], UnitType.BALLISTA);
                    }

                }

                if (uc.getRound() % 5 != 0) {
                    if (uc.canSpawn(data.dirs[i], UnitType.WARRIOR)) {
                        uc.spawn(data.dirs[i], UnitType.WARRIOR);
                    }
                } else if(uc.getRound() < 500){
                    if (uc.canSpawn(data.dirs[i], UnitType.KNIGHT)) {
                        uc.spawn(data.dirs[i], UnitType.KNIGHT);
                    }
                } else{
                    if (uc.canSpawn(data.dirs[i], UnitType.ARCHER)) {
                        uc.spawn(data.dirs[i], UnitType.ARCHER);
                    }
                }
            }
        }

    }

}
