package meranha.mekaweapons.items;

import java.util.List;
import java.util.function.Consumer;

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
import mekanism.common.util.text.BooleanStateDisplay;
import meranha.mekaweapons.MekaWeapons;
import meranha.mekaweapons.MekaWeaponsUtils;
import meranha.mekaweapons.WeaponsLang;
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

    public void onDestroyed(@NotNull ItemEntity item, @NotNull DamageSource damageSource) {
        ModuleHelper.INSTANCE.dropModuleContainerContents(item, damageSource);
    }

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

    public void adjustAttributes(@NotNull ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        IModule<ModuleWeaponAttackAmplificationUnit> attackAmplificationUnit = getEnabledModule(stack, MekaWeapons.ATTACKAMPLIFICATION_UNIT);
        double totalDamage = MekaWeaponsUtils.getTotalDamage(stack, attackAmplificationUnit, MekaWeapons.general.mekaBowBaseDamage, MekaWeapons.general.mekaBowEnergyUsage);

        event.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, totalDamage, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        //event.addModifier(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, (5 * installedModules) -9, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND); todo?
        IRadialModuleContainerItem.super.adjustAttributes(event);
    }

    public void onUseTick(@NotNull Level world, @NotNull LivingEntity player, @NotNull ItemStack stack, int timeLeft) {
        if (isModuleEnabled(stack, MekaWeapons.AUTOFIRE_UNIT) && getUseDuration(stack, player) - timeLeft == getUseTick(stack)) {
            player.stopUsingItem();
            stack.releaseUsing(world, player, 0);
            player.startUsingItem(player.getUsedItemHand());
        }
    }

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

    protected void shoot(@NotNull ServerLevel world, @NotNull LivingEntity entity, @NotNull InteractionHand hand, @NotNull ItemStack bow, @NotNull List<ItemStack> potentialAmmo, float velocity, float inaccuracy, boolean critical, @Nullable LivingEntity target) {
        super.shoot(world, entity, hand, bow, potentialAmmo, velocity, inaccuracy, critical, target);
        if(entity instanceof Player player && !player.isCreative()) {
            IModule<ModuleWeaponAttackAmplificationUnit> attackAmplificationUnit = getEnabledModule(bow, MekaWeapons.ATTACKAMPLIFICATION_UNIT);
            long energyNeeded = MekaWeaponsUtils.getEnergyNeeded(attackAmplificationUnit, MekaWeapons.general.mekaBowEnergyUsage);

            IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(bow, 0);
            if(energyContainer != null) {
                energyContainer.extract(energyNeeded, Action.EXECUTE, AutomationType.MANUAL);
            }
        }
    }

    @NotNull
    public AbstractArrow customArrow(@NotNull AbstractArrow arrow, @NotNull ItemStack projectileStack, @NotNull ItemStack weaponStack) {
        IModule<ModuleWeaponAttackAmplificationUnit> attackAmplificationUnit = getEnabledModule(weaponStack, MekaWeapons.ATTACKAMPLIFICATION_UNIT);
        long totalDamage = MekaWeaponsUtils.getTotalDamage(weaponStack, attackAmplificationUnit, MekaWeapons.general.mekaBowBaseDamage, MekaWeapons.general.mekaBowEnergyUsage);
        return new MekaArrowEntity(arrow.level(), arrow.getX(), arrow.getY(), arrow.getZ(), projectileStack, weaponStack, MathUtils.clampToInt(totalDamage));
    }

    public boolean shouldCauseReequipAnimation(@NotNull ItemStack oldStack, @NotNull ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    public void addItems(@NotNull Consumer<ItemStack> tabOutput) {
        tabOutput.accept(StorageUtils.getFilledEnergyVariant(this));
    }

    public boolean isBarVisible(@NotNull ItemStack stack) {
        return true;
    }

    public int getBarWidth(@NotNull ItemStack stack) {
        return StorageUtils.getEnergyBarWidth(stack);
    }

    public int getBarColor(@NotNull ItemStack stack) {
        return MekaWeaponsUtils.getBarCustomColor(stack, MekaWeapons.general.mekaBowEnergyUsage);
    }

    public boolean shouldCauseBlockBreakReset(@NotNull ItemStack oldStack, @NotNull ItemStack newStack) {
        return oldStack.getItem() != newStack.getItem();
    }

    public float getUseTick(@NotNull ItemStack stack) {
        float useTick = 20.0F;
        IModule<?> drawSpeedUnit = getEnabledModule(stack, MekaWeapons.DRAWSPEED_UNIT);
        if (drawSpeedUnit != null) {
            useTick -= 5.0f * drawSpeedUnit.getInstalledCount();
        }
        return useTick;
    }

    public boolean isEnchantable(@NotNull ItemStack stack) {
        return false;
    }

    public boolean isBookEnchantable(@NotNull ItemStack stack, @NotNull ItemStack book) {
        return false;
    }

    public ResourceLocation getRadialIdentifier() {
        return RADIAL_ID;
    }
}