package meranha.mekaweapons.items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Multimap;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.IModule;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.api.radial.mode.NestedRadialMode;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.item.RateLimitEnergyHandler;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.shared.ModuleEnergyUnit;
import mekanism.common.lib.attribute.AttributeCache;
import mekanism.common.lib.radial.IGenericRadialModeItem;
import mekanism.common.lib.radial.data.NestingRadialData;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.BooleanStateDisplay;
import meranha.mekaweapons.MekaWeapons;
import meranha.mekaweapons.MekaWeaponsUtils;
import static meranha.mekaweapons.MekaWeaponsUtils.getBarCustomColor;
import static meranha.mekaweapons.MekaWeaponsUtils.getEnabledModule;
import meranha.mekaweapons.WeaponsLang;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.ForgeEventFactory;

public class ItemMekaBow extends BowItem implements IModuleContainerItem, IGenericRadialModeItem {

    private final Int2ObjectMap<AttributeCache> attributeCaches = new Int2ObjectArrayMap<>(ModuleWeaponAttackAmplificationUnit.AttackDamage.values().length - 2);
    private static final ResourceLocation RADIAL_ID = MekaWeapons.rl("meka_bow");

    public ItemMekaBow(@NotNull Properties properties) {
        super(properties.rarity(Rarity.EPIC).setNoRepair().stacksTo(1));
    }

    public void appendHoverText(@NotNull @Nonnull ItemStack stack, @Nullable Level world, @NotNull @Nonnull List<Component> tooltip, @NotNull @Nullable TooltipFlag flag) {
        if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
            addModuleDetails(stack, tooltip);
            return;
        }

        StorageUtils.addStoredEnergy(stack, tooltip, true);
        if (hasModule(stack, MekaWeapons.AUTOFIRE_UNIT)) {
            tooltip.add(WeaponsLang.AUTOFIRE_MODE.translateColored(EnumColor.YELLOW, BooleanStateDisplay.OnOff.of(isModuleEnabled(stack, MekaWeapons.AUTOFIRE_UNIT))));
        }
        if (hasModule(stack, MekaWeapons.ARROWENERGY_UNIT)) {
            tooltip.add(WeaponsLang.ARROWENERGY_MODE.translateColored(EnumColor.YELLOW, BooleanStateDisplay.OnOff.of(isModuleEnabled(stack, MekaWeapons.ARROWENERGY_UNIT))));
        }
        tooltip.add(MekanismLang.HOLD_FOR_MODULES.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
    }

