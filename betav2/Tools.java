package betav1;

import aic2018.*;

public class Tools {

    Data data;

    //Constructor
    public Tools(Data _data) {
        data = _data;
    }

    //=================== MOVEMENT SECTION ===================//

    void MoveTo(Location loc) {
        Movement movement = new Movement(data.uc);
        movement.moveTo(loc);
    }

    public class Movement {

        UnitController uc;

        public Movement(UnitController _uc) {
            uc = _uc;
        }

        final int INF = 1000000;

        boolean rotateRight = true; //if I should rotate right or left
        Location lastObstacleFound = null; //latest obstacle I've found in my way
        int minDistToEnemy = INF; //minimum distance I've been to the enemy while going around an obstacle
        Location prevTarget = null; //previous target

        void moveTo(Location target){
            //No target? ==> bye!
            if (target == null) return;

            //different target? ==> previous data does not help!
            if (prevTarget == null || !target.isEqual(prevTarget)) resetMovement();

            //If I'm at a minimum distance to the target, I'm free!
            Location myLoc = uc.getLocation();
            int d = myLoc.distanceSquared(target);
            if (d <= minDistToEnemy) resetMovement();

            //Update data
            prevTarget = target;
            minDistToEnemy = Math.min(d, minDistToEnemy);

            //If there's an obstacle I try to go around it [until I'm free] instead of going to the target directly
            Direction dir = myLoc.directionTo(target);
            if (lastObstacleFound != null) dir = myLoc.directionTo(lastObstacleFound);

            //This should not happen for a single unit, but whatever
            if (uc.canMove(dir)) resetMovement();

            //I rotate clockwise or counterclockwise (depends on 'rotateRight'). If I try to go out of the map I change the orientation
            //Note that we have to try at most 16 times since we can switch orientation in the middle of the loop. (It can be done more efficiently)
            for (int i = 0; i < 16; ++i){
                if (uc.canMove(dir)){
                    uc.move(dir);
                    return;
                }
                Location newLoc = myLoc.add(dir);
                if (uc.isOutOfMap(newLoc)) rotateRight = !rotateRight;
                    //If I could not go in that direction and it was not outside of the map, then this is the latest obstacle found
                else lastObstacleFound = myLoc.add(dir);
                if (rotateRight) dir = dir.rotateRight();
                else dir = dir.rotateLeft();
            }

            if (uc.canMove(dir)) uc.move(dir);
        }

        //clear some of the previous data
        void resetMovement(){
            lastObstacleFound = null;
            minDistToEnemy = INF;
        }

    }


    //=================== RANDOM SECTION ===================//

    //Returns a random direction
    public Direction RandomDir() {
        int randomNum = (int)(Math.random()*8);
        return data.dirs[randomNum];
    }

    //Return a general direction towards the favourite direction
    public Direction GeneralDir(Direction favDir){
        UnitController uc = data.uc;
        for (int k = 0; k < 8 && !uc.canMove(favDir); ++k) {
            if (k%2 == 0) for (int j = 0; j < k; ++j) favDir = favDir.rotateLeft();
            else for (int j = 0; j < k; ++j) favDir = favDir.rotateRight();
        }
        return favDir;
    }

    //Returns the barycenter of a number of locations
    Location Barycenter(Location[] locs) {
        Location b = new Location(0,0);
        for (Location loc : locs) {
            b.x = b.x + loc.x;
            b.y = b.y + loc.y;
        }
        b.x = b.x/locs.length;
        b.y = b.y/locs.length;
        return b;
    }

    //Returns the barycenter of the allied units of a given type
    Location Barycenter(UnitType type){
        UnitInfo[] units = data.uc.senseUnits(data.ally);
        Location b = new Location(0,0);
        int n = 0;
        for (UnitInfo unit : units) {
            if (unit.getType() == type) {
                b.x = b.x + unit.getLocation().x;
                b.y = b.y + unit.getLocation().y;
                n++;
            }
        }
        b.x = b.x/n;
        b.y = b.y/n;
        return b;
    }

    //True if no units are in that location
    public boolean NoFriendlyUnitsAt(Location loc) {
        if (data.uc.getLocation() == loc) return false;
        UnitInfo[] nearbyAllies = data.uc.senseUnits(data.ally);
        for (UnitInfo unit : nearbyAllies) {
            if (unit.getLocation() == loc) return false;
        }
        return true;
    }

    //True if it is safe to attack that tree
    public boolean CanChop(TreeInfo tree) {
        return data.uc.canAttack(tree)
                && tree.remainingGrowthTurns == 0
                && tree.health > data.MIN_TREE_HEALTH
                && NoFriendlyUnitsAt(tree.location);
    }

    //Returns the # of allies of a given type in a squared radius
    public int MatesAround(int radius, UnitType type) {
        UnitInfo[] unitsNear = data.uc.senseUnits(radius, data.ally);
        int i = 0;
        for (UnitInfo unit : unitsNear)
            if (unit.getType() == type) i++;
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

    // TODO: mirar igualmente aunque los colegas ya tengan un sitio
    public boolean enemyFoundByMe() {
        UnitController uc = data.uc;
        UnitInfo[] enemiesNear = uc.senseUnits(uc.getOpponent());
        if (enemiesNear.length > 0) {
            // TODO: no coger el primero
            Location enemyLoc = enemiesNear[0].getLocation();
            int coded_location = enemyLoc.x * 10000 + enemyLoc.y;
            uc.write(uc.getInfo().getID(), coded_location);
            uc.write(data.mainstreamCh, coded_location);
            return true;
        } else {
            uc.write(uc.getInfo().getID(), 0);
        }
        return false;
    }
}
