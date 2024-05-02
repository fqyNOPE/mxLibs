package mxLibs.type.unit;

import mindustry.gen.Unit;
import mxLibs.entities.abilities.AircraftAbility;

public interface Aircraft {
    Unit baseUnit();

    boolean nearBaseUnit();

    boolean reloading();

    boolean closeToBaseUnit(float dis);

    void setBaseUnit(Unit unit, AircraftAbility ability);

    int getAmmoAmount();

    void useAmmo(int amount);


}
