package meranha.mekaweapons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import mekanism.common.config.IConfigTranslation;
import net.minecraft.Util;

public enum WeaponsConfigTranslations implements IConfigTranslation {
    MEKA_TANA("meka_tana", "Meka-Tana", "Meka-Tana Settings", true),
    MEKA_TANA_BASE_DAMAGE("meka_tana.base_damage", "Base Damage", "Base damage of Meka-Tana"),
    MEKA_TANA_ATTACK_SPEED("meka_tana.attack_speed", "Attack Speed", "Attack speed of Meka-Tana."),
    MEKA_TANA_ENERGY_USAGE("meka_tana.energy_usage", "Energy Usage", "Cost in Joules of using Meka-Tana."),
    MEKA_TANA_SWEEPING_ATTACK("meka_tana.sweeping_attack", "Sweeping Attack Energy Usage", "Additional cost in Joules of using Meka-Tana to perform a sweeping attack."),
    MEKA_TANA_TELEPORT_USAGE("meka_tana.teleport_energy_usage", "Teleport Energy Usage", "Cost in Joules of using Meka-Tana to teleport 10 blocks."),
    MEKA_TANA_MAX_TELEPORT_REACH("meka_tana.max_teleport_reach", "Max Teleport Reach", "Maximum distance a player can teleport with Meka-Tana."),
    MEKA_TANA_LOOTING_ENERGY_USAGE("meka_tana.looting_energy_usage", "Looting Energy Usage", "Additional cost in Joules of using Meka-Tana to apply Looting effect to a mob."),
    MEKA_TANA_FIRE_ASPECT_ENERGY_USAGE("meka_tana.fire_aspect_energy_usage", "Fire Aspect Energy Usage", "Additional cost in Joules of using Meka-Tana with the Fire Aspect Unit active."),
    MEKA_TANA_KNOCKBACK_ENERGY_USAGE("meka_tana.knockback_energy_usage", "Knockback Energy Usage", "Additional cost in Joules of using Meka-Tana with the Knockback Unit active."),
    MEKA_TANA_BASE_ENERGY_CAPACITY("meka_tana.base_energy_capacity", "Base Energy Capacity", "Base energy capacity of Meka-Tana."),
    MEKA_TANA_BASE_CHARGE_RATE("meka_tana.base_charge_rate", "Base Charge Rate", "Base charge rate of Meka-Tana."),
    MEKA_TANA_ENCHANTMENTS("meka_tana.enchantments", "Enchantments", "Whether Meka-Tana can be enchanted. False by default. Use at your own risk."),

    MEKA_BOW("meka_bow", "Meka-Bow", "Meka-Bow Settings", true),
    MEKA_BOW_BASE_DAMAGE("meka_bow.base_damage", "Base Damage", "Base Damage of Meka-Bow"),
    MEKA_BOW_ENERGY_USAGE("meka_bow.energy_usage", "Energy Usage", "Cost in Joules of using Meka-Bow."),
    MEKA_BOW_ENERGYARROW_USAGE("meka_bow.energy_arrow_usage", "Energy Arrows Unit Usage", "Additional cost in Joules of using Meka-Bow with Energy Arrow Unit active, per shot."),
    MEKA_BOW_AUTOFIRE_ENERGY_USAGE("meka_bow.autofire_energy_usage", "Auto-Fire Energy Usage", "Additional cost in Joules of using Meka-Bow with Auto-Fire active, per shot."),
    MEKA_BOW_DRAW_SPEED_USAGE("meka_bow.draw_speed_usage", "Draw Speed Usage", "Additional, cost in Joules of using Meka-Bow with Draw Speed Unit active, multiplied by unit amount, per shot."),
    MEKA_BOW_GRAVITY_DAMPENER_USAGE("meka_bow.gravity_dampener_usage", "Gravity Dampener Usage", "Additional cost in Joules of using Meka-Bow with Gravity Dampener Unit active, per shot."),
    MEKA_BOW_LOOTING_ENERGY_USAGE("meka_bow.looting_energy_usage", "Looting Energy Usage", "Additional cost in Joules of using Meka-Bow to apply Looting effect"),
    MEKA_BOW_PUNCH_ENERGY_USAGE("meka_bow.punch_energy_usage", "Punch Energy Usage", "Additional cost in Joules of using Meka-Bow with the Punch Unit active, per shot."),
    MEKA_BOW_BASE_ENERGY_CAPACITY("meka_bow.base_energy_capacity", "Base Energy Capacity", "Base energy capacity of Meka-Bow."),
    MEKA_BOW_BASE_CHARGE_RATE("meka_bow.base_charge_rate", "Base Charge Rate", "Base charge rate of Meka-Bow."),
    MEKA_BOW_ENCHANTMENTS("meka_bow.enchantments", "Enchantments", "Whether Meka-Bow can be enchanted. False by default. Use at your own risk."),

