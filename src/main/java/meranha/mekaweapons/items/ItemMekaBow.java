package meranha.mekaweapons.items;

import static meranha.mekaweapons.MekaWeaponsUtils.*;
import java.util.List;

import meranha.mekaweapons.items.modules.DrawSpeedUnit;
import meranha.mekaweapons.items.modules.WeaponsModules;
import net.minecraft.world.InteractionResultHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.IRadialModuleContainerItem;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.util.StorageUtils;
import meranha.mekaweapons.MekaWeapons;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

public class ItemMekaBow extends BowItem implements IRadialModuleContainerItem {
    private static final ResourceLocation RADIAL_ID = MekaWeapons.rl("meka_bow");

    public ItemMekaBow(@NotNull Properties properties) {
        super(IModuleHelper.INSTANCE.applyModuleContainerProperties(properties.rarity(Rarity.EPIC).setNoRepair().stacksTo(1)));
    }

    @Override
    public void onDestroyed(@NotNull ItemEntity item, @NotNull DamageSource damageSource) {
        ModuleHelper.INSTANCE.dropModuleContainerContents(item, damageSource);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
            addModuleDetails(stack, tooltip);
            return;
        }

        StorageUtils.addStoredEnergy(stack, tooltip, true);
        tooltip.add(MekanismLang.HOLD_FOR_MODULES.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
    }

    @Override
    public void adjustAttributes(@NotNull ItemAttributeModifierEvent event) {
        long totalDamage = getTotalDamage(event.getItemStack());
        event.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, totalDamage, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        IRadialModuleContainerItem.super.adjustAttributes(event);
    }

    @Override
    public void onUseTick(@NotNull Level world, @NotNull LivingEntity player, @NotNull ItemStack stack, int timeLeft) {
        if (player.isAlive() && isModuleEnabled(stack, WeaponsModules.AUTOFIRE_UNIT) && getUseDuration(stack, player) - timeLeft == getUseTick(stack)) {
            player.stopUsingItem();
            stack.releaseUsing(world, player, 0);
            player.startUsingItem(player.getUsedItemHand());
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack bow = player.getItemInHand(hand);
        if (world.isClientSide && isEnergyInsufficient(bow)) {
            return InteractionResultHolder.fail(bow);
        }
        return super.use(world, player, hand);
    }

    @Override
    public void releaseUsing(@NotNull ItemStack bow, @NotNull Level world, @NotNull LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player) || (!player.isCreative() && isEnergyInsufficient(bow))) {
            return;
        }
        super.releaseUsing(bow, world, entity, timeLeft);
    }

    @Override
    protected void shoot(@NotNull ServerLevel world, @NotNull LivingEntity entity, @NotNull InteractionHand hand, @NotNull ItemStack bow, @NotNull List<ItemStack> potentialAmmo, float velocity, float inaccuracy, boolean critical, @Nullable LivingEntity target) {
        if (!(entity instanceof Player player)) {
            return;
        }

        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(bow, 0);
        if (!player.isCreative() && energyContainer != null) {
            long energyNeeded = getEnergyNeeded(bow);
            energyContainer.extract(energyNeeded, Action.EXECUTE, AutomationType.MANUAL);
        }
        super.shoot(world, entity, hand, bow, potentialAmmo, velocity, inaccuracy, critical, target);
    }

    @NotNull
    @Override
    public AbstractArrow customArrow(@NotNull AbstractArrow arrow, @NotNull ItemStack projectileStack, @NotNull ItemStack weaponStack) {
        long totalDamage = getTotalDamage(weaponStack);
        return new MekaArrowEntity(arrow.level(), arrow, projectileStack, weaponStack, MathUtils.clampToInt(totalDamage));
    }

    @Override
    public boolean shouldCauseReequipAnimation(@NotNull ItemStack oldStack, @NotNull ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        return StorageUtils.getEnergyBarWidth(stack);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return getBarCustomColor(stack);
    }

    @Override
    public boolean shouldCauseBlockBreakReset(@NotNull ItemStack oldStack, @NotNull ItemStack newStack) {
        return oldStack.getItem() != newStack.getItem();
    }

    public float getUseTick(@NotNull ItemStack stack) {
        float useTick = 20.0F;
        IModule<DrawSpeedUnit> drawSpeedUnit = getEnabledModule(stack, WeaponsModules.DRAWSPEED_UNIT);
        if (drawSpeedUnit != null) {
            useTick -= 5.0f * drawSpeedUnit.getCustomInstance().getDrawSpeed();
        }
        return useTick;
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
        return MekaWeapons.general.mekaBowEnchantments.get();
    }

    @Override
    public boolean isBookEnchantable(@NotNull ItemStack stack, @NotNull ItemStack book) {
        return MekaWeapons.general.mekaBowEnchantments.get();
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return false;
    }

    public ResourceLocation getRadialIdentifier() {
        return RADIAL_ID;
    }
}
