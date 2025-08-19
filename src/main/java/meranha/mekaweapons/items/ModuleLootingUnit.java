package meranha.mekaweapons.items;

import mekanism.api.gear.EnchantmentAwareModule;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.NotNull;

public class ModuleLootingUnit implements EnchantmentAwareModule<ModuleLootingUnit> {
    public ModuleLootingUnit() {
    }

    @Override
    public @NotNull ResourceKey<Enchantment> enchantment() {
        return Enchantments.LOOTING;
    }
}