    @NotNull
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@NotNull EquipmentSlot slot, @NotNull ItemStack stack) {
        if (slot == EquipmentSlot.MAINHAND) {
            IModule<ModuleWeaponAttackAmplificationUnit> attackAmplificationUnit = getModule(stack, MekaWeapons.ATTACKAMPLIFICATION_UNIT);

            IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
            FloatingLong energy = energyContainer != null ? energyContainer.getEnergy() : FloatingLong.ZERO;

            int unitDamage = energy.greaterOrEqual(MekaWeapons.general.mekaBowEnergyUsage.get()) ? (attackAmplificationUnit != null)
                            ? attackAmplificationUnit.getCustomInstance().getCurrentUnit()
                            : 1 : 0;
            long totalDamage = MekaWeaponsUtils.getTotalDamage(stack);

            return attributeCaches.compute(unitDamage, (damage, previous) ->{
                AttributeModifier currentDamage = new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", totalDamage, Operation.ADDITION);
                // AttributeModifier currentSpeed = new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", (5 * installedModules) -9), Operation.ADDITION);

                if (previous != null && previous.get().containsKey(Attributes.ATTACK_DAMAGE) && previous.get().get(Attributes.ATTACK_DAMAGE).equals(currentDamage)) {
                    return previous;
                } else {
                    return new AttributeCache(builder -> {
                        builder.put(Attributes.ATTACK_DAMAGE, currentDamage);
                        // builder.put(Attributes.ATTACK_SPEED, currentSpeed);
                    });
                }
            }).get();
        }
        return super.getAttributeModifiers(slot,stack);
    }

    public void onUseTick(@NotNull @Nonnull Level world, @NotNull @Nonnull LivingEntity player, @NotNull @Nonnull ItemStack stack, int timeLeft) {
        if (isModuleEnabled(stack, MekaWeapons.AUTOFIRE_UNIT) && getUseDuration(stack) - timeLeft == getUseTick(stack)) {
            player.stopUsingItem();
            stack.releaseUsing(world, player, 0);
            player.startUsingItem(player.getUsedItemHand());
        }
    }

    public void releaseUsing(@NotNull @Nonnull ItemStack bow, @NotNull @Nonnull Level world, @NotNull @Nonnull LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player) {
            IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(bow, 0);
            if (!player.isCreative() && !hasEnoughEnergy(energyContainer)) {
                return;
            }

            ItemStack potentialAmmo = player.getProjectile(bow);
            boolean hasAmmo = !potentialAmmo.isEmpty() || isModuleEnabled(bow, MekaWeapons.ARROWENERGY_UNIT);

            int charge = ForgeEventFactory.onArrowLoose(bow, world, player, getUseDuration(bow) - timeLeft, hasAmmo);
            if (charge < 0 || !hasAmmo) {
                return;
            }

            float velocity = getPowerForTime(charge);
            if (velocity < 0.1) {
                return;
            }

            if (potentialAmmo.isEmpty()) {
                potentialAmmo = new ItemStack(Items.ARROW);
            }

            if (!world.isClientSide) {
                ArrowItem arrowitem = (ArrowItem) (potentialAmmo.getItem() instanceof ArrowItem ? potentialAmmo.getItem() : Items.ARROW);
                AbstractArrow arrowEntity = customArrow(arrowitem.createArrow(world, potentialAmmo, player));
                int unitMultiplier = 0;
                arrowEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, (3 + unitMultiplier) * velocity, 0);

                long totalDamage = MekaWeaponsUtils.getTotalDamage(bow);
                arrowEntity.setBaseDamage(totalDamage);
                if (isModuleEnabled(bow, MekaWeapons.ARROWENERGY_UNIT) && (potentialAmmo.getItem() == Items.SPECTRAL_ARROW || potentialAmmo.getItem() == Items.TIPPED_ARROW)) {
                    arrowEntity.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                }

                world.addFreshEntity(arrowEntity);
            }

            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1, 1.0F / (world.random.nextFloat() * 0.4F + 1.2F) + velocity * 0.5F);
            if (!isModuleEnabled(bow, MekaWeapons.ARROWENERGY_UNIT)) {
                potentialAmmo.shrink(1);
                if (potentialAmmo.isEmpty()) {
                    player.getInventory().removeItem(potentialAmmo);
                }
            }
            player.awardStat(Stats.ITEM_USED.get(this));

            long energyNeeded = MekaWeaponsUtils.getEnergyNeeded(bow);
            if (!player.isCreative()) {
                energyContainer.extract(FloatingLong.create(energyNeeded), Action.EXECUTE, AutomationType.MANUAL);
            }
        }
        super.releaseUsing(bow, world, entity, timeLeft);
    }

    private boolean hasEnoughEnergy(IEnergyContainer energyContainer) {
        return energyContainer != null && energyContainer.getEnergy().greaterOrEqual(MekaWeapons.general.mekaBowEnergyUsage.get());
    }

    @Override
    @NotNull
    public AbstractArrow customArrow(AbstractArrow arrow) {
        ItemStack weapon = arrow.getOwner() instanceof Player player ? player.getMainHandItem() : ItemStack.EMPTY;
        return new MekaArrowEntity(arrow, new ItemStack(Items.ARROW), weapon);
    }

    public boolean shouldCauseReequipAnimation(@NotNull ItemStack oldStack, @NotNull ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    public void addItems(@NotNull Consumer<ItemStack> tabOutput) {
        tabOutput.accept(StorageUtils.getFilledEnergyVariant(new ItemStack(this), MekaWeapons.general.mekaBowBaseEnergyCapacity.get()));
    }

    public boolean isBarVisible(@NotNull @Nonnull ItemStack stack) {
        return true;
    }

    public int getBarWidth(@NotNull @Nonnull ItemStack stack) {
        return StorageUtils.getEnergyBarWidth(stack);
    }

    public int getBarColor(@NotNull @Nonnull ItemStack stack) {
        return getBarCustomColor(stack);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        IModule<ModuleEnergyUnit> module = getModule(stack, MekanismModules.ENERGY_UNIT);
        @NotNull
        FloatingLongSupplier maxEnergy = () -> (module == null ? MekaWeapons.general.mekaBowBaseEnergyCapacity.get() : module.getCustomInstance().getEnergyCapacity(module));
        return new ItemCapabilityWrapper(stack, RateLimitEnergyHandler.create(MekaWeapons.general.mekaBowBaseChargeRate, maxEnergy, BasicEnergyContainer.manualOnly, BasicEnergyContainer.alwaysTrue));
    }

    public boolean shouldCauseBlockBreakReset(@NotNull ItemStack oldStack, @NotNull ItemStack newStack) {
        return oldStack.getItem() != newStack.getItem();
    }

    public float getUseTick(@NotNull ItemStack stack) {
        float useTick = 20;
        IModule<?> drawSpeedUnit = getEnabledModule(stack, MekaWeapons.DRAWSPEED_UNIT);
        if (drawSpeedUnit != null) {
            useTick -= 5 * drawSpeedUnit.getInstalledCount();
        }
        return useTick;
    }

    public boolean isEnchantable(@NotNull @Nonnull ItemStack stack) {
        return false;
    }

    public boolean isBookEnchantable(@NotNull ItemStack stack, @NotNull ItemStack book) {
        return false;
    }

    public ResourceLocation getRadialIdentifier() {
        return RADIAL_ID;
    }

    @Override
    public boolean supportsSlotType(ItemStack stack, @NotNull EquipmentSlot slotType) {
        return IGenericRadialModeItem.super.supportsSlotType(stack, slotType) && getModules(stack).stream().anyMatch(Module::handlesAnyModeChange);
    }

    @Override
    public void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, DisplayChange displayChange) {
        for (Module<?> module : getModules(stack)) {
            if (module.handlesModeChange()) {
                module.changeMode(player, stack, shift, displayChange);
                return;
            }
        }
    }

    protected FloatingLong getMaxEnergy(ItemStack stack) {
        IModule<ModuleEnergyUnit> module = getModule(stack, MekanismModules.ENERGY_UNIT);
        return module == null ? MekaWeapons.general.mekaBowBaseEnergyCapacity.get() : module.getCustomInstance().getEnergyCapacity(module);
    }

    protected FloatingLong getChargeRate(ItemStack stack) {
        IModule<ModuleEnergyUnit> module = getModule(stack, MekanismModules.ENERGY_UNIT);
        return module == null ? MekaWeapons.general.mekaBowBaseChargeRate.get() : module.getCustomInstance().getChargeRate(module);
    }

    @Nullable
    @Override
    public RadialData<?> getRadialData(ItemStack stack) {
        List<NestedRadialMode> nestedModes = new ArrayList<>();
        Consumer<NestedRadialMode> adder = nestedModes::add;
        for (Module<?> module : getModules(stack)) {
            if (module.handlesRadialModeChange()) {
                module.addRadialModes(stack, adder);
            }
        }
        if (nestedModes.isEmpty()) {
            // No modes available, return that we don't actually currently support radials
            return null;
        } else if (nestedModes.size() == 1) {
            // If we only have one mode available, just return it rather than having to
            // select the singular mode
            return nestedModes.get(0).nestedData();
        }
        return new NestingRadialData(RADIAL_ID, nestedModes);
    }

    @Nullable
    @Override
    public <M extends IRadialMode> M getMode(ItemStack stack, RadialData<M> radialData) {
        for (Module<?> module : getModules(stack)) {
            if (module.handlesRadialModeChange()) {
                M mode = module.getMode(stack, radialData);
                if (mode != null) {
                    return mode;
                }
            }
        }
        return null;
    }

    @Override
    public <M extends IRadialMode> void setMode(ItemStack stack, Player player, RadialData<M> radialData, M mode) {
        for (Module<?> module : getModules(stack)) {
            if (module.handlesRadialModeChange() && module.setMode(player, stack, radialData, mode)) {
                return;
            }
        }
    }

    @Nullable
    @Override
    public Component getScrollTextComponent(@NotNull ItemStack stack) {
        return getModules(stack).stream().filter(Module::handlesModeChange).findFirst().map(module -> module.getModeScrollComponent(stack)).orElse(null);
    }
}