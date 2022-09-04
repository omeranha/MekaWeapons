package meranha.mekatana;

import mekanism.api.math.FloatingLong;
import mekanism.common.config.BaseMekanismConfig;
import mekanism.common.config.value.CachedFloatingLongValue;
import mekanism.common.config.value.CachedIntValue;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public class WeaponsConfig extends BaseMekanismConfig {

    private final ForgeConfigSpec configSpec;

    public final CachedIntValue mekaTanaMinDamage;
    public final CachedIntValue mekaTanaMaxDamage;
    public final CachedFloatingLongValue mekaTanaEnergyUsage;
    public final CachedFloatingLongValue mekaTanaTeleportUsage;
    public final CachedIntValue mekaTanaMaxTeleportReach;

    public final CachedFloatingLongValue mekaBowEnergyUsage;
    public final CachedFloatingLongValue mekaBowEnergyUsageFire;
    public final CachedIntValue mekaBowDamage;

    public final CachedFloatingLongValue mekaDrillEnergyCapacity;
    public final CachedFloatingLongValue mekaDrillEnergyUsage;
    public final CachedFloatingLongValue mekaDrillChargeRate;

    WeaponsConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("MekaWeapons Settings. This config is synced from server to client. Joules to FE conversion: 1FE = 2.5J").push("weapons");

        builder.comment("Meka-tana Settings.").push("mekatana");
        mekaTanaMinDamage = CachedIntValue.wrap(this, builder.comment("Meka-Tana min damage.").define("mekaTanaMinDamage", 10));
        mekaTanaMaxDamage = CachedIntValue.wrap(this, builder.comment("Meka-Tana max damage.").define("mekaTanaMaxDamage", 100));
        mekaTanaEnergyUsage = CachedFloatingLongValue.define(this, builder, "Cost in Joules of using the Meka-Tana to deal damage.", "mekaTanaEnergyUsage", FloatingLong.createConst(4_000));
        mekaTanaTeleportUsage = CachedFloatingLongValue.define(this, builder, "Cost in Joules of using the Meka-Tana to teleport 10 blocks.", "mekaTanaTeleportUsage", FloatingLong.createConst(2_000));
        mekaTanaMaxTeleportReach = CachedIntValue.wrap(this, builder.comment("Maximum distance a player can teleport with the Meka-Tana.").define("maxTanaReach", 100));
        builder.pop();

        builder.comment("Meka-Bow Settings.").push("mekabow");
        mekaBowEnergyUsage = CachedFloatingLongValue.define(this, builder, "Cost in Joules of using the Meka-Bow.", "energyUsage", FloatingLong.createConst(2000));
        mekaBowEnergyUsageFire = CachedFloatingLongValue.define(this, builder, "Cost in Joules of using the Meka-Bow with flame mode active.", "energyUsageFire", FloatingLong.createConst(4000));
        mekaBowDamage = CachedIntValue.wrap(this, builder.comment("Be careful! The final damage of Meka-Bow is based on how fast the arrow is going when hits.").define("mekaBowDamage", 25));
        builder.pop(2);

        mekaDrillEnergyCapacity = CachedFloatingLongValue.define(this, builder, "Energy capacity in Joules of the Meka-Drill.", "energyCapacity", FloatingLong.createConst(125000));
        mekaDrillEnergyUsage = CachedFloatingLongValue.define(this, builder, "Cost in Joules of using the Meka-Drill.", "energyUsage", FloatingLong.createConst(1000));
        mekaDrillChargeRate = CachedFloatingLongValue.define(this, builder, "Energy charge rate in Joules of the Meka-Drill.", "chargeRate", FloatingLong.createConst(25000));

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
        return ModConfig.Type.CLIENT;
    }
}