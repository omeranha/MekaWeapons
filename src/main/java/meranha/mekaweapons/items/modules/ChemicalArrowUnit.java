package meranha.mekaweapons.items.modules;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.config.ModuleConfig;
import meranha.mekaweapons.MekaWeapons;
import meranha.mekaweapons.MekaWeaponsUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Backs the Chemical Arrows Unit. Exposes a momentary "dump" boolean config in the module tweaker: setting it to true
 * empties the Meka-Bow chemical tank on the next server tick and immediately resets itself back to false.
 */
@ParametersAreNotNullByDefault
public class ChemicalArrowUnit implements ICustomModule<ChemicalArrowUnit> {

    public static final ResourceLocation DUMP = MekaWeapons.rl("chemicalarrow_dump");

    @Override
    public void tickServer(IModule<ChemicalArrowUnit> module, IModuleContainer moduleContainer, ItemStack stack, Player player) {
        ModuleConfig<Boolean> dumpConfig = module.getConfig(DUMP);
        if (dumpConfig == null || !dumpConfig.get()) {
            return;
        }

        IChemicalTank tank = MekaWeaponsUtils.getChemicalTank(stack);
        if (tank != null && !tank.isEmpty()) {
            tank.setEmpty();
        }

        moduleContainer.replaceModuleConfig(player.registryAccess(), stack, module.getDataHolder(), dumpConfig.with(false));
    }
}
