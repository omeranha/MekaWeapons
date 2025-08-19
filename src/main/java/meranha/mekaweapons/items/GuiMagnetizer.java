package meranha.mekaweapons.items;

import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.custom.GuiFrequencySelector;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.lib.frequency.FrequencyType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import mekanism.client.gui.element.custom.GuiFrequencySelector.IItemGuiFrequencySelector;

public class GuiMagnetizer extends GuiMekanism<MagnetizerContainer> implements IItemGuiFrequencySelector<InventoryFrequency, MagnetizerContainer> {
    public GuiMagnetizer(MagnetizerContainer container, Inventory inv, Component title) {
        super(container, inv, title);
        imageHeight = 172;
        titleLabelY = 4;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiFrequencySelector<>(this, 14));
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
