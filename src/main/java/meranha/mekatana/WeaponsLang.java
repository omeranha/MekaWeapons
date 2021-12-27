package meranha.mekatana;

import mekanism.api.text.ILangEntry;
import mekanism.common.Mekanism;
import net.minecraft.util.Util;

public enum WeaponsLang implements ILangEntry {
    AUTOFIRE_MODE("tooltip", "autofire_mode"),
    ;

    private final String key;

    WeaponsLang(String type, String path) {
        this(Util.makeDescriptionId(type, Mekanism.rl(path)));
    }

    WeaponsLang(String key) {
        this.key = key;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }
}
