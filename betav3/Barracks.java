package betav3;

import aic2018.Location;
import aic2018.UnitController;
import aic2018.UnitInfo;
import aic2018.UnitType;

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
        UnitInfo[] enemiesAround = uc.senseUnits(data.enemyTeam);
        if (enemiesAround.length > 0) {
            //If there's enemies around and no enemy was reported, report an enemy
            if (data.enemyFound == 0) {
                Location enemyLoc = enemiesAround[0].getLocation();
                int encryptedLoc = tools.Encrypt(enemyLoc.x, enemyLoc.y);
                uc.write(data.enemyFoundCh, 1);
                uc.write(data.enemyLocCh, encryptedLoc);
            }
        } else {
            //If no one is around the reported enemy location reset it
            if (data.enemyFound == 1) {
                Location enemyLoc = tools.Decrypt(data.enemyLoc);
                if (uc.canSenseLocation(enemyLoc)) {
                    uc.write(data.enemyFoundCh, 0);
                    uc.write(data.enemyLocCh, -1);
                }
            }
        }
    }

    void recruit() {
        //TODO: intentar triar una bona primera direcció
        for (int i = 0; i < 8; ++i) {
            //TODO: buscar una millor condició
            if (data.stableEconomy && data.nAttackUnit < 2000) {
                //TODO: buscar una millor composició
                if(Math.random() < 0.1){
                    if (uc.canSpawn(data.dirs[i], UnitType.KNIGHT)) {
                        uc.spawn(data.dirs[i], UnitType.KNIGHT);
                    }
                } else {
                    if (uc.canSpawn(data.dirs[i], UnitType.WARRIOR)) {
                        uc.spawn(data.dirs[i], UnitType.WARRIOR);
                    }
                }
            }
        }
    }

}
