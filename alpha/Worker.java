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

        //Move towards the healthiest tree in sight
        TreeInfo visibleTrees[] = uc.senseTrees();
        if (visibleTrees.length > 0) {
            int healthiestTree = 0;
            for (int i = 0; i < visibleTrees.length; i++) {
                TreeInfo tree = visibleTrees[i];
                //TODO: si son iguals (o casi) ens quedem el mes proper
                if (tree.remainingGrowthTurns > 0) continue;
                if (tree.health > visibleTrees[healthiestTree].health) healthiestTree = i;
            }
            TreeInfo hT = visibleTrees[healthiestTree];
            Location htPos = visibleTrees[healthiestTree].location;
            Direction htDir = uc.getLocation().directionTo(htPos);
            int distToHt = uc.getLocation().distanceSquared(htPos);
            if (distToHt > 2) {
                if (uc.canMove(htDir)) uc.move(htDir);
            } else {
                if (tools.CanChop(hT)) uc.attack(hT);
            }
        } else {
            Direction randomDir = tools.RandomDir();
            if (uc.canMove(randomDir)) {
                uc.move(randomDir);
            }
        }


        //Code for petit tree creation

        //Code for wood chopping

    }

}
