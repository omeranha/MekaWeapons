package meranha.mekatana;

import mekanism.api.providers.IModuleDataProvider;
import net.minecraftforge.fml.InterModComms;

public class WeaponsIMC {

    private WeaponsIMC() { }

    /**
     * This method registers a module or modules as supporting the Meka-Tool. The body of the message should either be an {@link IModuleDataProvider} or an array of
     * {@link IModuleDataProvider}s. {@link #addMekaTanaModules(IModuleDataProvider[])} can be used as a helper to send properly structured messages of this type.
     */
    public static final String ADD_MEKA_TANA_MODULES = "add_meka_tana_modules";
    /**
     * This method registers a module or modules as supporting the Meka-Tool. The body of the message should either be an {@link IModuleDataProvider} or an array of
     * {@link IModuleDataProvider}s. {@link #addMekaBowModules(IModuleDataProvider[])} can be used as a helper to send properly structured messages of this type.
     */
    public static final String ADD_MEKA_BOW_MODULES = "add_meka_bow_modules";

    public static void addModulesToAll(IModuleDataProvider<?>... moduleDataProviders) {
        addMekaBowModules(moduleDataProviders);
        addMekaTanaModules(moduleDataProviders);
    }

    public static void addMekaTanaModules(IModuleDataProvider<?>... moduleDataProviders) {
        sendModuleIMC(ADD_MEKA_TANA_MODULES, moduleDataProviders);
    }

    public static void addMekaBowModules(IModuleDataProvider<?>... moduleDataProviders) {
        sendModuleIMC(ADD_MEKA_BOW_MODULES, moduleDataProviders);
    }

    private static void sendModuleIMC(String method, IModuleDataProvider<?>... moduleDataProviders) {
        if (moduleDataProviders == null || moduleDataProviders.length == 0) {
            throw new IllegalArgumentException("No module data providers given.");
        }
        InterModComms.sendTo(MekaWeapons.MOD_ID, method, () -> moduleDataProviders);
    }
}
