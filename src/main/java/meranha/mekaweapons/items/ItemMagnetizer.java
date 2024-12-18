package meranha.mekaweapons.items;

import mekanism.api.text.EnumColor;
import meranha.mekaweapons.WeaponsLang;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemMagnetizer extends Item {
    public ItemMagnetizer(Properties pProperties) {
        super(pProperties.rarity(Rarity.RARE).stacksTo(1));
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        tooltip.add(WeaponsLang.MAGNETIZER.translateColored(EnumColor.WHITE));
    }
}
