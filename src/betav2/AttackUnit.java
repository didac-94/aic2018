package betav2;

import aic2018.*;

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

    public void run() {

        //Update our info according to the comm channel
        data.Update();

        //Report myself
        reportMyself();

        //Try to attack
        attack();

        //Movement
        move();

        //Try to attack after movement
        attack();
        attackOak();

    }

    void reportMyself() {
        // Report to the Comm Channel
        uc.write(data.unitReportCh, uc.read(data.unitReportCh)+1);
        // Reset Next Slot
        uc.write(data.unitResetCh, 0);
    }

    void reportEnemies() {

    }

    void attack() {
        UnitInfo[] enemies = uc.senseUnits(uc.getOpponent());
        for (UnitInfo unit : enemies) {
            if (uc.canAttack(unit)) uc.attack(unit);
        }
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

    void move() {
        Location target = new Location(0,0);
        if (uc.getRound() < 1000 && data.enemyOnSight == 0) {
            Location enemyBase = tools.Barycenter(data.enemyTeam.getInitialLocations());
            Location allyBase = new Location(data.workerX/data.nWorker,data.workerY/data.nWorker);
            target = tools.BarCoord(new Location[] {enemyBase, allyBase}, new int[] {1,3});
        } else {
            target = tools.Decrypt(data.enemyLoc);
        }
        tools.MoveTo(target);
    }

    void move2() {

        int codedMainstreamLocation = uc.read(data.enemyOnSightCh);
        Location mainstreamLoc = new Location();
        mainstreamLoc.x = codedMainstreamLocation / 10000;
        mainstreamLoc.y = codedMainstreamLocation % 10000;
        //mainstreamLoc = data.enemyTeam.getInitialLocations()[0];

        if (uc.getLocation().distanceSquared(mainstreamLoc) <= data.NEAR_RADIUS) {
            uc.write(data.enemyOnSightCh, 0);
        }

        // Intento buscar enemics per la meva part
        boolean enemyFoundByMe = tools.enemyFoundByMe();

        // Miro si els colegas han trobat enemic
        //TODO: oblidar informacio desactualitzada
        //boolean enemyFoundByMates = false;
        //if (!enemyFoundByMe) enemyFoundByMates = tools.enemyFoundByMates();


        // TODO: go to the general direction of the enemyTeam or to allies
        Location myLoc = uc.getLocation();
        int mainstreamCodedLocation = uc.read(data.enemyOnSightCh);

        if (uc.read(1000 + uc.getInfo().getID()) == 0) {
            if (mainstreamCodedLocation > 0) {
                mainstreamLoc.x = mainstreamCodedLocation / 10000;
                mainstreamLoc.y = mainstreamCodedLocation % 10000;
                Direction toGeneralEnemy = myLoc.directionTo(mainstreamLoc);

                toGeneralEnemy = tools.GeneralDir(toGeneralEnemy);
                if (uc.canMove(toGeneralEnemy)) uc.move(toGeneralEnemy);
            } else if (uc.getRound() < 500) { // TODO: roundFirstBarraca + X
                // Move to a random direction searching for the meaning of life
                Direction randDir = tools.RandomDir();
                randDir = tools.GeneralDir(randDir);
                if (uc.canMove(randDir)) uc.move(randDir);
            } else {
                // TODO: implementar exploradors
                Direction toEnemySpawnDir = tools.RandomDir();
                toEnemySpawnDir = tools.GeneralDir(toEnemySpawnDir);
                if (uc.canMove(toEnemySpawnDir)) uc.move(toEnemySpawnDir);
            }

        } else {
            //Move in enemyTeam direction
            int coded_location = uc.read(1000 + uc.getInfo().getID());

            Location enemyLoc = new Location();
            enemyLoc.x = coded_location/10000;
            enemyLoc.y = coded_location%10000;
            // TODO: agruparnos antes de ir a partile(s) la boca
            Direction toEnemy = myLoc.directionTo(enemyLoc);

            toEnemy = tools.GeneralDir(toEnemy);
            if (uc.canMove(toEnemy)) uc.move(toEnemy);


        }
    }

}