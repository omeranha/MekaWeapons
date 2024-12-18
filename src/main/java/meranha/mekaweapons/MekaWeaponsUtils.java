package meranha.mekaweapons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.api.providers.IModuleDataProvider;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.value.CachedFloatingLongValue;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.config.value.CachedLongValue;
import mekanism.common.util.StorageUtils;
import meranha.mekaweapons.items.ItemMekaBow;
import meranha.mekaweapons.items.ItemMekaTana;
import meranha.mekaweapons.items.ModuleWeaponAttackAmplificationUnit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class MekaWeaponsUtils {
    public static int getBaseDamage(@NotNull ItemStack stack) {
        Item weapon = stack.getItem();
        CachedIntValue value;

        if(weapon instanceof ItemMekaBow) 
            value = MekaWeapons.general.mekaBowBaseDamage;
        else if(weapon instanceof ItemMekaTana) 
            value = MekaWeapons.general.mekaTanaBaseDamage; 
        else
            value = null;

        return value != null ? value.get() : 0;
    }

    public static long getBaseEnergyUsage(@NotNull ItemStack stack) {
        Item weapon = stack.getItem();
        CachedFloatingLongValue value;

        if(weapon instanceof ItemMekaBow) {
            value = MekaWeapons.general.mekaBowEnergyUsage;
        } else if(weapon instanceof ItemMekaTana) {
            value = MekaWeapons.general.mekaTanaEnergyUsage;
        } else {
            value = null;
        }

        return value != null ? value.get().longValue() : 0;
    }

    public static long getTotalDamage(@NotNull ItemStack weapon) {
        return getTotalDamage(weapon, getEnabledModule(weapon, MekaWeapons.ATTACKAMPLIFICATION_UNIT), getBaseDamage(weapon), getBaseEnergyUsage(weapon));
    }

    public static long getTotalDamage(@NotNull ItemStack weapon, @Nullable IModule<ModuleWeaponAttackAmplificationUnit> attackAmplificationUnit, int baseDamage, long energyUsage) {
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(weapon, 0);
        if(hasNotEnoughEnergy(energyContainer, energyUsage)) {
            return -1;
        }

        long damage = baseDamage;
        if (attackAmplificationUnit != null) {
            int unitDamage = attackAmplificationUnit.getCustomInstance().getCurrentUnit(), additionalDamage = (unitDamage - 1) * baseDamage;
            long energyCost = getEnergyNeeded(unitDamage, energyUsage);
            if (hasNotEnoughEnergy(energyContainer, energyCost)) {
                //If we don't have enough power use it at a reduced power level (this will be false the majority of the time)
                damage += Math.round(additionalDamage * (energyContainer.getEnergy().divide(FloatingLong.create(energyCost)).floatValue()));
            } else {
                damage += additionalDamage;
            }
        }

        return damage - 1;
    }

    public static long getEnergyNeeded(@Nullable ItemStack weaponStack) {
        return getEnergyNeeded(weaponStack, getBaseEnergyUsage(weaponStack));
    }

    public static long getEnergyNeeded(@Nullable ItemStack weaponStack, long energyUsage) {
        IModule<ModuleWeaponAttackAmplificationUnit> attackAmplificationUnit = getEnabledModule(weaponStack, MekaWeapons.ATTACKAMPLIFICATION_UNIT);
        if (attackAmplificationUnit != null) {
            return getEnergyNeeded(attackAmplificationUnit.getCustomInstance().getCurrentUnit(), energyUsage);
        }
        return -1;
    }

    public static long getEnergyNeeded(int unitDamage, long energyUsage) {
        return MathUtils.clampToLong(energyUsage * unitDamage);
    }

    public static int getBarCustomColor(@NotNull ItemStack stack) {
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        long energyUsage = getBaseEnergyUsage(stack);
        if(hasNotEnoughEnergy(energyContainer, energyUsage)) {
            return MekanismConfig.client.hudDangerColor.get();
        }

        long energyNeeded = getEnergyNeeded(stack, energyUsage);
        if (hasNotEnoughEnergy(energyContainer, energyNeeded)) {
            return MekanismConfig.client.hudWarningColor.get();
        }

        return MekanismConfig.client.energyColor.get();
    }

    public static boolean hasNotEnoughEnergy(@Nullable IEnergyContainer energyContainer, long minEnergy) {
        return energyContainer == null || energyContainer.getEnergy().smallerThan(FloatingLong.create(minEnergy));
    }

    @Nullable
    public static <MODULE extends ICustomModule<MODULE>> IModule<MODULE> getEnabledModule(ItemStack stack, IModuleDataProvider<MODULE> typeProvider) {
        IModule<MODULE> module = IModuleHelper.INSTANCE.load(stack, typeProvider);
        return module != null && module.isEnabled() ? module : null;
    }

    public static boolean isModuleEnabled(ItemStack stack, IModuleDataProvider<?> type) {
        return IModuleHelper.INSTANCE.isEnabled(stack, type);
    }
}