package meranha.mekaweapons;

import meranha.mekaweapons.items.modules.WeaponsModules;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.providers.IModuleDataProvider;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.value.CachedFloatingLongValue;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.util.StorageUtils;
import meranha.mekaweapons.items.ItemMekaBow;
import meranha.mekaweapons.items.ItemMekaTana;
import meranha.mekaweapons.items.modules.WeaponAttackAmplificationUnit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class MekaWeaponsUtils {
    public static long getBaseEnergyUsage(@NotNull ItemStack stack) {
        Item weapon = stack.getItem();
        CachedFloatingLongValue energy = null;

        if (weapon instanceof ItemMekaBow) {
            energy = MekaWeapons.general.mekaBowEnergyUsage;
        } else if (weapon instanceof ItemMekaTana) {
            energy = MekaWeapons.general.mekaTanaEnergyUsage;
        }

        return energy != null ? energy.get().longValue() : 0;
    }

    public static long getEnergyNeeded(@Nullable ItemStack weaponStack) {
        if (weaponStack == null) {
            return -1;
        }

        long energy = getBaseEnergyUsage(weaponStack);
        if (weaponStack.getItem() instanceof ItemMekaBow) {
            if (isModuleEnabled(weaponStack, WeaponsModules.ARROWENERGY_UNIT)) {
                energy += MekaWeapons.general.mekaBowEnergyArrowUsage.get().longValue();
            }

            if (isModuleEnabled(weaponStack, WeaponsModules.AUTOFIRE_UNIT)) {
                energy += MekaWeapons.general.mekabowAutoFireEnergyUsage.get().longValue();
            }

            if (isModuleEnabled(weaponStack, WeaponsModules.GRAVITYDAMPENER_UNIT)) {
                energy += MekaWeapons.general.mekabowGravityDampenerUsage.get().longValue();
            }

            IModule<?> drawSpeedUnit = getEnabledModule(weaponStack, WeaponsModules.DRAWSPEED_UNIT);
            if (drawSpeedUnit != null) {
                energy += drawSpeedUnit.getInstalledCount() * MekaWeapons.general.mekabowDrawSpeedUsage.get().longValue();
            }

            if (isModuleEnabled(weaponStack, WeaponsModules.LOOTING_UNIT)) {
                energy += MekaWeapons.general.mekaBowLootingEnergyUsage.get().longValue();
            }
        } else if (weaponStack.getItem() instanceof ItemMekaTana) {
            if (isModuleEnabled(weaponStack, WeaponsModules.SWEEPING_UNIT)) {
                energy += MekaWeapons.general.mekaTanaSweepingEnergyUsage.get().longValue();
            }

            if (isModuleEnabled(weaponStack, WeaponsModules.LOOTING_UNIT)) {
                energy += MekaWeapons.general.mekaTanaLootingEnergyUsage.get().longValue();
            }
        }

        IModule<WeaponAttackAmplificationUnit> attackAmplificationUnit = getEnabledModule(weaponStack, WeaponsModules.ATTACKAMPLIFICATION_UNIT);
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
        Item weapon = stack.getItem();
        CachedIntValue damage = null;

        if (weapon instanceof ItemMekaBow) {
            damage = MekaWeapons.general.mekaBowBaseDamage;
        } else if (weapon instanceof ItemMekaTana) {
            damage = MekaWeapons.general.mekaTanaBaseDamage;
        }

        return damage != null ? damage.get() : 0;
    }

    public static long getTotalDamage(@NotNull ItemStack weapon) {
        if (isEnergyInsufficient(weapon)) {
            return 20;
        }

        long damage = getBaseDamage(weapon);
        IModule<WeaponAttackAmplificationUnit> attackAmplificationUnit = getEnabledModule(weapon, WeaponsModules.ATTACKAMPLIFICATION_UNIT);
        if (attackAmplificationUnit != null) {
            damage *= attackAmplificationUnit.getCustomInstance().getCurrentUnit();
        }
        return damage;
    }

    public static boolean isEnergyInsufficient(@Nullable IEnergyContainer energyContainer, long energyNeeded) {
        return (energyContainer == null) || energyContainer.getEnergy().longValue() < energyNeeded;
    }

    public static boolean isEnergyInsufficient(@NotNull ItemStack weapon) {
        return isEnergyInsufficient(StorageUtils.getEnergyContainer(weapon, 0), getEnergyNeeded(weapon));
    }

    @Nullable
    public static <MODULE extends ICustomModule<MODULE>> IModule<MODULE> getEnabledModule(ItemStack stack, IModuleDataProvider<MODULE> typeProvider) {
        return IModuleHelper.INSTANCE.load(stack, typeProvider);
    }

    public static boolean isModuleEnabled(ItemStack stack, IModuleDataProvider<?> type) {
        return IModuleHelper.INSTANCE.isEnabled(stack, type);
    }
}