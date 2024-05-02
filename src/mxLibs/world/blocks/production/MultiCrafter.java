package mxLibs.world.blocks.production;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Table;
import arc.struct.EnumSet;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.entities.Effect;
import mindustry.gen.Sounds;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.logic.LAccess;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.ui.ItemDisplay;
import mindustry.ui.ItemImage;
import mindustry.ui.LiquidDisplay;
import mindustry.ui.Styles;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.consumers.Consume;
import mindustry.world.consumers.ConsumeItems;
import mindustry.world.consumers.ConsumeLiquids;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.BlockStatus;
import mindustry.world.meta.Stat;
import mxLibs.content.MxStats;
import mxLibs.ui.LiquidImage;

import static mindustry.Vars.state;
import static mindustry.Vars.tilesize;


public class MultiCrafter extends GenericCrafter {

    public Seq<CraftPlan> craftPlans = new Seq<>(16);

    public MultiCrafter(String name) {
        super(name);
        configurable = true;
        saveConfig = true;
        enableDrawStatus = true;
        update = true;
        solid = true;
        hasItems = true;
        ambientSound = Sounds.machine;
        ambientSoundVolume = 0.03f;
        logicConfigurable = true;
        flags = EnumSet.of(BlockFlag.factory);
        drawArrow = false;

        config(Integer.class, (MultiCrafterBuild tile, Integer i) -> {
            if (tile.currentPlan != null && craftPlans.indexOf(tile.currentPlan) == i) return;
            tile.currentPlan = i < 0 || i >= craftPlans.size ? null : craftPlans.get(i);
        });
    }

    @Override
    public boolean configSenseable() {
        return true;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.remove(Stat.productionTime);
        stats.remove(Stat.output);
        stats.remove(Stat.input);
        stats.add(MxStats.craftPlan, table -> {
            table.row();
            table.table(Styles.grayPanel, t -> {
                if (craftPlans != null) {
                    for (CraftPlan plan : craftPlans) {
                        t.add(String.format("%.2f", plan.craftTime / 60f) + " " + Core.bundle.get("unit.seconds")).color(Color.lightGray);
                        t.row();
                        t.add(Core.bundle.get("stat.input")).color(Color.lightGray);
                        for (var consumer : plan.consumers) {
                            if (consumer instanceof ConsumeItems consumeItem) {
                                for (ItemStack stack : consumeItem.items) {
                                    t.add(new ItemDisplay(stack.item, stack.amount)).padRight(5);
                                }
                            }
                            if (consumer instanceof ConsumeLiquids consumeLiquids) {
                                for (LiquidStack stack : consumeLiquids.liquids) {
                                    t.add(new LiquidDisplay(stack.liquid, stack.amount * 60f, true)).padRight(5);
                                }
                            }
                        }
                        t.row();
                        t.add(Core.bundle.get("stat.output")).color(Color.lightGray);
                        if (plan.outputItems != null) {
                            for (ItemStack stack : plan.outputItems) {
                                t.add(new ItemDisplay(stack.item, stack.amount)).padRight(5);
                            }
                        }
                        if (plan.outputLiquids != null) {
                            for (LiquidStack stack : plan.outputLiquids) {
                                t.add(new LiquidDisplay(stack.liquid, stack.amount * 60f, true)).padRight(5);
                            }
                        }
                        t.row();
                    }
                }

            }).right().growX().pad(5);

        });
    }


    @Override
    public void setBars() {
        super.setBars();
        removeBar("items");
        removeBar("liquids");
        removeBar("liquid-water");
        Seq<Liquid> liquidSeq = new Seq<>();

        for (var plan : craftPlans) {
            if (plan.outputLiquids != null) {
                for (var stack : plan.outputLiquids) {
                    if (!liquidSeq.contains(stack.liquid)) liquidSeq.add(stack.liquid);
                }
            }
            for (var consumer : plan.consumers) {
                if (consumer instanceof ConsumeLiquids consumeLiquids) {
                    for (LiquidStack liquidStack : consumeLiquids.liquids) {
                        if (!liquidSeq.contains(liquidStack.liquid)) liquidSeq.add(liquidStack.liquid);
                    }
                }
            }
        }
        for (var liquid : liquidSeq) {
            addLiquidBar(liquid);
        }
    }


