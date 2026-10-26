package meranha.mekaweapons.client;

import io.netty.buffer.ByteBuf;
import mekanism.common.network.IMekanismPacket;
import meranha.mekaweapons.MekaWeapons;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Sent when the Imbue button is pressed. Acts only on the container the sending player currently has open, so no slot
 * indices or stacks have to be trusted from the client.
 */
public record PacketImbueEnchantments() implements IMekanismPacket {

    public static final PacketImbueEnchantments INSTANCE = new PacketImbueEnchantments();
    public static final CustomPacketPayload.Type<PacketImbueEnchantments> TYPE = new CustomPacketPayload.Type<>(MekaWeapons.rl("imbue_enchantments"));
    public static final StreamCodec<ByteBuf, PacketImbueEnchantments> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketImbueEnchantments> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        if (context.player().containerMenu instanceof EnchantmentImbuerContainer container) {
            container.imbue(context.player());
        }
    }
}
