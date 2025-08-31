package meranha.mekaweapons.items;

import java.util.List;
import java.util.Optional;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.api.security.ISecurityUtils;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import mekanism.common.capabilities.security.item.ItemStackOwnerObject;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.integration.curios.CuriosIntegration;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.item.CapabilityItem;
import mekanism.common.item.interfaces.IGuiItem;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import meranha.mekaweapons.MekaWeapons;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class ItemMagnetizer extends CapabilityItem implements IFrequencyItem, IGuiItem {

    public ItemMagnetizer(Properties properties) {
        super(properties.rarity(Rarity.RARE).stacksTo(1));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        ISecurityUtils.INSTANCE.addSecurityTooltip(stack, tooltip);
        MekanismUtils.addFrequencyItemTooltip(stack, tooltip);
        super.appendHoverText(stack, world, tooltip, flag);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide() || !(entity instanceof Player player)) {
            return;
        }

        Frequency frequency = getFrequency(stack);
        if (frequency == null || frequency.getType() != FrequencyType.INVENTORY) return;
        IEnergyContainer frequencyContainer = ((InventoryFrequency)frequency).storedEnergy;
        FloatingLong toCharge = MekaWeapons.general.wirelessChargerEnergyRate.get();
        if (toCharge.isZero()) {
            return;
        }

        for (ItemStack slot : player.getInventory().items) {
            toCharge = charge(frequencyContainer, slot, toCharge);
            if (toCharge.isZero()) break;
        }

        if (!toCharge.isZero() && Mekanism.hooks.CuriosLoaded) {
            Optional<? extends IItemHandler> curiosInventory = CuriosIntegration.getCuriosInventory(player);
            if (curiosInventory.isEmpty()) return;
            IItemHandler handler = curiosInventory.get();
            for (int slot = 0, slots = handler.getSlots(); slot < slots; slot++) {
                toCharge = charge(frequencyContainer, handler.getStackInSlot(slot), toCharge);
                if (toCharge.isZero()) break;
            }
        }
    }

    private FloatingLong charge(IEnergyContainer energyContainer, ItemStack stack, FloatingLong amount) {
        if (!stack.isEmpty() && !amount.isZero()) {
            IStrictEnergyHandler handler = EnergyCompatUtils.getStrictEnergyHandler(stack);
            if (handler != null) {
                FloatingLong remaining = handler.insertEnergy(amount, Action.SIMULATE);
                if (remaining.smallerThan(amount)) {
                    FloatingLong toExtract = amount.subtract(remaining);
                    FloatingLong extracted = energyContainer.extract(toExtract, Action.EXECUTE, AutomationType.MANUAL);
                    FloatingLong inserted = handler.insertEnergy(extracted, Action.EXECUTE);
                    return inserted.add(remaining);
                }
            }
        }
        return amount;
    }

    @Override
    public FrequencyType<?> getFrequencyType() {
        return FrequencyType.INVENTORY;
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level world, @NotNull Player player, @NotNull InteractionHand hand) {
        return SecurityUtils.get().claimOrOpenGui(world, player, hand, getContainerType()::tryOpenGui);
    }

    @Override
    public ContainerTypeRegistryObject<?> getContainerType() {
        return MekaWeapons.MAGNETIZER_CONTAINER;
    }

    @Override
    protected void gatherCapabilities(List<ItemCapability> capabilities, ItemStack stack, CompoundTag nbt) {
        capabilities.add(new ItemStackOwnerObject());
        super.gatherCapabilities(capabilities, stack, nbt);
    }
}