    @Override
    public void init() {
        outputItems = new ItemStack[]{new ItemStack(Items.copper, 1)};
        outputLiquids = new LiquidStack[]{new LiquidStack(Liquids.water, 1)};
        ;
        super.init();
        for (var plan : craftPlans) {
            if (plan.outputItems == null && plan.outputItem != null) {
                plan.outputItems = new ItemStack[]{plan.outputItem};
            }
            if (plan.outputLiquids == null && plan.outputLiquid != null) {
                plan.outputLiquids = new LiquidStack[]{plan.outputLiquid};
            }
            if (plan.outputLiquid == null && plan.outputLiquids != null && plan.outputLiquids.length > 0) {
                plan.outputLiquid = plan.outputLiquids[0];
            }
            if (plan.outputItems != null) hasItems = true;
            if (plan.outputLiquids != null) hasLiquids = true;
            for (var consumer : plan.consumers) {
                if (consumer instanceof ConsumeItems consumeItem) {
                    for (ItemStack itemStack : consumeItem.items) {
                        this.itemFilter[itemStack.item.id] = true;
                    }
                }
                if (consumer instanceof ConsumeLiquids consumeLiquids) {
                    for (LiquidStack liquidStack : consumeLiquids.liquids) {
                        this.liquidFilter[liquidStack.liquid.id] = true;
                    }
                }
            }
        }
        hasConsumers = true;
    }

    public static class CraftPlan {
        public Seq<Consume> consumerBuilder;
        public float craftTime = 60f;
        public @Nullable ItemStack outputItem;
        public @Nullable ItemStack[] outputItems;
        public @Nullable LiquidStack outputLiquid;
        public @Nullable LiquidStack[] outputLiquids;
        public Effect craftEffect = Fx.none;
        public Effect updateEffect = Fx.none;
        public float updateEffectChance = 0.04f;
        public Consume[] consumers = {}, optionalConsumers = {}, nonOptionalConsumers = {}, updateConsumers = {};//Consumes should be controllable

        public CraftPlan(Seq<Consume> consumerBuilder) {
            this.consumerBuilder = consumerBuilder;
            this.consumers = consumerBuilder.toArray(Consume.class);
            this.optionalConsumers = consumerBuilder.select(consume -> consume.optional && !consume.ignore()).toArray(Consume.class);
            this.nonOptionalConsumers = consumerBuilder.select(consume -> !consume.optional && !consume.ignore()).toArray(Consume.class);
            this.updateConsumers = consumerBuilder.select(consume -> consume.update && !consume.ignore()).toArray(Consume.class);
        }
    }

    public class MultiCrafterBuild extends GenericCrafterBuild {

        public @Nullable CraftPlan currentPlan;

        @Override
        public void drawStatus() {

            if (block.enableDrawStatus) {
                float multiplier = block.size > 1 ? 1 : 0.64F;
                float brcx = x + (block.size * tilesize / 2.0F) - (tilesize * multiplier / 2.0F);
                float brcy = y - (block.size * tilesize / 2.0F) + (tilesize * multiplier / 2.0F);
                Draw.z(Layer.power + 1);
                Draw.color(Pal.gray);
                Fill.square(brcx, brcy, 2.5F * multiplier, 45);
                Draw.color(status().color);
                Fill.square(brcx, brcy, 1.5F * multiplier, 45);
                Draw.color();
            }
        }

        @Override
        public Object config() {
            return currentPlan == null ? -1 : craftPlans.indexOf(currentPlan);
        }

