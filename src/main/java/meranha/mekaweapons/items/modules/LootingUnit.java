package meranha.mekaweapons.items.modules;

import mekanism.api.gear.EnchantmentAwareModule;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.NotNull;

public class LootingUnit implements EnchantmentAwareModule<LootingUnit> {
    public LootingUnit() {
    }

    @Override
    public @NotNull ResourceKey<Enchantment> enchantment() {
        return Enchantments.LOOTING;
    }
}
