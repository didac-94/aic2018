package padrosalpha;

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

        // Basic toEnemySpawn direction
        Location enemySpawnLoc = uc.getOpponent().getInitialLocations()[0];
        Direction toEnemySpawn = uc.getLocation().directionTo(enemySpawnLoc);

        //Make barracks according to economy data
        // TODO: uc.getResources() + TURNS_TO_WAIT*INCOME > GameConstants.BARRACKS_COST => DON'T SPEND MONEY
        if (data.stableEconomy && data.nBarracks < data.nWorker/3) {
            toEnemySpawn = tools.Spawnable(toEnemySpawn, UnitType.BARRACKS);
            if (uc.canSpawn(toEnemySpawn, UnitType.BARRACKS)) {
                uc.spawn(toEnemySpawn, UnitType.BARRACKS);
                uc.write(data.nBarracksChannel, data.nBarracks + 1);
            }
        }

        //Report enemies around
        boolean enemyFoundByMates = tools.enemyFoundByMates();
        boolean enemyFoundByMe = false;
        if (!enemyFoundByMates) enemyFoundByMe = tools.enemyFoundByMe();

        if (enemyFoundByMates) {
            //Move in enemy direction
            int coded_location = uc.read(uc.getInfo().getID());
            // uc.println(coded_location);
        }

        // Units and workers near
        UnitInfo[] unitsNear = uc.senseUnits(data.ADJ_RADIUS, uc.getTeam());
        UnitInfo[] nonWorkersNear = unitsNear;
        int nWorkersAround = tools.MatesAround(unitsNear, UnitType.WORKER);
        int nUnitsAround = unitsNear.length;

        //Plant some trees or make some workers
        if (data.nTrees < data.treeCap) {
            if (data.nTrees >= 6 * data.nWorker) {
                Direction randomDir = tools.RandomDir();
                if (uc.canSpawn(randomDir, UnitType.WORKER)) {
                    uc.spawn(randomDir, UnitType.WORKER);
                    uc.write(data.nWorkerChannel, uc.read(data.nWorkerChannel) + 1);
                }
            } else {
                // TODO: else if (nWorkersAround > 0 || uc.getResources() > 300)
                // Enough money to spawn a Worker => Enough money to plant a Tree
                Location plantLoc = tools.Plantable(toEnemySpawn);
                //Plant a tree
                while (uc.canUseActiveAbility(plantLoc)) {
                    uc.useActiveAbility(plantLoc);
                    uc.write(data.nTreesChannel, uc.read(data.nTreesChannel) + 1);
                }
            }
        }

        //Movement
        for (UnitInfo unit : unitsNear)
            if (unit.getType() == UnitType.BARRACKS)
                --nUnitsAround; // Not considered an attack unit

        //Check for other workers around and spread if any
        if (nWorkersAround > 0) {
            Direction oppDir = tools.OppositeDir(nWorkersAround, unitsNear);
            oppDir = tools.Roomba(oppDir);
            if (uc.canMove(oppDir)) uc.move(oppDir);
        } else if (nUnitsAround > 0) {
            // Shake if there are attack units trying to pass
            Direction moveDir = tools.Roomba(tools.RandomDir());
            if (uc.canMove(moveDir)) uc.move(moveDir);
        }

        // Else move towards the closest healthy tree in sight
        TreeInfo visibleTrees[] = uc.senseTrees();
        if (visibleTrees.length > 0) {
            int closestHealthyTree = -1;
            int distToClosestTree = 10000;
            Location myLoc = uc.getLocation();

            boolean targetIsAdjAndSmall = false;

            for (int i = 0; i < visibleTrees.length; i++) {
                TreeInfo tree = visibleTrees[i];
                if (tree.remainingGrowthTurns > 0
                        || tree.health <= data.MIN_TO_HEALTHY
                        || myLoc.distanceSquared(tree.location) == 0) continue;
                if (closestHealthyTree < 0) closestHealthyTree = i;

                int distToTree = myLoc.distanceSquared(tree.location);
                Direction meToHt = uc.getLocation().directionTo(tree.location);
                boolean isDiag = meToHt.dx != 0 && meToHt.dy != 0;
                boolean isAdjacent = distToTree == 1 || (distToTree == 2 && isDiag);

                TreeInfo cHT = visibleTrees[closestHealthyTree];
                boolean iAmAdjAndSmall = isAdjacent && !tree.oak;
                boolean isObstr = tools.freePath(tree.location);

                // Best case scenario: adjacent, small tree, and healthier.
                if (isAdjacent && !tree.oak && tree.health > cHT.health) {
                    distToClosestTree = myLoc.distanceSquared(tree.location);
                    targetIsAdjAndSmall = true;
                    closestHealthyTree = i;
                } else if (!targetIsAdjAndSmall && iAmAdjAndSmall) {
                    distToClosestTree = myLoc.distanceSquared(tree.location);
                    targetIsAdjAndSmall = true;
                    closestHealthyTree = i;
                } else if (!targetIsAdjAndSmall && !iAmAdjAndSmall
                        && distToTree < distToClosestTree && !isObstr) {
                    distToClosestTree = myLoc.distanceSquared(tree.location);
                    closestHealthyTree = i;
                }
            }

            if (closestHealthyTree >= 0) {
                TreeInfo chT = visibleTrees[closestHealthyTree];
                Location chtPos = chT.location;
                Direction chtDir = uc.getLocation().directionTo(chtPos);

                // If can reach, chop.
                if (tools.CanChop(chT)) {
                    uc.attack(chT);
                } else if (tools.freePath(chtPos)) {
                    // If not adjecent, but can reach; move.
                    chtDir = tools.Roomba(chtDir);
                    if (uc.canMove(chtDir)) uc.move(chtDir);
                } else {
                    // Path obstructed, no accessible trees around.
                    closestHealthyTree = -1;
                }
            }

            if (closestHealthyTree < 0) {
                // No accessible trees, move randomly.
                // TODO: BugPath to X location
                double alpha = tools.getAlpha(uc.read(data.alphaChannel));
                Direction toThird = tools.toHomotheticPoint(alpha/2);
                if (uc.canMove(toThird)) uc.move(toThird);
            }
        } else {
            // No accessible trees nor units, move randomly.
            // TODO: BugPath to X location
            double alpha = tools.getAlpha(uc.read(data.alphaChannel));
            Direction toThird = tools.toHomotheticPoint(alpha/2);
            if (uc.canMove(toThird)) uc.move(toThird);
        }

    }

}
