package mxLibs;

import arc.Events;
import arc.struct.ObjectMap;
import arc.util.serialization.JsonValue;
import mindustry.ctype.UnlockableContent;
import mindustry.game.EventType;

import static mindustry.Vars.content;
import static mindustry.Vars.state;

public class ResearchObjectives {

    public static class ResearchPlan {
        public int tier;
        public UnlockableContent target;

        public JsonValue value;

        public ResearchPlan(int tier, UnlockableContent target, JsonValue value) {
            this.tier = tier;
            this.target = target;
            this.value = value;
        }
    }


    public static ObjectMap<UnlockableContent, ResearchPlan> objectives = new ObjectMap<>();
    public static ObjectMap<UnlockableContent, JsonValue> toBeParsed = new ObjectMap<>();

    public static ObjectMap<UnlockableContent, JsonValue> defaultValue = new ObjectMap<>();

    public static void load() {
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
                if (c != null && v != null) objectives.put(c, new ResearchPlan(tier, value.key, v));
            }
        }

        Events.on(EventType.WorldLoadEndEvent.class, (c) -> {
            objectives.each((content, plan) -> {
                if (content.unlocked() || !state.isCampaign()) {
                    replaceBlock(plan.target, plan.value);
                }
            });
        });

        Events.on(EventType.ResearchEvent.class, (c) -> {
            objectives.each((content, plan) -> {
                if (content == c.content) {
                    replaceBlock(plan.target, plan.value);
                }
            });
        });
    }

    public static void replaceBlock(UnlockableContent content, JsonValue value) {
        mxLibs.parser.replaceBlock(content, value);
        var map = content.stats.toMap();
        map.clear();
        content.stats.intialized = false;
    }


}
