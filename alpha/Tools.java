package alpha;

import aic2018.*;

public class Tools {

    Data data;

    public Tools(Data _data) {
        data = _data;
    }

    public Direction RandomDir() {
        int randomNum = (int)(Math.random()*8);
        return data.dirs[randomNum];
    }

    public boolean CanChop(TreeInfo tree) {
        return data.uc.canAttack(tree)
                && tree.remainingGrowthTurns == 0
                && tree.health > 12
                && NobodyAt(tree.location);
    }

    public boolean NobodyAt(Location loc) {
        return data.uc.isAccessible(loc)
                && data.uc.getLocation() != loc;
    }

}
