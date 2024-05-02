package mxLibs.content;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.world.Block;
import mxLibs.world.blocks.production.MultiCrafter;

import static mindustry.type.ItemStack.with;

public class TestBlocks {
    public static Block nope;

    public static void load() {
        nope = new MultiCrafter("nopee") {{
            requirements(Category.crafting, with(Items.silicon, 375));
            hasItems = true;
            itemCapacity = 40;
            hasPower = true;
            size = 4;
            consumeItems(with(Items.coal, 1));
//            craftPlans.add(
//
//                    new CraftPlan(){{
//
//                        outputItems = with(Items.thorium,2,Items.plastanium,2);
//                        outputLiquids = LiquidStack.with(Liquids.slag,0.15);
//                        craftTime = 45f;
//                        craftEffect = new Effect(30, e -> {
//                            randLenVectors(e.id, 12, 12f + e.fin() * 5f, (x, y) -> {
//                                color(Color.white, Items.thorium.color, e.fin());
//                                Fill.square(e.x + x, e.y + y, 0.6f + e.fout() * 2f, 45);
//                            });
//                        });
//
//                    }},
//                    new CraftPlan(){{
//                        Seq<Consume> consumeBuilder = new Seq<>();
//                        consumeBuilder.add(new ConsumeItems(with(Items.titanium,3,Items.scrap,3)));
//                        consumeBuilder.add(new ConsumeLiquids(LiquidStack.with(Liquids.cryofluid,0.25)));
//                        consumers = consumeBuilder.toArray(Consume.class);
//                        outputItems = with(Items.surgeAlloy,2);
//                        outputLiquids = LiquidStack.with(Liquids.water,0.5);
//                        craftTime = 120f;
//                        craftEffect = new Effect(40, e -> {
//                            randLenVectors(e.id, 18, 12f + e.fin() * 5f, (x, y) -> {
//                                color(Color.white, Items.surgeAlloy.color, e.fin());
//                                Fill.square(e.x + x, e.y + y, 1f + e.fout() * 2f, 45);
//                            });
//                        });
//                    }}
//            );
        }};
    }

}
