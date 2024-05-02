package mxLibs;

import arc.Events;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.serialization.JsonValue;
import mindustry.ctype.UnlockableContent;
import mindustry.game.EventType;

import java.util.Comparator;

import static mindustry.Vars.content;
import static mindustry.Vars.state;

public class ResearchObjectives {

    public static class ResearchPlan {
        public int tier;
        public UnlockableContent content;
        public UnlockableContent target;
        public JsonValue value;

        public ResearchPlan(int tier, UnlockableContent content, UnlockableContent target, JsonValue value) {
            this.tier = tier;
            this.content = content;
            this.target = target;
            this.value = value;
        }
    }


    public static Seq<ResearchPlan> objectives = new Seq<>();
    public static ObjectMap<UnlockableContent, JsonValue> toBeParsed = new ObjectMap<>();
    public static int[] tiers;
    public static ObjectMap<UnlockableContent, JsonValue> defaultValue = new ObjectMap<>();

    public static void load() {
        tiers = new int[content.blocks().size];
        for (var value : toBeParsed) {
            for (var child : value.value) {
                UnlockableContent c = null;
                int tier = 0;
                if (child.has("content") && child.get("content").isString()) {
                    c = content.block(child.get("content").asString());
                    child.remove("content");
                }

                if (child.has("tier") && child.get("tier").isNumber()) {
                    tier = child.get("tier").asInt();
                    child.remove("tier");
                }
                JsonValue v = null;
                if (child.has("objectives") && child.get("objectives").isObject()) {
                    v = child.get("objectives");
                }
                if (c != null && v != null) objectives.addUnique(new ResearchPlan(tier, c, value.key, v));
            }
        }
        objectives.sort(Comparator.comparingInt(a -> a.tier));
        Events.on(EventType.WorldLoadEndEvent.class, (c) -> {
            defaultValue.each((content, value) -> {
                replaceBlock(content, value);
            });
            int[] t = new int[content.blocks().size];
            for (var plan : objectives) {
                if (plan.content.unlocked() || !state.isCampaign()) {
                    if (t[plan.content.id] < plan.tier) {
                        t[plan.content.id] = plan.tier;
                        replaceBlock(plan.target, plan.value);
                    }
                }
            }
            tiers = t;
        });

        Events.on(EventType.ResearchEvent.class, (c) -> {
            for (var plan : objectives) {
                if (plan.content == c.content) {
                    if (tiers[plan.content.id] < plan.tier) {
                        tiers[plan.content.id] = plan.tier;
                        replaceBlock(plan.target, plan.value);
                    }
                }
            }
        });
    }

    public static void replaceBlock(UnlockableContent content, JsonValue value) {
        mxLibs.parser.replaceBlock(content, value);
        var map = content.stats.toMap();
        map.clear();
        content.stats.intialized = false;
    }


}
