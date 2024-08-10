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

        builder.comment("Meka-tana Settings").push("mekatana");
        mekaTanaBaseDamage = CachedIntValue.wrap(this, builder.comment("Base damage of the Meka-Tana, multiply it with Attack Amplification Units.").define("baseDamage", 50));
        mekaTanaAttackSpeed = CachedDoubleValue.wrap(this, builder.comment("Attack speed of the Meka-Tana.").defineInRange("attackSpeed", -2.4, -Attributes.ATTACK_SPEED.value().getDefaultValue(), 100));
        mekaTanaEnergyUsage = CachedLongValue.definePositive(this, builder, "Cost in Joules of using the Meka-Tana to deal damage.", "energyUsage", 625_000);
        mekaTanaTeleportUsage = CachedLongValue.definePositive(this, builder, "Cost in Joules of using the Meka-Tana to teleport 10 blocks.", "teleportEnergyUsage", 5_000);
        mekaTanaMaxTeleportReach = CachedIntValue.wrap(this, builder.comment("Maximum distance a player can teleport with the Meka-Tana.").defineInRange("maxTeleportReach", 100, 3, 1_024));
        mekaTanaBaseEnergyCapacity = CachedLongValue.definePositive(this, builder, "Base energy capacity of the Meka-Tana.", "baseEnergyCapacity", 16_000_000);
        mekaTanaBaseChargeRate = CachedLongValue.definePositive(this, builder, "Base charge rate of the Meka-Tana.", "baseChargeRate", 350_000);
        builder.pop();

        builder.comment("Meka-Bow Settings").push("mekabow");
        mekaBowBaseDamage = CachedIntValue.wrap(this, builder.comment("Attention: The final damage of Meka-Bow is based on how fast the arrow is going when hits, multiply it with Attack Amplification Units.").define("baseDamage", 50));
        mekaBowEnergyUsage = CachedLongValue.definePositive(this, builder, "Cost in Joules of using the Meka-Bow.", "energyUsage", 625_000);
        mekaBowFireModeEnergyUsage = CachedLongValue.definePositive(this, builder, "Cost in Joules of using the Meka-Bow with flame mode active.", "fireModeEnergyUsage", 825_000);
        mekaBowBaseEnergyCapacity = CachedLongValue.definePositive(this, builder, "Base energy capacity of Meka-Bow.", "baseEnergyCapacity", 16_000_000);
        mekaBowBaseChargeRate = CachedLongValue.definePositive(this, builder, "Base charge rate of Meka-Bow.", "baseChargeRate", 350_000);
        this.configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "mekaweapons";
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
