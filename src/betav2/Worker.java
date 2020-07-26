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

        //Report to Comm Channel
        report();

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

        //Buy VPs if too much money or near the end
        buyVP();

        //if (data.loneWorker) uc.println("I'm alone");
        //if (data.setWorker) uc.println("I'm set");

    }


    void report() {
        reportMyself();
        reportTrees();
        reportEnemies();
    }

    void reportMyself() {
        // Report to the Comm Channel
        uc.write(data.unitReportCh, uc.read(data.unitReportCh)+1);
        uc.write(data.workerReportCh, uc.read(data.workerReportCh)+1);
        uc.write(data.workerXReportCh, uc.read(data.workerXReportCh) + uc.getLocation().x);
        uc.write(data.workerYReportCh, uc.read(data.workerYReportCh) + uc.getLocation().y);
        // Reset Next Slot
        uc.write(data.unitResetCh, 0);
        uc.write(data.workerResetCh, 0);
        uc.write(data.workerXResetCh, 0);
        uc.write(data.workerYResetCh, 0);
    }

    void reportEnemies() {

    }

    void reportTrees() {
        if (data.loneWorker) {
            int nTreesAround = uc.senseTrees(2).length;
            uc.write(data.treeReportCh, uc.read(data.treeReportCh)+nTreesAround);
            uc.write(data.treeResetCh, 0);
        }
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
            TreeInfo[] nearbyTrees = uc.senseTrees(2);
            int nNearbyTrees = nearbyTrees.length;
            if (nNearbyTrees < 6) {
                for (Direction dir : data.dirs) {
                    if (dir != Direction.ZERO) {
                        Location targetLoc = uc.getLocation().add(dir);
                        if (uc.canUseActiveAbility(targetLoc)) {
                            uc.useActiveAbility(targetLoc);
                            uc.write(data.plantedTreesCh, data.nPlantedTrees+1);
                            break;
                        }
                    } else if (uc.getResources() >= 180){
                        uc.write(data.setWorkerReportCh, uc.read(data.setWorkerReportCh)+1);
                        uc.write(data.setWorkerResetCh, 0);
                        data.setWorker = true;
                    }
                }
            } else {
                uc.write(data.setWorkerReportCh, uc.read(data.setWorkerReportCh)+1);
                uc.write(data.setWorkerResetCh, 0);
                data.setWorker = true;
            }
        }
    }

    void move() {
        //Early  in the game look for nearby oaks
        //TODO: do it early in the lifetime of each worker
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
                tools.MoveTo(nearestOak.location);
            } else {
                data.loneWorker = true;
            }
        //If not isolated try to flee friendly workers
        } else if (!data.loneWorker) {
            if (tools.MatesAround(4, UnitType.WORKER) == 0) {
                data.loneWorker = true;
            } else {
                Location away = tools.Barycenter(UnitType.WORKER);
                away.x = 2*uc.getLocation().x - away.x;
                away.y = 2*uc.getLocation().y - away.y;
                if (!away.isEqual(uc.getLocation()) && uc.isAccessible(away)) {
                    tools.MoveTo(away);
                } else uc.move(tools.GeneralDir(tools.RandomDir()));
            }
        }
    }

    void gatherVP() {
        tools.GatherVP();
    };

    void buyVP() {
        if (data.overflowingEconomy || uc.getRound() > 1800) {
            if (uc.canBuyVP(1)) uc.buyVP(1);
        }
    }

    void buildBarracks() {
        //Choose direction to avoid trees
        Direction[] allowedDir = data.dirs;
        TreeInfo[] nearbyTrees = uc.senseTrees(2);
        for (TreeInfo tree : nearbyTrees) {
            for (int i = 0; i < 8; i++) {
                Direction dir = allowedDir[i];
                if (dir != null) {
                    Location loc = uc.getLocation().add(dir);
                    if (tree.location.isEqual(loc) || !uc.isAccessible(loc)) allowedDir[i] = null;
                }
            }
        }
        Direction chosenDir = null;
        for (Direction dir : allowedDir) {
            if (dir != null) {
                chosenDir = dir;
                break;
            }
        }
        //Spawn in said direction
        if (uc.canSpawn(chosenDir, UnitType.BARRACKS)) {
            uc.spawn(chosenDir, UnitType.BARRACKS);
        }
    }

    void spawnWorker() {
        Direction randomDir = tools.RandomDir();
        if (uc.canSpawn(randomDir, UnitType.WORKER)) {
            uc.spawn(randomDir, UnitType.WORKER);
            uc.write(data.workerCh, uc.read(data.workerCh) + 1);
        }
    }

    void buildEconomy() {
        //Only spawn new units in light pop density areas to maximize expansion
        if (tools.MatesAround(GameConstants.WORKER_SIGHT_RANGE_SQUARED, UnitType.WORKER) < 4) {
            //Create workers to keep production
            if ((data.nTrees > 6 * data.nWorker - 1 || data.growthEconomy)
                    && (double) data.nSetWorker / (double) data.nWorker > 0.8
                    && uc.getRound() < 1800) {
                spawnWorker();
            }
            //Make barracks according to economy data
            if (data.stableEconomy && data.nBarracks <= data.nUnits / 10) {
                buildBarracks();
            }
        }
    }

}
