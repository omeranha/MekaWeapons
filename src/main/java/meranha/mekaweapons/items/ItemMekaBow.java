package meranha.mekaweapons.items;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.IModule;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.item.RateLimitEnergyHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.mekatool.ModuleAttackAmplificationUnit;
import mekanism.common.content.gear.shared.ModuleEnergyUnit;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.BooleanStateDisplay;
import meranha.mekaweapons.MekaWeapons;
import meranha.mekaweapons.WeaponsLang;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;

@SuppressWarnings("deprecation")
public class ItemMekaBow extends BowItem implements IModuleContainerItem, IModeItem {
    public boolean isFullChargeShot = false;

    public ItemMekaBow(Properties properties) {
        super(properties.rarity(Rarity.EPIC).setNoRepair().stacksTo(1));
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
            addModuleDetails(stack, tooltip);
        } else {
            StorageUtils.addStoredEnergy(stack, tooltip, true);
            if (hasModule(stack, MekaWeapons.AUTOFIRE_UNIT)) {
                tooltip.add(WeaponsLang.AUTOFIRE_MODE.translateColored(EnumColor.YELLOW, BooleanStateDisplay.OnOff.of(isModuleEnabled(stack, MekaWeapons.AUTOFIRE_UNIT))));
            }
            if (hasModule(stack, MekaWeapons.ARROWENERGY_UNIT)) {
                tooltip.add(WeaponsLang.ARROWENERGY_MODE.translateColored(EnumColor.YELLOW, BooleanStateDisplay.OnOff.of(isModuleEnabled(stack, MekaWeapons.ARROWENERGY_UNIT))));
            }
            tooltip.add(MekanismLang.HOLD_FOR_MODULES.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
            tooltip.add(Component.translatable("tooltip.mekaweapons.attackdamage", getDamage(stack)).withStyle(ChatFormatting.DARK_GREEN));
        }
    }

