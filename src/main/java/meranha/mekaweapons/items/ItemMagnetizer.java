package meranha.mekaweapons.items;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.Mekanism;
import mekanism.common.attachments.FrequencyAware;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.integration.curios.CuriosIntegration;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.lib.security.ItemSecurityUtils;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.MekanismUtils;
import meranha.mekaweapons.MekaWeapons;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import mekanism.api.text.EnumColor;
import meranha.mekaweapons.client.WeaponsLang;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.common.capabilities.ICapabilityAware;
import mekanism.common.capabilities.security.OwnerObject;
import mekanism.common.item.interfaces.IGuiItem;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class ItemMagnetizer extends Item implements IFrequencyItem, IGuiItem, ICapabilityAware, IModuleContainerItem {
    private record ChargeTarget(IStrictEnergyHandler handler, long wanted) {}

    public ItemMagnetizer(@NotNull Properties pProperties) {
        super(pProperties.rarity(Rarity.RARE).stacksTo(1).component(MekaWeapons.TOGGLE_RENDER_MEKATANA, true).component(MekaWeapons.TOGGLE_RENDER_MEKABOW, true).component(MekaWeapons.TOGGLE_RENDER_MEKAGUN, true));
    }

    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        IItemSecurityUtils.INSTANCE.addSecurityTooltip(stack, tooltip);
        MekanismUtils.addFrequencyItemTooltip(stack, tooltip);
        tooltip.add(WeaponsLang.MAGNETIZER.translateColored(EnumColor.WHITE));
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide() || !(entity instanceof Player player)) return;

        FrequencyAware<InventoryFrequency> frequencyAware = stack.get(getFrequencyComponent());
        if (frequencyAware == null || !(frequencyAware.getFrequency(stack, getFrequencyComponent()) instanceof InventoryFrequency frequency)) return;
        IEnergyContainer frequencyContainer = frequency.storedEnergy;
        long available = Math.min(MekaWeapons.general.wirelessChargerEnergyRate.get(), frequencyContainer.getEnergy());
        if (available <= 0) {
            return;
        }

        List<ChargeTarget> targets = new ArrayList<>();
        for (List<ItemStack> compartment : List.of(player.getInventory().items, player.getInventory().armor, player.getInventory().offhand)) {
            for (ItemStack inventorySlot : compartment) {
                collectChargeTarget(targets, inventorySlot, available);
            }
        }

        if (Mekanism.hooks.curios.isLoaded()) {
            IItemHandler handler = CuriosIntegration.getCuriosInventory(player);
            if (handler != null) {
                for (int i = 0; i < handler.getSlots(); i++) {
                    collectChargeTarget(targets, handler.getStackInSlot(i), available);
                }
            }
        }

        if (targets.isEmpty()) {
            return;
        }

        long totalWanted = 0;
        for (ChargeTarget target : targets) {
            totalWanted += target.wanted();
        }

        if (totalWanted <= 0) {
            return;
        }

        long remainingBudget = available;
        for (int i = 0; i < targets.size() && remainingBudget > 0; i++) {
            ChargeTarget target = targets.get(i);
            long allocation;
            if (i == targets.size() - 1) {
                allocation = remainingBudget;
            } else {
                allocation = Math.min(target.wanted(), available * target.wanted() / totalWanted);
            }
            long unused = charge(frequencyContainer, target.handler(), allocation);
            remainingBudget = remainingBudget - allocation + unused;
        }
    }

    private void collectChargeTarget(List<ChargeTarget> targets, ItemStack stack, long maxTransfer) {
        if (stack.isEmpty()) {
            return;
        }

        IStrictEnergyHandler handler = EnergyCompatUtils.getStrictEnergyHandler(stack);
        if (handler == null) {
            return;
        }

        long remainder = handler.insertEnergy(maxTransfer, Action.SIMULATE);
        long wanted = maxTransfer - remainder;
        if (wanted > 0) {
            targets.add(new ChargeTarget(handler, wanted));
        }
    }

    private long charge(IEnergyContainer energyContainer, IStrictEnergyHandler handler, long amount) {
        if (amount <= 0) {
            return amount;
        }

        long remaining = handler.insertEnergy(amount, Action.SIMULATE);
        if (remaining == amount) {
            return amount;
        }

        long toExtract = amount - remaining;
        long extracted = energyContainer.extract(toExtract, Action.EXECUTE, AutomationType.MANUAL);
        long leftover = handler.insertEnergy(extracted, Action.EXECUTE);
        return remaining + leftover;
    }

    @Override
    public FrequencyType<?> getFrequencyType() {
        return FrequencyType.INVENTORY;
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level world, @NotNull Player player, @NotNull InteractionHand hand) {
        return ItemSecurityUtils.get().claimOrOpenGui(world, player, hand, getContainerType()::tryOpenGui);
    }

    @Override
    public ContainerTypeRegistryObject<?> getContainerType() {
        return MekaWeapons.MAGNETIZER_CONTAINER;
    }

    @Override
    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(IItemSecurityUtils.INSTANCE.ownerCapability(), (stack, ctx) -> new OwnerObject(stack), this);
    }

    @NotNull
    public DataComponentType<FrequencyAware<InventoryFrequency>> getFrequencyComponent() {
        return MekanismDataComponents.INVENTORY_FREQUENCY.get();
    }
}