    CHEMICAL_ARROWS("meka_bow.chemical_arrows", "Chemical Arrows", "Chemical Arrows Unit Settings", true),
    CHEMICAL_ARROWS_ENERGY_USAGE("meka_bow.chemical_arrows.energy_usage", "Energy Usage", "Additional cost in Joules of using Meka-Bow with the Chemical Arrows Unit active, per shot."),
    CHEMICAL_ARROWS_CAPACITY("meka_bow.chemical_arrows.capacity", "Capacity", "Chemical capacity in mB of the tank the Chemical Arrows Unit adds to Meka-Bow."),
    CHEMICAL_ARROWS_TRANSFER_RATE("meka_bow.chemical_arrows.transfer_rate", "Transfer Rate", "Chemical transfer rate in mB per tick of the tank the Chemical Arrows Unit adds to Meka-Bow."),
    CHEMICAL_ARROWS_USAGE("meka_bow.chemical_arrows.usage_per_shot", "Usage Per Shot", "Chemical consumed in mB per arrow infused by the Chemical Arrows Unit. Arrows are fired without an effect when the tank runs dry."),
    CHEMICAL_ARROWS_HYDROGEN_BURN("meka_bow.chemical_arrows.hydrogen_burn_seconds", "Hydrogen Burn Seconds", "Seconds a target hit by a hydrogen infused arrow burns for. Set to 0 to disable hydrogen arrows."),
    CHEMICAL_ARROWS_SULFURIC_ACID_DURATION("meka_bow.chemical_arrows.sulfuric_acid_duration", "Sulfuric Acid Duration", "Duration in ticks of the Poison applied by a sulfuric acid infused arrow. Set to 0 to disable sulfuric acid arrows."),
    CHEMICAL_ARROWS_SULFURIC_ACID_AMPLIFIER("meka_bow.chemical_arrows.sulfuric_acid_amplifier", "Sulfuric Acid Amplifier", "Amplifier of the Poison applied by a sulfuric acid infused arrow. 0 is level I."),
    CHEMICAL_ARROWS_HYDROGEN_CHLORIDE_DURATION("meka_bow.chemical_arrows.hydrogen_chloride_duration", "Hydrogen Chloride Duration", "Duration in ticks of the Weakness applied by a hydrogen chloride infused arrow. Set to 0 to disable hydrogen chloride arrows."),
    CHEMICAL_ARROWS_HYDROGEN_CHLORIDE_AMPLIFIER("meka_bow.chemical_arrows.hydrogen_chloride_amplifier", "Hydrogen Chloride Amplifier", "Amplifier of the Weakness applied by a hydrogen chloride infused arrow. 0 is level I."),
    CHEMICAL_ARROWS_WATER_VAPOR_DURATION("meka_bow.chemical_arrows.water_vapor_duration", "Water Vapor Duration", "Duration in ticks of the Slowness applied by a water vapor infused arrow, which also extinguishes the target. Set to 0 to disable water vapor arrows."),
    CHEMICAL_ARROWS_WATER_VAPOR_AMPLIFIER("meka_bow.chemical_arrows.water_vapor_amplifier", "Water Vapor Amplifier", "Amplifier of the Slowness applied by a water vapor infused arrow. 0 is level I."),

