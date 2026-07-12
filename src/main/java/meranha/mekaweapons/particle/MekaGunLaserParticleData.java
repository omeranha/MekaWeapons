package meranha.mekaweapons.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import meranha.mekaweapons.MekaWeapons;
import mekanism.common.util.RegistryUtils;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public record MekaGunLaserParticleData(double dx, double dy, double dz, int r, int g, int b) implements ParticleOptions {

    @SuppressWarnings("deprecation")
    public static final Deserializer<MekaGunLaserParticleData> DESERIALIZER = new Deserializer<>() {
        @NotNull
        @Override
        public MekaGunLaserParticleData fromCommand(@NotNull ParticleType<MekaGunLaserParticleData> type, @NotNull StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            double dx = reader.readDouble();
            reader.expect(' ');
            double dy = reader.readDouble();
            reader.expect(' ');
            double dz = reader.readDouble();
            reader.expect(' ');
            int r = reader.readInt();
            reader.expect(' ');
            int g = reader.readInt();
            reader.expect(' ');
            int b = reader.readInt();
            return new MekaGunLaserParticleData(dx, dy, dz, r, g, b);
        }

        @NotNull
        @Override
        public MekaGunLaserParticleData fromNetwork(@NotNull ParticleType<MekaGunLaserParticleData> type, FriendlyByteBuf buf) {
            return new MekaGunLaserParticleData(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readInt(), buf.readInt(), buf.readInt());
        }
    };

    public static final Codec<MekaGunLaserParticleData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("dx").forGetter(MekaGunLaserParticleData::dx),
            Codec.DOUBLE.fieldOf("dy").forGetter(MekaGunLaserParticleData::dy),
            Codec.DOUBLE.fieldOf("dz").forGetter(MekaGunLaserParticleData::dz),
            Codec.INT.fieldOf("r").forGetter(MekaGunLaserParticleData::r),
            Codec.INT.fieldOf("g").forGetter(MekaGunLaserParticleData::g),
            Codec.INT.fieldOf("b").forGetter(MekaGunLaserParticleData::b)
    ).apply(instance, MekaGunLaserParticleData::new));

    @NotNull
    @Override
    public ParticleType<MekaGunLaserParticleData> getType() {
        return MekaWeapons.MEKA_GUN_LASER.get();
    }

    @Override
    public void writeToNetwork(@NotNull FriendlyByteBuf buffer) {
        buffer.writeDouble(dx);
        buffer.writeDouble(dy);
        buffer.writeDouble(dz);
        buffer.writeInt(r);
        buffer.writeInt(g);
        buffer.writeInt(b);
    }

    @NotNull
    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %.6f %.6f %.6f %d %d %d", RegistryUtils.getName(getType()), dx, dy, dz, r, g, b);
    }
}
