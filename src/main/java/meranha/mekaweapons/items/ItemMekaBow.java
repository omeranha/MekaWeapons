package meranha.mekaweapons.items;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.mekatool.ModuleAttackAmplificationUnit;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.BooleanStateDisplay;
import meranha.mekaweapons.MekaWeapons;
import meranha.mekaweapons.WeaponsLang;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
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

    @Override
    public void adjustAttributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        event.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, getDamage(stack) - 1, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        //event.addModifier(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, (5 * installedModules) -9, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND); todo?
    }

    public void onUseTick(@Nonnull Level world, @Nonnull LivingEntity player, @Nonnull ItemStack stack, int timeLeft) {
        if (isModuleEnabled(stack, MekaWeapons.AUTOFIRE_UNIT) && getUseDuration(stack, player) - timeLeft == getUseTick(stack)) {
            player.stopUsingItem();
            stack.releaseUsing(world, player, 0);
            player.startUsingItem(player.getUsedItemHand());
        }
    }

    @Override
    public void releaseUsing(@NotNull ItemStack bow, @NotNull Level world, @NotNull LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player && !player.isCreative()) {
            IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(bow, 0);
            long energyNeeded = MekaWeapons.general.mekaBowEnergyUsage.get();
            if (energyContainer == null || energyContainer.extract(energyNeeded, Action.SIMULATE, AutomationType.MANUAL) < energyNeeded) {
                return;
            }
        }
        super.releaseUsing(bow, world, entity, timeLeft);
    }

    @Override
    protected void shoot(@NotNull ServerLevel world, @NotNull LivingEntity entity, @NotNull InteractionHand hand, @NotNull ItemStack bow, @NotNull List<ItemStack> potentialAmmo, float velocity, float inaccuracy, boolean critical, @Nullable LivingEntity target) {
        super.shoot(world, entity, hand, bow, potentialAmmo, velocity, inaccuracy, critical, target);
        if (entity instanceof Player player && !player.isCreative() && (!potentialAmmo.isEmpty() || isModuleEnabled(bow, MekaWeapons.ARROWENERGY_UNIT))) {
            IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(bow, 0);
            IModule<ModuleAttackAmplificationUnit> attackAmplificationUnit = getEnabledModule(bow, MekanismModules.ATTACK_AMPLIFICATION_UNIT);
            int installedModules = (attackAmplificationUnit != null) ? attackAmplificationUnit.getInstalledCount() : 1;
            if (energyContainer != null) {
                long energyNeeded = MekaWeapons.general.mekaBowEnergyUsage.get() * installedModules;
                energyContainer.extract(energyNeeded, Action.EXECUTE, AutomationType.MANUAL);
            }
        }
    }

    @Override
    public AbstractArrow customArrow(AbstractArrow arrow, ItemStack projectileStack, ItemStack weaponStack) {
        return new MekaArrowEntity(arrow.level(), arrow.getX(), arrow.getY(), arrow.getZ(), projectileStack, isModuleEnabled(weaponStack, MekaWeapons.GRAVITYDAMPENER_UNIT), getDamage(weaponStack));
    }

    public int getDamage(@Nonnull ItemStack stack) {
        IModule<ModuleAttackAmplificationUnit> attackAmplificationUnit = getEnabledModule(stack, MekanismModules.ATTACK_AMPLIFICATION_UNIT);
        int installedModules = (attackAmplificationUnit != null) ? attackAmplificationUnit.getInstalledCount() : 0;
        return MekaWeapons.general.mekaBowBaseDamage.get() * (installedModules + 1);
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
