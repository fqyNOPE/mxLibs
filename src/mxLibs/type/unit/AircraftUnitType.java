package mxLibs.type.unit;

import mindustry.type.UnitType;
import mxLibs.ai.types.AircraftController;

public class AircraftUnitType extends UnitType {

    public float drawSize = 0f;

    public AircraftUnitType(String name) {
        super(name);
        aiController = AircraftController::new;
        constructor = AircraftUnitEntity::create;
        payloadCapacity = 0f;
        logicControllable = false;
        playerControllable = false;
        allowedInPayloads = false;
        useUnitCap = false;
        hitSize = 0.1f;
    }

}
