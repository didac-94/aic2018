package betav3.2;

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

    //Class implementing bugpath
    public class Movement {

        UnitController uc;

        public Movement(UnitController _uc) {
            uc = _uc;
        }

        final int INF = 1000000;

        boolean rotateRight = true; //if I should rotate right or left
        Location lastObstacleFound = null; //latest obstacle I've found in my way
        int minDistToEnemy = INF; //minimum distance I've been to the enemyTeam while going around an obstacle
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


    //=================== FUNCTION SECTION ===================//

    //Returns a random direction
    Direction RandomDir() {
        int randomNum = (int)(Math.random()*8);
        return data.dirs[randomNum];
    }

    //Return a general direction towards the favourite direction
    Direction GeneralDir(Direction favDir){
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

    //Returns the barycenter of nearby allied units of a given type
    Location Barycenter(UnitType type){
        UnitInfo[] units = data.uc.senseUnits(data.allyTeam);
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

    //Returns the location with certain barycentric coordinates in the barycentric reference B
    Location BarCoord(Location[] B, int[] coord) {
        int s = 0;
        Location R = new Location(0,0);
        for (int i = 0; i < B.length; i++) {
            s += coord[i];
            R.x = R.x + coord[i]*B[i].x;
            R.y = R.y + coord[i]*B[i].y;
        }
        R.x = R.x/s;
        R.y = R.y/s;
        return R;
    }

    //Turn a pair of coordinates into an integer
    int Encrypt(int x, int y) {
        return x*1000 + y;
    }

    //Decrypt a location from an integer
    Location Decrypt(int n) {
        return new Location(n/1000, n%1000);
    }

    //True if no friendly units are in that location
    boolean NoFriendlyUnitsAt(Location loc) {
        if (data.uc.getLocation().isEqual(loc)) return false;
        UnitInfo[] nearbyAllies = data.uc.senseUnits(data.allyTeam);
        for (UnitInfo unit : nearbyAllies) {
            if (unit.getLocation().isEqual(loc)) return false;
        }
        return true;
    }

    //True if it is safe to attack that tree
    boolean CanChop(TreeInfo tree) {
        return data.uc.canAttack(tree)
                && tree.remainingGrowthTurns == 0
                && tree.health > data.minimumTreeHealth
                && NoFriendlyUnitsAt(tree.location);
    }

    //Gathers any VPs lying around
    void GatherVP() {
        for (Direction dir : data.dirs) {
            Location loc = data.uc.getLocation().add(dir);
            if (data.uc.isAccessible(loc) && data.uc.senseVPsAtLocation(loc).getVictoryPoints() > 0) {
                if (data.uc.canGatherVPs(dir)) data.uc.gatherVPs(dir);
            }
        }
    }

    //Returns the # of allies of a given type in a squared radius
    int MatesAround(int radius, UnitType type) {
        UnitInfo[] unitsNear = data.uc.senseUnits(radius, data.allyTeam);
        int i = 0;
        for (UnitInfo unit : unitsNear)
            if (unit.getType() == type) i++;
        return i;
    }

    //Returns the number of adjacent units
    int Adjacent(UnitInfo[] units) {
        Location myLoc = data.uc.getLocation();
        int count = 0;
        for (Direction dir : data.dirs) {
            for (UnitInfo unit : units) {
                if (myLoc.add(dir).isEqual(unit.getLocation())) count += 1;
            }
        }
        return count;
    }

    //Returns the number of adjacent units of the given type
    int Adjacent(UnitInfo[] units, UnitType type) {
        Location myLoc = data.uc.getLocation();
        int count = 0;
        for (Direction dir : data.dirs) {
            for (UnitInfo unit : units) {
                if (unit.getType() == type) {
                    if (myLoc.add(dir).isEqual(unit.getLocation())) count += 1;
                }
            }
        }
        return count;
    }

    //Returns the number of adjacent trees
    int Adjacent(TreeInfo[] trees) {
        Location myLoc = data.uc.getLocation();
        int count = 0;
        for (Direction dir : data.dirs) {
            for (TreeInfo tree : trees) {
                if (myLoc.add(dir).isEqual(tree.getLocation())) count += 1;
            }
        }
        return count;
    }

}
