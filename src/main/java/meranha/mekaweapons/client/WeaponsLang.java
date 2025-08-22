package meranha.mekaweapons.client;

import meranha.mekaweapons.MekaWeapons;
import org.jetbrains.annotations.NotNull;

import mekanism.api.text.ILangEntry;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;

public enum WeaponsLang implements ILangEntry {
    AUTOFIRE_MODE("tooltip", "autofire_mode"),
    ARROWENERGY_MODE("tooltip", "arrowenergy_mode"),
    MAGNETIZER("tooltip", "magnetizer"),
    DRAWSPEED("tooltip", "drawspeed"),

    RADIAL_TOGGLE_ON("radial", "toggle_on"),
    RADIAL_TOGGLE_OFF("radial", "toggle_off"),
    RADIAL_ATTACK_DAMAGE("radial", "attack_damage"),
    RADIAL_ATTACK_DAMAGE_LOW("radial", "attack_damage.low"),
    RADIAL_ATTACK_DAMAGE_MEDIUM("radial", "attack_damage.medium"),
    RADIAL_ATTACK_DAMAGE_HIGH("radial", "attack_damage.high"),
    RADIAL_ATTACK_DAMAGE_SUPER("radial", "attack_damage.super_high"),
    RADIAL_ATTACK_DAMAGE_EXTREME("radial", "attack_damage.extreme"),
    RADIAL_DRAWSPEED_MODE("radial", "drawspeed_mode");

    private final String key;

    WeaponsLang(String type, String path) {
        this(Util.makeDescriptionId(type, ResourceLocation.fromNamespaceAndPath(MekaWeapons.MODID, path)));
    }

    WeaponsLang(String key) {
        this.key = key;
    }

    @Override
    public @NotNull String getTranslationKey() {
        return key;
    }
}