package betav4;

import aic2018.Location;
import aic2018.UnitController;
import aic2018.UnitInfo;

public class Warrior extends AttackUnit {

    public Warrior(UnitController _uc) {
        super(_uc);
    }

    @Override
    void run() {

        //Update our info according to the Comm Channel
        data.Update();

        //Try to attack before movement
        attack();
        counter();
        gatherVP();

        //Movement
        move();

        //Try to attack after movement
        attack();
        counter();
        attackOak();
        gatherVP();

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
        uc.write(data.warriorReportCh, uc.read(data.warriorReportCh)+1);
        // Reset Next Slot
        uc.write(data.unitResetCh, 0);
        uc.write(data.warriorResetCh, 0);
    }

    @Override
    void move() {
        Location target;
        if (uc.getRound() < 700 && !data.enemyFound) {
            Location enemyBase = tools.Barycenter(data.enemyBases);
            Location allyBase = new Location(data.workerX/data.nWorker,data.workerY/data.nWorker);
            target = tools.BarCoord(new Location[] {enemyBase, allyBase}, new int[] {1,3});
        } else {
            target = tools.Decrypt(data.enemyLoc);
        }
        tools.MoveTo(target);
    }

    void counter() {
        //TODO: improve counter logic
        //Counter the enemy with the most health that can attack me
        UnitInfo[] enemies = uc.senseUnits(data.enemyTeam);
        int maxEnemyHealth = 0;
        UnitInfo target = null;

        for(UnitInfo enemy : enemies){
            if(enemy.getHealth() > maxEnemyHealth && uc.canUseActiveAbility(enemy.getLocation())){
                maxEnemyHealth = enemy.getHealth();
                target = enemy;
            }
        }
        if (target != null) uc.useActiveAbility(target.getLocation());
    }

}
