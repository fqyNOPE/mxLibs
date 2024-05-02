package mxLibs.entities.abilities;

import arc.Core;
import arc.Events;
import arc.graphics.g2d.Draw;
import arc.math.Angles;
import arc.util.Time;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.abilities.Ability;
import mindustry.game.EventType;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.type.UnitType;
import mxLibs.type.unit.Aircraft;

import static mindustry.Vars.state;

public class AircraftAbility extends Ability {
    public UnitType unitType;
    public Unit aircraft;
    public float spawnTime = 300f, spawnX = 0f, spawnY = 0f;
    public Effect spawnEffect = Fx.spawn;
    protected float spawnTimer;

    public AircraftAbility(UnitType unitType, float spawnTime, float spawnX, float spawnY) {
        this.unitType = unitType;
        this.spawnTime = spawnTime;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
    }

    @Override
    public void update(Unit unit) {
        if ((aircraft == null || aircraft.dead)) {
            spawnTimer += Time.delta * state.rules.unitBuildSpeed(unit.team);

            if (spawnTimer >= spawnTime && Units.canCreate(unit.team, this.unitType)) {
                float x = unit.x + Angles.trnsx(unit.rotation, spawnY, spawnX), y = unit.y + Angles.trnsy(unit.rotation, spawnY, spawnX);
                spawnEffect.at(x, y, 0f, unit);

                Unit u = this.unitType.create(unit.team);
                u.set(x, y);
                u.rotation = unit.rotation;

                if (u instanceof Aircraft ae) {
                    ae.setBaseUnit(unit, this);
                }
                aircraft = u;
                Events.fire(new EventType.UnitCreateEvent(u, null, unit));
                if (!Vars.net.client()) {
                    u.add();
                }

                spawnTimer = 0f;
            }
        }
    }


    @Override
    public void draw(Unit unit) {
        float x = unit.x + Angles.trnsx(unit.rotation, spawnY, spawnX), y = unit.y + Angles.trnsy(unit.rotation, spawnY, spawnX);
        if ((aircraft == null || aircraft.dead)) {
            if (Units.canCreate(unit.team, this.unitType)) {
                Draw.draw(Draw.z(), () -> {
                    Drawf.construct(x, y, this.unitType.fullIcon, unit.rotation - 90, spawnTimer / spawnTime, 1f, spawnTimer);
                });
            }
        } else {
            if ((aircraft instanceof Aircraft ae) && ae.nearBaseUnit()) {
                Draw.rect(unitType.fullIcon, x, y, unit.rotation - 90);
            }
        }

    }

    @Override
    public String localized() {
        return Core.bundle.format("ability.aircraftSpawn", unitType.localizedName);
    }
}
