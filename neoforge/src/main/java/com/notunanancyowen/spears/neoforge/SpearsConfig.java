package com.notunanancyowen.spears.neoforge;

import com.notunanancyowen.spears.Spears;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = Spears.MOD_ID)
public class SpearsConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue SPEAR_CHARGE_ATTACKS = BUILDER
            .comment("Whether spears have a charge attack")
            .define("spear_charge_attacks", true);

    private static final ModConfigSpec.BooleanValue SPEAR_STABBING_ANIMATION = BUILDER
            .comment("Whether spears have a stabbing animation")
            .define("spear_stabbing_animation", true);

    private static final ModConfigSpec.BooleanValue TRIDENTS_USE_SPEAR_ATTACK_ANIMATION = BUILDER
            .comment("Whether tridents attack like spears")
            .define("tridents_use_spear_attack_animation", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        Spears.config = Spears.makeConfig();
        Spears.config.put("spear_charge_attacks", SPEAR_CHARGE_ATTACKS.getAsBoolean());
        Spears.config.put("spear_stabbing_animation", SPEAR_STABBING_ANIMATION.getAsBoolean());
        Spears.config.put("tridents_use_spear_attack_animation", TRIDENTS_USE_SPEAR_ATTACK_ANIMATION.getAsBoolean());
    }
}
