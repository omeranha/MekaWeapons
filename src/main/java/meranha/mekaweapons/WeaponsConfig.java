package meranha.mekaweapons;

import mekanism.api.math.FloatingLong;
import mekanism.common.config.BaseMekanismConfig;
import mekanism.common.config.value.*;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class WeaponsConfig extends BaseMekanismConfig {

    private final ForgeConfigSpec configSpec;

    public final CachedFloatingLongValue wirelessChargerEnergyRate;

    public final CachedIntValue mekaTanaBaseDamage;
    public final CachedDoubleValue mekaTanaAttackSpeed;
    public final CachedFloatingLongValue mekaTanaEnergyUsage;
    public final CachedFloatingLongValue mekaTanaSweepingEnergyUsage;
    public final CachedFloatingLongValue mekaTanaTeleportUsage;
    public final CachedIntValue mekaTanaMaxTeleportReach;
    public final CachedFloatingLongValue mekaTanaLootingEnergyUsage;
    public final CachedFloatingLongValue mekaTanaBaseEnergyCapacity;
    public final CachedFloatingLongValue mekaTanaBaseChargeRate;

    public final CachedIntValue mekaBowBaseDamage;
    public final CachedFloatingLongValue mekaBowEnergyUsage;
    public final CachedFloatingLongValue mekaBowFireModeEnergyUsage;
    public final CachedFloatingLongValue mekaBowEnergyArrowUsage;
    public final CachedFloatingLongValue mekabowAutoFireEnergyUsage;
    public final CachedFloatingLongValue mekabowDrawSpeedUsage;
    public final CachedFloatingLongValue mekabowGravityDampenerUsage;
    public final CachedFloatingLongValue mekaBowLootingEnergyUsage;
    public final CachedFloatingLongValue mekaBowBaseEnergyCapacity;
    public final CachedFloatingLongValue mekaBowBaseChargeRate;

    public final CachedBooleanValue mekaTanaEnchantments;
    public final CachedBooleanValue mekaBowEnchantments;

    WeaponsConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("MekaWeapons Settings. Joules to FE conversion: 2.5J = 1FE").push("weapons");

        wirelessChargerEnergyRate = CachedFloatingLongValue.define(this, builder, "Transfer energy rate of the Wireless Charger (Magnetizer), in Joules per tick.", "wirelessChargerEnergyRate", FloatingLong.createConst(1_250_000));

        builder.comment("Meka-tana Settings").push("mekatana");
        mekaTanaBaseDamage = CachedIntValue.wrap(this, builder.comment("Base damage of the Meka-Tana, multiply it with Attack Amplification Units.").define("baseDamage", 50));
        mekaTanaAttackSpeed = CachedDoubleValue.wrap(this, builder.comment("Attack speed of the Meka-Tana.").defineInRange("attackSpeed", -2.4, -Attributes.ATTACK_SPEED.getDefaultValue(), 100));
        mekaTanaEnergyUsage = CachedFloatingLongValue.define(this, builder, "Cost in Joules of using the Meka-Tana to deal damage.", "energyUsage", FloatingLong.createConst(625_000));
        mekaTanaSweepingEnergyUsage = CachedFloatingLongValue.define(this, builder, "Additional cost in Joules of using the Meka-Tana to perform a sweeping attack.", "sweepingAttackEnergyUsage", FloatingLong.createConst(125_000));
        mekaTanaTeleportUsage = CachedFloatingLongValue.define(this, builder, "Cost in Joules of using the Meka-Tana to teleport 10 blocks.", "teleportEnergyUsage", FloatingLong.createConst(5_000));
        mekaTanaMaxTeleportReach = CachedIntValue.wrap(this, builder.comment("Maximum distance a player can teleport with the Meka-Tana.").defineInRange("maxTeleportReach", 100, 3, 1_024));
        mekaTanaLootingEnergyUsage = CachedFloatingLongValue.define(this, builder, "Cost in Joules of using the Meka-Tana to apply Looting effect to a mob.", "lootingEnergyUsage", FloatingLong.createConst(125_000));
        mekaTanaBaseEnergyCapacity = CachedFloatingLongValue.define(this, builder, "Base energy capacity of the Meka-Tana.", "baseEnergyCapacity", FloatingLong.createConst(16_000_000));
        mekaTanaBaseChargeRate = CachedFloatingLongValue.define(this, builder, "Base charge rate of the Meka-Tana.", "baseChargeRate", FloatingLong.createConst(350_000));
        mekaTanaEnchantments = CachedBooleanValue.wrap(this, builder.comment("Whether Meka-Tana can be enchanted. False by default. Use at your own risk.").define("enchantments", false));
        builder.pop();

        builder.comment("Meka-Bow Settings").push("mekabow");
        mekaBowBaseDamage = CachedIntValue.wrap(this, builder.comment("Attention: The final damage of Meka-Bow is based on how fast the arrow is going when hits, multiply it with Attack Amplification Units.").define("baseDamage", 50));
        mekaBowEnergyUsage = CachedFloatingLongValue.define(this, builder, "Cost in Joules of using the Meka-Bow.", "energyUsage", FloatingLong.createConst(625_000));
        mekaBowFireModeEnergyUsage = CachedFloatingLongValue.define(this, builder, "Cost in Joules of using the Meka-Bow with flame mode active.", "fireModeEnergyUsage", FloatingLong.createConst(825_000));
        mekaBowEnergyArrowUsage = CachedFloatingLongValue.define(this, builder, "Additional cost in Joules of firing an arrow with the Meka-Bow when the Arrow Energy Unit module is installed.", "energyArrowUsage", FloatingLong.createConst(125_000));
        mekabowAutoFireEnergyUsage = CachedFloatingLongValue.define(this, builder, "Additional cost in Joules of firing an arrow with the Meka-Bow when the Auto-Fire Unit module is installed.", "autoFireEnergyUsage", FloatingLong.createConst(100_000));
        mekabowDrawSpeedUsage = CachedFloatingLongValue.define(this, builder, "Additional cost in Joules of firing an arrow with the Meka-Bow per level of Draw Speed Unit installed.", "drawSpeedUsage", FloatingLong.createConst(50_000));
        mekabowGravityDampenerUsage = CachedFloatingLongValue.define(this, builder, "Additional cost in Joules of firing an arrow with the Meka-Bow when the Gravity Dampener Unit module is installed.", "gravityDampenerUsage", FloatingLong.createConst(75_000));
        mekaBowLootingEnergyUsage = CachedFloatingLongValue.define(this, builder, "Additional cost in Joules of firing an arrow with the Meka-Bow when the Looting Unit module is installed.", "lootingEnergyUsage", FloatingLong.createConst(125_000));
        mekaBowBaseEnergyCapacity = CachedFloatingLongValue.define(this, builder, "Base energy capacity of Meka-Bow.", "baseEnergyCapacity", FloatingLong.createConst(16_000_000));
        mekaBowBaseChargeRate = CachedFloatingLongValue.define(this, builder, "Base charge rate of Meka-Bow.", "baseChargeRate", FloatingLong.createConst(350_000));
        mekaBowEnchantments = CachedBooleanValue.wrap(this, builder.comment("Whether Meka-Bow can be enchanted. False by default. Use at your own risk.").define("enchantments", false));
        this.configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "mekaweapons";
    }

    @Override
    public ForgeConfigSpec getConfigSpec() {
        return this.configSpec;
    }

    @Override
    public ModConfig.Type getConfigType() {
        return ModConfig.Type.SERVER;
    }
}