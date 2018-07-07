package alpha;

import aic2018.*;

public class UsefulInfo {

    UnitController uc;
    UnitType types[];
    int nWorkerIndex;
    int nBarracksIndex;
    int nWarriorIndex;
    int nArcherIndex;
    int nKnightIndex;
    int nBallistaIndex;
    int nWorker;
    int nBarracks;
    int nWarrior;
    int nArcher;
    int nKnight;
    int nBallista;
    Team ally;
    Team enemy;
    Direction[] dirs;

    public UsefulInfo(UnitController _uc) {

        uc = _uc;
        types = UnitType.values();
        nWorkerIndex = UnitType.WORKER.ordinal();
        nBarracksIndex = UnitType.BARRACKS.ordinal();
        nWarriorIndex = UnitType.WARRIOR.ordinal();
        nArcherIndex = UnitType.ARCHER.ordinal();
        nKnightIndex = UnitType.KNIGHT.ordinal();
        nBallistaIndex = UnitType.BALLISTA.ordinal();
        nWorker = uc.read(nWorkerIndex);
        nBarracks = uc.read(nBarracksIndex);
        nWarrior = uc.read(nWarriorIndex);
        nArcher = uc.read(nArcherIndex);
        nKnight = uc.read(nKnightIndex);
        nBallista = uc.read(nBallistaIndex);
        ally = uc.getTeam();
        enemy = uc.getOpponent();
        dirs = Direction.values();
    }

}
