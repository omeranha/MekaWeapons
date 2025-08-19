package meranha.mekaweapons.items;

import java.util.List;

import mekanism.common.attachments.FrequencyAware;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.lib.security.ItemSecurityUtils;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.MekanismUtils;
import meranha.mekaweapons.MekaWeapons;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.InteractionResultHolder;
import org.jetbrains.annotations.NotNull;

import mekanism.api.text.EnumColor;
import meranha.mekaweapons.WeaponsLang;
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
