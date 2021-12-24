package meranha.mekatana;

import mekanism.common.config.MekanismConfigHelper;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;

public class ModConfig {
    public static final MekaConfig general = new MekaConfig();

    private ModConfig() {
    }

    public static void registerConfigs(ModLoadingContext modLoadingContext) {
        ModContainer modContainer = modLoadingContext.getActiveContainer();
        MekanismConfigHelper.registerConfig(modContainer, general);
    }
}
