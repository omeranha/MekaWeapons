package meranha.mekaweapons.client;

import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiDigitalSwitch;
import mekanism.client.gui.element.custom.GuiFrequencySelector;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.network.PacketUtils;
import mekanism.common.util.MekanismUtils;
import meranha.mekaweapons.MekaWeapons;
import net.minecraft.client.gui.components.Button;
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
        // position relative to GUI origin
        int x = leftPos + 120;
        int y = topPos + 20;

        ItemStack stack = menu.getMagnetizerStack();
        if (stack.isEmpty()) {
            return;
        }
        addRenderableWidget(new GuiDigitalSwitch(this, x, y, SILK,
                () -> {
                    ItemStack s = menu.getMagnetizerStack();
                    return !s.isEmpty() && !Boolean.FALSE.equals(s.get(MekaWeapons.TOGGLE_RENDER.get()));
                },
                (element, mouseX, mouseY) -> {
                    PacketUtils.sendToServer(
                }, GuiDigitalSwitch.SwitchType.LEFT_ICON
        ));
    }

    private Component getToggleLabel() {
        ItemStack stack = menu.getMagnetizerStack();
        boolean enabled = !Boolean.FALSE.equals(stack.get(MekaWeapons.TOGGLE_RENDER.get()));
        return Component.translatable(enabled ? "gui.mekaweapons.render_on" : "gui.mekaweapons.render_off");
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
