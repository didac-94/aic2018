package betav2;

import aic2018.*;

public class Worker {

    UnitController uc;
    Data data;
    Tools tools;


    public Worker(UnitController _uc) {
        uc = _uc;
        data = new Data(uc);
        tools = new Tools(data);
    }

    public void run() {

        //Update our info according to the comm channel
        data.Update();

        //Report myself
        reportMyself();

        //Report enemies around
        reportEnemies();

        //Make barracks according to economy data
        makeBarracks();

        //Movement
        move();

        //Plant trees if necessary
        plant();

        //Harvest wood
        chop();

        //Gather VPs if available
        gatherVP();

        //Plant some trees or make some workers
        buildEconomy();

        //if (data.loneWorker) uc.println("I'm alone");

    }

    void makeBarracks() {
        if (data.stableEconomy && data.nBarracks < 5) {
            int randIndex = (int)(Math.random()*8);
            if (uc.canSpawn(data.dirs[randIndex], UnitType.BARRACKS)) {
                uc.spawn(data.dirs[randIndex], UnitType.BARRACKS);
                uc.write(data.barracksCh, data.nBarracks + 1);
            }
        }
    }

    void reportMyself() {
        // Report to the Comm Channel
        uc.write(data.unitReportCh, uc.read(data.unitReportCh)+1);
        uc.write(data.workerReportCh, uc.read(data.workerReportCh)+1);
        // Reset Next Slot
        uc.write(data.unitResetCh, 0);
        uc.write(data.workerResetCh, 0);
    }

    void reportEnemies() {
        return;
    }

    void chop() {
        if (data.loneWorker) {
            TreeInfo[] myTrees = uc.senseTrees(2);
            for (TreeInfo tree : myTrees) {
                if (tools.CanChop(tree)) uc.attack(tree);
            }
        }
    }

    void plant() {
        if (data.loneWorker) {
            TreeInfo[] nearbyTrees = uc.senseTrees();
            int nNearbyTrees = nearbyTrees.length;
            if (nNearbyTrees < 6) {
                for (Direction dir : data.dirs) {
                    if (dir != Direction.ZERO) {
                        Location targetLoc = uc.getLocation().add(dir);
                        if (uc.canUseActiveAbility(targetLoc)) {
                            uc.useActiveAbility(targetLoc);
                            uc.write(data.plantedTreesCh, data.nPlantedTrees+1);
                        }
                    }
                }
            }
        }
    }

    void move() {
        if (data.currentRound < 10 && !data.loneWorker) {
            TreeInfo[] visibleTrees = uc.senseTrees();
            int distToNearestOak = data.INF;
            TreeInfo nearestOak = null;
            for (TreeInfo tree : visibleTrees) {
                if (tree.oak) {
                    int distToTree = uc.getLocation().distanceSquared(tree.location);

                    if (distToTree < distToNearestOak) {
                        distToNearestOak = distToTree;
                        nearestOak = tree;
                    }
                }
            }
            if (distToNearestOak != data.INF && distToNearestOak > 1) {
                Direction dirToTree = uc.getLocation().directionTo(nearestOak.location);
                uc.move(tools.GeneralDir(dirToTree));
            } else {
                data.loneWorker = true;
            }
        } else if (!data.loneWorker) {
            if (tools.MatesAround(4, UnitType.WORKER) == 0) {
                data.loneWorker = true;
            } else {
                uc.move(tools.GeneralDir(tools.RandomDir()));
            }
        }
    }

    //TODO
    void gatherVP() {

    };

    void buildEconomy() {
        if ((data.nPlantedTrees > 6*data.nWorker-1 || data.growthEconomy) && data.nWorker < uc.getRound()/40) {
            Direction randomDir = tools.RandomDir();
            if (uc.canSpawn(randomDir, UnitType.WORKER)) {
                uc.spawn(randomDir, UnitType.WORKER);
                uc.write(data.workerCh, uc.read(data.workerCh) + 1);
            }
        } else {
            int randomNum = (int) (Math.random() * 8);
            Direction randomDir = data.dirs[randomNum];
            Location randomLoc = new Location();
            randomLoc.x = uc.getLocation().x + randomDir.dx;
            randomLoc.y = uc.getLocation().y + randomDir.dy;
            //Plant a tree
            if (uc.canUseActiveAbility(randomLoc)) {
                uc.useActiveAbility(randomLoc);
                uc.write(data.plantedTreesCh, uc.read(data.plantedTreesCh) + 1);
            }
        }
    }

}
