package meranha.mekaweapons;

import mekanism.common.config.BaseMekanismConfig;
import mekanism.common.config.value.*;
import meranha.mekaweapons.items.modules.ModuleDamageMode;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;

public class WeaponsConfig extends BaseMekanismConfig {

    private final ModConfigSpec configSpec;

    public final CachedLongValue wirelessChargerEnergyRate;
    public final CachedEnumValue<ModuleDamageMode> moduleDamageMode;
    public final CachedIntValue moduleCustomDamage;

    public final CachedIntValue mekaTanaBaseDamage;
    public final CachedDoubleValue mekaTanaAttackSpeed;
    public final CachedLongValue mekaTanaEnergyUsage;
    public final CachedLongValue mekaTanaSweepingEnergyUsage;
    public final CachedLongValue mekaTanaTeleportUsage;
    public final CachedIntValue mekaTanaMaxTeleportReach;
    public final CachedLongValue mekaTanaLootingEnergyUsage;
    public final CachedLongValue mekaTanaFireAspectEnergyUsage;
    public final CachedLongValue mekaTanaKnockbackEnergyUsage;
    public final CachedLongValue mekaTanaBaseEnergyCapacity;
    public final CachedLongValue mekaTanaBaseChargeRate;

    public final CachedIntValue mekaBowBaseDamage;
    public final CachedLongValue mekaBowEnergyUsage;
    public final CachedLongValue mekaBowEnergyArrowUsage;
    public final CachedLongValue mekabowAutoFireEnergyUsage;
    public final CachedLongValue mekabowDrawSpeedUsage;
    public final CachedLongValue mekabowGravityDampenerUsage;
    public final CachedLongValue mekaBowLootingEnergyUsage;
    public final CachedLongValue mekaBowPunchEnergyUsage;
    public final CachedLongValue mekaBowBaseEnergyCapacity;
    public final CachedLongValue mekaBowBaseChargeRate;

    public final CachedLongValue chemicalArrowsEnergyUsage;
    public final CachedLongValue chemicalArrowsCapacity;
    public final CachedLongValue chemicalArrowsTransferRate;
    public final CachedLongValue chemicalArrowsUsage;
    public final CachedIntValue chemicalArrowsHydrogenBurnSeconds;
    public final CachedIntValue chemicalArrowsSulfuricAcidDuration;
    public final CachedIntValue chemicalArrowsSulfuricAcidAmplifier;
    public final CachedIntValue chemicalArrowsHydrogenChlorideDuration;
    public final CachedIntValue chemicalArrowsHydrogenChlorideAmplifier;
    public final CachedIntValue chemicalArrowsWaterVaporDuration;
    public final CachedIntValue chemicalArrowsWaterVaporAmplifier;

    public final CachedIntValue mekaGunBaseDamage;
    public final CachedLongValue mekaGunEnergyUsage;
    public final CachedLongValue mekaGunBaseEnergyCapacity;
    public final CachedLongValue mekaGunBaseChargeRate;
    public final CachedLongValue mekaGunLootingEnergyUsage;
    public final CachedLongValue mekaGunCoolingEnergyUsage;
    public final CachedIntValue mekaGunCoolingHeatLossBonus;
    public final CachedIntValue mekaGunCoolingDelayReduction;
    public final CachedDoubleValue mekaGunLaserSoundVolume;
    public final CachedIntValue mekaGunBeamLength;
    public final CachedIntValue mekaGunMaxHeat;
    public final CachedIntValue mekaGunHeatPerShot;
    public final CachedIntValue mekaGunHeatLossPerSecond;
    public final CachedIntValue mekaGunCooldownDelayTicks;
    public final CachedIntValue mekaGunFireInterval;

    public final CachedBooleanValue mekaTanaEnchantments;
    public final CachedBooleanValue mekaBowEnchantments;
    public final CachedBooleanValue mekaGunEnchantments;

    WeaponsConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("MekaWeapons Settings. Joules to FE conversion: 2.5J = 1FE");

        wirelessChargerEnergyRate = CachedLongValue.definePositive(this, builder, WeaponsConfigTranslations.WIRELESS_CHARGE_RATE, "wireless_charge_rate", 2_500_000);

        moduleDamageMode = CachedEnumValue.wrap(this, WeaponsConfigTranslations.MODULE_DAMAGE_MODE.applyToBuilder(builder).defineEnum("module_damage_mode", ModuleDamageMode.HALF_BASE_DAMAGE));
        moduleCustomDamage = CachedIntValue.wrap(this, WeaponsConfigTranslations.MODULE_DAMAGE_CUSTOM.applyToBuilder(builder).define("module_damage_custom", 32));