    MEKA_GUN("meka_gun", "Meka-Gun", "Meka-Gun Settings", true),
    MEKA_GUN_BASE_DAMAGE("meka_gun.base_damage", "Base Damage", "Base damage of Meka-Gun"),
    MEKA_GUN_ENERGY_USAGE("meka_gun.energy_usage", "Energy Usage", "Cost in Joules of using Meka-Gun."),
    MEKA_GUN_BEAM_LENGTH("meka_gun.beam_length", "Beam Length", "Laser beam length of Meka-Gun, in blocks."),
    MEKA_GUN_MAX_HEAT("meka_gun.max_heat", "Max Heat", "Max heat percentage of Meka-Gun."),
    MEKA_GUN_LOOTING_ENERGY_USAGE("meka_bow.looting_energy_usage", "Looting Energy Usage", "Additional cost in Joules of using Meka-Gun to apply Looting effect"),
    MEKA_GUN_HEAT_PER_SHOT("meka_gun.heat_per_shot", "Heat Per Shot", "Heat percentage added to Meka-Gun per shot."),
    MEKA_GUN_HEAT_LOSS_PER_SECOND("meka_gun.heat_loss_per_second", "Heat Loss Per Second", "Heat percentage lost by Meka-Gun per second."),
    MEKA_GUN_COOLDOWN_DELAY_TICKS("meka_gun.cooldown_delay_ticks", "Cooldown Delay Ticks", "Ticks to wait before Meka-Gun starts cooling down."),
    MEKA_GUN_FIRE_INTERVAL("meka_gun.fire_interval", "Fire Interval", "Ticks between shots of Meka-Gun."),
    MEKA_GUN_COOLING_ENERGY_USAGE("meka_gun.cooling_energy_usage", "Cooling Energy Usage", "Additional cost in Joules of using Meka-Gun, multiplied by the amount of installed Cooling Units."),
    MEKA_GUN_COOLING_HEAT_LOSS("meka_gun.cooling_heat_loss_bonus", "Cooling Heat Loss Bonus", "Extra heat percentage lost per second for each installed Cooling Unit."),
    MEKA_GUN_COOLING_DELAY_REDUCTION("meka_gun.cooling_delay_reduction", "Cooling Delay Reduction", "Ticks removed from the Meka-Gun cooldown delay for each installed Cooling Unit."),
    MEKA_GUN_LASER_SOUND_VOLUME("meka_gun.laser_sound_volume", "Laser Sound Volume", "Volume of the looping Meka-Gun laser sound. Set to 0 to mute it."),
    MEKA_GUN_BASE_ENERGY_CAPACITY("meka_gun.base_energy_capacity", "Base Energy Capacity", "Base energy capacity of Meka-Gun."),
    MEKA_GUN_BASE_CHARGE_RATE("meka_gun.base_charge_rate", "Base Charge Rate", "Base charge rate of Meka-Gun."),
    MEKA_GUN_ENCHANTMENTS("meka_gun.enchantments", "Enchantments", "Whether Meka-Gun can be enchanted. False by default. Use at your own risk."),

    WIRELESS_CHARGE_RATE("wireless_charger_energy_rate", "Wireless Charger Energy Rate", "Transfer energy rate of the Wireless Charger (Magnetizer), in Joules per tick."),
    MODULE_DAMAGE_MODE("module_damage_mode", "Module Damage Mode", "Determines the damage mode of advanced attack amplification modules."),
    MODULE_DAMAGE_CUSTOM("module_damage_custom", "Module Custom Damage", "Determines how much damage advanced attack amplification modules add to weapons. Used only when damage_mode is CUSTOM."),

    MODULE_LIMITS("module_limits", "Module Limits", "How many copies of each Mekanism: Weapons module can be installed on a single weapon. Lowering a limit does not remove modules that are already installed.", true),
    MAX_ATTACK_AMPLIFICATION_UNITS("module_limits.max_attack_amplification_units", "Max Attack Amplification Units", "Maximum amount of Advanced Attack Amplification Units installable on a weapon. Each unit unlocks one extra damage step."),
    MAX_DRAW_SPEED_UNITS("module_limits.max_draw_speed_units", "Max Draw Speed Units", "Maximum amount of Draw Speed Units installable on Meka-Bow. Each unit unlocks one extra draw speed step."),
    MAX_LOOTING_UNITS("module_limits.max_looting_units", "Max Looting Units", "Maximum amount of Looting Units installable on a weapon. Acts as the Looting enchantment level."),
    MAX_PUNCH_UNITS("module_limits.max_punch_units", "Max Punch Units", "Maximum amount of Punch Units installable on Meka-Bow. Acts as the Punch enchantment level."),
    MAX_FIRE_ASPECT_UNITS("module_limits.max_fire_aspect_units", "Max Fire Aspect Units", "Maximum amount of Fire Aspect Units installable on Meka-Tana. Acts as the Fire Aspect enchantment level."),
    MAX_KNOCKBACK_UNITS("module_limits.max_knockback_units", "Max Knockback Units", "Maximum amount of Knockback Units installable on Meka-Tana. Acts as the Knockback enchantment level."),
    MAX_COOLING_UNITS("module_limits.max_cooling_units", "Max Cooling Units", "Maximum amount of Cooling Units installable on Meka-Gun.");


    private final String key;
    private final String title;
    private final String tooltip;
    @Nullable
    private final String button;

    WeaponsConfigTranslations(String path, String title, String tooltip) {
        this(path, title, tooltip, false);
    }

    WeaponsConfigTranslations(String path, String title, String tooltip, boolean isSection) {
        this(path, title, tooltip, IConfigTranslation.getSectionTitle(title, isSection));
    }

    WeaponsConfigTranslations(String path, String title, String tooltip, @Nullable String button) {
        this.key = Util.makeDescriptionId("configuration", MekaWeapons.rl(path));
        this.title = title;
        this.tooltip = tooltip;
        this.button = button;
    }

    @NotNull
    public String getTranslationKey() {
        return key;
    }

    public String title() {
        return title;
    }

    public String tooltip() {
        return tooltip;
    }

    @Nullable
    public String button() {
        return button;
    }
}
