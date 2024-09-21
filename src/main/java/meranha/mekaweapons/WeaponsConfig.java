package meranha.mekaweapons;

import mekanism.common.config.BaseMekanismConfig;
import mekanism.common.config.value.CachedDoubleValue;
import mekanism.common.config.value.CachedLongValue;
import mekanism.common.config.value.CachedIntValue;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class WeaponsConfig extends BaseMekanismConfig {

    private final ModConfigSpec configSpec;

    public final CachedIntValue mekaTanaBaseDamage;
    public final CachedDoubleValue mekaTanaAttackSpeed;
    public final CachedLongValue mekaTanaEnergyUsage;
    public final CachedLongValue mekaTanaTeleportUsage;
    public final CachedIntValue mekaTanaMaxTeleportReach;
    public final CachedLongValue mekaTanaBaseEnergyCapacity;
    public final CachedLongValue mekaTanaBaseChargeRate;

    public final CachedIntValue mekaBowBaseDamage;
    public final CachedLongValue mekaBowEnergyUsage;
    public final CachedLongValue mekaBowFireModeEnergyUsage;
    public final CachedLongValue mekaBowBaseEnergyCapacity;
    public final CachedLongValue mekaBowBaseChargeRate;

    WeaponsConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("MekaWeapons Settings. Joules to FE conversion: 2.5J = 1FE").push("weapons");

        WeaponsConfigTranslations.MEKA_TANA.applyToBuilder(builder).push("meka_tana");
        mekaTanaBaseDamage = CachedIntValue.wrap(this, WeaponsConfigTranslations.MEKA_TANA_BASE_DAMAGE.applyToBuilder(builder).define("base_damage", 50));
        mekaTanaAttackSpeed = CachedDoubleValue.wrap(this, WeaponsConfigTranslations.MEKA_TANA_ATTACK_SPEED.applyToBuilder(builder).defineInRange("attack_speed", -2.4, -Attributes.ATTACK_SPEED.value().getDefaultValue(), 100));
        mekaTanaEnergyUsage = CachedLongValue.definePositive(this, builder, WeaponsConfigTranslations.MEKA_TANA_ENERGY_USAGE, "energy_usage", 625_000);
        mekaTanaTeleportUsage = CachedLongValue.definePositive(this, builder, WeaponsConfigTranslations.MEKA_TANA_TELEPORT_USAGE, "teleport_energy_usage", 5_000);
        mekaTanaMaxTeleportReach = CachedIntValue.wrap(this, WeaponsConfigTranslations.MEKA_TANA_MAX_TELEPORT_REACH.applyToBuilder(builder).defineInRange("max_teleport_reach", 100, 3, 1_024));
        mekaTanaBaseEnergyCapacity = CachedLongValue.definePositive(this, builder, WeaponsConfigTranslations.MEKA_TANA_BASE_ENERGY_CAPACITY, "base_energy_capacity", 16_000_000);
        mekaTanaBaseChargeRate = CachedLongValue.definePositive(this, builder, WeaponsConfigTranslations.MEKA_TANA_BASE_CHARGE_RATE, "base_charge_rate", 350_000);
        builder.pop();

        WeaponsConfigTranslations.MEKA_BOW.applyToBuilder(builder).push("meka_bow");
        mekaBowBaseDamage = CachedIntValue.wrap(this, WeaponsConfigTranslations.MEKA_BOW_BASE_DAMAGE.applyToBuilder(builder).define("base_damage", 50));
        mekaBowEnergyUsage = CachedLongValue.definePositive(this, builder, WeaponsConfigTranslations.MEKA_BOW_ENERGY_USAGE, "energy_usage", 625_000);
        mekaBowFireModeEnergyUsage = CachedLongValue.definePositive(this, builder, WeaponsConfigTranslations.MEKA_BOW_FIRE_MODE_ENERGY_USAGE, "fire_mode_energy_usage", 825_000);
        mekaBowBaseEnergyCapacity = CachedLongValue.definePositive(this, builder, WeaponsConfigTranslations.MEKA_BOW_BASE_ENERGY_CAPACITY, "base_energy_capacity", 16_000_000);
        mekaBowBaseChargeRate = CachedLongValue.definePositive(this, builder, WeaponsConfigTranslations.MEKA_BOW_BASE_CHARGE_RATE, "base_charge_rate", 350_000);
        builder.pop();

        this.configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "mekaweapons";
    }

    @Override
    public String getTranslation() {
        return "Mekanism Weapons";
    }

    @Override
    public ModConfigSpec getConfigSpec() {
        return this.configSpec;
    }

    @Override
    public Type getConfigType() {
        return Type.SERVER;
    }
}
