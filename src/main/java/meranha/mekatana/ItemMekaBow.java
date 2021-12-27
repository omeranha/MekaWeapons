package meranha.mekatana;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.NBTConstants;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.item.RateLimitEnergyHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.ForgeEventFactory;

public class ItemMekaBow extends BowItem implements IModeItem, IItemHUDProvider {

    public ItemMekaBow(Properties properties) {
        super(properties.rarity(Rarity.EPIC).setNoRepair().stacksTo(1));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        StorageUtils.addStoredEnergy(stack, tooltip, true);
        tooltip.add(MekanismLang.FIRE_MODE.translateColored(EnumColor.PINK, OnOff.of(getFireState(stack))));
        tooltip.add(WeaponsLang.AUTOFIRE_MODE.translateColored(EnumColor.PINK, OnOff.of(getAutoFire(stack))));
    }

    public void onUseTick(World world, LivingEntity player, ItemStack stack, int count) {
        boolean autoFire = getAutoFire(stack);
        if (autoFire && getUseDuration(stack) - count >= getChargeTicks()) {
            player.stopUsingItem();
            stack.releaseUsing(world, player, 0);
            player.startUsingItem(player.getUsedItemHand());
        }
    }

    public static int getChargeTicks() {
        return (int)Math.ceil(20.0F);
    }

