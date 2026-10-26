package meranha.mekaweapons.items.modules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import mekanism.api.gear.config.ModuleConfig;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.NotNull;

/**
 * Module config that stores an arbitrary set of enchantments. Mekanism's module tweaker only knows how to render
 * boolean, enum and color configs, so this one is simply skipped there and is instead edited through the Enchantment
 * Imbuer GUI.
 */
public class ModuleEnchantmentsConfig extends ModuleConfig<ItemEnchantments> {

    public static final Codec<ModuleEnchantmentsConfig> CODEC = RecordCodecBuilder.create(instance -> ModuleEnchantmentsConfig.<ItemEnchantments, ModuleEnchantmentsConfig>baseCodec(instance)
            .and(ItemEnchantments.CODEC.fieldOf("enchantments").forGetter(ModuleEnchantmentsConfig::get))
            .apply(instance, ModuleEnchantmentsConfig::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ModuleEnchantmentsConfig> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, ModuleEnchantmentsConfig::name,
            ItemEnchantments.STREAM_CODEC, ModuleEnchantmentsConfig::get,
            ModuleEnchantmentsConfig::new
    );

    private final ItemEnchantments enchantments;

    public static ModuleEnchantmentsConfig create(ResourceLocation name) {
        return new ModuleEnchantmentsConfig(name, ItemEnchantments.EMPTY);
    }

    private ModuleEnchantmentsConfig(ResourceLocation name, ItemEnchantments enchantments) {
        super(name);
        this.enchantments = enchantments;
    }

    @NotNull
    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, ModuleConfig<ItemEnchantments>> namedStreamCodec(@NotNull ResourceLocation name) {
        return ItemEnchantments.STREAM_CODEC.map(value -> new ModuleEnchantmentsConfig(name, value), ModuleConfig::get);
    }

    @NotNull
    @Override
    public ItemEnchantments get() {
        return enchantments;
    }

    @NotNull
    @Override
    public ModuleEnchantmentsConfig with(@NotNull ItemEnchantments value) {
        return new ModuleEnchantmentsConfig(name(), value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass() || !super.equals(o)) {
            return false;
        }
        return enchantments.equals(((ModuleEnchantmentsConfig) o).enchantments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), enchantments);
    }
}
