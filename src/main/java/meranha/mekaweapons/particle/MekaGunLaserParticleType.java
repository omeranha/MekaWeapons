package meranha.mekaweapons.particle;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;
import org.jetbrains.annotations.NotNull;

public class MekaGunLaserParticleType extends ParticleType<MekaGunLaserParticleData> {
    public MekaGunLaserParticleType() {
        super(false, MekaGunLaserParticleData.DESERIALIZER);
    }

    @NotNull
    @Override
    public Codec<MekaGunLaserParticleData> codec() {
        return MekaGunLaserParticleData.CODEC;
    }
}