        @Override
        public BlockStatus status() {
            if (!enabled || currentPlan == null) {
                return BlockStatus.logicDisable;
            }
            if (!shouldConsume()) {
                return BlockStatus.noOutput;
            }
            if (efficiency <= 0 || !productionValid()) {
                return BlockStatus.noInput;
            }
            return ((state.tick / 30.0F) % 1.0F) < efficiency ? BlockStatus.active : BlockStatus.noInput;
        }

        @Override
        public void updateConsumption() {
            if (currentPlan == null) return;

            if (cheating()) {
                potentialEfficiency = enabled && productionValid() ? 1.0F : 0.0F;
                efficiency = optionalEfficiency = shouldConsume() ? potentialEfficiency : 0.0F;
                updateEfficiencyMultiplier();
                return;
            }

            if (!enabled) {
                potentialEfficiency = efficiency = optionalEfficiency = 0.0F;
                return;
            }

            boolean update = shouldConsume() && productionValid();
            float minEfficiency = 1.0F;
            efficiency = optionalEfficiency = 1.0F;
            for (var cons : currentPlan.nonOptionalConsumers) {
                minEfficiency = Math.min(minEfficiency, cons.efficiency(this));
            }
            for (var cons : block.nonOptionalConsumers) {
                minEfficiency = Math.min(minEfficiency, cons.efficiency(this));
            }
            for (var cons : currentPlan.optionalConsumers) {
                optionalEfficiency = Math.min(optionalEfficiency, cons.efficiency(this));
            }
            for (var cons : block.optionalConsumers) {
                optionalEfficiency = Math.min(optionalEfficiency, cons.efficiency(this));
            }
            efficiency = minEfficiency;
            optionalEfficiency = Math.min(optionalEfficiency, minEfficiency);
            potentialEfficiency = efficiency;
            if (!update) {
                efficiency = optionalEfficiency = 0.0F;
            }
            updateEfficiencyMultiplier();
            if (update && efficiency > 0) {
                for (var cons : currentPlan.updateConsumers) {
                    cons.update(this);
                }
                for (var cons : block.updateConsumers) {
                    cons.update(this);
                }
            }
        }

        @Override
        public void updateTile() {

            if (currentPlan == null) return;

            if (efficiency > 0) {
                progress += getProgressIncrease(currentPlan.craftTime);
                warmup = Mathf.approachDelta(warmup, warmupTarget(), warmupSpeed);
                if (currentPlan.outputLiquids != null) {
                    float inc = getProgressIncrease(1f);
                    for (var output : currentPlan.outputLiquids) {
                        handleLiquid(this, output.liquid, Math.min(output.amount * inc, liquidCapacity - liquids.get(output.liquid)));
                    }
                }

                if (wasVisible && Mathf.chanceDelta(currentPlan.updateEffectChance)) {
                    currentPlan.updateEffect.at(x + Mathf.range(size * 4f), y + Mathf.range(size * 4));
                }
            } else {
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
            }
            if (progress >= 1f) {
                craft();
            }
            dumpOutputs();
        }

        @Override
        public float getProgressIncrease(float baseTime) {
            float base = 1.0F / baseTime * edelta();
            if (ignoreLiquidFullness || currentPlan == null) {
                return base;
            }

            float scaling = 1f, max = 1f;
            if (currentPlan.outputLiquids != null) {
                max = 0f;
                for (var s : currentPlan.outputLiquids) {
                    float value = (liquidCapacity - liquids.get(s.liquid)) / (s.amount * edelta());
                    scaling = Math.min(scaling, value);
                    max = Math.max(max, value);
                }
            }

            //when dumping excess take the maximum value instead of the minimum.
            return base * (dumpExtraLiquid ? Math.min(max, 1f) : scaling);
        }

        @Override
        public void consume() {
            if (currentPlan == null) return;
            for (Consume cons : currentPlan.consumers) {
                cons.trigger(this);
            }

        }

