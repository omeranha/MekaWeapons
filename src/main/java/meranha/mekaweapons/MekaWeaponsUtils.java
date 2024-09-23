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
import meranha.mekaweapons.items.ModuleWeaponAttackAmplificationUnit;
import net.minecraft.world.item.ItemStack;

public class MekaWeaponsUtils {
    public static long getTotalDamage(@NotNull ItemStack stack, @Nullable IModule<ModuleWeaponAttackAmplificationUnit> attackAmplificationUnit, @NotNull CachedIntValue baseDamage, @NotNull CachedLongValue energyUsage) {
        return getTotalDamage(stack, attackAmplificationUnit, baseDamage.get(), energyUsage.get());
    }

    public static long getTotalDamage(@NotNull ItemStack stack, @Nullable IModule<ModuleWeaponAttackAmplificationUnit> attackAmplificationUnit, int baseDamage, long energyUsage) {
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        long energy = energyContainer != null ? energyContainer.getEnergy() : 0;
        if(energy < energyUsage) {
            return -1;
        }

        double damage = baseDamage;
        if (attackAmplificationUnit != null) {
            int unitDamage = attackAmplificationUnit.getCustomInstance().getDamage();
            if (unitDamage > 0) {
                double additionalDamage = baseDamage * attackAmplificationUnit.getCustomInstance().getDamageMultiplicator();
                long energyCost = getEnergyNeeded(unitDamage, energyUsage);
                // todo always max damage if in creative
                if (energy < energyCost){
                    //If we don't have enough power use it at a reduced power level (this will be false the majority of the time)
                    damage += additionalDamage * MathUtils.divideToLevel(energy - energyUsage, energyCost - energyUsage);
                } else {
                    damage += additionalDamage;
                }
            }
        }

        return Math.round(damage) - 1;
    }

    public static long getEnergyNeeded(@Nullable IModule<ModuleWeaponAttackAmplificationUnit> attackAmplificationUnit, CachedLongValue energyUsage) {
        if (attackAmplificationUnit != null) {
            return getEnergyNeeded(attackAmplificationUnit.getCustomInstance().getDamage(), energyUsage.get());
        }
        return -1;
    }

    public static long getEnergyNeeded(@Nullable IModule<ModuleWeaponAttackAmplificationUnit> attackAmplificationUnit, long energyUsage) {
        if (attackAmplificationUnit != null) {
            return getEnergyNeeded(attackAmplificationUnit.getCustomInstance().getDamage(), energyUsage);
        }
        return -1;
    }

    public static long getEnergyNeeded(int unitDamage, long energyUsage) {
        return MathUtils.clampToLong(energyUsage * (1 + unitDamage / 4F));
    }

    public static int getBarCustomColor(@NotNull ItemStack stack, @NotNull CachedLongValue energyUsage) {
        return getBarCustomColor(stack, energyUsage.get());
    }

    public static int getBarCustomColor(@NotNull ItemStack stack, long energyUsage) {
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        if(hasNotEnoughEnergy(energyContainer, energyUsage)) {
            return MekanismConfig.client.hudDangerColor.get();
        }

        IModule<ModuleWeaponAttackAmplificationUnit> attackAmplificationUnit = getEnabledModule(stack, MekaWeapons.ATTACKAMPLIFICATION_UNIT);
        long energyNeeded = MekaWeaponsUtils.getEnergyNeeded(attackAmplificationUnit, energyUsage);
        if (hasNotEnoughEnergy(energyContainer, energyNeeded)) {
            return MekanismConfig.client.hudWarningColor.get();
        }

        return MekanismConfig.client.energyColor.get();
    }

    private static boolean hasNotEnoughEnergy(@Nullable IEnergyContainer energyContainer, long minEnergy) {
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
