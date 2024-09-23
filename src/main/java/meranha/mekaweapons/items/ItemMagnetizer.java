package meranha.mekaweapons.items;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import mekanism.api.text.EnumColor;
import meranha.mekaweapons.WeaponsLang;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

public class ItemMagnetizer extends Item {
    public ItemMagnetizer(@NotNull Properties pProperties) {
        super(pProperties.rarity(Rarity.RARE).stacksTo(1));
    }

    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(WeaponsLang.MAGNETIZER.translateColored(EnumColor.WHITE));
    }
}
