package meranha.mekaweapons.client;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.render.IFancyFontRenderer.TextAlignment;
import mekanism.common.network.PacketUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import meranha.mekaweapons.client.EnchantmentImbuerContainer.StoredEnchantment;
import meranha.mekaweapons.items.modules.EnchantmentUnit;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;

public class GuiEnchantmentImbuer extends GuiMekanism<EnchantmentImbuerContainer> {

    private static final int LIST_TOP = 66;
    private static final int ROW_HEIGHT = 10;
    private static final int TEXT_X = 12;
    private static final int REMOVE_X = 152;

    private final List<MekanismImageButton> removeButtons = new ArrayList<>();
    private TranslationButton imbueButton;

    public GuiEnchantmentImbuer(EnchantmentImbuerContainer container, Inventory inv, Component title) {
        super(container, inv, title);
        imageHeight = 210;
        inventoryLabelY = imageHeight - 94;
        titleLabelY = 4;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiInnerScreen(this, 8, 15, 160, 12, this::getStatusText));
        addRenderableWidget(new GuiInnerScreen(this, 8, 52, 160, 58));
        imbueButton = addRenderableWidget(new TranslationButton(this, 62, 32, 52, 16, WeaponsLang.BUTTON_IMBUE, (element, mouseX, mouseY) -> {
            PacketUtils.sendToServer(PacketImbueEnchantments.INSTANCE);
            return true;
        }));

        removeButtons.clear();
        for (int i = 0; i < EnchantmentUnit.MAX_ENCHANTMENTS; i++) {
            int row = i;
            MekanismImageButton button = addRenderableWidget(new RemoveButton(this, REMOVE_X, LIST_TOP + row * ROW_HEIGHT, (element, mouseX, mouseY) -> {
                List<StoredEnchantment> stored = menu.getStored();
                if (row < stored.size()) {
                    PacketUtils.sendToServer(new PacketRemoveEnchantment(stored.get(row).id()));
                    return true;
                }
                return false;
            }));
            button.setTooltip(WeaponsLang.IMBUER_REMOVE);
            removeButtons.add(button);
        }
        updateButtons();
    }

    @Override
    public void containerTick() {
        super.containerTick();
        updateButtons();
    }

    private void updateButtons() {
        if (imbueButton != null) {
            imbueButton.active = menu.canImbue();
        }
        int stored = menu.getStored().size();
        for (int i = 0; i < removeButtons.size(); i++) {
            MekanismImageButton button = removeButtons.get(i);
            button.visible = i < stored;
            button.active = i < stored;
        }
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics);

        List<StoredEnchantment> stored = menu.getStored();
        drawScaledScrollingString(guiGraphics, WeaponsLang.IMBUER_STORED.translateColored(EnumColor.WHITE, EnumColor.AQUA, stored.size(), EnchantmentUnit.MAX_ENCHANTMENTS),
                TEXT_X, 55, TextAlignment.LEFT, screenTextColor(), 148, 0, false, 0.8F);
        for (int i = 0; i < stored.size(); i++) {
            StoredEnchantment entry = stored.get(i);
            drawScaledScrollingString(guiGraphics, Enchantment.getFullname(entry.enchantment(), entry.level()), TEXT_X, LIST_TOP + i * ROW_HEIGHT,
                    TextAlignment.LEFT, screenTextColor(), REMOVE_X - TEXT_X - 4, 0, false, 0.8F);
        }

        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }

    private List<Component> getStatusText() {
        List<Component> text = new ArrayList<>();
        text.add(switch (menu.getStatus()) {
            case NOT_SUPPORTED -> WeaponsLang.IMBUER_NOT_SUPPORTED.translateColored(EnumColor.RED);
            case INCOMPATIBLE -> WeaponsLang.IMBUER_INCOMPATIBLE.translateColored(EnumColor.RED);
            case FULL -> WeaponsLang.IMBUER_FULL.translateColored(EnumColor.YELLOW, EnumColor.AQUA, EnchantmentUnit.MAX_ENCHANTMENTS);
            case NOTHING_NEW -> WeaponsLang.IMBUER_NOTHING_TO_ADD.translateColored(EnumColor.YELLOW);
            default -> WeaponsLang.IMBUER_READY.translateColored(EnumColor.BRIGHT_GREEN);
        });
        return text;
    }

    private static class RemoveButton extends MekanismImageButton {

        RemoveButton(IGuiWrapper gui, int x, int y, IClickable onPress) {
            super(gui, x, y, 8, 12, MekanismUtils.getResource(ResourceType.GUI_BUTTON, "close.png"), onPress);
        }

        @Override
        public boolean resetColorBeforeRender() {
            return false;
        }
    }
}
