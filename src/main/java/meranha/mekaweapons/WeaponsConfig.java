package meranha.mekaweapons;

import mekanism.api.math.FloatingLong;
import mekanism.common.config.BaseMekanismConfig;
import mekanism.common.config.value.CachedDoubleValue;
import mekanism.common.config.value.CachedFloatingLongValue;
import mekanism.common.config.value.CachedIntValue;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class WeaponsConfig extends BaseMekanismConfig {

    private final ForgeConfigSpec configSpec;

    public final CachedIntValue mekaTanaBaseDamage;
    public final CachedDoubleValue mekaTanaAttackSpeed;
    public final CachedFloatingLongValue mekaTanaEnergyUsage;
    public final CachedFloatingLongValue mekaTanaTeleportUsage;
    public final CachedIntValue mekaTanaMaxTeleportReach;
    public final CachedFloatingLongValue mekaTanaBaseEnergyCapacity;
    public final CachedFloatingLongValue mekaTanaBaseChargeRate;

    public final CachedIntValue mekaBowBaseDamage;
    public final CachedFloatingLongValue mekaBowEnergyUsage;
    public final CachedFloatingLongValue mekaBowFireModeEnergyUsage;
    public final CachedFloatingLongValue mekaBowBaseEnergyCapacity;
    public final CachedFloatingLongValue mekaBowBaseChargeRate;

    WeaponsConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("MekaWeapons Settings. Joules to FE conversion: 2.5J = 1FE").push("weapons");

        builder.comment("Meka-tana Settings").push("mekatana");
        mekaTanaBaseDamage = CachedIntValue.wrap(this, builder.comment("Base damage of the Meka-Tana, multiply it with Attack Amplification Units.").define("baseDamage", 50));
        mekaTanaAttackSpeed = CachedDoubleValue.wrap(this, builder.comment("Attack speed of the Meka-Tana.").defineInRange("attackSpeed", -2.4, -Attributes.ATTACK_SPEED.getDefaultValue(), 100));
        mekaTanaEnergyUsage = CachedFloatingLongValue.define(this, builder, "Cost in Joules of using the Meka-Tana to deal damage.", "energyUsage", FloatingLong.createConst(625_000));
        mekaTanaTeleportUsage = CachedFloatingLongValue.define(this, builder, "Cost in Joules of using the Meka-Tana to teleport 10 blocks.", "teleportEnergyUsage", FloatingLong.createConst(5_000));
        mekaTanaMaxTeleportReach = CachedIntValue.wrap(this, builder.comment("Maximum distance a player can teleport with the Meka-Tana.").defineInRange("maxTeleportReach", 100, 3, 1_024));
        mekaTanaBaseEnergyCapacity = CachedFloatingLongValue.define(this, builder, "Base energy capacity of the Meka-Tana.", "baseEnergyCapacity", FloatingLong.createConst(16_000_000));
        mekaTanaBaseChargeRate = CachedFloatingLongValue.define(this, builder, "Base charge rate of the Meka-Tana.", "baseChargeRate", FloatingLong.createConst(350_000));
        builder.pop();

        builder.comment("Meka-Bow Settings").push("mekabow");
        mekaBowBaseDamage = CachedIntValue.wrap(this, builder.comment("Attention: The final damage of Meka-Bow is based on how fast the arrow is going when hits, multiply it with Attack Amplification Units.").define("baseDamage", 50));
        mekaBowEnergyUsage = CachedFloatingLongValue.define(this, builder, "Cost in Joules of using the Meka-Bow.", "energyUsage", FloatingLong.createConst(625_000));
        mekaBowFireModeEnergyUsage = CachedFloatingLongValue.define(this, builder, "Cost in Joules of using the Meka-Bow with flame mode active.", "fireModeEnergyUsage", FloatingLong.createConst(825_000));
        mekaBowBaseEnergyCapacity = CachedFloatingLongValue.define(this, builder, "Base energy capacity of Meka-Bow.", "baseEnergyCapacity", FloatingLong.createConst(16_000_000));
        mekaBowBaseChargeRate = CachedFloatingLongValue.define(this, builder, "Base charge rate of Meka-Bow.", "baseChargeRate", FloatingLong.createConst(350_000));
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