    @Override
    public void releaseUsing(@Nonnull ItemStack stack, @Nonnull World world, @Nonnull LivingEntity entityLiving, int timeLeft) {
        if (entityLiving instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entityLiving;
            IEnergyContainer energyContainer = null;
            boolean fireState = getFireState(stack);
            if (!player.isCreative()) {
                energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                FloatingLong energyNeeded = fireState ? ModConfig.general.mekaBowEnergyUsageFire.get() : ModConfig.general.mekaBowEnergyUsage.get();
                if (energyContainer == null || energyContainer.extract(energyNeeded, Action.SIMULATE, AutomationType.MANUAL).smallerThan(energyNeeded)) {
                    return;
                }
            }
            boolean infinity = player.isCreative() || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;
            ItemStack ammo = player.getProjectile(stack);
            int charge = ForgeEventFactory.onArrowLoose(stack, world, player, getUseDuration(stack) - timeLeft, !ammo.isEmpty() || infinity);
            if (charge < 0) {
                return;
            }
            if (!ammo.isEmpty() || infinity) {
                float velocity = getPowerForTime(charge);
                if (velocity < 0.1) {
                    return;
                }
                if (ammo.isEmpty()) {
                    ammo = new ItemStack(Items.ARROW);
                }
                boolean noConsume = player.isCreative() || (ammo.getItem() instanceof ArrowItem && ((ArrowItem) ammo.getItem()).isInfinite(ammo, stack, player));
                if (!world.isClientSide) {
                    ArrowItem arrowitem = (ArrowItem) (ammo.getItem() instanceof ArrowItem ? ammo.getItem() : Items.ARROW);
                    AbstractArrowEntity arrowEntity = arrowitem.createArrow(world, ammo, player);
                    arrowEntity = customArrow(arrowEntity);
                    arrowEntity.shootFromRotation(player, player.xRot, player.yRot, 0, 3 * velocity, 1);
                    if (velocity == 1) {
                        arrowEntity.setCritArrow(true);
                    }
                    int power = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
                    int damage = ModConfig.general.mekaBowDamage.get();
                    if (power == 0) {
                        arrowEntity.setBaseDamage(damage);
                    }
                    if (power > 0) {
                        arrowEntity.setBaseDamage(arrowEntity.getBaseDamage() + 0.5 * power + 0.5);
                    }
                    int punch = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
                    if (punch > 0) {
                        arrowEntity.setKnockback(punch);
                    }
                    if (fireState || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
                        arrowEntity.setSecondsOnFire(100);
                    }
                    if (!player.isCreative() && energyContainer != null) {
                        energyContainer.extract(fireState ? ModConfig.general.mekaBowEnergyUsageFire.get() : ModConfig.general.mekaBowEnergyUsage.get(), Action.EXECUTE, AutomationType.MANUAL);
                    }
                    if (noConsume || player.isCreative() && (ammo.getItem() == Items.SPECTRAL_ARROW || ammo.getItem() == Items.TIPPED_ARROW)) {
                        arrowEntity.pickup = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
                    }
                    world.addFreshEntity(arrowEntity);
                }
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundCategory.PLAYERS, 1, 1.0F / (random.nextFloat() * 0.4F + 1.2F) + velocity * 0.5F);
                if (!noConsume && !player.isCreative()) {
                    ammo.shrink(1);
                    if (ammo.isEmpty()) {
                        player.inventory.removeItem(ammo);
                    }
                }
                player.awardStat(Stats.ITEM_USED.get(this));
            }
        }
    }

    public void setFireState(ItemStack stack, boolean state) {
        ItemDataUtils.setBoolean(stack, NBTConstants.MODE, state);
    }

    public boolean getFireState(ItemStack stack) {
        return ItemDataUtils.getBoolean(stack, NBTConstants.MODE);
    }

    public void setAutoFire(ItemStack stack, boolean state) {
        ItemDataUtils.setBoolean(stack, NBTConstants.MODE, state);
    }

    public boolean getAutoFire(ItemStack stack) {
        return ItemDataUtils.getBoolean(stack, NBTConstants.MODE);
    }

    @Override
    public void addHUDStrings(List<ITextComponent> list, PlayerEntity player, ItemStack stack, EquipmentSlotType slotType) {
        list.add(WeaponsLang.AUTOFIRE_MODE.translateColored(EnumColor.PINK, OnOff.of(getAutoFire(stack))));
        list.add(MekanismLang.FIRE_MODE.translateColored(EnumColor.PINK, OnOff.of(getFireState(stack))));
        StorageUtils.addStoredEnergy(stack, list, true, MekanismLang.STORED_ENERGY);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return StorageUtils.getEnergyDurabilityForDisplay(stack);
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return MekanismConfig.client.energyColor.get();
    }

    @Override
    public void fillItemCategory(@Nonnull ItemGroup group, @Nonnull NonNullList<ItemStack> items) {
        super.fillItemCategory(group, items);
        if (allowdedIn(group)) {
            items.add(StorageUtils.getFilledEnergyVariant(new ItemStack(this), MekanismConfig.gear.mekaToolBaseEnergyCapacity.get()));
        }
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
        return new ItemCapabilityWrapper(stack, RateLimitEnergyHandler.create(MekanismConfig.gear.mekaToolBaseChargeRate, MekanismConfig.gear.mekaToolBaseEnergyCapacity, BasicEnergyContainer.manualOnly, BasicEnergyContainer.alwaysTrue));
    }

    @Override
    public void changeMode(@Nonnull PlayerEntity player, @Nonnull ItemStack stack, int shift, boolean displayChangeMessage) {
        if (Math.abs(shift) % 2 == 1) {
            boolean newState = !getFireState(stack);
            setFireState(stack, newState);
            if (displayChangeMessage) {
                player.sendMessage(MekanismUtils.logFormat(MekanismLang.FIRE_MODE.translate(OnOff.of(newState, true))), Util.NIL_UUID);
            }
        }
        if (player.isShiftKeyDown()) {
            boolean newState = !getAutoFire(stack);
            setAutoFire(stack, newState);
            if (displayChangeMessage) {
                player.sendMessage(MekanismUtils.logFormat(WeaponsLang.AUTOFIRE_MODE.translate(OnOff.of(newState, true))), Util.NIL_UUID);
            }
        }
    }

    @Nonnull
    @Override
    public ITextComponent getScrollTextComponent(@Nonnull ItemStack stack) {
        return MekanismLang.FIRE_MODE.translateColored(EnumColor.PINK, OnOff.of(getFireState(stack), true));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return oldStack.getItem() != newStack.getItem();
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return oldStack.getItem() != newStack.getItem();
    }

    @Override
    public boolean isFoil(@Nonnull ItemStack stack) {
        return false;
    }
}