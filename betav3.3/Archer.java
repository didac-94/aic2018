package betav3.3;

import aic2018.UnitController;

public class Archer extends AttackUnit {

    public Archer(UnitController _uc) {
        super(_uc);
    }

    @Override
    void run() {

        //Update our info according to the Comm Channel
        data.Update();

        //Try to attack before movement
        attack();

        //Movement
        move();

        //Try to attack after movement
        attack();
        attackOak();

        //Report intel to the Comm Channel
        report();
    }

    @Override
    void report() {
        reportMyself();
        reportEnemies();
        reportEnemyBases();
    }

    @Override
    void reportMyself() {
        // Report to the Comm Channel
        uc.write(data.unitReportCh, uc.read(data.unitReportCh)+1);
        uc.write(data.archerReportCh, uc.read(data.archerReportCh)+1);
        // Reset Next Slot
        uc.write(data.unitResetCh, 0);
        uc.write(data.archerResetCh, 0);
    }

}
