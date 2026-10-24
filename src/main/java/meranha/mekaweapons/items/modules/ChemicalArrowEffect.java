package meranha.mekaweapons.items.modules;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.common.registries.MekanismChemicals;
import meranha.mekaweapons.MekaWeapons;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Chemicals the Chemical Arrows Unit can store in the Meka-Bow, and the effect each one applies on hit. An effect with
 * a duration of zero is disabled and its chemical is no longer accepted by the tank.
 */
public enum ChemicalArrowEffect {
    HYDROGEN(MekanismChemicals.HYDROGEN) {
        @Override
        public boolean isEnabled() {
            return MekaWeapons.general.chemicalArrowsHydrogenBurnSeconds.get() > 0;
        }

        @Override
        public void apply(LivingEntity target) {
            if (!target.fireImmune()) {
                target.igniteForSeconds(MekaWeapons.general.chemicalArrowsHydrogenBurnSeconds.get());
            }
        }
    },
    SULFURIC_ACID(MekanismChemicals.SULFURIC_ACID) {
        @Override
        public boolean isEnabled() {
            return MekaWeapons.general.chemicalArrowsSulfuricAcidDuration.get() > 0;
        }

        @Override
        public void apply(LivingEntity target) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, MekaWeapons.general.chemicalArrowsSulfuricAcidDuration.get(), MekaWeapons.general.chemicalArrowsSulfuricAcidAmplifier.get()));
        }
    },
    HYDROGEN_CHLORIDE(MekanismChemicals.HYDROGEN_CHLORIDE) {
        @Override
        public boolean isEnabled() {
            return MekaWeapons.general.chemicalArrowsHydrogenChlorideDuration.get() > 0;
        }

        @Override
        public void apply(LivingEntity target) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, MekaWeapons.general.chemicalArrowsHydrogenChlorideDuration.get(), MekaWeapons.general.chemicalArrowsHydrogenChlorideAmplifier.get()));
        }
    },
    WATER_VAPOR(MekanismChemicals.WATER_VAPOR) {
        @Override
        public boolean isEnabled() {
            return MekaWeapons.general.chemicalArrowsWaterVaporDuration.get() > 0;
        }

        @Override
        public void apply(LivingEntity target) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, MekaWeapons.general.chemicalArrowsWaterVaporDuration.get(), MekaWeapons.general.chemicalArrowsWaterVaporAmplifier.get()));
        }
    };

    private final String serializedName;
    private final Holder<Chemical> chemical;

    ChemicalArrowEffect(Holder<Chemical> chemical) {
        this.serializedName = name().toLowerCase(Locale.ROOT);
        this.chemical = chemical;
    }

    public abstract boolean isEnabled();

    public abstract void apply(LivingEntity target);

    public Holder<Chemical> chemical() {
        return chemical;
    }

    public String getSerializedName() {
        return serializedName;
    }

    public static boolean isSupported(ChemicalStack stack) {
        return byStack(stack) != null;
    }

    @Nullable
    public static ChemicalArrowEffect byStack(ChemicalStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        for (ChemicalArrowEffect effect : values()) {
            if (stack.is(effect.chemical) && effect.isEnabled()) {
                return effect;
            }
        }
        return null;
    }

    @Nullable
    public static ChemicalArrowEffect bySerializedName(String name) {
        for (ChemicalArrowEffect effect : values()) {
            if (effect.serializedName.equals(name)) {
                return effect;
            }
        }
        return null;
    }
}
