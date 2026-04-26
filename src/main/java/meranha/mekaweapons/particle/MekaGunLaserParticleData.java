package meranha.mekaweapons.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import meranha.mekaweapons.MekaWeapons;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public record MekaGunLaserParticleData(double dx, double dy, double dz, int r, int g, int b) implements ParticleOptions {
    public static final MapCodec<MekaGunLaserParticleData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.DOUBLE.fieldOf("dx").forGetter(MekaGunLaserParticleData::dx),
            Codec.DOUBLE.fieldOf("dy").forGetter(MekaGunLaserParticleData::dy),
            Codec.DOUBLE.fieldOf("dz").forGetter(MekaGunLaserParticleData::dz),
            Codec.INT.fieldOf("r").forGetter(MekaGunLaserParticleData::r),
            Codec.INT.fieldOf("g").forGetter(MekaGunLaserParticleData::g),
            Codec.INT.fieldOf("b").forGetter(MekaGunLaserParticleData::b)
    ).apply(instance, MekaGunLaserParticleData::new));

    public static final StreamCodec<ByteBuf, MekaGunLaserParticleData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, MekaGunLaserParticleData::dx,
            ByteBufCodecs.DOUBLE, MekaGunLaserParticleData::dy,
            ByteBufCodecs.DOUBLE, MekaGunLaserParticleData::dz,
            ByteBufCodecs.INT, MekaGunLaserParticleData::r,
            ByteBufCodecs.INT, MekaGunLaserParticleData::g,
            ByteBufCodecs.INT, MekaGunLaserParticleData::b,
            MekaGunLaserParticleData::new
    );

    @Override
    public @NotNull ParticleType<?> getType() {
        return MekaWeapons.MEKA_GUN_LASER.get();
    }
}
