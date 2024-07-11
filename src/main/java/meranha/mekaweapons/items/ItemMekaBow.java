package meranha.mekaweapons.items;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.BooleanStateDisplay;
import meranha.mekaweapons.MekaWeapons;
import meranha.mekaweapons.WeaponsLang;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import java.util.function.Consumer;

public class ItemMekaBow extends BowItem implements IModuleContainerItem {
    public ItemMekaBow(Properties properties) {
        super(IModuleHelper.INSTANCE.applyModuleContainerProperties(properties.rarity(Rarity.EPIC).setNoRepair().stacksTo(1)));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
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
        if (hasModule(stack, MekaWeapons.AUTOFIRE_UNIT) && getUseDuration(stack, player) - timeLeft == getUseTick(stack)) {
            player.stopUsingItem();
            stack.releaseUsing(world, player, 0);
            player.startUsingItem(player.getUsedItemHand());
        }
    }

    @Override
    public void releaseUsing(@NotNull ItemStack bow, @NotNull Level world, @NotNull LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player && !player.isCreative()) {
            IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(bow, 0);
            FloatingLong energyNeeded = MekaWeapons.general.mekaBowEnergyUsage.get();
            if (energyContainer == null || energyContainer.extract(energyNeeded, Action.SIMULATE, AutomationType.MANUAL).smallerThan(energyNeeded)) {
                return;
            }
        }
        super.releaseUsing(bow, world, entity, timeLeft);
    }

    @Override
    protected void shoot(@NotNull ServerLevel world, @NotNull LivingEntity entity, @NotNull InteractionHand hand, @NotNull ItemStack bow,
          @NotNull List<ItemStack> potentialAmmo, float velocity, float inaccuracy, boolean critical, @Nullable LivingEntity target) {
        super.shoot(world, entity, hand, bow, potentialAmmo, velocity, inaccuracy, critical, target);
        if (entity instanceof Player player && !player.isCreative() && (!potentialAmmo.isEmpty() || hasModule(bow, MekaWeapons.ARROWENERGY_UNIT))) {
            IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(bow, 0);
            if (energyContainer != null) {
                FloatingLong energyNeeded = MekaWeapons.general.mekaBowEnergyUsage.get();
                energyContainer.extract(energyNeeded, Action.EXECUTE, AutomationType.MANUAL);
            }
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    public void addItems(Consumer<ItemStack> tabOutput) {
        tabOutput.accept(StorageUtils.getFilledEnergyVariant(this));
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
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return oldStack.getItem() != newStack.getItem();
    }

    public float getUseTick(@Nonnull ItemStack stack) {
        float useTick = 20.0F;
        IModule<?> drawSpeedUnit = getEnabledModule(stack, MekaWeapons.DRAWSPEED_UNIT);
        if (drawSpeedUnit != null) {
            useTick -= 5.0f * drawSpeedUnit.getInstalledCount();
        }
        return useTick;
    }
}