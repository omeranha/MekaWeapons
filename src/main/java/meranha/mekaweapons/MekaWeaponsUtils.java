package meranha.mekaweapons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.IModule;
import mekanism.api.math.MathUtils;
import mekanism.common.util.StorageUtils;
import meranha.mekaweapons.items.ModuleWeaponAttackAmplificationUnit;
import net.minecraft.world.item.ItemStack;

public class MekaWeaponsUtils {
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
                long energyCost = getEnergyNeeded(energyUsage, unitDamage);
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

    public static long getEnergyNeeded(@Nullable IModule<ModuleWeaponAttackAmplificationUnit> attackAmplificationUnit, long energyUsage) {
        if (attackAmplificationUnit != null) {
            return getEnergyNeeded(energyUsage, attackAmplificationUnit.getCustomInstance().getDamage());
        }
        return MekaWeapons.general.mekaBowEnergyUsage.get();
    }

    private static long getEnergyNeeded(double energyUsage, int unitDamage) {
        return MathUtils.clampToLong(energyUsage * (1 + unitDamage / 4F));
    }
}
