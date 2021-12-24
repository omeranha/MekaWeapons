package meranha.mekatana;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import mekanism.api.Action;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.ItemEnergized;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.network.to_client.PacketPortalFX;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidBlock;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemMekaTana extends ItemEnergized implements IItemHUDProvider {

    public ItemMekaTana(Properties properties) {
        super(MekanismConfig.gear.mekaToolBaseChargeRate, MekanismConfig.gear.mekaToolBaseEnergyCapacity, properties.rarity(Rarity.EPIC).setNoRepair());
        Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2.4D, Operation.ADDITION));
        builder.build();
    }

    @Override
    public boolean hurtEnemy(@Nonnull ItemStack stack, @Nonnull LivingEntity target, @Nonnull LivingEntity attacker) {
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        FloatingLong energy = energyContainer == null ? FloatingLong.ZERO : energyContainer.getEnergy();
        FloatingLong energyCost = FloatingLong.ZERO;
        int minDamage = ModConfig.general.mekaTanaMinDamage.get(), maxDamage;
        maxDamage = ModConfig.general.mekaTanaMaxDamage.get();
        if (maxDamage > minDamage) {
            energyCost = ModConfig.general.mekaTanaEnergyUsage.get().multiply((maxDamage - minDamage) / 4F);
        }
        minDamage = Math.min(minDamage, maxDamage);
        int damageDifference = maxDamage - minDamage;
        double percent = 1;
        if (energy.smallerThan(energyCost)) {
            percent = energy.divideToLevel(energyCost);
        }
        float damage = (float) (minDamage + damageDifference * percent);
        if (attacker instanceof PlayerEntity) {
            target.hurt(DamageSource.playerAttack((PlayerEntity) attacker), damage);
        } else {
            target.hurt(DamageSource.mobAttack(attacker), damage);
        }
        if (energyContainer != null && !energy.isZero()) {
            energyContainer.extract(energyCost, Action.EXECUTE, AutomationType.MANUAL);
        }
        return false;
    }

    private boolean isValidDestinationBlock(World world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        return blockState.is(Blocks.AIR) || blockState.getBlock() instanceof FlowingFluidBlock || blockState.getBlock() instanceof IFluidBlock;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, @Nonnull Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide()) {
            BlockRayTraceResult result = MekanismUtils.rayTrace(player, ModConfig.general.mekaTanaMaxTeleportReach.get());
            BlockPos pos = result.getBlockPos();
            if (isValidDestinationBlock(world, pos.above()) && isValidDestinationBlock(world, pos.above(2))) {
                double distance = player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ());
                if (distance < 5) {
                    return new ActionResult<>(ActionResultType.PASS, stack);
                }
                IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                FloatingLong energyNeeded = ModConfig.general.mekaTanaTeleportUsage.get().multiply(distance / 10D);
                if (energyContainer == null || energyContainer.getEnergy().smallerThan(energyNeeded)) {
                    return new ActionResult<>(ActionResultType.FAIL, stack);
                }
                energyContainer.extract(energyNeeded, Action.EXECUTE, AutomationType.MANUAL);
                if (player.isPassenger()) {
                    player.stopRiding();
                }
                player.teleportTo(pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5);
                player.fallDistance = 0.0F;
                Mekanism.packetHandler.sendToAllTracking(new PacketPortalFX(pos.above()), world, pos);
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                return new ActionResult<>(ActionResultType.SUCCESS, stack);
            }
        }
        return new ActionResult<>(ActionResultType.PASS, stack);
    }

    @Override
    public void addHUDStrings(List<ITextComponent> list, PlayerEntity player, ItemStack stack, EquipmentSlotType slotType) {
        StorageUtils.addStoredEnergy(stack, list, true, MekanismLang.STORED_ENERGY);
    }

    @Override
    public boolean isEnchantable(@Nonnull ItemStack stack) {
        return true;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return true;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return true;
    }

    @Override
    public boolean isFoil(@Nonnull ItemStack stack) {
        return false;
    }
}