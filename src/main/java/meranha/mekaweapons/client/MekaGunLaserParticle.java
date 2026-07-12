package meranha.mekaweapons.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import meranha.mekaweapons.particle.MekaGunLaserParticleData;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class MekaGunLaserParticle extends TextureSheetParticle {
    private final Vec3 axis;
    private final float halfLength;

    private MekaGunLaserParticle(ClientLevel world, Vec3 start, Vec3 end, int r, int g, int b) {
        super(world, (start.x + end.x) * 0.5D, (start.y + end.y) * 0.5D, (start.z + end.z) * 0.5D);
        lifetime = 5;
        rCol = toColor(r);
        gCol = toColor(g);
        bCol = toColor(b);
        alpha = 0.5F;
        quadSize = 1F;
        Vec3 diff = end.subtract(start);
        halfLength = (float) (diff.length() * 0.5D);
        axis = diff.normalize();
        updateBoundingBox();
    }

    private static float toColor(int value) {
        return Mth.clamp(value, 0, 255) / 255F;
    }

    @Override
    public void render(@NotNull VertexConsumer builder, Camera camera, float partialTicks) {
        Vec3 camPos = camera.getPosition();

        float x = (float) (Mth.lerp(partialTicks, xo, this.x) - camPos.x());
        float y = (float) (Mth.lerp(partialTicks, yo, this.y) - camPos.y());
        float z = (float) (Mth.lerp(partialTicks, zo, this.z) - camPos.z());

        float uMin = getU0();
        float uMax = getU1();
        float vMin = getV0();
        float vMax = getV1();

        float size = getQuadSize(partialTicks) * 0.004F;

        Vec3 dir = axis;
        Vec3 toCamera = new Vec3(this.x, this.y, this.z).subtract(camPos).normalize();

        Vec3 right = dir.cross(toCamera);
        if (right.lengthSqr() < 1E-6) {
            right = new Vec3(1, 0, 0);
        } else {
            right = right.normalize();
        }

        Vec3 up = dir.cross(right).normalize();

        right = right.scale(size);
        up = up.scale(size);

        Vector3f[] quadA = buildBeam(x, y, z, dir, right);
        Vector3f[] quadB = buildBeam(x, y, z, dir, up);

        drawComponent(builder, quadA, uMin, uMax, vMin, vMax);
        drawComponent(builder, quadB, uMin, uMax, vMin, vMax);
    }

    private Vector3f[] buildBeam(float x, float y, float z, Vec3 dir, Vec3 offset) {
        return new Vector3f[]{
                new Vector3f((float) (x - offset.x - dir.x * halfLength), (float) (y - offset.y - dir.y * halfLength), (float) (z - offset.z - dir.z * halfLength)),
                new Vector3f((float) (x - offset.x + dir.x * halfLength), (float) (y - offset.y + dir.y * halfLength), (float) (z - offset.z + dir.z * halfLength)),
                new Vector3f((float) (x + offset.x + dir.x * halfLength), (float) (y + offset.y + dir.y * halfLength), (float) (z + offset.z + dir.z * halfLength)),
                new Vector3f((float) (x + offset.x - dir.x * halfLength), (float) (y + offset.y - dir.y * halfLength), (float) (z + offset.z - dir.z * halfLength))
        };
    }

    private void drawComponent(VertexConsumer builder, Vector3f[] vertices, float uMin, float uMax, float vMin, float vMax) {
        add(builder, vertices[0], uMax, vMax);
        add(builder, vertices[1], uMax, vMin);
        add(builder, vertices[2], uMin, vMin);
        add(builder, vertices[3], uMin, vMax);
        add(builder, vertices[1], uMax, vMin);
        add(builder, vertices[0], uMax, vMax);
        add(builder, vertices[3], uMin, vMax);
        add(builder, vertices[2], uMin, vMin);
    }

    private void add(VertexConsumer builder, Vector3f pos, float u, float v) {
        builder.vertex(pos.x(), pos.y(), pos.z()).uv(u, v).color(rCol, gCol, bCol, alpha).uv2(240, 240).endVertex();
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected void setSize(float width, float height) {
        if (width != bbWidth || height != bbHeight) {
            bbWidth = width;
            bbHeight = height;
        }
    }

    @Override
    public void setPos(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;

        if (axis != null) {
            updateBoundingBox();
        }
    }

    private void updateBoundingBox() {
        float r = quadSize * 0.5F;

        setBoundingBox(new AABB(x - halfLength - r, y - halfLength - r, z - halfLength - r, x + halfLength + r, y + halfLength + r, z + halfLength + r));
    }

    public static class Factory implements ParticleProvider<MekaGunLaserParticleData> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public MekaGunLaserParticle createParticle(MekaGunLaserParticleData data, @NotNull ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            Vec3 start = new Vec3(x, y, z);
            Vec3 offset = new Vec3(data.dx(), data.dy(), data.dz());

            if (offset.lengthSqr() < 1E-6) {
                offset = new Vec3(0, 0.001, 0);
            }

            Vec3 end = start.add(offset);
            MekaGunLaserParticle particle = new MekaGunLaserParticle(world, start, end, data.r(), data.g(), data.b());
            particle.pickSprite(spriteSet);
            return particle;
        }
    }
}
