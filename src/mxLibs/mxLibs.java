package mxLibs;

import arc.Events;
import mindustry.game.EventType;
import mindustry.gen.EntityMapping;
import mindustry.mod.ClassMap;
import mindustry.mod.Mod;
import mindustry.mod.Mods;
import mxLibs.type.status.ExtentionStatus;
import mxLibs.type.unit.AircraftUnitEntity;
import mxLibs.world.blocks.power.PowerTower;
import mxLibs.world.blocks.production.MultiCrafter;

import java.lang.reflect.Field;

import static mindustry.Vars.mods;

public class mxLibs extends Mod {

    public static final ExtendedParser parser = new ExtendedParser();
    public static int[] entityIds;

    public mxLibs() {
        entityIds = new int[16];
        entityIds[0] = EntityMapping.register("AircraftUnitEntity", AircraftUnitEntity::new);
        Events.on(EventType.ContentInitEvent.class, (c) -> {
            ResearchObjectives.load();
        });
    }

    @Override
    public void loadContent() {
        putClasses();
        var modClass = Mods.class;
        try {
            Field field = modClass.getDeclaredField("parser");
            field.setAccessible(true);
            field.set(mods, parser);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //TestUnits.load();
        //TestBlocks.load();


    }

    public void putClasses() {
        //Blocks
        ClassMap.classes.put("MultiCrafter", MultiCrafter.class);
        ClassMap.classes.put("PowerTower", PowerTower.class);
        //Status
        ClassMap.classes.put("ExtentionStatus", ExtentionStatus.class);

    }
}
