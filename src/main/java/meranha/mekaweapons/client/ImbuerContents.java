package meranha.mekaweapons.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

/**
 * Contents of the Enchantment Imbuer's two slots, stored as a data component on the imbuer itself so they survive a
 * server crash or any other shutdown where the container never gets a chance to close.
 */
public record ImbuerContents(ItemStack book, ItemStack weapon) {

    public static final ImbuerContents EMPTY = new ImbuerContents(ItemStack.EMPTY, ItemStack.EMPTY);

    public static final Codec<ImbuerContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("book", ItemStack.EMPTY).forGetter(ImbuerContents::book),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("weapon", ItemStack.EMPTY).forGetter(ImbuerContents::weapon)
    ).apply(instance, ImbuerContents::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ImbuerContents> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC, ImbuerContents::book,
            ItemStack.OPTIONAL_STREAM_CODEC, ImbuerContents::weapon,
            ImbuerContents::new
    );

    public boolean isEmpty() {
        return book.isEmpty() && weapon.isEmpty();
    }

    /**
     * Non-empty contents, for dropping or listing in a tooltip.
     */
    public List<ItemStack> nonEmpty() {
        if (book.isEmpty()) {
            return weapon.isEmpty() ? List.of() : List.of(weapon);
        }
        return weapon.isEmpty() ? List.of(book) : List.of(book, weapon);
    }
}
