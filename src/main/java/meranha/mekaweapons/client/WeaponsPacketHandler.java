package meranha.mekaweapons.client;

import mekanism.common.lib.Version;
import mekanism.common.network.BasePacketHandler;
import net.neoforged.bus.api.IEventBus;

public class WeaponsPacketHandler extends BasePacketHandler {
    public WeaponsPacketHandler(IEventBus modEventBus, Version version) {
        super(modEventBus, version);
    }

    @Override
    protected void registerClientToServer(PacketRegistrar registrar) {
        registrar.play(PacketWeaponItemGuiInteract.TYPE, PacketWeaponItemGuiInteract.STREAM_CODEC);
    }

    @Override
    protected void registerServerToClient(PacketRegistrar registrar) {

    }
}
