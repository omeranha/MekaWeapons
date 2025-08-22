package meranha.mekaweapons;

import mekanism.api.IIncrementalEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.gear.ModuleData;
import mekanism.api.radial.IRadialDataHelper;
import mekanism.api.radial.mode.BasicRadialMode;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.util.MekanismUtils;
import meranha.mekaweapons.client.WeaponsLang;
import meranha.mekaweapons.items.modules.WeaponsModules;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.common.TranslatableEnum;
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
import meranha.mekaweapons.items.modules.WeaponAttackAmplificationUnit;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;
import java.util.function.IntFunction;

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
            if (isModuleEnabled(weaponStack, WeaponsModules.ARROWENERGY_UNIT)) {
                energy += MekaWeapons.general.mekaBowEnergyArrowUsage.get();
            }

            if (isModuleEnabled(weaponStack, WeaponsModules.AUTOFIRE_UNIT)) {
                energy += MekaWeapons.general.mekabowAutoFireEnergyUsage.get();
            }

            if (isModuleEnabled(weaponStack, WeaponsModules.GRAVITYDAMPENER_UNIT)) {
                energy += MekaWeapons.general.mekabowGravityDampenerUsage.get();
            }

            IModule<?> drawSpeedUnit = getEnabledModule(weaponStack, WeaponsModules.DRAWSPEED_UNIT);
            if (drawSpeedUnit != null) {
                energy += drawSpeedUnit.getInstalledCount() * MekaWeapons.general.mekabowDrawSpeedUsage.get();
            }

            if (isModuleEnabled(weaponStack, WeaponsModules.LOOTING_UNIT)) {
                energy += MekaWeapons.general.mekaBowLootingEnergyUsage.get();
            }
        } else if (weaponStack.getItem() instanceof ItemMekaTana) {
            if (isModuleEnabled(weaponStack, WeaponsModules.SWEEPING_UNIT)) {
                energy += MekaWeapons.general.mekaTanaSweepingEnergyUsage.get();
            }

            if (isModuleEnabled(weaponStack, WeaponsModules.LOOTING_UNIT)) {
                energy += MekaWeapons.general.mekaTanaLootingEnergyUsage.get();
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
        IModule<WeaponAttackAmplificationUnit> attackAmplificationUnit = getEnabledModule(weapon, WeaponsModules.ATTACKAMPLIFICATION_UNIT);
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

    @NothingNullByDefault
    public enum ToggleableModule implements IIncrementalEnum<ToggleableModule>, IHasTextComponent, TranslatableEnum, IRadialMode, StringRepresentable {
        OFF(WeaponsLang.RADIAL_TOGGLE_OFF, EnumColor.WHITE, "off"),
        ON(WeaponsLang.RADIAL_TOGGLE_ON, EnumColor.BRIGHT_GREEN, "on");

        private final String serializedName;
        private final ResourceLocation icon;
        private final Component label;
        private final EnumColor color;
        private final Component sliceNamePreCalc;
        public static final IntFunction<ToggleableModule> BY_ID = ByIdMap.continuous(ToggleableModule::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final IRadialDataHelper.BooleanRadialModes RADIAL_MODES = new IRadialDataHelper.BooleanRadialModes(
                new BasicRadialMode(WeaponsLang.RADIAL_TOGGLE_OFF, ToggleableModule.OFF.icon(), EnumColor.RED),
                new BasicRadialMode(WeaponsLang.RADIAL_TOGGLE_ON, ToggleableModule.ON.icon(), EnumColor.BRIGHT_GREEN)
        );

        ToggleableModule(ILangEntry langEntry, EnumColor color, String texture) {
            this.serializedName = name().toLowerCase(Locale.ROOT);
            this.color = color;
            this.icon = MekaWeapons.getResource(MekanismUtils.ResourceType.GUI_RADIAL, texture + ".png");
            this.label = TextComponentUtil.getString(Integer.toString(this.ordinal()));
            this.sliceNamePreCalc = langEntry.translateColored(color);
        }

        @Override
        public ToggleableModule byIndex(int index) {
            return BY_ID.apply(index);
        }

        @Override
        public Component getTextComponent() {
            return label;
        }

        @Override
        public Component getTranslatedName() {
            return sliceName();
        }

        @Override
        public Component sliceName() {
            return sliceNamePreCalc;
        }

        @Override
        public ResourceLocation icon() {
            return icon;
        }

        @Override
        public EnumColor color() {
            return color;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}