        @Override
        public void craft() {
            if (currentPlan == null) return;
            consume();
            if (currentPlan.outputItems != null) {
                for (var output : currentPlan.outputItems) {
                    for (int i = 0; i < output.amount; i++) {
                        offload(output.item);
                    }
                }
            }

            if (wasVisible) {
                currentPlan.craftEffect.at(x, y);
            }
            progress %= 1f;
        }

        @Override
        public void dumpOutputs() {
            if (currentPlan == null) return;
            if (currentPlan.outputItems != null && timer(timerDump, dumpTime / timeScale)) {
                for (ItemStack output : currentPlan.outputItems) {
                    dump(output.item);
                }
            }

            if (currentPlan.outputLiquids != null) {
                for (int i = 0; i < currentPlan.outputLiquids.length; i++) {
                    int dir = liquidOutputDirections.length > i ? liquidOutputDirections[i] : -1;

                    dumpLiquid(currentPlan.outputLiquids[i].liquid, 2f, dir);
                }
            }
        }

        @Override
        public boolean shouldConsume() {
            if (currentPlan == null) return false;
            if (currentPlan.outputItems != null) {
                for (var output : currentPlan.outputItems) {
                    if (items.get(output.item) + output.amount > itemCapacity) {
                        return false;
                    }
                }
            }
            if (currentPlan.outputLiquids != null && !ignoreLiquidFullness) {
                boolean allFull = true;
                for (var output : currentPlan.outputLiquids) {
                    if (liquids.get(output.liquid) >= liquidCapacity - 0.001f) {
                        if (!dumpExtraLiquid) {
                            return false;
                        }
                    } else {
                        allFull = false;
                    }
                }

                if (allFull) {
                    return false;
                }
            }

            return enabled;
        }

        @Override
        public double sense(LAccess sensor) {
            if (sensor == LAccess.config) {
                if (currentPlan == null) return Float.NaN;
                return craftPlans.indexOf(currentPlan);
            }
            return super.sense(sensor);
        }

        public CraftPlan getCurrentPlan() {
            return currentPlan;
        }

        @Override
        public void buildConfiguration(Table table) {
            super.buildConfiguration(table);
            if (craftPlans != null) {
                int index = 0;
                for (CraftPlan plan : craftPlans) {
                    ImageButton button = table.button(Styles.grayPanel, Styles.clearTogglei, () -> {

                    }).get();
                    if (plan.consumers != null) {
                        for (Consume cons : plan.consumers) {
                            if (cons instanceof ConsumeItems consumeItems) {
                                for (ItemStack itemStack : consumeItems.items) {
                                    button.add(new ItemImage(itemStack)).growX().height(40f).left();
                                }
                            }
                            if (cons instanceof ConsumeLiquids consumeLiquids) {
                                for (var liquidStack : consumeLiquids.liquids) {
                                    button.add(new LiquidImage(liquidStack)).growX().height(40f).left();
                                }
                            }
                        }
                    }
                    button.add("->");
                    if (plan.outputItems != null) {
                        for (var itemStack : plan.outputItems) {
                            button.add(new ItemImage(itemStack)).growX().height(40f).left();
                        }
                    }
                    if (plan.outputLiquids != null) {
                        for (var liquidStack : plan.outputLiquids) {
                            button.add(new LiquidImage(liquidStack)).growX().height(40f).left();
                        }
                    }
                    button.changed(() -> {
                        configure(craftPlans.indexOf(plan));
                        deselect();
                    });
                    button.setChecked(getCurrentPlan() == plan);

                    if ((++index % 2) == 0) table.row();
                }
            }

        }

        @Override
        public void control(LAccess type, double p1, double p2, double p3, double p4) {
            super.control(type, p1, p2, p3, p4);
        }

        @Override
        public byte version() {
            return 1;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(currentPlan == null ? -1 : craftPlans.indexOf(currentPlan));
            write.f(progress);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            int i = read.i();
            currentPlan = i == -1 ? null : craftPlans.get(i);
            progress = read.f();
            if (currentPlan != null) configure(currentPlan);
        }
    }
}
