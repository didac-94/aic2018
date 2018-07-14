package padrosalpha;

import aic2018.*;

public class Data {

    UnitController uc;
    UnitType types[];
    int nWorkerChannel;
    int nBarracksChannel;
    int nWarriorChannel = 2;
    int nArcherChannel;
    int nKnightChannel;
    int nBallistaChannel;
    int nTreesChannel = 6;
    int mainstreamChannel = 7;
    int touchedChannel = 8;
    int alphaChannel = 9;
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
    boolean richEconomy;

    final int treeCap = 55;
    final int NEAR_RADIUS = 4;
    final int ADJ_RADIUS = 2;

    int DEFAULT_ALPHA_CODE = 1;
    int DEFEND_ALPHA_CODE = 2;
    int ATTACK_ALPHA_CODE = 3;
    int SEARCHING_ALPHA_CODE = 4;
    double DEFAULT_ALPHA = 0.333;
    // TODO: double ALPHA_INCREASING = 0.333 + uc.getRound()/1000 (best if pseudo-exponential);
    double DEFEND_ALPHA = 0.001;
    double ATTACK_ALPHA = 0.999;
    double SEARCHING_ALPHA = 0.70;

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

        stableEconomy = (uc.getResources() > 750);
        richEconomy = (uc.getResources() > 1500);
        nWorker = uc.read(nWorkerChannel);
        nBarracks = uc.read(nBarracksChannel);
        nWarrior = uc.read(nWarriorChannel);
        nArcher = uc.read(nArcherChannel);
        nKnight = uc.read(nKnightChannel);
        nBallista = uc.read(nBallistaChannel);
        nTrees = uc.read(nTreesChannel);

    }

}
