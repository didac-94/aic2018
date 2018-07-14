package betav2;

import aic2018.*;

public class Data {

    UnitController uc;

    // Comm Channels
    int UnitsCh;                // Ch 0, 1, 2
    int unitReportCh;           // Ch 0, 1, 2
    int unitResetCh;            // Ch 0, 1, 2
    int workerCh;               // Ch 3, 4, 5
    int workerReportCh;         // Ch 3, 4, 5
    int workerResetCh;          // Ch 3, 4, 5
    int barracksCh;             // Ch 6, 7, 8
    int barracksReportCh;       // Ch 6, 7, 8
    int barracksResetCh;        // Ch 6, 7, 8
    int warriorCh;              // Ch 9, 10, 11
    int warriorReportCh;        // Ch 9, 10, 11
    int warriorResetCh;         // Ch 9, 10, 11
    int archerCh;               // Ch 12, 13, 14
    int archerReportCh;         // Ch 12, 13, 14
    int archerResetCh;          // Ch 12, 13, 14
    int knightCh;               // Ch 15, 16, 17
    int knightReportCh;         // Ch 15, 16, 17
    int knightResetCh;          // Ch 15, 16, 17
    int ballistaCh;             // Ch 18, 19, 20
    int ballistaReportCh;       // Ch 18, 19, 20
    int ballistaResetCh;        // Ch 18, 19, 20
    int plantedTreesCh = 21;    // Ch 21
    int mainstreamCh = 22;      // Ch 22
    int touchedCh = 23;         // Ch 23

    // Comm Info
    int nUnits;
    int nWorker;
    int nBarracks;
    int nWarrior;
    int nArcher;
    int nKnight;
    int nBallista;
    int nPlantedTrees;


    Team ally;
    Team enemy;
    Direction[] dirs;
    UnitType[] types;
    int currentRound;
    boolean stableEconomy;

    final int treeCap = 55;
    final int NEAR_RADIUS = 2;
    final int MIN_TREE_HEALTH = GameConstants.SMALL_TREE_CHOPPING_DMG;

    public Data(){}

    public Data(UnitController _uc) {

        uc = _uc;
        ally = uc.getTeam();
        enemy = uc.getOpponent();
        dirs = Direction.values();
        types = UnitType.values();
        currentRound = uc.getRound();
    }

    public void Update() {

        stableEconomy = (uc.getResources() > 500);
        currentRound = uc.getRound();

        // Update Comm Channels
        int x = currentRound%3;
        int y = (currentRound+1)%3;
        int z = (currentRound+2)%3;
        unitReportCh = x;
        unitResetCh = y;
        UnitsCh = z;
        workerReportCh = 3 + x;
        workerResetCh = 3 + y;
        workerCh = 3 + z;
        barracksReportCh = 6 + x;
        barracksResetCh = 6 + y;
        barracksCh = 6 + z;
        warriorReportCh = 9 + x;
        warriorResetCh = 9 + y;
        warriorCh = 9 + x;
        archerReportCh = 12 + x;
        archerResetCh = 12 + y;
        archerCh = 12 + z;
        knightReportCh = 15 + x;
        knightResetCh = 15 + y;
        knightCh = 15 + z;
        ballistaReportCh = 18 + x;
        ballistaResetCh = 18 + y;
        ballistaCh = 18 + z;

        // Fetch Comm Info
        nUnits = uc.read(UnitsCh);
        nWorker = uc.read(workerCh);
        nBarracks = uc.read(barracksCh);
        nWarrior = uc.read(warriorCh);
        nArcher = uc.read(archerCh);
        nKnight = uc.read(knightCh);
        nBallista = uc.read(ballistaCh);
        nPlantedTrees = uc.read(plantedTreesCh);

    }

}
