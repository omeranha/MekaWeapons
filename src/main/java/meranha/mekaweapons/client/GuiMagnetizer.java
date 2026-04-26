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
        imageHeight = 182;
        titleLabelY = 4;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiFrequencySelector<>(this, 14));
        if (menu.getMagnetizerStack().isEmpty()) {
            return;
        }

        addRenderableWidget(new GuiDigitalSwitch(this, 27, 150, SILK,
                () -> menu.getMagnetizerStack().getOrDefault(MekaWeapons.TOGGLE_RENDER_MEKATANA.get(), true),
                (element, mouseX, mouseY) -> {
                    PacketUtils.sendToServer(new PacketWeaponItemGuiInteract(PacketWeaponItemGuiInteract.ItemGuiInteraction.TOGGLE_MEKATANA_RENDER, menu.getHand()));
                    return true;
                }, GuiDigitalSwitch.SwitchType.LOWER_ICON
        )).setTooltip(Tooltip.create(Component.translatable("gui.mekaweapons.render_tana")));

        addRenderableWidget(new GuiDigitalSwitch(this, 50, 150, SILK,
                () -> menu.getMagnetizerStack().getOrDefault(MekaWeapons.TOGGLE_RENDER_MEKABOW.get(), true),
                (element, mouseX, mouseY) -> {
                    PacketUtils.sendToServer(new PacketWeaponItemGuiInteract(PacketWeaponItemGuiInteract.ItemGuiInteraction.TOGGLE_MEKABOW_RENDER, menu.getHand()));
                    return true;
                }, GuiDigitalSwitch.SwitchType.LOWER_ICON
        )).setTooltip(Tooltip.create(Component.translatable("gui.mekaweapons.render_bow")));

        addRenderableWidget(new GuiDigitalSwitch(this, 73, 150, SILK,
                () -> menu.getMagnetizerStack().getOrDefault(MekaWeapons.TOGGLE_RENDER_MEKAGUN.get(), true),
                (element, mouseX, mouseY) -> {
                    PacketUtils.sendToServer(new PacketWeaponItemGuiInteract(PacketWeaponItemGuiInteract.ItemGuiInteraction.TOGGLE_MEKAGUN_RENDER, menu.getHand()));
                    return true;
                }, GuiDigitalSwitch.SwitchType.LOWER_ICON
        )).setTooltip(Tooltip.create(Component.translatable("gui.mekaweapons.render_gun")));
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
