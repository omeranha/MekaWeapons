package meranha.mekaweapons.client;

import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.inventory.container.item.FrequencyItemContainer;
import mekanism.common.lib.frequency.FrequencyType;
import meranha.mekaweapons.MekaWeapons;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class MagnetizerContainer extends FrequencyItemContainer<InventoryFrequency> implements IEmptyContainer {
    public MagnetizerContainer(int id, Inventory inv, InteractionHand hand, ItemStack stack) {
        super(MekaWeapons.MAGNETIZER_CONTAINER, id, inv, hand, stack);
    }

    @Override
    protected FrequencyType<InventoryFrequency> getFrequencyType() {
        return FrequencyType.INVENTORY;
    }
}
