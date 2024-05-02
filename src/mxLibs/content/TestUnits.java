package mxLibs.content;

import arc.graphics.Color;
import arc.math.geom.Rect;
import mindustry.content.Fx;
import mindustry.entities.bullet.ArtilleryBulletType;
import mindustry.entities.bullet.LaserBulletType;
import mindustry.gen.Sounds;
import mindustry.gen.TankUnit;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.type.unit.TankUnitType;
import mxLibs.entities.abilities.AircraftAbility;
import mxLibs.type.unit.AircraftUnitType;

public class TestUnits {

    public static UnitType baseUnit, flyUnit;

    public static void load() {
        baseUnit = new TankUnitType("base-unit") {{
            speed = 0.4f;
            hitSize = 18f;

            health = 650;
            armor = 6f;
            circleTarget = true;
            faceTarget = false;
            range = 300f;
            constructor = TankUnit::create;
            allowLegStep = false;
            rotateSpeed = 3.5f;
            itemCapacity = 0;
            treadRects = new Rect[]{new Rect(12 - 32f, 7 - 32f, 14, 51)};
            treadPullOffset = 3;
            abilities.add(new AircraftAbility(new AircraftUnitType("fly-unit") {{
                armor = 4f;
                health = 200;
                speed = 3f;
                rotateSpeed = 5f;
                lowAltitude = false;
                flying = true;
                circleTarget = true;
                faceTarget = false;
                drawSize = 12f;//Use drawSize instead of hitSize.
                range = 600f;
                targetAir = true;
                weapons.add(
                        new Weapon() {{
                            shake = 1f;
                            shootY = 2f;
                            x = 0f;
                            y = 0f;
                            reload = 120f;
                            recoil = 4f;
                            shootSound = Sounds.laser;
                            rotate = true;

                            bullet = new LaserBulletType() {{
                                damage = 115f;
                                sideAngle = 20f;
                                sideWidth = 1.5f;
                                targetAir = false;
                                sideLength = 80f;
                                width = 25f;
                                length = 45f;
                                shootEffect = Fx.shockwave;
                                colors = new Color[]{Color.valueOf("ec7458aa"), Color.valueOf("ff9c5a"), Color.white};
                            }};
                        }},
                        new Weapon() {{
                            top = false;
                            y = 1f;
                            x = 2f;
                            reload = 30f;
                            recoil = 4f;
                            shake = 2f;
                            rotate = true;
                            ejectEffect = Fx.casing2;
                            shootSound = Sounds.artillery;
                            bullet = new ArtilleryBulletType(4f, 20, "shell") {{
                                hitEffect = Fx.blastExplosion;
                                knockback = 0.8f;
                                lifetime = 60f;
                                width = height = 20f;
                                collides = true;
                                collidesTiles = true;
                                splashDamageRadius = 35f;
                                splashDamage = 80f;
                                backColor = Pal.bulletYellowBack;
                                frontColor = Pal.bulletYellow;
                            }};
                        }});
            }}, 300f, 0, 0));
        }};
    }
}
