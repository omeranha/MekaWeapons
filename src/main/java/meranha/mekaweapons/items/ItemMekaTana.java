package meranha.mekaweapons.items;

import static meranha.mekaweapons.MekaWeaponsUtils.*;

import java.util.List;

import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.NotNull;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.event.MekanismTeleportEvent;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.IRadialModuleContainerItem;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.content.gear.mekatool.ModuleTeleportationUnit;
import mekanism.common.item.ItemEnergized;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_client.PacketPortalFX;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import meranha.mekaweapons.MekaWeapons;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

public class ItemMekaTana extends ItemEnergized implements IRadialModuleContainerItem {
    private static final ResourceLocation RADIAL_ID = MekaWeapons.rl("meka_tana");

    public ItemMekaTana(@NotNull Properties properties) {
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
    public boolean canPerformAction(@NotNull ItemStack stack, @NotNull ItemAbility itemAbility) {
        if (isModuleEnabled(stack, MekaWeapons.SWEEPING_UNIT)) {
            return ItemAbilities.DEFAULT_SWORD_ACTIONS.contains(itemAbility);
        }
        return false;
    }

    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        if(attacker instanceof Player player && !player.isCreative()) {
            long energyNeeded = getEnergyNeeded(stack);
            IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
            if(energyContainer != null) {
                energyContainer.extract(energyNeeded, Action.EXECUTE, AutomationType.MANUAL);
            }
        }
        return true;
    }

    @Override
    public void adjustAttributes(@NotNull ItemAttributeModifierEvent event) {
        long totalDamage = getTotalDamage(event.getItemStack());
        event.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, totalDamage, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        event.addModifier(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, MekaWeapons.general.mekaTanaAttackSpeed.get(), Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        IRadialModuleContainerItem.super.adjustAttributes(event);
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level world, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (world.isClientSide()) {
            return InteractionResultHolder.pass(stack);
        }

        IModule<ModuleTeleportationUnit> module = getEnabledModule(stack, MekanismModules.TELEPORTATION_UNIT);
        BlockHitResult result = MekanismUtils.rayTrace(player, MekaWeapons.general.mekaTanaMaxTeleportReach.get());
        if (module == null || module.getCustomInstance().requiresBlockTarget() && result.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(stack);
        }

        BlockPos pos = result.getBlockPos();
        double distance = player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ());
        if (!isValidDestination(world, pos) || distance < 5) {
            return InteractionResultHolder.pass(stack);
        }

        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        long energyNeeded = MathUtils.ceilToLong(MekaWeapons.general.mekaTanaTeleportUsage.get() * (distance / 10D));
        if (energyContainer == null || energyContainer.getEnergy() < energyNeeded) {
            return InteractionResultHolder.fail(stack);
        }

        return teleportPlayer(world, player, stack, pos, energyContainer, energyNeeded, result);
    }

    private boolean isValidDestination(@NotNull Level world, @NotNull BlockPos pos) {
        return isValidDestinationBlock(world, pos.above()) && isValidDestinationBlock(world, pos.above(2));
    }

    private boolean isValidDestinationBlock(@NotNull Level world, @NotNull BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        return blockState.isAir() || MekanismUtils.isLiquidBlock(blockState.getBlock());
    }

    private InteractionResultHolder<ItemStack> teleportPlayer(Level world, Player player, ItemStack stack, @NotNull BlockPos pos, IEnergyContainer energyContainer, long energyNeeded, BlockHitResult result) {
        double targetX = pos.getX() + 0.5;
        double targetY = pos.getY() + 1.5;
        double targetZ = pos.getZ() + 0.5;
        
        MekanismTeleportEvent.MekaTool event = new MekanismTeleportEvent.MekaTool(player, targetX, targetY, targetZ, stack, result);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
            return InteractionResultHolder.fail(stack);
        }

        energyContainer.extract(energyNeeded, Action.EXECUTE, AutomationType.MANUAL);
        if (player.isPassenger()) {
            player.dismountTo(targetX, targetY, targetZ);
        } else {
            player.teleportTo(targetX, targetY, targetZ);
        }

        player.resetFallDistance();
        PacketUtils.sendToAllTracking(new PacketPortalFX(pos.above()), world, pos);
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_TELEPORT, SoundSource.PLAYERS);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return getBarCustomColor(stack);
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
        return MekaWeapons.general.mekaTanaEnchantments.get();
    }

    @Override
    public boolean isBookEnchantable(@NotNull ItemStack stack, @NotNull ItemStack book) {
        return MekaWeapons.general.mekaTanaEnchantments.get();
    }

    public ResourceLocation getRadialIdentifier() {
        return RADIAL_ID;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return false;
    }
}