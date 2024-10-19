package meranha.mekaweapons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.math.MathUtils;
import mekanism.api.providers.IModuleDataProvider;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.config.value.CachedLongValue;
import mekanism.common.util.StorageUtils;
import meranha.mekaweapons.items.ItemMekaBow;
import meranha.mekaweapons.items.ItemMekaTana;
import meranha.mekaweapons.items.ModuleWeaponAttackAmplificationUnit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class MekaWeaponsUtils {
    public static int getBaseDamage(ItemStack stack) {
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

    public static long getBaseEnergyUsage(ItemStack stack) {
        Item weapon = stack.getItem();
        CachedLongValue value;

        if(weapon instanceof ItemMekaBow) {
            value = MekaWeapons.general.mekaBowEnergyUsage;
        } else if(weapon instanceof ItemMekaTana) {
            value = MekaWeapons.general.mekaTanaEnergyUsage;
        } else {
            value = null;
        }

        return value != null ? value.get() : 0L;
    }

    public static long getTotalDamage(@NotNull ItemStack weapon) {
        return getTotalDamage(weapon, getEnabledModule(weapon, MekaWeapons.ATTACKAMPLIFICATION_UNIT), getBaseDamage(weapon), getBaseEnergyUsage(weapon));
    }
    
    public static long getTotalDamage(@NotNull ItemStack weapon, @Nullable IModule<ModuleWeaponAttackAmplificationUnit> attackAmplificationUnit, @NotNull CachedIntValue baseDamage, @NotNull CachedLongValue energyUsage) {
        return getTotalDamage(weapon, attackAmplificationUnit, baseDamage.get(), energyUsage.get());
    }

    public static long getTotalDamage(@NotNull ItemStack weapon, @Nullable IModule<ModuleWeaponAttackAmplificationUnit> attackAmplificationUnit, int baseDamage, long energyUsage) {
        boolean isCreative = weapon.getItemHolder() instanceof Player player && player.isCreative();
        // TODO I was trying to check if the holder is a creative mode player,
        // but this seems not to be a player when called from this method, as the log below prints false.
        /*
        MekaWeapons.logger.info("Holder is Player: {}", weapon.getItemHolder() instanceof Player);
        if(weapon.getItemHolder() instanceof Player player) {
            MekaWeapons.logger.info("Creative: {}", player.isCreative());
        }
        */
        
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(weapon, 0);
        long energy = energyContainer != null ? energyContainer.getEnergy() : 0;
        if(energy < energyUsage && !isCreative) {
            return -1;
        }

        long damage = baseDamage;
        if (attackAmplificationUnit != null) {
            int unitDamage = attackAmplificationUnit.getCustomInstance().getCurrentUnit(), additionalDamage = (unitDamage - 1) * baseDamage;
            long energyCost = getEnergyNeeded(unitDamage, energyUsage);
            if (energy >= energyCost || isCreative) {
                damage += additionalDamage;
            } else {
                //If we don't have enough power use it at a reduced power level (this will be false the majority of the time)
                damage += Math.round(additionalDamage * MathUtils.divideToLevel(energy - energyUsage, energyCost - energyUsage));
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
        return energyContainer == null || energyContainer.getEnergy() < minEnergy;
    }

    @Nullable
    public static <MODULE extends ICustomModule<MODULE>> IModule<MODULE> getEnabledModule(ItemStack stack, IModuleDataProvider<MODULE> typeProvider) {
        return IModuleHelper.INSTANCE.getIfEnabled(stack, typeProvider);
    }

    public static boolean isModuleEnabled(ItemStack stack, IModuleDataProvider<?> type) {
        return IModuleHelper.INSTANCE.isEnabled(stack, type);
    }
}