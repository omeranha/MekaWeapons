package meranha.mekaweapons.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public class MekaGunLaserParticleType extends ParticleType<MekaGunLaserParticleData> {
    public MekaGunLaserParticleType() {
        super(false);
    }

    @NotNull
    @Override
    public MapCodec<MekaGunLaserParticleData> codec() {
        return MekaGunLaserParticleData.CODEC;
    }

    @NotNull
    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, MekaGunLaserParticleData> streamCodec() {
        return MekaGunLaserParticleData.STREAM_CODEC;
    }
}