        WeaponsConfigTranslations.MEKA_TANA.applyToBuilder(builder).push("meka_tana");
        mekaTanaBaseDamage = CachedIntValue.wrap(this, WeaponsConfigTranslations.MEKA_TANA_BASE_DAMAGE.applyToBuilder(builder).define("base_damage", 32));
        mekaTanaAttackSpeed = CachedDoubleValue.wrap(this, WeaponsConfigTranslations.MEKA_TANA_ATTACK_SPEED.applyToBuilder(builder).defineInRange("attack_speed", -2.4, -Attributes.ATTACK_SPEED.value().getDefaultValue(), 100));
        mekaTanaEnergyUsage = CachedLongValue.definePositive(this, builder, WeaponsConfigTranslations.MEKA_TANA_ENERGY_USAGE, "energy_usage", 625_000);
        mekaTanaSweepingEnergyUsage = CachedLongValue.definePositive(this, builder, WeaponsConfigTranslations.MEKA_TANA_SWEEPING_ATTACK, "sweeping_attack_energy_usage", 125_000);
        mekaTanaTeleportUsage = CachedLongValue.definePositive(this, builder, WeaponsConfigTranslations.MEKA_TANA_TELEPORT_USAGE, "teleport_energy_usage", 5_000);
        mekaTanaMaxTeleportReach = CachedIntValue.wrap(this, WeaponsConfigTranslations.MEKA_TANA_MAX_TELEPORT_REACH.applyToBuilder(builder).defineInRange("max_teleport_reach", 100, 3, 1_024));
        mekaTanaLootingEnergyUsage = CachedLongValue.definePositive(this, builder, WeaponsConfigTranslations.MEKA_TANA_LOOTING_ENERGY_USAGE, "looting_energy_usage", 125_000);
        mekaTanaFireAspectEnergyUsage = CachedLongValue.definePositive(this, builder, WeaponsConfigTranslations.MEKA_TANA_FIRE_ASPECT_ENERGY_USAGE, "fire_aspect_energy_usage", 125_000);
        mekaTanaKnockbackEnergyUsage = CachedLongValue.definePositive(this, builder, WeaponsConfigTranslations.MEKA_TANA_KNOCKBACK_ENERGY_USAGE, "knockback_energy_usage", 125_000);
        mekaTanaBaseEnergyCapacity = CachedLongValue.definePositive(this, builder, WeaponsConfigTranslations.MEKA_TANA_BASE_ENERGY_CAPACITY, "base_energy_capacity", 16_000_000);
        mekaTanaBaseChargeRate = CachedLongValue.definePositive(this, builder, WeaponsConfigTranslations.MEKA_TANA_BASE_CHARGE_RATE, "base_charge_rate", 350_000);
        mekaTanaEnchantments = CachedBooleanValue.wrap(this, WeaponsConfigTranslations.MEKA_TANA_ENCHANTMENTS.applyToBuilder(builder).define("enchantments", false));
        builder.pop();

        WeaponsConfigTranslations.MEKA_BOW.applyToBuilder(builder).push("meka_bow");
        mekaBowBaseDamage = CachedIntValue.wrap(this, WeaponsConfigTranslations.MEKA_BOW_BASE_DAMAGE.applyToBuilder(builder).define("base_damage", 32));
        mekaBowEnergyUsage = CachedLongValue.definePositive(this, builder, WeaponsConfigTranslations.MEKA_BOW_ENERGY_USAGE, "energy_usage", 625_000);
        mekaBowEnergyArrowUsage = CachedLongValue.definePositive(this, builder, WeaponsConfigTranslations.MEKA_BOW_ENERGYARROW_USAGE, "energy_arrow_usage", 625_000);
        mekabowAutoFireEnergyUsage = CachedLongValue.definePositive(this, builder, WeaponsConfigTranslations.MEKA_BOW_AUTOFIRE_ENERGY_USAGE, "autofire_energy_usage", 125_000);
        mekabowDrawSpeedUsage = CachedLongValue.definePositive(this, builder, WeaponsConfigTranslations.MEKA_BOW_DRAW_SPEED_USAGE, "draw_speed_usage", 125_000);
        mekabowGravityDampenerUsage = CachedLongValue.definePositive(this, builder, WeaponsConfigTranslations.MEKA_BOW_GRAVITY_DAMPENER_USAGE, "gravity_dampener_usage", 125_000);
        mekaBowLootingEnergyUsage = CachedLongValue.definePositive(this, builder, WeaponsConfigTranslations.MEKA_BOW_LOOTING_ENERGY_USAGE, "looting_energy_usage", 125_000);
        mekaBowPunchEnergyUsage = CachedLongValue.definePositive(this, builder, WeaponsConfigTranslations.MEKA_BOW_PUNCH_ENERGY_USAGE, "punch_energy_usage", 125_000);
        mekaBowBaseEnergyCapacity = CachedLongValue.definePositive(this, builder, WeaponsConfigTranslations.MEKA_BOW_BASE_ENERGY_CAPACITY, "base_energy_capacity", 16_000_000);
        mekaBowBaseChargeRate = CachedLongValue.definePositive(this, builder, WeaponsConfigTranslations.MEKA_BOW_BASE_CHARGE_RATE, "base_charge_rate", 350_000);
        mekaBowEnchantments = CachedBooleanValue.wrap(this, WeaponsConfigTranslations.MEKA_BOW_ENCHANTMENTS.applyToBuilder(builder).define("enchantments", false));