    public void onUseTick(@Nonnull Level world, @Nonnull LivingEntity player, @Nonnull ItemStack stack, int timeLeft) {
        if ((!player.getProjectile(stack).isEmpty() || isInfinite((Player) player, stack)) && isModuleEnabled(stack, MekaWeapons.AUTOFIRE_UNIT) && getUseDuration(stack) - timeLeft == getUseTick(stack)) {
            player.stopUsingItem();
            stack.releaseUsing(world, player, 0);
            player.startUsingItem(player.getUsedItemHand());
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean hasAmmo = (!player.getProjectile(stack).isEmpty() || isInfinite(player, stack));
        InteractionResultHolder<ItemStack> ret = ForgeEventFactory.onArrowNock(stack, world, player, hand, hasAmmo);
        if (ret != null) return ret;
        if (!player.getAbilities().instabuild && !hasAmmo) {
            return InteractionResultHolder.fail(stack);
        } else {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
    }

    @Override
    public void releaseUsing(@Nonnull ItemStack stack, @Nonnull Level world, @Nonnull LivingEntity entityLiving, int timeLeft) {
        if (entityLiving instanceof Player player) {
            IEnergyContainer energyContainer = null;
            if (!player.isCreative()) {
                energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                FloatingLong energyNeeded = MekaWeapons.general.mekaBowEnergyUsage.get();
                if (energyContainer == null || energyContainer.extract(energyNeeded, Action.SIMULATE, AutomationType.MANUAL).smallerThan(energyNeeded)) {
                    return;
                }

                energyContainer.extract(MekaWeapons.general.mekaBowEnergyUsage.get(), Action.EXECUTE, AutomationType.MANUAL);
            }

            ItemStack ammo = player.getProjectile(stack);
            int charge = ForgeEventFactory.onArrowLoose(stack, world, player, getUseDuration(stack) - timeLeft, isInfinite(player, stack));
            if (charge < 0) return;

            if (!ammo.isEmpty() || isInfinite(player, stack)) {
                float velocity = getPowerForTime(charge);
                if (velocity < 0.1) {
                    return;
                }
                if (ammo.isEmpty()) {
                    ammo = new ItemStack(Items.ARROW);
                }

                if (!world.isClientSide) {
                    ArrowItem arrowitem = (ArrowItem) (ammo.getItem() instanceof ArrowItem ? ammo.getItem() : Items.ARROW);
                    AbstractArrow arrowEntity = customArrow(arrowitem.createArrow(world, ammo, player));
                    isFullChargeShot = velocity >= 1;
                    int unitMultiplier = 0;
                    /*
                    IModule<?> arrowVelocityUnit = getModule(stack, MekaWeapons.ARROWVELOCITY_UNIT);
                    if (arrowVelocityUnit != null && arrowVelocityUnit.isEnabled()) {
                        for (int i = 0; i < arrowVelocityUnit.getInstalledCount(); i++) {
                            unitMultiplier += 1;
                        }
                    }
                    */
                    arrowEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, (3 + unitMultiplier) * velocity, 0);
                    int damage = getDamage(stack);
                    arrowEntity.setBaseDamage((double) damage / 3);
                    int power = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
                    if (power > 0) {
                        arrowEntity.setBaseDamage(damage + 0.5 * power + 0.5);
                    }

                    int punch = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
                    if (punch > 0) {
                        arrowEntity.setKnockback(punch);
                    }

                    if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
                        arrowEntity.setSecondsOnFire(100);
                    }

                    if (isInfinite(player, stack) || player.isCreative() && (ammo.getItem() == Items.SPECTRAL_ARROW || ammo.getItem() == Items.TIPPED_ARROW)) {
                        arrowEntity.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                    }

                    world.addFreshEntity(arrowEntity);
                }
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1, 1.0F / (world.random.nextFloat() * 0.4F + 1.2F) + velocity * 0.5F);
                if (!isInfinite(player, stack) && !player.isCreative()) {
                    ammo.shrink(1);
                    if (ammo.isEmpty()) {
                        player.getInventory().removeItem(ammo);
                    }
                }
                player.awardStat(Stats.ITEM_USED.get(this));
            }
        }
    }

    public boolean isInfinite(Player player, ItemStack stack) {
        return player.isCreative() || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0 || isModuleEnabled(stack, MekaWeapons.ARROWENERGY_UNIT);
    }

    @Override
    public boolean isBarVisible(@Nonnull ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(@Nonnull ItemStack stack) {
        return StorageUtils.getEnergyBarWidth(stack);
    }

    @Override
    public int getBarColor(@Nonnull ItemStack stack) {
        return MekanismConfig.client.energyColor.get();
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        IModule<ModuleEnergyUnit> module = getModule(stack, MekanismModules.ENERGY_UNIT);
        @NotNull FloatingLongSupplier maxEnergy = () -> (module == null ? MekaWeapons.general.mekaBowBaseEnergyCapacity.get() : module.getCustomInstance().getEnergyCapacity(module));
        return new ItemCapabilityWrapper(stack, RateLimitEnergyHandler.create(MekaWeapons.general.mekaBowBaseChargeRate, maxEnergy, BasicEnergyContainer.manualOnly, BasicEnergyContainer.alwaysTrue));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return oldStack.getItem() != newStack.getItem();
    }

    @Override
    public boolean isFoil(@Nonnull ItemStack stack) {
        return false;
    }

    @Override
    public void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, IModeItem.DisplayChange displayChange) {
        IModule<?> autoFireUnit = getModule(stack, MekaWeapons.AUTOFIRE_UNIT);
        if (autoFireUnit != null) {
            autoFireUnit.toggleEnabled(player, WeaponsLang.AUTOFIRE_MODE_CHANGE.translateColored(EnumColor.WHITE));
        }
    }

    public int getDamage(@Nonnull ItemStack stack) {
        IModule<ModuleAttackAmplificationUnit> attackAmplificationUnit = getModule(stack, MekanismModules.ATTACK_AMPLIFICATION_UNIT);
        int damage = MekaWeapons.general.mekaBowBaseDamage.get();
        if (attackAmplificationUnit != null && attackAmplificationUnit.isEnabled()) {
            for (int i = 0; i < attackAmplificationUnit.getInstalledCount(); i++) {
                damage += MekaWeapons.general.mekaBowBaseDamage.get();
            }
        }
        return damage;
    }

    public float getUseTick(@Nonnull ItemStack stack) {
        float useTick = 20.0F;
        IModule<?> drawSpeedUnit = getModule(stack, MekaWeapons.DRAWSPEED_UNIT);
        if (drawSpeedUnit != null && drawSpeedUnit.isEnabled()) {
            for (int i = 0; i < drawSpeedUnit.getInstalledCount(); i++) {
                useTick -= 5.0F;
            }
        }
        return useTick;
    }

    @Override
    public @NotNull AbstractArrow customArrow(AbstractArrow arrow) {
        Entity owner = arrow.getOwner();
        if (!(owner instanceof LivingEntity)) {
            return new MekaArrowEntity(MekaWeapons.MEKA_ARROW.getEntityType(), arrow.level());
        }
        return new MekaArrowEntity((LivingEntity) arrow.getOwner(), arrow.level());
    }

    public boolean isGravityDampenerEnabled(@Nonnull ItemStack stack) {
        IModule<?> gravityDampenerUnit = getModule(stack, MekaWeapons.GRAVITYDAMPENER_UNIT);
        return (gravityDampenerUnit != null && gravityDampenerUnit.isEnabled());
    }
}