package meranha.mekaweapons.client;

import mekanism.common.registries.MekanismSounds;
import meranha.mekaweapons.items.ItemMekaGun;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

/**
 * Looping laser sound that follows a player while they fire a Meka-Gun, instead of replaying a one shot sound on every
 * shot.
 */
public class LaserSoundInstance extends AbstractTickableSoundInstance {
    private final Player player;

    public LaserSoundInstance(Player player, float volume) {
        super(MekanismSounds.LASER.get(), SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.player = player;
        this.looping = true;
        this.delay = 0;
        this.volume = volume;
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public void tick() {
        if (player.isRemoved() || !ItemMekaGun.isFiring(player)) {
            stop();
            return;
        }

        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }
}
