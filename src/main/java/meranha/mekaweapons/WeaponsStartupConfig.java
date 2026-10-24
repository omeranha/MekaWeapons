package meranha.mekaweapons;

import mekanism.common.config.BaseMekanismConfig;
import mekanism.common.config.value.CachedIntValue;
import meranha.mekaweapons.items.modules.AdvancedAttackAmplificationUnit;
import meranha.mekaweapons.items.modules.DrawSpeedUnit;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Module install limits have to be known while the modules are being registered, so they live in a startup config
 * instead of the server config.
 */
public class WeaponsStartupConfig extends BaseMekanismConfig {

    private static final int ATTACK_AMPLIFICATION_STEPS = AdvancedAttackAmplificationUnit.AttackDamage.values().length - 2;
    private static final int DRAW_SPEED_STEPS = DrawSpeedUnit.DrawSpeed.values().length - 1;

    private final ModConfigSpec configSpec;

    public final CachedIntValue maxAttackAmplificationUnits;
    public final CachedIntValue maxDrawSpeedUnits;
    public final CachedIntValue maxLootingUnits;
    public final CachedIntValue maxPunchUnits;
    public final CachedIntValue maxFireAspectUnits;
    public final CachedIntValue maxKnockbackUnits;
    public final CachedIntValue maxCoolingUnits;

    WeaponsStartupConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("MekaWeapons startup settings. These are read while the game is loading, so they cannot be changed per world.");

        WeaponsConfigTranslations.MODULE_LIMITS.applyToBuilder(builder).push("module_limits");
        maxAttackAmplificationUnits = CachedIntValue.wrap(this, WeaponsConfigTranslations.MAX_ATTACK_AMPLIFICATION_UNITS.applyToBuilder(builder).defineInRange("max_attack_amplification_units", ATTACK_AMPLIFICATION_STEPS, 1, ATTACK_AMPLIFICATION_STEPS));
        maxDrawSpeedUnits = CachedIntValue.wrap(this, WeaponsConfigTranslations.MAX_DRAW_SPEED_UNITS.applyToBuilder(builder).defineInRange("max_draw_speed_units", DRAW_SPEED_STEPS, 1, DRAW_SPEED_STEPS));
        maxLootingUnits = CachedIntValue.wrap(this, WeaponsConfigTranslations.MAX_LOOTING_UNITS.applyToBuilder(builder).defineInRange("max_looting_units", 5, 1, 10));
        maxPunchUnits = CachedIntValue.wrap(this, WeaponsConfigTranslations.MAX_PUNCH_UNITS.applyToBuilder(builder).defineInRange("max_punch_units", 2, 1, 10));
        maxFireAspectUnits = CachedIntValue.wrap(this, WeaponsConfigTranslations.MAX_FIRE_ASPECT_UNITS.applyToBuilder(builder).defineInRange("max_fire_aspect_units", 2, 1, 10));
        maxKnockbackUnits = CachedIntValue.wrap(this, WeaponsConfigTranslations.MAX_KNOCKBACK_UNITS.applyToBuilder(builder).defineInRange("max_knockback_units", 2, 1, 10));
        maxCoolingUnits = CachedIntValue.wrap(this, WeaponsConfigTranslations.MAX_COOLING_UNITS.applyToBuilder(builder).defineInRange("max_cooling_units", 4, 1, 10));
        builder.pop();

        this.configSpec = builder.build();
    }

    public String getFileName() {
        return "mekaweapons-startup";
    }

    public String getTranslation() {
        return "Mekanism Weapons Startup";
    }

    @Override
    public ModConfigSpec getConfigSpec() {
        return configSpec;
    }

    public Type getConfigType() {
        return Type.STARTUP;
    }
}
