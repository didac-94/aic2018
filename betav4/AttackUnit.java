package betav4;

import aic2018.Location;
import aic2018.TreeInfo;
import aic2018.UnitController;
import aic2018.UnitInfo;

public class AttackUnit {

    UnitController uc;
    Data data;
    Tools tools;

    public AttackUnit(){}

    public AttackUnit(UnitController _uc) {
        uc = _uc;
        data = new Data(uc);
        tools = new Tools(data);
    }

    void run() {

        //Update our info according to the Comm Channel
        data.Update();

        //Try to attack before movement
        attack();
        gatherVP();

        //Movement
        move();

        //Try to attack after movement
        attack();
        attackOak();
        gatherVP();

        //Report intel to the Comm Channel
        report();

    }

    void report() {
        reportMyself();
        reportEnemies();
        reportEnemyBases();
    }

    void reportMyself() {
        // Report to the Comm Channel
        uc.write(data.unitReportCh, uc.read(data.unitReportCh)+1);
        // Reset Next Slot
        uc.write(data.unitResetCh, 0);
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

    void reportEnemyBases() {
        for (int i = data.firstEnemyBase; i < data.nEnemyBases; i++) {
            if (uc.canSenseLocation(data.enemyBases[i])) {
                if (uc.senseUnits(data.enemyTeam).length == 0) {
                    uc.write(data.enemyBase0Ch + i, 1);
                }
            }
        }
    }

    void attack() {

        UnitInfo[] enemies = uc.senseUnits(data.enemyTeam);

        //attack the enemy with less health
        int minEnemyHealth = data.INF;
        UnitInfo target = null;
        for (UnitInfo unit : enemies) {
            if (unit.getHealth() < minEnemyHealth && uc.canAttack(unit)){
                minEnemyHealth = unit.getHealth();
                target = unit;
            }
        }
        if( target != null) uc.attack(target);
    }

    void attackOak() {
        TreeInfo[] trees = uc.senseTrees();
        for (TreeInfo tree : trees) {
            if (tree.oak) {
                if (uc.canAttack(tree)) {
                    uc.attack(tree);
                }
            }
        }
    }

    void gatherVP() {
        tools.GatherVP();
    }

    void move() {
        Location target;
        if (!data.enemyFound) {
            Location enemyBase = tools.Barycenter(data.enemyTeam.getInitialLocations());
            Location allyBase = new Location(data.workerX/data.nWorker,data.workerY/data.nWorker);
            target = tools.BarCoord(new Location[] {enemyBase, allyBase}, new int[] {1,3});
        } else {
            target = tools.Decrypt(data.enemyLoc);
        }
        tools.MoveTo(target);
    }

}