package com.notunanancyowen.spears.criteria;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.notunanancyowen.spears.Spears;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.dynamic.Codecs;

import java.util.Optional;

public class SpearedMobs extends AbstractCriterion<SpearedMobs.AllSpearedMobs> {
	@Override
	public Codec<AllSpearedMobs> getConditionsCodec() {
		return AllSpearedMobs.CODEC;
	}

	public void spearMob(ServerPlayerEntity serverPlayerEntity, int i) {
		this.trigger(serverPlayerEntity, arg -> arg.spearedMobsOver(i));
	}

	public record AllSpearedMobs(Optional<LootContextPredicate> player, Optional<Integer> count) implements Conditions {
		public static final Codec<AllSpearedMobs> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(AllSpearedMobs::player),
					Codecs.POSITIVE_INT.optionalFieldOf("count").forGetter(AllSpearedMobs::count)
				)
				.apply(instance, AllSpearedMobs::new)
		);

		public static AdvancementCriterion<AllSpearedMobs> setSpearedMobs(int i) {
			return Spears.SPEARED_MOBS.create(new AllSpearedMobs(Optional.empty(), Optional.of(i)));
		}
		public boolean spearedMobsOver(int i) {
			return this.count.isEmpty() || i >= (Integer)this.count.get();
		}
	}
}