        WeaponsConfigTranslations.CHEMICAL_ARROWS.applyToBuilder(builder).push("chemical_arrows");
        chemicalArrowsEnergyUsage = CachedLongValue.definePositive(this, builder, WeaponsConfigTranslations.CHEMICAL_ARROWS_ENERGY_USAGE, "energy_usage", 125_000);
        chemicalArrowsCapacity = CachedLongValue.wrap(this, WeaponsConfigTranslations.CHEMICAL_ARROWS_CAPACITY.applyToBuilder(builder).defineInRange("capacity", 2_000L, 1L, Long.MAX_VALUE));
        chemicalArrowsTransferRate = CachedLongValue.wrap(this, WeaponsConfigTranslations.CHEMICAL_ARROWS_TRANSFER_RATE.applyToBuilder(builder).defineInRange("transfer_rate", 256L, 1L, Long.MAX_VALUE));
        chemicalArrowsUsage = CachedLongValue.wrap(this, WeaponsConfigTranslations.CHEMICAL_ARROWS_USAGE.applyToBuilder(builder).defineInRange("usage_per_shot", 10L, 1L, Long.MAX_VALUE));
        chemicalArrowsHydrogenBurnSeconds = CachedIntValue.wrap(this, WeaponsConfigTranslations.CHEMICAL_ARROWS_HYDROGEN_BURN.applyToBuilder(builder).defineInRange("hydrogen_burn_seconds", 5, 0, 1_000));
        chemicalArrowsSulfuricAcidDuration = CachedIntValue.wrap(this, WeaponsConfigTranslations.CHEMICAL_ARROWS_SULFURIC_ACID_DURATION.applyToBuilder(builder).defineInRange("sulfuric_acid_duration", 100, 0, 72_000));
        chemicalArrowsSulfuricAcidAmplifier = CachedIntValue.wrap(this, WeaponsConfigTranslations.CHEMICAL_ARROWS_SULFURIC_ACID_AMPLIFIER.applyToBuilder(builder).defineInRange("sulfuric_acid_amplifier", 0, 0, 9));
        chemicalArrowsHydrogenChlorideDuration = CachedIntValue.wrap(this, WeaponsConfigTranslations.CHEMICAL_ARROWS_HYDROGEN_CHLORIDE_DURATION.applyToBuilder(builder).defineInRange("hydrogen_chloride_duration", 100, 0, 72_000));
        chemicalArrowsHydrogenChlorideAmplifier = CachedIntValue.wrap(this, WeaponsConfigTranslations.CHEMICAL_ARROWS_HYDROGEN_CHLORIDE_AMPLIFIER.applyToBuilder(builder).defineInRange("hydrogen_chloride_amplifier", 0, 0, 9));
        chemicalArrowsWaterVaporDuration = CachedIntValue.wrap(this, WeaponsConfigTranslations.CHEMICAL_ARROWS_WATER_VAPOR_DURATION.applyToBuilder(builder).defineInRange("water_vapor_duration", 60, 0, 72_000));
        chemicalArrowsWaterVaporAmplifier = CachedIntValue.wrap(this, WeaponsConfigTranslations.CHEMICAL_ARROWS_WATER_VAPOR_AMPLIFIER.applyToBuilder(builder).defineInRange("water_vapor_amplifier", 0, 0, 9));
        builder.pop();
        builder.pop();

