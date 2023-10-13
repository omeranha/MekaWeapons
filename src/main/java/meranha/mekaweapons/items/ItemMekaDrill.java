package meranha.mekaweapons.items;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.common.item.ItemEnergized;
import mekanism.common.util.StorageUtils;
import meranha.mekaweapons.MekaWeapons;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.Objects;

public class ItemMekaDrill extends ItemEnergized {
    public ItemMekaDrill(Properties properties) {
        super(MekaWeapons.general.mekaDrillChargeRate, MekaWeapons.general.mekaDrillEnergyCapacity, properties.rarity(Rarity.RARE).setNoRepair());
    }

    @Override
    public boolean isCorrectToolForDrops(@Nonnull BlockState state) {
        //Allow harvesting everything, things that are unbreakable are caught elsewhere
        return true;
    }

    @Override
    public float getDestroySpeed(@Nonnull ItemStack stack, @Nonnull BlockState state) {
        FloatingLong energyRequired = MekaWeapons.general.mekaDrillEnergyUsage.get();
        if (Objects.requireNonNull(StorageUtils.getEnergyContainer(stack, 0)).extract(energyRequired, Action.SIMULATE, AutomationType.MANUAL).smallerThan(energyRequired)) {
            return 0;
        }

        if (state.is(BlockTags.MINEABLE_WITH_SHOVEL) || state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            return 25;
        }
        return 5;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        if (player.level().isClientSide || player.isCreative()) {
            return super.onBlockStartBreak(stack, pos, player);
        }

        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        if (energyContainer != null) {
            FloatingLong energyRequired = MekaWeapons.general.mekaDrillEnergyUsage.get();
            if (energyContainer.extract(energyRequired, Action.SIMULATE, AutomationType.MANUAL).greaterOrEqual(energyRequired)) {
                energyContainer.extract(energyRequired, Action.EXECUTE, AutomationType.MANUAL);
            }
        }
        return super.onBlockStartBreak(stack, pos, player);
    }

    @Override
    public boolean isFoil(@Nonnull ItemStack stack) {
        return false;
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        return true;
    }
}