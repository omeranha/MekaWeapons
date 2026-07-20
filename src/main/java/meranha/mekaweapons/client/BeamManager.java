package meranha.mekaweapons.client;

import meranha.mekaweapons.MekaWeapons;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = MekaWeapons.MODID, value = Dist.CLIENT)
public class BeamManager {
    private static final Map<UUID, BeamData> ACTIVE_BEAMS = new HashMap<>();

    @SubscribeEvent
    public static void renderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        for (BeamData beam : BeamManager.ACTIVE_BEAMS.values()) {
            BeamRenderer.renderBeam(Minecraft.getInstance().renderBuffers().bufferSource(), camera, beam);
        }
        Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
    }

    public static void setBeam(UUID player, Vec3 start, Vec3 end, float r, float b, float g, float a) {
        ACTIVE_BEAMS.put(player, new BeamData(start, end, 70 / 255F, 242 / 255F, 149 / 255F, 0.5F));
    }

    public static void removeBeam(UUID player) {
        ACTIVE_BEAMS.remove(player);
    }
}
