package meranha.mekaweapons.mixin;

import mekanism.common.network.BasePacketHandler;
import mekanism.common.network.PacketHandler;
import meranha.mekaweapons.MekaWeapons;
import meranha.mekaweapons.client.PacketWeaponItemGuiInteract;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PacketHandler.class, remap = false)
public abstract class PacketHandlerMixin extends BasePacketHandler {

    @Inject(method = "initialize", at = @At("RETURN"))
    private void addPackets(CallbackInfo ci) {
        MekaWeapons.logger.debug("Weapons: Injected addPackets");
        this.registerClientToServer(PacketWeaponItemGuiInteract.class, PacketWeaponItemGuiInteract::decode);
    }
}
