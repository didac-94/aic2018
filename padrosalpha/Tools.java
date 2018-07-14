package padrosalpha;

import aic2018.*;

public class Tools {

    Data data;

    public Tools(Data _data) {
        data = _data;
    }

    //Returns a random direction
    public Direction RandomDir() {
        int randomNum = (int)(Math.random()*8);
        return data.dirs[randomNum];
    }

    //True if it is safe to attack that tree
    public boolean CanChop(TreeInfo tree) {
        return data.uc.canAttack(tree)
                && tree.remainingGrowthTurns == 0
                && tree.health > data.MIN_TO_HEALTHY
                && NobodyAt(tree.location);
    }

    //True if no units are in that location
    public boolean NobodyAt(Location loc) {
        UnitController uc = data.uc;
        int distToTarget = uc.getLocation().distanceSquared(loc);
        UnitInfo[] unitsNear = uc.senseUnits(distToTarget, uc.getTeam());

        for (UnitInfo unit : unitsNear)
            if (unit.getLocation().isEqual(loc)) return false;

        return uc.getLocation() != loc;
    }

    // Pre: UnitInfo[] unitsNear = uc.senseUnits(data.NEAR_RADIUS, uc.getTeam());
    // Post: Returns the # of workers in a squared radius of NEAR_RADIUS.
    public int MatesAround(UnitInfo[] unitsNear, UnitType type) {
        int i = 0;
        for (UnitInfo unit : unitsNear)
            if (unit.getType() == type)
                unitsNear[i++] = unit;
        return i;
    }

    // Pre: nWorkers > 0. # workers in a near radius.
    public Direction OppositeDir(int nWorkers, UnitInfo[] workersNear) {
        Location myLoc = data.uc.getLocation();
        int worker = 0; // we begin with the first one
        Direction toWorkerDir;

        do toWorkerDir = myLoc.directionTo(workersNear[worker++].getLocation());
        while (!data.uc.canMove(toWorkerDir.opposite()) && worker < nWorkers);
        if (!data.uc.canMove(toWorkerDir.opposite())) toWorkerDir = RandomDir();
        return toWorkerDir.opposite();
    }

    // TODO: mirar si la loc que tens tu esta mes a prop que la que et diuen els colegas
    // Post: escriu al vector comú, a la teva ID, la location d'un enemic
    public boolean enemyFoundByMates() {
        UnitController uc = data.uc;
        UnitInfo[] unitsNear = uc.senseUnits(data.NEAR_RADIUS, uc.getTeam());
        for (int i = 0; i < unitsNear.length; ++i) {
            int mateID = unitsNear[i].getID();
            // Si hi ha un enemic, jo ho comunico tambe
            // (Tècnica Nazi. (Sigo ordenes))
            if (uc.read(mateID) > 0) {
                uc.write(uc.getInfo().getID(), uc.read(mateID));
                return true;
            }
        }

        return false;
    }

    // TODO: mirar igualmente aunque los colegas ya tengan un sitio
    public boolean enemyFoundByMe() {
        UnitController uc = data.uc;
        UnitInfo[] enemiesNear = uc.senseUnits(uc.getOpponent());
        if (enemiesNear.length > 0) {
            // TODO: no coger el primero
            Location enemyLoc = enemiesNear[0].getLocation();
            int coded_location = enemyLoc.x * 10000 + enemyLoc.y;
            uc.write(uc.getInfo().getID(), coded_location);
            uc.write(data.mainstreamChannel, coded_location);
            return true;
        } else {
            uc.write(uc.getInfo().getID(), 0);
        }
        return false;
    }

    //Return a general direction towards the favourite direction
    public Direction Roomba(Direction favDir){
        UnitController uc = data.uc;
        for (int k = 0; k < 8 && !uc.canMove(favDir); ++k) {
            if (k%2 == 0) for (int j = 0; j < k; ++j) favDir = favDir.rotateLeft();
            else for (int j = 0; j < k; ++j) favDir = favDir.rotateRight();
        }
        return favDir;
    }

    //Return a spawnable direction towards the favourite direction
    public Direction Spawnable(Direction favDir, UnitType type){
        UnitController uc = data.uc;
        for (int k = 0; k < 8 && !uc.canSpawn(favDir, type); ++k) {
            if (k%2 == 0) for (int j = 0; j < k; ++j) favDir = favDir.rotateLeft();
            else for (int j = 0; j < k; ++j) favDir = favDir.rotateRight();
        }
        return favDir;
    }

