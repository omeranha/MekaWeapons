package meranha.mekaweapons.client;

import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiDigitalSwitch;
import mekanism.client.gui.element.custom.GuiFrequencySelector;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketItemGuiInteract;
import mekanism.common.util.MekanismUtils;
import meranha.mekaweapons.MekaWeapons;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import mekanism.client.gui.element.custom.GuiFrequencySelector.IItemGuiFrequencySelector;
import net.minecraft.world.item.ItemStack;

public class GuiMagnetizer extends GuiMekanism<MagnetizerContainer> implements IItemGuiFrequencySelector<InventoryFrequency, MagnetizerContainer> {
    private static final ResourceLocation SILK = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "switch/silk.png");
    public GuiMagnetizer(MagnetizerContainer container, Inventory inv, Component title) {
        super(container, inv, title);
        imageHeight = 172;
        titleLabelY = 4;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiFrequencySelector<>(this, 14));
        if (menu.getMagnetizerStack().isEmpty()) {
            return;
        }

        addRenderableWidget(new GuiDigitalSwitch(this, 150, 14, SILK,
                () -> menu.getMagnetizerStack().getOrDefault(MekaWeapons.TOGGLE_RENDER.get(), true),
                (element, mouseX, mouseY) -> {
                    PacketUtils.sendToServer(new PacketWeaponItemGuiInteract(PacketWeaponItemGuiInteract.ItemGuiInteraction.TOGGLE_RENDER, menu.getHand()));
                    return true;
                }, GuiDigitalSwitch.SwitchType.LOWER_ICON
        )).setTooltip(Tooltip.create(Component.translatable(menu.getMagnetizerStack().getOrDefault(MekaWeapons.TOGGLE_RENDER.get(), true) ? "gui.mekaweapons.render_on" : "gui.mekaweapons.render_off")));
    }

    @Override
    public MagnetizerContainer getFrequencyContainer() {
        return menu;
    }

    @Override
    public FrequencyType<InventoryFrequency> getFrequencyType() {
        return FrequencyType.INVENTORY;
    }
}
