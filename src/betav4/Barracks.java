package betav4;

import aic2018.*;

public class Barracks {

    UnitController uc;
    Data data;
    Tools tools;


    public Barracks(UnitController _uc) {
        uc = _uc;
        data = new Data(uc);
        tools = new Tools(data);
    }

    public void run() {

        //Update our info according to the comm channel
        data.Update();

        //Report myself
        report();

        //Spawn units
        recruit();

    }

    void report() {
        reportMyself();
        reportEnemies();
    }

    void reportMyself() {
        // Report to the Comm Channel
        uc.write(data.barracksReportCh, uc.read(data.barracksReportCh)+1);
        // Reset Next Slot
        uc.write(data.barracksResetCh, 0);
    }

    void reportEnemies() {
        if (!data.enemyContact) {
            //Check if there has been direct contact with the enemy
            UnitInfo[] nearbyEnemies = uc.senseUnits(2, data.enemyTeam);
            if (tools.Adjacent(nearbyEnemies) > 0) {
                uc.write(data.enemyContactCh, 1);
                data.enemyContact = true;
            }
        } else {
            //Find a target for the army
            UnitInfo[] enemiesAround = uc.senseUnits(data.enemyTeam);
            if (enemiesAround.length > 0) {
                //If there's enemies around report an enemy
                Location enemyLoc = enemiesAround[0].getLocation();
                int encryptedLoc = tools.Encrypt(enemyLoc.x, enemyLoc.y);
                uc.write(data.enemyFoundCh, 1);
                uc.write(data.enemyLocCh, encryptedLoc);

            } else {
                //If no one is around the reported enemy location reset it
                if (data.enemyFound) {
                    Location enemyLoc = tools.Decrypt(data.enemyLoc);
                    if (uc.canSenseLocation(enemyLoc)) {
                        uc.write(data.enemyFoundCh, 0);
                        uc.write(data.enemyLocCh, -1);
                    }
                }
            }
        }
    }

    void recruit() {
        //TODO: find a better direction
        for (Direction dir : data.dirs) {
            //TODO: find a better condition
            if (!data.enemyContact && data.nScout < 2) {
                if (uc.canSpawn(dir, UnitType.KNIGHT)) {
                    uc.spawn(dir, UnitType.KNIGHT);
                }
            } else if (data.enemyContact) {
                if (data.nAttackUnit < 150) {
                    //TODO: dynamic composition
                    double R = Math.random();
                    if (data.nKnight < 2 || R < 0.3) {
                        if (uc.canSpawn(dir, UnitType.KNIGHT)) {
                            uc.spawn(dir, UnitType.KNIGHT);
                        }
                    } else if (R < 0.4) {
                        if (uc.canSpawn(dir, UnitType.ARCHER)) {
                            uc.spawn(dir, UnitType.ARCHER);
                        }
                    } else {
                        if (uc.canSpawn(dir, UnitType.WARRIOR)) {
                            uc.spawn(dir, UnitType.WARRIOR);
                        }
                    }
                }
            }
        }
    }

}
