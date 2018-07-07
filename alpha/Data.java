package alpha;

import aic2018.*;

public class Data {

    UnitController uc;
    UnitType types[];
    int nWorkerChannel;
    int nBarracksChannel;
    int nWarriorChannel;
    int nArcherChannel;
    int nKnightChannel;
    int nBallistaChannel;
    int nTreesChannel = 6;
    int mainstreamChannel = 7;
    int touchedChannel = 8;
    int nWorker;
    int nBarracks;
    int nWarrior;
    int nArcher;
    int nKnight;
    int nBallista;
    int nTrees;
    Team ally;
    Team enemy;
    Direction[] dirs;
    boolean stableEconomy;

    final int NEAR_RADIUS = 2;
    final int MIN_TO_HEALTHY = GameConstants.SMALL_TREE_CHOPPING_DMG;

    public Data(){}

    public Data(UnitController _uc) {

        uc = _uc;
        types = UnitType.values();
        nWorkerChannel = UnitType.WORKER.ordinal();
        nBarracksChannel = UnitType.BARRACKS.ordinal();
        nWarriorChannel = UnitType.WARRIOR.ordinal();
        nArcherChannel = UnitType.ARCHER.ordinal();
        nKnightChannel = UnitType.KNIGHT.ordinal();
        nBallistaChannel = UnitType.BALLISTA.ordinal();
        ally = uc.getTeam();
        enemy = uc.getOpponent();
        dirs = Direction.values();
    }

    public void Update() {

        stableEconomy = (uc.getResources() > 1000);
        nWorker = uc.read(nWorkerChannel);
        nBarracks = uc.read(nBarracksChannel);
        nWarrior = uc.read(nWarriorChannel);
        nArcher = uc.read(nArcherChannel);
        nKnight = uc.read(nKnightChannel);
        nBallista = uc.read(nBallistaChannel);
        nTrees = uc.read(nTreesChannel);

    }

}
