package meranha.mekaweapons;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import mekanism.api.IIncrementalEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.gear.ModuleData;
import mekanism.api.radial.IRadialDataHelper;
import mekanism.api.radial.mode.BasicRadialMode;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.util.MekanismUtils;
import meranha.mekaweapons.client.WeaponsLang;
import meranha.mekaweapons.items.ItemMekaGun;
import meranha.mekaweapons.items.modules.WeaponsModules;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.common.TranslatableEnum;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.config.value.CachedLongValue;
import mekanism.common.util.StorageUtils;
import meranha.mekaweapons.items.ItemMekaBow;
import meranha.mekaweapons.items.ItemMekaTana;
import meranha.mekaweapons.items.modules.AdvancedAttackAmplificationUnit;
import meranha.mekaweapons.items.modules.EnchantmentUnit;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;
import java.util.function.IntFunction;

public class MekaWeaponsUtils {
    public static long getBaseEnergyUsage(@NotNull ItemStack stack) {
        CachedLongValue energy = switch (stack.getItem()) {
            case ItemMekaBow ignored -> MekaWeapons.general.mekaBowEnergyUsage;
            case ItemMekaTana ignored -> MekaWeapons.general.mekaTanaEnergyUsage;
            case ItemMekaGun ignored -> MekaWeapons.general.mekaGunEnergyUsage;
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

            if (isModuleEnabled(weaponStack, WeaponsModules.PUNCH_UNIT)) {
                energy += MekaWeapons.general.mekaBowPunchEnergyUsage.get();
            }

            if (isModuleEnabled(weaponStack, WeaponsModules.CHEMICALARROW_UNIT)) {
                energy += MekaWeapons.general.chemicalArrowsEnergyUsage.get();
            }
        } else if (weaponStack.getItem() instanceof ItemMekaTana) {
            if (isModuleEnabled(weaponStack, WeaponsModules.SWEEPING_UNIT)) {
                energy += MekaWeapons.general.mekaTanaSweepingEnergyUsage.get();
            }

            if (isModuleEnabled(weaponStack, WeaponsModules.LOOTING_UNIT)) {
                energy += MekaWeapons.general.mekaTanaLootingEnergyUsage.get();
            }

            if (isModuleEnabled(weaponStack, WeaponsModules.FIREASPECT_UNIT)) {
                energy += MekaWeapons.general.mekaTanaFireAspectEnergyUsage.get();
            }

            if (isModuleEnabled(weaponStack, WeaponsModules.KNOCKBACK_UNIT)) {
                energy += MekaWeapons.general.mekaTanaKnockbackEnergyUsage.get();
            }
        } else if (weaponStack.getItem() instanceof ItemMekaGun) {
            if (isModuleEnabled(weaponStack, WeaponsModules.LOOTING_UNIT)) {
                energy += MekaWeapons.general.mekaGunLootingEnergyUsage.get();
            }

            energy += getEnabledModuleCount(weaponStack, WeaponsModules.COOLING_UNIT) * MekaWeapons.general.mekaGunCoolingEnergyUsage.get();
        }

        IModule<AdvancedAttackAmplificationUnit> attackAmplificationUnit = getEnabledModule(weaponStack, WeaponsModules.ATTACKAMPLIFICATION_UNIT);
        if (attackAmplificationUnit != null) {
            energy *= attackAmplificationUnit.getCustomInstance().attackDamage().ordinal();
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
            case ItemMekaGun ignored -> MekaWeapons.general.mekaGunBaseDamage;
            default -> null;
        };

        return (damage != null) ? damage.get() : 0;
    }

    public static long getTotalDamage(@NotNull ItemStack weapon) {
        if (isEnergyInsufficient(weapon)) {
            return 20;
        }

        long damage = getBaseDamage(weapon);
        IModule<AdvancedAttackAmplificationUnit> attackAmplificationUnit = getEnabledModule(weapon, WeaponsModules.ATTACKAMPLIFICATION_UNIT);
        if (attackAmplificationUnit != null) {
            damage = attackAmplificationUnit.getCustomInstance().getCurrentMaxDamage(weapon);
        }
        return damage;
    }

    public static int getModuleAdditionalDamage(int baseDamage) {
        return switch (MekaWeapons.general.moduleDamageMode.get()) {
            case BASE_DAMAGE -> baseDamage;
            case HALF_BASE_DAMAGE -> baseDamage / 2;
            case CUSTOM -> MekaWeapons.general.moduleCustomDamage.get();
        };
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

    public static boolean isModuleInstalled(ItemStack stack, Holder<ModuleData<?>> type) {
        IModuleContainer container = IModuleHelper.INSTANCE.getModuleContainer(stack);
        return container != null && container.has(type);
    }

    /**
     * Amount of copies of the given module installed on the stack, or 0 when the module is missing or disabled.
     */
    public static int getEnabledModuleCount(ItemStack stack, Holder<ModuleData<?>> type) {
        IModuleContainer container = IModuleHelper.INSTANCE.getModuleContainer(stack);
        return (container != null && container.hasEnabled(type)) ? container.installedCount(type) : 0;
    }

    @Nullable
    public static IChemicalTank getChemicalTank(ItemStack stack) {
        if (ContainerType.CHEMICAL.getContainerCount(stack) == 0) {
            return null;
        }
        return ContainerType.CHEMICAL.createContainer(stack, 0);
    }

    /**
     * Merges the enchantments the installed modules provide into the ones already on the stack. Without this the
     * enchantment based modules, such as the Looting Unit, are stored but never applied.
     */
    public static ItemEnchantments mergeModuleEnchantments(ItemStack stack, ItemEnchantments enchantments) {
        IModuleContainer container = IModuleHelper.INSTANCE.getModuleContainer(stack);
        if (container == null) {
            return enchantments;
        }

        ItemEnchantments moduleBased = container.moduleBasedEnchantments();
        ItemEnchantments infused = EnchantmentUnit.getInfused(stack);
        if (moduleBased.isEmpty() && infused.isEmpty()) {
            return enchantments;
        }

        ItemEnchantments.Mutable merged = new ItemEnchantments.Mutable(enchantments);
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : moduleBased.entrySet()) {
            merged.upgrade(entry.getKey(), entry.getIntValue());
        }
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : infused.entrySet()) {
            merged.upgrade(entry.getKey(), entry.getIntValue());
        }
        return merged.toImmutable();
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