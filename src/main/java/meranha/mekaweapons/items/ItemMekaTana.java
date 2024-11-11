package meranha.mekaweapons.items;

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
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.mekatool.ModuleAttackAmplificationUnit;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ItemMekaTana extends ItemEnergized implements IModuleContainerItem {
    public ItemMekaTana(Properties properties) {
        super(IModuleHelper.INSTANCE.applyModuleContainerProperties(properties.rarity(Rarity.EPIC).setNoRepair().stacksTo(1)));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
            addModuleDetails(stack, tooltip);
        } else {
            StorageUtils.addStoredEnergy(stack, tooltip, true);
            tooltip.add(MekanismLang.HOLD_FOR_MODULES.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
        }
    }

    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        IModule<ModuleAttackAmplificationUnit> attackAmplificationUnit = getEnabledModule(stack, MekanismModules.ATTACK_AMPLIFICATION_UNIT);
        int installedModules = (attackAmplificationUnit != null) ? attackAmplificationUnit.getInstalledCount() : 0;
        int baseDamage = MekaWeapons.general.mekaTanaBaseDamage.get();
        int totalDamage = baseDamage * (installedModules + 1);

        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        if (totalDamage > baseDamage && energyContainer != null && attacker instanceof Player player && !player.isCreative()) {
            energyContainer.extract(MekaWeapons.general.mekaTanaEnergyUsage.get() * installedModules, Action.EXECUTE, AutomationType.MANUAL);
        }
        return true;
    }

    @Override
    public void adjustAttributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        IModule<ModuleAttackAmplificationUnit> attackAmplificationUnit = getEnabledModule(stack, MekanismModules.ATTACK_AMPLIFICATION_UNIT);
        int installedModules = (attackAmplificationUnit != null) ? attackAmplificationUnit.getInstalledCount() : 1;
        int totalDamage = MekaWeapons.general.mekaTanaBaseDamage.get() * (installedModules);
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        long currentEnergy = (energyContainer != null) ? energyContainer.getEnergy(): 0;
        if (currentEnergy < MekaWeapons.general.mekaTanaEnergyUsage.get()) {
            totalDamage = MekaWeapons.general.mekaTanaBaseDamage.get();
        }

        event.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, totalDamage - 1, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        event.addModifier(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, MekaWeapons.general.mekaTanaAttackSpeed.get(), Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level world, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide()) {
            IModule<ModuleTeleportationUnit> module = getEnabledModule(stack, MekanismModules.TELEPORTATION_UNIT);
            if (module != null) {
                BlockHitResult result = MekanismUtils.rayTrace(player, MekaWeapons.general.mekaTanaMaxTeleportReach.get());
                if (!module.getCustomInstance().requiresBlockTarget() || result.getType() != HitResult.Type.MISS) {
                    BlockPos pos = result.getBlockPos();
                    if (isValidDestinationBlock(world, pos.above()) && isValidDestinationBlock(world, pos.above(2))) {
                        double distance = player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ());
                        if (distance < 5) {
                            return InteractionResultHolder.pass(stack);
                        }
                        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                        long energyNeeded = MathUtils.ceilToLong(MekaWeapons.general.mekaTanaTeleportUsage.get() * (distance / 10D));
                        if (energyContainer == null || energyContainer.getEnergy() < energyNeeded) {
                            return InteractionResultHolder.fail(stack);
                        }
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
                }
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    private boolean isValidDestinationBlock(Level world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        return blockState.isAir() || MekanismUtils.isLiquidBlock(blockState.getBlock());
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }
}
