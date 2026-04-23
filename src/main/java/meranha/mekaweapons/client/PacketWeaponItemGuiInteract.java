package meranha.mekaweapons.client;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import mekanism.api.functions.TriConsumer;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketUtils;
import meranha.mekaweapons.MekaWeapons;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketWeaponItemGuiInteract(ItemGuiInteraction interaction, InteractionHand hand, int extra) implements IMekanismPacket {
    public static final CustomPacketPayload.Type<PacketWeaponItemGuiInteract> TYPE = new CustomPacketPayload.Type<>(MekaWeapons.rl("weapon_gui_interact"));
    public static final StreamCodec<ByteBuf, PacketWeaponItemGuiInteract> STREAM_CODEC = StreamCodec.composite(
            ItemGuiInteraction.STREAM_CODEC, PacketWeaponItemGuiInteract::interaction,
            PacketUtils.INTERACTION_HAND_STREAM_CODEC, PacketWeaponItemGuiInteract::hand,
            ByteBufCodecs.VAR_INT, PacketWeaponItemGuiInteract::extra,
            PacketWeaponItemGuiInteract::new
    );

    public PacketWeaponItemGuiInteract(ItemGuiInteraction interaction, InteractionHand hand) {
        this(interaction, hand, 0);
    }

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketWeaponItemGuiInteract> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        Player player = context.player();
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.isEmpty()) {
            interaction.consume(stack, player, extra);
        }
    }

    public enum ItemGuiInteraction {
        TOGGLE_RENDER((stack, player, extra) -> {
            stack.update(MekaWeapons.TOGGLE_RENDER.get(), true, val -> !val);
        });

        public static final IntFunction<ItemGuiInteraction> BY_ID = ByIdMap.continuous(ItemGuiInteraction::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, ItemGuiInteraction> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ItemGuiInteraction::ordinal);

        private final TriConsumer<ItemStack, Player, Integer> consumerForTile;

        ItemGuiInteraction(TriConsumer<ItemStack, Player, Integer> consumerForTile) {
            this.consumerForTile = consumerForTile;
        }

        public void consume(ItemStack stack, Player player, int extra) {
            consumerForTile.accept(stack, player, extra);
        }
    }
}
