package mxLibs.type.status;

import arc.util.Log;
import arc.util.Time;
import mindustry.gen.Unit;
import mindustry.type.StatusEffect;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

public class ExtentionStatus extends StatusEffect {

    public int armor = 10;

    public ExtentionStatus(String name) {
        super(name);
    }

    @Override
    public void update(Unit unit, float time) {
        super.update(unit, time);
        if (time - Time.delta <= 0f) {
            unit.armor -= armor;//Maybe seldom cause problems,not sure.
        }
        Log.info(unit.armor);
    }

    @Override
    public void setStats() {
        if (armor > 0) stats.add(Stat.armor, armor, StatUnit.perSecond);
        super.setStats();
    }

    @Override
    public void applied(Unit unit, float time, boolean extend) {
        super.applied(unit, time, extend);
        if (!extend) unit.armor += armor;
    }
}
