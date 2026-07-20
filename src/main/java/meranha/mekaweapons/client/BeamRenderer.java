package meranha.mekaweapons.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class BeamRenderer {

    public static void renderBeam(MultiBufferSource bufferSource, Camera camera, BeamData beam) {
        VertexConsumer builder = bufferSource.getBuffer(RenderType.lightning());
        Vec3 camPos = camera.getPosition();
        Vec3 diff = beam.end().subtract(beam.start());
        float halfLength = (float) diff.length() / 2F;
        if (halfLength < 0.0001F) {
            return;
        }

        Vec3 dir = diff.normalize();
        float size = 0.004F;
        Vec3 center = beam.start().add(beam.end()).scale(0.5);
        Vec3 toCamera = center.subtract(camPos).normalize();
        Vec3 right = dir.cross(toCamera);
        if (right.lengthSqr() < 1E-6) {
            right = new Vec3(1, 0, 0);
        } else {
            right = right.normalize();
        }

        Vec3 up = dir.cross(right).normalize();
        right = right.scale(size);
        up = up.scale(size);
        float cx = (float) (center.x - camPos.x);
        float cy = (float) (center.y - camPos.y);
        float cz = (float) (center.z - camPos.z);
        Vector3f[] quadA = buildBeam(cx, cy, cz, dir, right, halfLength);
        Vector3f[] quadB = buildBeam(cx, cy, cz, dir, up, halfLength);
        draw(builder, quadA, beam.r(), beam.g(), beam.b(), beam.a());
        draw(builder, quadB, beam.r(), beam.g(), beam.b(), beam.a());
    }

    private static Vector3f[] buildBeam(float x, float y, float z, Vec3 dir, Vec3 offset, float halfLength) {
        return new Vector3f[] {
                new Vector3f((float) (x - offset.x - dir.x * halfLength), (float) (y - offset.y - dir.y * halfLength), (float) (z - offset.z - dir.z * halfLength)),
                new Vector3f((float) (x - offset.x + dir.x * halfLength), (float) (y - offset.y + dir.y * halfLength), (float) (z - offset.z + dir.z * halfLength)),
                new Vector3f((float) (x + offset.x + dir.x * halfLength), (float) (y + offset.y + dir.y * halfLength), (float) (z + offset.z + dir.z * halfLength)),
                new Vector3f((float) (x + offset.x - dir.x * halfLength), (float) (y + offset.y - dir.y * halfLength), (float) (z + offset.z - dir.z * halfLength))
        };
    }

    private static void draw(VertexConsumer builder, Vector3f[] quad, float r, float g, float b, float alpha) {
        vertex(builder, quad[0], r, g, b, alpha);
        vertex(builder, quad[1], r, g, b, alpha);
        vertex(builder, quad[2], r, g, b, alpha);
        vertex(builder, quad[3], r, g, b, alpha);
        vertex(builder, quad[1], r, g, b, alpha);
        vertex(builder, quad[0], r, g, b, alpha);
        vertex(builder, quad[3], r, g, b, alpha);
        vertex(builder, quad[2], r, g, b, alpha);
    }

    private static void vertex(VertexConsumer builder, Vector3f pos, float r, float g, float b, float alpha) {
        builder.addVertex(pos.x(), pos.y(), pos.z()).setColor(r, g, b, alpha).setUv(0, 0).setLight(LightTexture.FULL_BRIGHT);
    }
}
