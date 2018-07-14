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

        data.Update();

        //Attack the first target you see
        UnitInfo[] enemies = uc.senseUnits(uc.getOpponent());
        for (UnitInfo unit : enemies) {
            if (uc.canAttack(unit)) uc.attack(unit);
        }

        // Attack an oak
        TreeInfo[] trees = uc.senseTrees();
        for (TreeInfo tree : trees) {
            if (tree.oak) {
                if (uc.canAttack(tree)) {
                    uc.attack(tree);
                }
            }
        }

        int codedMainstreamLocation = uc.read(data.mainstreamCh);
        Location mainstreamLoc = new Location();
        mainstreamLoc.x = codedMainstreamLocation / 10000;
        mainstreamLoc.y = codedMainstreamLocation % 10000;

        if (uc.getLocation().distanceSquared(mainstreamLoc) <= data.NEAR_RADIUS) {
            uc.write(data.mainstreamCh, 0);
        }

        // Intento buscar enemics per la meva part
        boolean enemyFoundByMe = tools.enemyFoundByMe();

        // Miro si els colegas han trobat enemic
        //TODO: oblidar informacio desactualitzada
        //boolean enemyFoundByMates = false;
        //if (!enemyFoundByMe) enemyFoundByMates = tools.enemyFoundByMates();


        // TODO: go to the general direction of the enemy or to allies
        Location myLoc = uc.getLocation();
        int mainstreamCodedLocation = uc.read(data.mainstreamCh);

        if (uc.read(uc.getInfo().getID()) == 0) {
            if (mainstreamCodedLocation > 0) {
                mainstreamLoc.x = mainstreamCodedLocation / 10000;
                mainstreamLoc.y = mainstreamCodedLocation % 10000;
                Direction toGeneralEnemy = myLoc.directionTo(mainstreamLoc);

                toGeneralEnemy = tools.Roomba(toGeneralEnemy);
                if (uc.canMove(toGeneralEnemy)) uc.move(toGeneralEnemy);
            } else if (uc.getRound() < 200) { // TODO: roundFirstBarraca + X
                // Move to a random direction searching for the meaning of life
                Direction randDir = tools.RandomDir();
                randDir = tools.Roomba(randDir);
                if (uc.canMove(randDir)) uc.move(randDir);
            } else {
                // TODO: implementar exploradors
                Direction toEnemySpawnDir = tools.RandomDir();
                toEnemySpawnDir = tools.Roomba(toEnemySpawnDir);
                if (uc.canMove(toEnemySpawnDir)) uc.move(toEnemySpawnDir);
            }

        } else {
            //Move in enemy direction
            int coded_location = uc.read(uc.getInfo().getID());

            Location enemyLoc = new Location();
            enemyLoc.x = coded_location/10000;
            enemyLoc.y = coded_location%10000;
            // TODO: agruparnos antes de ir a partile(s) la boca
            Direction toEnemy = myLoc.directionTo(enemyLoc);

            toEnemy = tools.Roomba(toEnemy);
            if (uc.canMove(toEnemy)) uc.move(toEnemy);


        }

        //TODO: atacar despres de moures

    }

}