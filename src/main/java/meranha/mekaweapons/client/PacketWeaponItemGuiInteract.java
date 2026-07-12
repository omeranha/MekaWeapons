package meranha.mekaweapons.client;

import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class PacketWeaponItemGuiInteract implements IMekanismPacket {
    private final InteractionHand hand;
    private final String setting;
    private final boolean value;

    public PacketWeaponItemGuiInteract(InteractionHand hand, String setting, boolean value) {
        this.hand = hand;
        this.setting = setting;
        this.value = value;
    }

    public static PacketWeaponItemGuiInteract decode(FriendlyByteBuf buffer) {
        return new PacketWeaponItemGuiInteract(buffer.readEnum(InteractionHand.class), buffer.readUtf(), buffer.readBoolean());
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(hand);
        buffer.writeUtf(setting);
        buffer.writeBoolean(value);
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) {
            return;
        }

        stack.getOrCreateTagElement("Settings").putBoolean(setting, value);
        player.containerMenu.broadcastChanges();
    }
}
