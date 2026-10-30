package meranha.mekaweapons.client;

import io.netty.buffer.ByteBuf;
import mekanism.common.network.IMekanismPacket;
import meranha.mekaweapons.MekaWeapons;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Sent when the Infuse button is pressed. Acts only on the container the sending player currently has open, so no slot
 * indices or stacks have to be trusted from the client.
 */
public record PacketInfuseEnchantments() implements IMekanismPacket {

    public static final PacketInfuseEnchantments INSTANCE = new PacketInfuseEnchantments();
    public static final CustomPacketPayload.Type<PacketInfuseEnchantments> TYPE = new CustomPacketPayload.Type<>(MekaWeapons.rl("infuse_enchantments"));
    public static final StreamCodec<ByteBuf, PacketInfuseEnchantments> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketInfuseEnchantments> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        if (context.player().containerMenu instanceof EnchantmentInfuserContainer container) {
            container.infuse(context.player());
        }
    }
}
