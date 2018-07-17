package betav1;

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

        //if (data.loneWorker) uc.println("I'm alone");
        if (data.setWorker) uc.println("I'm set");

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
        // Reset Next Slot
        uc.write(data.unitResetCh, 0);
        uc.write(data.workerResetCh, 0);
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
                //Direction dirToTree = uc.getLocation().directionTo(nearestOak.location);
                //uc.move(tools.GeneralDir(dirToTree));
            } else {
                data.loneWorker = true;
            }
        } else if (!data.loneWorker) {
            if (tools.MatesAround(4, UnitType.WORKER) == 0) {
                data.loneWorker = true;
            } else {
                Location away = tools.Barycenter(UnitType.WORKER);
                away.x = 2*uc.getLocation().x - away.x;
                away.y = 2*uc.getLocation().y - away.y;
                if (away != uc.getLocation()) {
                    tools.MoveTo(away);
                } else uc.move(tools.GeneralDir(tools.RandomDir()));
            }
        }
    }

    //TODO
    void gatherVP() {

    };

    void buildEconomy() {
        //Create workers to keep production
        if ((data.nTrees > 6*data.nWorker-1 || data.growthEconomy) && (double)data.nSetWorker/(double)data.nWorker > 0.9 ) {
            Direction randomDir = tools.RandomDir();
            if (uc.canSpawn(randomDir, UnitType.WORKER)) {
                uc.spawn(randomDir, UnitType.WORKER);
                uc.write(data.workerCh, uc.read(data.workerCh) + 1);
            }
        }
        //Make barracks according to economy data
        if (data.stableEconomy && data.nBarracks <= data.nUnits/10) {
            TreeInfo[] adjacentTrees = uc.senseTrees(2);
            int randIndex = (int) (Math.random()*8);
            if (uc.canSpawn(data.dirs[randIndex], UnitType.BARRACKS)) {
                uc.spawn(data.dirs[randIndex], UnitType.BARRACKS);
                uc.write(data.barracksCh, data.nBarracks + 1);
            }
        }
    }

}
