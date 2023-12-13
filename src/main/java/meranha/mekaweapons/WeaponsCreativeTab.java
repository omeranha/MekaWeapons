package meranha.mekaweapons;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class WeaponsCreativeTab extends CreativeModeTab {
    public WeaponsCreativeTab() {
        super(MekaWeapons.MODID);
    }

    @Override
    public ItemStack makeIcon() {
        return MekaWeapons.MAGNETIZER.getItemStack();
    }
}
