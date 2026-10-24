package meranha.mekaweapons.client;

import meranha.mekaweapons.MekaWeapons;
import meranha.mekaweapons.items.ItemMekaGun;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Starts and stops the looping Meka-Gun laser sound from the synced item use state, so it also plays for other players.
 */
@EventBusSubscriber(modid = MekaWeapons.MODID, value = Dist.CLIENT)
public class LaserSoundManager {
    private static final Map<UUID, LaserSoundInstance> ACTIVE_SOUNDS = new HashMap<>();

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            ACTIVE_SOUNDS.clear();
            return;
        }

        Iterator<Map.Entry<UUID, LaserSoundInstance>> iterator = ACTIVE_SOUNDS.entrySet().iterator();
        while (iterator.hasNext()) {
            LaserSoundInstance sound = iterator.next().getValue();
            if (sound.isStopped() || sound.getPlayer().isRemoved()) {
                iterator.remove();
            }
        }

        float volume = (float) MekaWeapons.general.mekaGunLaserSoundVolume.getOrDefault();
        if (volume <= 0) {
            return;
        }

        for (Player player : level.players()) {
            if (!ItemMekaGun.isFiring(player) || ACTIVE_SOUNDS.containsKey(player.getUUID())) {
                continue;
            }

            LaserSoundInstance sound = new LaserSoundInstance(player, volume);
            ACTIVE_SOUNDS.put(player.getUUID(), sound);
            minecraft.getSoundManager().play(sound);
        }
    }
}
