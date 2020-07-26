package padrosalpha;

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

            boolean SPAWN_FREQ = uc.getRound() % 7 == 0;

            if (uc.senseUnits(uc.getOpponent()).length > 0) {
                uc.write(data.alphaChannel, data.DEFEND_ALPHA_CODE);
                SPAWN_FREQ = true;
            } else if (uc.getResources() > 200) {
                // uc.write(data.alphaChannel, data.DEFAULT_ALPHA_CODE);
                SPAWN_FREQ = uc.getRound() % 3 == 0;
            }

            // If enemy has moved its base
            if (uc.read(data.alphaChannel) == data.ATTACK_ALPHA_CODE
                    && uc.getLocation().distanceSquared(uc.getOpponent().getInitialLocations()[0]) == 0
                    && uc.senseUnits(uc.getOpponent()).length == 0) {
                uc.write(data.alphaChannel, data.SEARCHING_ALPHA_CODE);
            }

            if (SPAWN_FREQ) {

                /*
                if(uc.getRound() % 113 == 0){
                    if (uc.canSpawn(data.dirs[i], UnitType.BALLISTA)) {
                        uc.spawn(data.dirs[i], UnitType.BALLISTA);
                    }
                }
                */

                Location enemySpawn = uc.getOpponent().getInitialLocations()[0];
                Direction toEnemySpawn = uc.getLocation().directionTo(enemySpawn);

                // TODO: int waveSize = uc.read(data.nTreesChannel)/3;
                int waveSize = 15;
                if (uc.read(data.nWarriorChannel) > waveSize) {
                    uc.write(data.alphaChannel, data.ATTACK_ALPHA_CODE);
                    uc.write(data.nWarriorChannel, 0); // Consider them dead
                }

                if (uc.getRound() % 5 != 0) {
                    if (uc.canSpawn(data.dirs[i], UnitType.WARRIOR)) {
                        toEnemySpawn = tools.Spawnable(toEnemySpawn, UnitType.WARRIOR);
                        uc.write(data.nWarriorChannel, uc.read(data.nWarriorChannel) + 1);
                        uc.spawn(toEnemySpawn, UnitType.WARRIOR);
                    }
                } else if(uc.getRound() < 500){
                    if (uc.canSpawn(data.dirs[i], UnitType.KNIGHT)) {
                        toEnemySpawn = tools.Spawnable(toEnemySpawn, UnitType.KNIGHT);
                        uc.write(data.nWarriorChannel, uc.read(data.nWarriorChannel) + 1);
                        uc.spawn(toEnemySpawn, UnitType.KNIGHT);
                    }
                } else{
                    if (uc.canSpawn(data.dirs[i], UnitType.ARCHER)) {
                        toEnemySpawn = tools.Spawnable(toEnemySpawn, UnitType.ARCHER);
                        uc.write(data.nWarriorChannel, uc.read(data.nWarriorChannel) + 1);
                        uc.spawn(toEnemySpawn, UnitType.ARCHER);
                    }
                }
            }
        }

    }

}
