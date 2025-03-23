package meranha.mekaweapons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import mekanism.common.config.IConfigTranslation;
import mekanism.common.config.TranslationPreset;
import net.minecraft.Util;

public enum WeaponsConfigTranslations implements IConfigTranslation {

    MEKA_TANA("meka_tana", "Meka-Tana", "Meka-Tana Settings", true),
    MEKA_TANA_BASE_DAMAGE("meka_tana.base_damage", "Base Damage", "Base damage of the Meka-Tana, multiply it with Attack Amplification Units."),
    MEKA_TANA_ATTACK_SPEED("meka_tana.attack_speed", "Attack Speed", "Attack speed of the Meka-Tana."),
    MEKA_TANA_ENERGY_USAGE("meka_tana.energy_usage", "Energy Usage", "Cost in Joules of using the Meka-Tana to deal damage."),
    MEKA_TANA_TELEPORT_USAGE("meka_tana.teleport_energy_usage", "Teleport Energy Usage", "Cost in Joules of using the Meka-Tana to teleport 10 blocks."),
    MEKA_TANA_MAX_TELEPORT_REACH("meka_tana.max_teleport_reach", "Max Teleport Reach", "Maximum distance a player can teleport with the Meka-Tana."),
    MEKA_TANA_BASE_ENERGY_CAPACITY("meka_tana.base_energy_capacity", "Base Energy Capacity", "Base energy capacity of the Meka-Tana."),
    MEKA_TANA_BASE_CHARGE_RATE("meka_tana.base_charge_rate", "Base Charge Rate", "Base charge rate of the Meka-Tana."),
    MEKA_TANA_ENCHANTMENTS("meka_tana.enchantments", "Enchantments", "Whether the Meka-Tana can be enchanted. False by default. Use at your own risk."),

    MEKA_BOW("meka_bow", "Meka-Bow", "Meka-Bow Settings", true),
    MEKA_BOW_BASE_DAMAGE("meka_bow.base_damage", "Base Damage", "Attention: The final damage of Meka-Bow is based on how fast the arrow is going when it hits, multiply it with Attack Amplification Units."),
    MEKA_BOW_ENERGY_USAGE("meka_bow.energy_usage", "Energy Usage", "Cost in Joules of using the Meka-Bow."),
    MEKA_BOW_FIRE_MODE_ENERGY_USAGE("meka_bow.fire_mode_energy_usage", "Fire Mode Energy Usage", "Cost in Joules of using the Meka-Bow with flame mode active."),
    MEKA_BOW_BASE_ENERGY_CAPACITY("meka_bow.base_energy_capacity", "Base Energy Capacity", "Base energy capacity of Meka-Bow."),
    MEKA_BOW_BASE_CHARGE_RATE("meka_bow.base_charge_rate", "Base Charge Rate", "Base charge rate of Meka-Bow."),
    MEKA_BOW_ENCHANTMENTS("meka_bow.enchantments", "Enchantments", "Whether the Meka-Bow can be enchanted. False by default. Use at your own risk.");

    private final String key;
    private final String title;
    private final String tooltip;
    @Nullable
    private final String button;

    WeaponsConfigTranslations(@NotNull TranslationPreset preset, String type) {
        this(preset.path(type), preset.title(type), preset.tooltip(type));
    }

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
