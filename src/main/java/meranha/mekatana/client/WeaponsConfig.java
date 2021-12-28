package meranha.mekatana.client;

import mekanism.common.config.MekanismConfigHelper;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;

public class WeaponsConfig {
    public static final MekaConfig general = new MekaConfig();

    private WeaponsConfig() {
    }

    public static void registerConfigs(ModLoadingContext modLoadingContext) {
        ModContainer modContainer = modLoadingContext.getActiveContainer();
        MekanismConfigHelper.registerConfig(modContainer, general);
    }
}
