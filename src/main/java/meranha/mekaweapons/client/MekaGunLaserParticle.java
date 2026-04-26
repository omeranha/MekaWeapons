package meranha.mekaweapons.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import meranha.mekaweapons.particle.MekaGunLaserParticleData;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class MekaGunLaserParticle extends TextureSheetParticle {
    private final Vec3 axis;
    private final float halfLength;
    private final float r;
    private final float g;
    private final float b;

    private MekaGunLaserParticle(ClientLevel world, Vec3 start, Vec3 end, int r, int g, int b) {
        super(world, (start.x + end.x) / 2D, (start.y + end.y) / 2D, (start.z + end.z) / 2D);
        lifetime = 10;
        this.r = toColor(r);
        this.g = toColor(g);
        this.b = toColor(b);
        alpha = 0.5F;
        quadSize = 1F;
        Vec3 diff = end.subtract(start);
        halfLength = (float) (diff.length() / 2.0);
        axis = diff.normalize();
        updateBoundingBox();
    }

    @Override
    protected int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT;
    }

    private static float toColor(int c) {
        return Math.clamp(c, 0, 255) / 255f;
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

        int light = getLightColor(partialTicks);
        float size = getQuadSize(partialTicks) * 0.004f;
        Vec3 dir = axis;
        Vec3 toCamera = new Vec3(this.x, this.y, this.z).subtract(camPos).normalize();
        Vec3 right = dir.cross(toCamera);
        if (right.lengthSqr() < 1e-6) {
            right = new Vec3(1, 0, 0);
        } else {
            right = right.normalize();
        }

        Vec3 up = dir.cross(right).normalize();
        right = right.scale(size);
        up = up.scale(size);
        Vector3f[] quadA = buildBeam(x, y, z, dir, right);
        Vector3f[] quadB = buildBeam(x, y, z, dir, up);
        drawComponent(builder, quadA, uMin, uMax, vMin, vMax, light);
        drawComponent(builder, quadB, uMin, uMax, vMin, vMax, light);
    }

    private Vector3f[] buildBeam(float x, float y, float z, Vec3 dir, Vec3 offset) {
        return new Vector3f[] {
                new Vector3f((float)(x - offset.x - dir.x * halfLength), (float)(y - offset.y - dir.y * halfLength), (float)(z - offset.z - dir.z * halfLength)),
                new Vector3f((float)(x - offset.x + dir.x * halfLength), (float)(y - offset.y + dir.y * halfLength), (float)(z - offset.z + dir.z * halfLength)),
                new Vector3f((float)(x + offset.x + dir.x * halfLength), (float)(y + offset.y + dir.y * halfLength), (float)(z + offset.z + dir.z * halfLength)),
                new Vector3f((float)(x + offset.x - dir.x * halfLength), (float)(y + offset.y - dir.y * halfLength), (float)(z + offset.z - dir.z * halfLength))
        };
    }

    private void drawComponent(VertexConsumer builder, Vector3f[] v, float uMin, float uMax, float vMin, float vMax, int light) {
        add(builder, v[0], uMax, vMax, light);
        add(builder, v[1], uMax, vMin, light);
        add(builder, v[2], uMin, vMin, light);
        add(builder, v[3], uMin, vMax, light);

        add(builder, v[1], uMax, vMin, light);
        add(builder, v[0], uMax, vMax, light);
        add(builder, v[3], uMin, vMax, light);
        add(builder, v[2], uMin, vMin, light);
    }

    private void add(VertexConsumer builder, Vector3f pos, float u, float v, int light) {
        builder.addVertex(pos.x(), pos.y(), pos.z()).setUv(u, v).setColor(r, g, b, alpha).setLight(light);
    }

    @NotNull
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected void setSize(float w, float h) {
        this.bbWidth = w;
        this.bbHeight = h;
    }

    @Override
    public void setPos(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        updateBoundingBox();
    }

    private void updateBoundingBox() {
        float r = quadSize / 2;
        setBoundingBox(new AABB(x - halfLength - r,
                y - halfLength - r,
                z - halfLength - r,
                x + halfLength + r,
                y + halfLength + r,
                z + halfLength + r
        ));
    }

    @NotNull
    @Override
    public AABB getRenderBoundingBox(float partialTicks) {
        return getBoundingBox();
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
            if (offset.lengthSqr() < 1e-6) {
                offset = new Vec3(0, 0.001, 0);
            }

            Vec3 end = start.add(offset);
            MekaGunLaserParticle particle = new MekaGunLaserParticle(world, start, end, data.r(), data.g(), data.b());
            particle.pickSprite(this.spriteSet);
            return particle;
        }
    }
}
