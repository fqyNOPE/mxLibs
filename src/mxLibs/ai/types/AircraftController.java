package mxLibs.ai.types;

import arc.math.Angles;
import arc.util.Log;
import mindustry.entities.units.AIController;
import mindustry.gen.Unit;
import mxLibs.type.unit.Aircraft;

public class AircraftController extends AIController {

    public boolean moveTowards = false;

    @Override
    public void updateMovement() {
        if (target != null && unit.hasWeapons() && unit instanceof Aircraft ae && !ae.reloading() && ae.getAmmoAmount() > 0) {
            if (target instanceof Unit u && u.isFlying()) {
                Log.info("a");
                circleAttackAir(u.hitSize * 2f);
            } else {
                circleAttackGround(target instanceof Unit u ? u.hitSize * 2f : 120f);
            }
        } else {
            moveToBase();
        }
    }

    public void moveToBase() {
        if (unit instanceof Aircraft ae) {
            Unit baseUnit = ae.baseUnit();
            boolean arrive = ae.closeToBaseUnit(baseUnit.hitSize * 0.2f);
            vec.set(baseUnit).sub(unit);
            vec.setLength(arrive ? unit.speed() : unit.speed() + baseUnit.hitSize);
            if (!unit.type.omniMovement && unit.type.rotateMoveFirst) {
                float angle = vec.angle();
                unit.lookAt(angle);
                if (Angles.within(unit.rotation, angle, 3f)) {
                    unit.movePref(vec);
                }
            } else {
                unit.movePref(vec);
            }
        }

    }


    @Override
    public void updateTargeting() {
        if (unit.hasWeapons()) {
            updateWeapons();
            if (unit instanceof Aircraft ae) {
                for (var mount : unit.mounts) {
                    if (mount.reload - mount.weapon.reload >= -0.1f) {
                        ae.useAmmo(1);
                    }
                }
            }

        }
    }


    public void circleAttackGround(float circleLength) {
        if (unit instanceof Aircraft ae) {
            vec.set(target).sub(unit);

            float ang = unit.angleTo(target);
            float diff = Angles.angleDist(ang, unit.rotation());

            if (diff > 70f && vec.len() < circleLength) {
                vec.setAngle(unit.vel().angle());
            } else {
                vec.setAngle(Angles.moveToward(unit.vel().angle(), vec.angle(), 6f));
            }
            vec.setLength(unit.speed());
            unit.moveAt(vec);


        }

    }

    public void circleAttackAir(float circleLength) {
        if (unit instanceof Aircraft ae) {
            if (!target.within(unit.x, unit.y, circleLength)) {
                vec.set(target).sub(unit);
                vec.trns(15f, unit.x, unit.y);
            } else {
                vec.set(unit).sub(target);
            }
            vec.setLength(unit.speed());
            unit.moveAt(vec);
        }

    }
}
