package meranha.mekaweapons;

import mekanism.api.gear.ModuleData;
import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.config.value.CachedLongValue;
import mekanism.common.util.StorageUtils;
import meranha.mekaweapons.items.ItemMekaBow;
import meranha.mekaweapons.items.ItemMekaTana;
import meranha.mekaweapons.items.ModuleWeaponAttackAmplificationUnit;
import net.minecraft.world.item.ItemStack;

public class MekaWeaponsUtils {
    public static long getBaseEnergyUsage(@NotNull ItemStack stack) {
        CachedLongValue energy = switch (stack.getItem()) {
            case ItemMekaBow ignored -> MekaWeapons.general.mekaBowEnergyUsage;
            case ItemMekaTana ignored -> MekaWeapons.general.mekaTanaEnergyUsage;
            default -> null;
        };

        return (energy != null) ? energy.get() : 0L;
    }

    public static long getEnergyNeeded(@Nullable ItemStack weaponStack) {
        if (weaponStack == null) {
            return -1;
        }

        long energy = getBaseEnergyUsage(weaponStack);
        if (weaponStack.getItem() instanceof ItemMekaBow) {
            if (isModuleEnabled(weaponStack, MekaWeapons.ARROWENERGY_UNIT)) {
                energy += MekaWeapons.general.mekaBowEnergyArrowUsage.get();
            }

            if (isModuleEnabled(weaponStack, MekaWeapons.AUTOFIRE_UNIT)) {
                energy += MekaWeapons.general.mekabowAutoFireEnergyUsage.get();
            }

            if (isModuleEnabled(weaponStack, MekaWeapons.GRAVITYDAMPENER_UNIT)) {
                energy += MekaWeapons.general.mekabowGravityDampenerUsage.get();
            }

            IModule<?> drawSpeedUnit = getEnabledModule(weaponStack, MekaWeapons.DRAWSPEED_UNIT);
            if (drawSpeedUnit != null) {
                energy += drawSpeedUnit.getInstalledCount() * MekaWeapons.general.mekabowDrawSpeedUsage.get();
            }
        }

        IModule<ModuleWeaponAttackAmplificationUnit> attackAmplificationUnit = getEnabledModule(weaponStack, MekaWeapons.ATTACKAMPLIFICATION_UNIT);
        if (attackAmplificationUnit != null) {
            energy *= attackAmplificationUnit.getCustomInstance().getCurrentUnit();
        }
        return energy;
    }

    public static int getBarCustomColor(@NotNull ItemStack stack) {
        if (isEnergyInsufficient(stack)) {
            return MekanismConfig.client.hudDangerColor.get();
        }

        return MekanismConfig.client.energyColor.get();
    }

    public static int getBaseDamage(@NotNull ItemStack stack) {
        CachedIntValue damage = switch (stack.getItem()) {
            case ItemMekaBow ignored -> MekaWeapons.general.mekaBowBaseDamage;
            case ItemMekaTana ignored -> MekaWeapons.general.mekaTanaBaseDamage;
            default -> null;
        };

        return (damage != null) ? damage.get() : 0;
    }

    public static long getTotalDamage(@NotNull ItemStack weapon) {
        if (isEnergyInsufficient(weapon)) {
            return 20;
        }

        long damage = getBaseDamage(weapon);
        IModule<ModuleWeaponAttackAmplificationUnit> attackAmplificationUnit = getEnabledModule(weapon, MekaWeapons.ATTACKAMPLIFICATION_UNIT);
        if (attackAmplificationUnit != null) {
            damage *= attackAmplificationUnit.getCustomInstance().getCurrentUnit();
        }
        return damage;
    }

    public static boolean isEnergyInsufficient(@Nullable IEnergyContainer energyContainer, long energyNeeded) {
        return (energyContainer == null) || energyContainer.getEnergy() < energyNeeded;
    }

    public static boolean isEnergyInsufficient(@NotNull ItemStack weapon) {
        return isEnergyInsufficient(StorageUtils.getEnergyContainer(weapon, 0), getEnergyNeeded(weapon));
    }

    @Nullable
    public static <MODULE extends ICustomModule<MODULE>> IModule<MODULE> getEnabledModule(ItemStack stack, DeferredHolder<ModuleData<?>, ModuleData<MODULE>> type) {
        return IModuleHelper.INSTANCE.getIfEnabled(stack, type);
    }

    public static boolean isModuleEnabled(ItemStack stack, Holder<ModuleData<?>> type) {
        return IModuleHelper.INSTANCE.isEnabled(stack, type);
    }
}