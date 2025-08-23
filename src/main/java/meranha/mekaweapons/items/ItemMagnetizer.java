package meranha.mekaweapons.items;

import java.util.List;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.Mekanism;
import mekanism.common.attachments.FrequencyAware;
import mekanism.common.content.entangloporter.InventoryFrequency;
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

public class ItemMagnetizer extends Item implements IFrequencyItem, IGuiItem, ICapabilityAware {
    public ItemMagnetizer(@NotNull Properties pProperties) {
        super(pProperties.rarity(Rarity.RARE).stacksTo(1));
    }

    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        IItemSecurityUtils.INSTANCE.addSecurityTooltip(stack, tooltip);
        MekanismUtils.addFrequencyItemTooltip(stack, tooltip);
        tooltip.add(WeaponsLang.MAGNETIZER.translateColored(EnumColor.WHITE));
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide() || !(entity instanceof Player player)) {
            return;
        }

        FrequencyAware<InventoryFrequency> frequencyAware = stack.get(getFrequencyComponent());
        if (frequencyAware == null || !(frequencyAware.getFrequency(stack, getFrequencyComponent()) instanceof InventoryFrequency frequency)) return;
        IEnergyContainer frequencyContainer = frequency.storedEnergy;
        long toCharge = Math.min(MekaWeapons.general.wirelessChargerEnergyRate.get(), frequencyContainer.getEnergy());
        if (toCharge == 0L) {
            return;
        }

        for (ItemStack slot : player.getInventory().items) {
            toCharge = charge(frequencyContainer, slot, toCharge);
            if (toCharge == 0L) return;
        }

        if (Mekanism.hooks.curios.isLoaded()) {
            IItemHandler handler = CuriosIntegration.getCuriosInventory(player);
            if (handler == null) return;
            for (int slot = 0, slots = handler.getSlots(); slot < slots; slot++) {
                toCharge = charge(frequencyContainer, handler.getStackInSlot(slot), toCharge);
                if (toCharge == 0L) return;
            }
        }
    }

    private long charge(IEnergyContainer energyContainer, ItemStack stack, long amount) {
        if (!stack.isEmpty() && amount > 0L) {
            IStrictEnergyHandler handler = EnergyCompatUtils.getStrictEnergyHandler(stack);
            if (handler != null) {
                long remaining = handler.insertEnergy(amount, Action.SIMULATE);
                if (remaining < amount) {
                    long toExtract = amount - remaining;
                    long extracted = energyContainer.extract(toExtract, Action.EXECUTE, AutomationType.MANUAL);
                    long inserted = handler.insertEnergy(extracted, Action.EXECUTE);
                    return inserted + remaining;
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
