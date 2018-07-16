package betav2;

import aic2018.*;

public class Barracks {

    UnitController uc;
    Data data;
    Tools tools;


    public Barracks(UnitController _uc) {
        uc = _uc;
        data = new Data(uc);
        tools = new Tools(data);
    }

    public void run() {

        //Update our info according to the comm channel
        data.Update();

        //Report myself
        reportMyself();

        //Spawn units
        recruit();

    }

    void reportMyself() {
        // Report to the Comm Channel
        uc.write(data.barracksReportCh, uc.read(data.barracksReportCh)+1);
        // Reset Next Slot
        uc.write(data.barracksResetCh, 0);
    }

    void recruit() {
        //TODO: intentar triar una bona primera direcció
        for (int i = 0; i < 8; ++i) {
            //TODO: buscar una millor condició
            if (data.stableEconomy) {
                //TODO: buscar una millor composició
                if(Math.random() < 1/3){
                    if (uc.canSpawn(data.dirs[i], UnitType.KNIGHT)) {
                        uc.spawn(data.dirs[i], UnitType.KNIGHT);
                    }
                } else {
                    if (uc.canSpawn(data.dirs[i], UnitType.WARRIOR)) {
                        uc.spawn(data.dirs[i], UnitType.WARRIOR);
                    }
                }
            }
        }
    }

}
