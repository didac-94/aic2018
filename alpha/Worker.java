package alpha;

import aic2018.*;

public class Worker {

    UnitController uc;
    Data data;
    Tools tools;


    public Worker(UnitController _uc, Data _data) {
        uc = _uc;
        data = _data;
        tools = new Tools(data);
    }

    public void run() {

        //Make a single barracks
        int randIndex = (int)(Math.random()*8);
        if (data.nBarracks < data.nWorker/6 && data.stableEconomy) {
            if (uc.canSpawn(data.dirs[randIndex], UnitType.BARRACKS)) {
                uc.spawn(data.dirs[randIndex], UnitType.BARRACKS);
                uc.write(data.nBarracksChannel, data.nBarracks + 1);
            }
        }

        // Report enemies around
        boolean enemyFoundByMates = tools.enemyFoundByMates();
        boolean enemyFoundByMe = false;
        if (!enemyFoundByMates) enemyFoundByMe = tools.enemyFoundByMe();

        if (enemyFoundByMates) {
            //Move in enemy direction
            int coded_location = uc.read(uc.getInfo().getID());
            // uc.println(coded_location);
        }

        //Plant some trees or make some workers
        if (data.nTrees > 6*data.nWorker) {
            Direction randomDir = tools.RandomDir();
            if (uc.canSpawn(randomDir, UnitType.WORKER)) {
                uc.spawn(randomDir, UnitType.WORKER);
                uc.write(data.nWorkerChannel, uc.read(data.nWorkerChannel) + 1);
            }
        } else {
            int randomNum = (int) (Math.random()*8);
            Direction randomDir = data.dirs[randomNum];
            Location randomLoc = new Location();
            randomLoc.x = uc.getLocation().x + randomDir.dx;
            randomLoc.y = uc.getLocation().y + randomDir.dy;
            //Plant a tree
            if (uc.canUseActiveAbility(randomLoc)) {
                uc.useActiveAbility(randomLoc);
                uc.write(data.nTreesChannel, uc.read(data.nTreesChannel) + 1);
            }
        }

        UnitInfo[] unitsNear = uc.senseUnits(data.NEAR_RADIUS, uc.getTeam());
        int nWorkersAround = tools.MatesAround(unitsNear, UnitType.WORKER);

        if (nWorkersAround > 0) {
            Direction factDir = tools.OppositeDir(nWorkersAround, unitsNear);
            factDir = tools.Roomba(factDir);
            if (uc.canMove(factDir)) uc.move(factDir); // canMove irrelevant
        } else {
            //Move towards the closest healthy tree in sight
            TreeInfo visibleTrees[] = uc.senseTrees();
            if (visibleTrees.length > 0) {
                int closestHealthyTree = 0;
                int distToClosestTree = 10000;
                Location myLoc = uc.getLocation();

                for (int i = 0; i < visibleTrees.length; i++) {
                    TreeInfo tree = visibleTrees[i];
                    if (tree.remainingGrowthTurns > 0
                            || tree.health <= data.MIN_TO_HEALTHY
                            || myLoc.distanceSquared(tree.location) == 0) continue;
                    if (myLoc.distanceSquared(tree.location) < distToClosestTree) {
                        distToClosestTree = myLoc.distanceSquared(tree.location);
                        closestHealthyTree = i;
                    }
                }

                TreeInfo hT = visibleTrees[closestHealthyTree];
                Location htPos = visibleTrees[closestHealthyTree].location;
                Direction htDir = uc.getLocation().directionTo(htPos);

                int distToHt = uc.getLocation().distanceSquared(htPos);
                if (distToHt > 2) {
                    htDir = tools.Roomba(htDir);
                    if (uc.canMove(htDir)) uc.move(htDir);
                } else {
                    htDir = tools.Roomba(htDir);
                    if (tools.CanChop(hT)) uc.attack(hT);
                }
            } else {
                Direction randomDir = tools.RandomDir();
                randomDir = tools.Roomba(randomDir);
                if (uc.canMove(randomDir)) {
                    uc.move(randomDir);
                }
            }
        }


        //Code for petit tree creation

        //Code for wood chopping

    }

}
