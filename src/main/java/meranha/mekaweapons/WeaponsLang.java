package meranha.mekaweapons;

import mekanism.api.text.ILangEntry;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public enum WeaponsLang implements ILangEntry {
    AUTOFIRE_MODE("tooltip", "autofire_mode"),
    AUTOFIRE_MODE_CHANGE("tooltip", "autofire_mode_change"),
    ARROWENERGY_MODE("tooltip", "arrowenergy_mode"),
    MAGNETIZER("tooltip", "magnetizer"),
    ATTACKDAMAGE("tooltip", "attackdamage");

    WeaponsLang(String type, String path) {
        this(Util.makeDescriptionId(type, new ResourceLocation(MekaWeapons.MODID, path)));
    }

    private final String key;
    WeaponsLang(String key) {
        this.key = key;
    }

    @Override
    public @NotNull String getTranslationKey() {
        return key;
    }
}