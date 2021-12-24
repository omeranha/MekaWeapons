package meranha.mekatana;

import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.registration.impl.ItemRegistryObject;

public final class Items {
    public static final ItemDeferredRegister ITEMS = new ItemDeferredRegister("mekaweapons");

    public static final ItemRegistryObject<ItemMekaTana> MEKA_TANA = ITEMS.registerUnburnable("mekatana", ItemMekaTana::new);
    public static final ItemRegistryObject<ItemMekaBow> MEKA_BOW = ITEMS.registerUnburnable("mekabow", ItemMekaBow::new);
}
