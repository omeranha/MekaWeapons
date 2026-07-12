package meranha.mekaweapons.client;

import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiDigitalSwitch;
import mekanism.client.gui.element.custom.GuiFrequencySelector;
import mekanism.common.Mekanism;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.util.MekanismUtils;
import meranha.mekaweapons.items.ItemMagnetizer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import mekanism.client.gui.element.custom.GuiFrequencySelector.IItemGuiFrequencySelector;
import net.minecraft.world.item.Item;

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

        int buttonsX = 61;
        int buttonsY = 150;
        Item item = menu.getMagnetizerStack().getItem();
        if (!(item instanceof ItemMagnetizer magnetizer)) {
            return;
        }

        addRenderableWidget(new GuiDigitalSwitch(this, buttonsX, buttonsY, SILK, () ->  magnetizer.getRenderValue(menu.getMagnetizerStack(), ItemMagnetizer.RENDER_KATANA), WeaponsLang.MEKATANA_TOGGLE_RENDER.translate(),
                () -> {
                    Mekanism.packetHandler().sendToServer(new PacketWeaponItemGuiInteract(menu.getHand(), ItemMagnetizer.RENDER_KATANA, !magnetizer.getRenderValue(menu.getMagnetizerStack(), ItemMagnetizer.RENDER_KATANA)));
                    menu.broadcastChanges();
                }, GuiDigitalSwitch.SwitchType.LOWER_ICON));

        buttonsX += 20;
        addRenderableWidget(new GuiDigitalSwitch(this, buttonsX, buttonsY, SILK, () -> magnetizer.getRenderValue(menu.getMagnetizerStack(), ItemMagnetizer.RENDER_BOW), WeaponsLang.MEKABOW_TOGGLE_RENDER.translate(),
                () -> {
                    Mekanism.packetHandler().sendToServer(new PacketWeaponItemGuiInteract(menu.getHand(), ItemMagnetizer.RENDER_BOW, !magnetizer.getRenderValue(menu.getMagnetizerStack(), ItemMagnetizer.RENDER_BOW)));
                    menu.broadcastChanges();
                }, GuiDigitalSwitch.SwitchType.LOWER_ICON));

        buttonsX += 20;
        addRenderableWidget(new GuiDigitalSwitch(this, buttonsX, buttonsY, SILK, () -> magnetizer.getRenderValue(menu.getMagnetizerStack(), ItemMagnetizer.RENDER_GUN), WeaponsLang.MEKAGUN_TOGGLE_RENDER.translate(),
                () -> {
                    Mekanism.packetHandler().sendToServer(new PacketWeaponItemGuiInteract(menu.getHand(), ItemMagnetizer.RENDER_GUN, !magnetizer.getRenderValue(menu.getMagnetizerStack(), ItemMagnetizer.RENDER_GUN)));
                    menu.broadcastChanges();
                }, GuiDigitalSwitch.SwitchType.LOWER_ICON));
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