        WeaponsConfigTranslations.MEKA_GUN.applyToBuilder(builder).push("meka_gun");
        mekaGunBaseDamage = CachedIntValue.wrap(this, WeaponsConfigTranslations.MEKA_GUN_BASE_DAMAGE.applyToBuilder(builder).define("base_damage", 32));
        mekaGunEnergyUsage = CachedLongValue.definePositive(this, builder, WeaponsConfigTranslations.MEKA_GUN_ENERGY_USAGE, "energy_usage", 625_000);
        mekaGunBeamLength = CachedIntValue.wrap(this, WeaponsConfigTranslations.MEKA_GUN_BEAM_LENGTH.applyToBuilder(builder).defineInRange("beam_length", 20, 1, 1_024));
        mekaGunLootingEnergyUsage = CachedLongValue.definePositive(this, builder, WeaponsConfigTranslations.MEKA_GUN_LOOTING_ENERGY_USAGE, "looting_energy_usage", 125_000);
        mekaGunMaxHeat = CachedIntValue.wrap(this, WeaponsConfigTranslations.MEKA_GUN_MAX_HEAT.applyToBuilder(builder).defineInRange("max_heat", 100, 1, 10_000));
        mekaGunHeatPerShot = CachedIntValue.wrap(this, WeaponsConfigTranslations.MEKA_GUN_HEAT_PER_SHOT.applyToBuilder(builder).defineInRange("heat_per_shot", 5, 1, 10_000));
        mekaGunHeatLossPerSecond = CachedIntValue.wrap(this, WeaponsConfigTranslations.MEKA_GUN_HEAT_LOSS_PER_SECOND.applyToBuilder(builder).defineInRange("heat_loss_per_second", 5, 1, 10_000));
        mekaGunCooldownDelayTicks = CachedIntValue.wrap(this, WeaponsConfigTranslations.MEKA_GUN_COOLDOWN_DELAY_TICKS.applyToBuilder(builder).defineInRange("cooldown_delay_ticks", 20, 1, 10_000));
        mekaGunFireInterval = CachedIntValue.wrap(this, WeaponsConfigTranslations.MEKA_GUN_FIRE_INTERVAL.applyToBuilder(builder).defineInRange("fire_interval", 5, 1, 10_000));
        mekaGunCoolingEnergyUsage = CachedLongValue.definePositive(this, builder, WeaponsConfigTranslations.MEKA_GUN_COOLING_ENERGY_USAGE, "cooling_energy_usage", 125_000);
        mekaGunCoolingHeatLossBonus = CachedIntValue.wrap(this, WeaponsConfigTranslations.MEKA_GUN_COOLING_HEAT_LOSS.applyToBuilder(builder).defineInRange("cooling_heat_loss_bonus", 5, 0, 10_000));
        mekaGunCoolingDelayReduction = CachedIntValue.wrap(this, WeaponsConfigTranslations.MEKA_GUN_COOLING_DELAY_REDUCTION.applyToBuilder(builder).defineInRange("cooling_delay_reduction", 3, 0, 10_000));
        mekaGunLaserSoundVolume = CachedDoubleValue.wrap(this, WeaponsConfigTranslations.MEKA_GUN_LASER_SOUND_VOLUME.applyToBuilder(builder).defineInRange("laser_sound_volume", 1.0, 0.0, 1.0));
        mekaGunBaseEnergyCapacity = CachedLongValue.definePositive(this, builder, WeaponsConfigTranslations.MEKA_GUN_BASE_ENERGY_CAPACITY, "base_energy_capacity", 16_000_000);
        mekaGunBaseChargeRate = CachedLongValue.definePositive(this, builder, WeaponsConfigTranslations.MEKA_GUN_BASE_CHARGE_RATE, "base_charge_rate", 350_000);
        mekaGunEnchantments = CachedBooleanValue.wrap(this, WeaponsConfigTranslations.MEKA_GUN_ENCHANTMENTS.applyToBuilder(builder).define("enchantments", false));
        builder.pop();

        this.configSpec = builder.build();
    }

    public String getFileName() {
        return "mekaweapons";
    }

    public String getTranslation() {
        return "Mekanism Weapons";
    }

    @Override
    public ModConfigSpec getConfigSpec() {
        return configSpec;
    }

    public Type getConfigType() {
        return Type.SERVER;
    }
}
