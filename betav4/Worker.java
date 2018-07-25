package betav4;

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

        //Update info according to the Comm Channel
        data.Update();

        //Movement
        move();

        //Check status
        checkStatus();

        //Plant trees if necessary
        plant();

        //Harvest wood
        chop();

        //Gather VPs if available
        gatherVP();

        //Build barracks or create workers
        buildEconomy();

        //Buy VPs if too much money or near the end
        buyVP();

        //Report to Comm Channel
        report();

        if (data.loneWorker) uc.println("I'm alone");
        if (data.setWorker) uc.println("I'm set");

    }

    void checkStatus() {
        //Check if alone
        if (!data.loneWorker) {
            if (tools.MatesAround(4, UnitType.WORKER) == 0) {
                data.loneWorker = true;
            }
        }
        //Check if set
        TreeInfo[] nearbyTrees = uc.senseTrees(2);
        if (tools.Adjacent(nearbyTrees) > 0) {
            boolean healthyOakFound = false;
            for (TreeInfo tree : nearbyTrees) {
                if (tools.AreAdjacent(uc.getLocation(), tree.getLocation())) {
                    if (tree.isOak() && tree.getHealth() > 500) {
                        healthyOakFound = true;
                    }
                }
            }
            if (healthyOakFound) {
                uc.write(data.setWorkerReportCh, uc.read(data.setWorkerReportCh)+1);
                uc.write(data.setWorkerResetCh, 0);
                data.setWorker = true;
            } else {
                data.setWorker = false;
            }
        }
    }

    void report() {
        reportMyself();
        reportTrees();
        reportEnemies();
        reportEnemyBases();
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

    void reportTrees() {
        if (data.loneWorker) {
            TreeInfo[] nearbyTrees = uc.senseTrees(2);
            int nTreesAround = tools.Adjacent(nearbyTrees);
            uc.write(data.treeReportCh, uc.read(data.treeReportCh)+nTreesAround);
            uc.write(data.treeResetCh, 0);
        }
    }

    void reportEnemies() {
        if (!data.enemyContact) {
            //Check if there has been direct contact with the enemy
            UnitInfo[] nearbyEnemies = uc.senseUnits(2, data.enemyTeam);
            if (tools.Adjacent(nearbyEnemies) > 0) {
                uc.write(data.enemyContactCh, 1);
                data.enemyContact = true;
            }
        } else {
            //Find targets for the army
            UnitInfo[] enemiesAround = uc.senseUnits(data.enemyTeam);
            if (enemiesAround.length > 0) {
                //If there's enemies around and no enemy was reported then report an enemy
                Location enemyLoc = enemiesAround[0].getLocation();
                int encryptedLoc = tools.Encrypt(enemyLoc.x, enemyLoc.y);
                uc.write(data.enemyFoundCh, 1);
                uc.write(data.enemyLocCh, encryptedLoc);
            } else {
                //If no enemy is around the reported enemy location then reset it
                if (data.enemyFound) {
                    Location enemyLoc = tools.Decrypt(data.enemyLoc);
                    if (uc.canSenseLocation(enemyLoc)) {
                        uc.write(data.enemyFoundCh, 0);
                        uc.write(data.enemyLocCh, -1);
                    }
                }
            }
        }
    }

    void reportEnemyBases() {
        for (int i = data.firstEnemyBase; i < data.nEnemyBases; i++) {
            if (uc.canSenseLocation(data.enemyBases[i])) {
                if (uc.senseUnits(data.enemyTeam).length == 0) {
                    uc.write(data.enemyBase0Ch + i, 1);
                }
            }
        }
    }

    void chop() {
        TreeInfo[] myTrees = uc.senseTrees(2);
        for (TreeInfo tree : myTrees) {
            if (tools.CanChop(tree)) uc.attack(tree);
        }
    }

    void plant() {
        if (data.loneWorker && !data.setWorker) {
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
        if (!data.loneWorker && !data.setWorker) {
            if (data.turnsAlive < 10) {
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
                }
            } else if (!data.loneWorker) {
                Location away = tools.Barycenter(UnitType.WORKER);
                away.x = 2*uc.getLocation().x - away.x;
                away.y = 2*uc.getLocation().y - away.y;
                if (!away.isEqual(uc.getLocation()) && uc.isAccessible(away)) {
                    tools.MoveTo(away);
                } else uc.move(tools.GeneralDir(tools.RandomDir()));
            }
        }


        //Early  in the life of the unit look for nearby oaks
        /*if (data.turnsAlive < 10 && !data.loneWorker) {
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
            }
        } else if (!data.loneWorker) {  //If not isolated try to flee friendly workers
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
        }*/
    }

    void gatherVP() {
        tools.GatherVP();
    }

    void buyVP() {
        if (data.overflowingEconomy || uc.getRound() > 1800) {
            if (uc.canBuyVP(1)) uc.buyVP(1);
        }
        if (uc.canBuyVP(2000-data.VP)) uc.buyVP(2000-data.VP);
    }

    void buildBarracks() {
        TreeInfo[] nearbyTrees = uc.senseTrees(2);
        for (Direction dir : data.dirs) {
            Location target = uc.getLocation().add(dir);
            Boolean emptyLoc = true;
            for(TreeInfo tree : nearbyTrees){
                if(target.isEqual(tree.location)) {
                    emptyLoc = false;
                    break;
                }
            }
            if (emptyLoc && uc.canSpawn(dir, UnitType.BARRACKS ) ) {
                uc.spawn(dir, UnitType.BARRACKS);
                uc.write(data.barracksCh, uc.read(data.barracksCh)+1);
                uc.write(data.barracksReportCh, uc.read(data.barracksReportCh)+1);
                uc.write(data.barracksResetCh, uc.read(data.barracksResetCh)+1);

            }
        }
    }

    void spawnWorker() {
        Direction randomDir = tools.GeneralDir(tools.RandomDir());
        if (uc.canSpawn(randomDir, UnitType.WORKER)) {
            uc.spawn(randomDir, UnitType.WORKER);
            uc.write(data.workerCh, uc.read(data.workerCh) + 1);
        }
    }

    void buildEconomy() {
        //First barracks as soon as possible
        if (data.nBarracks < 2) {
            buildBarracks();
        }

        //This doesn't work as intended
        /*if (data.nWorker == data.nSetWorker) {
            spawnWorker();
        }*/

        //Only spawn new units in low pop density areas to maximize expansion
        if (tools.MatesAround(GameConstants.WORKER_SIGHT_RANGE_SQUARED, UnitType.WORKER) < 4) {
            //Create workers to keep a steady production
            if (/*(data.nTrees > 6 * data.nWorker - 1 || data.growthEconomy)
                    &&*/ (double) data.nSetWorker / (double) data.nWorker > 0.8
                    && uc.getRound() < 1800) {
                spawnWorker();
            }
            //Make barracks according to economy data
            if (data.enemyFound && data.stableEconomy && data.nBarracks <= data.nUnits / 15) {
                buildBarracks();
            }
        }
    }

}
