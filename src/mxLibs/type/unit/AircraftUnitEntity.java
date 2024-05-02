package mxLibs.type.unit;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Fx;
import mindustry.content.UnitTypes;
import mindustry.entities.abilities.Ability;
import mindustry.entities.units.StatusEntry;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.gen.UnitEntity;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.io.TypeIO;
import mxLibs.entities.abilities.AircraftAbility;

import static mindustry.Vars.tilesize;
import static mxLibs.mxLibs.entityIds;

public class AircraftUnitEntity extends UnitEntity implements Aircraft {


    public AircraftAbility ability;
    public Unit baseUnit;
    public int baseUnitId = -1;
    public int maxAmmoAmount = 10;
    public int ammoAmount = 0;
    public float reloadTime = 30f;
    public boolean shown = true;
    public boolean nearBaseUnit = true;
    public boolean reloading = true;
    protected float reloadTimer = 0f;

    public AircraftUnitEntity() {

    }


    @Override
    public int classId() {
        return entityIds[0];
    }

    public boolean closeToBaseUnit(float dis) {
        float spawnX = ability.spawnX, spawnY = ability.spawnY;
        float x = baseUnit.x + Angles.trnsx(baseUnit.rotation, spawnY, spawnX), y = baseUnit.y + Angles.trnsy(baseUnit.rotation, spawnY, spawnX);
        return this.within(x, y, ((AircraftUnitType) this.type).drawSize * 1.2f - dis);
    }


    public static AircraftUnitEntity create() {
        return new AircraftUnitEntity();
    }

    @Override
    public void update() {
        if (baseUnitId != -1) {
            baseUnit = Groups.unit.getByID(baseUnitId);
            baseUnitId = -1;
            if (baseUnit == null) {
                this.kill();
                return;
            }
            int i = 0;
            for (Ability a : baseUnit.abilities) {
                if (a.data == ability.data) {
                    ((AircraftAbility) baseUnit.abilities[i]).aircraft = this;
                    break;
                }
                i++;
            }
        }
        if (baseUnit == null) {
            this.kill();
            return;
        }
        float spawnX = ability.spawnX, spawnY = ability.spawnY;
        float x = baseUnit.x + Angles.trnsx(baseUnit.rotation, spawnY, spawnX), y = baseUnit.y + Angles.trnsy(baseUnit.rotation, spawnY, spawnX);
        if (this.within(x, y, ((AircraftUnitType) this.type).drawSize * 1.2f)) {
            if (shown) {
                shown = false;
                Fx.spawn.at(x, y);
                Fx.spawn.at(this.x, this.y);
            }

            nearBaseUnit = true;

            if (ammoAmount < maxAmmoAmount) {
                reloadTimer += Time.delta;
                if (reloadTimer >= reloadTime) {
                    reloadTimer = 0f;
                    ammoAmount++;
                }
                reloading = true;
            } else {
                reloading = false;
            }
        } else {
            shown = true;
            nearBaseUnit = false;

        }
        super.update();
    }

    public boolean reloading() {
        return reloading;
    }

    public Unit baseUnit() {
        return baseUnit;
    }

    public boolean nearBaseUnit() {
        return nearBaseUnit;
    }

    public void setBaseUnit(Unit unit, AircraftAbility ability) {
        baseUnit = unit;
        this.ability = ability;
    }

    public int getAmmoAmount() {
        return ammoAmount;
    }

    ;


    public void useAmmo(int amount) {
        ammoAmount -= amount;
    }

    @Override
    public boolean canShoot() {
        return ammoAmount > 0 && !nearBaseUnit;
    }

    @Override
    public void draw() {
        builder:
        {

            drawBuilding();
        }
        draw:
        {

        }
        miner:
        {

            if (!mining()) break miner;
            float focusLen = hitSize / 2.0F + Mathf.absin(Time.time, 1.1F, 0.5F);
            float swingScl = 12.0F;
            float swingMag = tilesize / 8.0F;
            float flashScl = 0.3F;
            float px = x + Angles.trnsx(rotation, focusLen);
            float py = y + Angles.trnsy(rotation, focusLen);
            float ex = mineTile.worldx() + Mathf.sin(Time.time + 48, swingScl, swingMag);
            float ey = mineTile.worldy() + Mathf.sin(Time.time + 48, swingScl + 2.0F, swingMag);
            Draw.z(Layer.flyingUnit + 0.1F);
            Draw.color(Color.lightGray, Color.white, 1.0F - flashScl + Mathf.absin(Time.time, 0.5F, flashScl));
            Drawf.laser(Core.atlas.find("minelaser"), Core.atlas.find("minelaser-end"), px, py, ex, ey, 0.75F);
            if (isLocal()) {
                Lines.stroke(1.0F, Pal.accent);
                Lines.poly(mineTile.worldx(), mineTile.worldy(), 4, tilesize / 2.0F * Mathf.sqrt2, Time.time);
            }
            Draw.color();
        }
        status:
        {
            if (this.shown) {
                for (StatusEntry e : statuses) {
                    e.effect.draw(this, e.time);
                }
            }
        }
        unit:
        {
            if (this.shown) {
                type.draw(this);
            }
        }
    }

    @Override
    public void read(Reads read) {
        super.read(read);
        Ability[] ab = new Ability[1];
        ab[0] = new AircraftAbility(UnitTypes.alpha, 0f, 0f, 0f);
        ab = TypeIO.readAbilities(read, ab);
        ability = (AircraftAbility) ab[0];
        baseUnitId = read.i();
        ammoAmount = read.i();
    }

    @Override
    public void write(Writes write) {
        super.write(write);
        TypeIO.writeAbilities(write, new Ability[]{ability});
        write.i(baseUnit == null ? -1 : baseUnit.id);
        write.i(ammoAmount);
    }
}
