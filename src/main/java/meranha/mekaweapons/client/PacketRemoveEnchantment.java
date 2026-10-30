package meranha.mekaweapons.client;

import io.netty.buffer.ByteBuf;
import mekanism.common.network.IMekanismPacket;
import meranha.mekaweapons.MekaWeapons;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Removes a single enchantment from the Enchantment Unit and returns it as a book. Only the enchantment id travels, the
 * container itself is resolved from what the sending player has open.
 */
public record PacketRemoveEnchantment(ResourceLocation enchantment) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketRemoveEnchantment> TYPE = new CustomPacketPayload.Type<>(MekaWeapons.rl("remove_enchantment"));
    public static final StreamCodec<ByteBuf, PacketRemoveEnchantment> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, PacketRemoveEnchantment::enchantment,
            PacketRemoveEnchantment::new
    );

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketRemoveEnchantment> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        if (context.player().containerMenu instanceof EnchantmentInfuserContainer container) {
            container.removeEnchantment(context.player(), enchantment);
        }
    }
}