    //Return a plantable direction towards the favourite direction
    public Location Plantable(Direction favDir){
        UnitController uc = data.uc;
        Location myLoc = uc.getLocation();
        for (int k = 0; k < 8 && !uc.canUseActiveAbility(myLoc.add(favDir)); ++k) {
            if (k%2 == 0) for (int j = 0; j < k; ++j) favDir = favDir.rotateLeft();
            else for (int j = 0; j < k; ++j) favDir = favDir.rotateRight();
        }
        return myLoc.add(favDir);
    }


    public Direction dirFarFromWorker(Direction favDir, UnitInfo[] unitsAround) {
        boolean valid = true;
        UnitController uc = data.uc;
        for (int k = 0; k < 8 && !uc.canMove(favDir) && valid; ++k) {
            if (k%2 == 0) for (int j = 0; j < k; ++j) favDir = favDir.rotateLeft();
            else for (int j = 0; j < k; ++j) favDir = favDir.rotateRight();

            valid = true;
            // Check if the direction doesn't intercept with workers
            for (UnitInfo unit : unitsAround) {
                if (unit.getType() == UnitType.WORKER) {
                    Location myLoc = uc.getLocation();
                    Location newLoc = myLoc.add(favDir);
                    if (myLoc.distanceSquared(newLoc) < data.ADJ_RADIUS) {
                        valid = false;
                        break;
                    }
                }
            }

        }
        return favDir;
    }

    public boolean freePath(Location targetPos) {
        boolean isObstr = false;
        UnitController uc = data.uc;
        Location myLoc = uc.getLocation();
        int distToTree = myLoc.distanceSquared(targetPos);

        Direction dirToTarget = myLoc.directionTo(targetPos);
        Location newLoc = myLoc.add(dirToTarget);

        while (!isObstr && newLoc.distanceSquared(targetPos) < distToTree) {
            isObstr = uc.isAccessible(newLoc);
            newLoc = newLoc.add(dirToTarget);
        }

        return !isObstr;
    }

    public double getAlpha(int code) {
        if (code == data.DEFEND_ALPHA_CODE) return data.DEFEND_ALPHA;
        if (code == data.ATTACK_ALPHA_CODE) return data.ATTACK_ALPHA;
        if (code == data.SEARCHING_ALPHA_CODE) return data.SEARCHING_ALPHA;
        return data.DEFAULT_ALPHA;
    }

    public Direction toHomotheticPoint(double alpha) {
        UnitController uc = data.uc;
        Location mySpawnLoc = uc.getTeam().getInitialLocations()[0];
        Location enemySpawnLoc = uc.getOpponent().getInitialLocations()[0];
        Location myLoc = uc.getLocation();

        Direction actualDir = Roomba(RandomDir());
        Direction toMidMap = actualDir;
        double minAbsChangeOfDist = 100000;

        for (int k = 0; k < 8; ++k) {
            if (uc.canMove(actualDir)) {
                Location newLoc = myLoc.add(actualDir);
                double distToEnemySpawn = Math.sqrt(newLoc.distanceSquared(enemySpawnLoc));
                double distToMySpawn = Math.sqrt(newLoc.distanceSquared(mySpawnLoc));
                double homeoDist = distToEnemySpawn*alpha - distToMySpawn*(1 - alpha);
                int absChangeOfDist = Math.abs((int) homeoDist);

                if (absChangeOfDist < minAbsChangeOfDist) {
                    minAbsChangeOfDist = absChangeOfDist;
                    toMidMap = actualDir;
                }
            }
            actualDir = actualDir.rotateRight();
        }

        return toMidMap;
    }

    public Direction toNowhereDir() {
        UnitController uc = data.uc;
        Location mySpawnLoc = uc.getTeam().getInitialLocations()[0];
        Location enemySpawnLoc = uc.getOpponent().getInitialLocations()[0];
        Location myLoc = uc.getLocation();

        Direction actualDir = Roomba(RandomDir());
        Direction toMidMap = actualDir;
        double maxAbsChangeOfDist = 0;

        for (int k = 0; k < 8; ++k) {
            if (uc.canMove(actualDir)) {
                Location newLoc = myLoc.add(actualDir);
                double distToEnemySpawn = Math.sqrt(newLoc.distanceSquared(enemySpawnLoc));
                double distToMySpawn = Math.sqrt(newLoc.distanceSquared(mySpawnLoc));
                double homeoDist = distToEnemySpawn + distToMySpawn;
                int absChangeOfDist = (int) homeoDist;

                if (absChangeOfDist > maxAbsChangeOfDist) {
                    maxAbsChangeOfDist = absChangeOfDist;
                    toMidMap = actualDir;
                }
            }
            actualDir = actualDir.rotateRight();
        }

        return toMidMap;
    }
}
