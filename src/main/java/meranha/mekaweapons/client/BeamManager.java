package meranha.mekaweapons.client;

import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.common.content.gear.shared.ModuleColorModulationUnit;
import mekanism.common.lib.Color;
import mekanism.common.registries.MekanismModules;
import meranha.mekaweapons.MekaWeapons;
import meranha.mekaweapons.items.ItemMekaGun;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = MekaWeapons.MODID, value = Dist.CLIENT)
public class BeamManager {
    private static final Color DEFAULT_BEAM_COLOR = Color.rgbai(70, 242, 149, 128);

    @SubscribeEvent
    public static void renderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        Camera camera = event.getCamera();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        boolean rendered = false;
        for (Player player : level.players()) {
            BeamData beam = getActiveBeam(level, player, partialTick);
            if (beam == null) {
                continue;
            }

            BeamRenderer.renderBeam(bufferSource, camera, beam);
            rendered = true;
        }

        if (rendered) {
            bufferSource.endBatch(RenderType.lightning());
        }
    }

    /**
     * Rebuilds the beam of a player currently firing a Meka-Gun. The use state is synced to every client, so no extra
     * packets are needed and the beam disappears on its own as soon as the player stops firing.
     */
    private static BeamData getActiveBeam(ClientLevel level, Player player, float partialTick) {
        if (!player.isAlive() || !player.isUsingItem()) {
            return null;
        }

        ItemStack stack = player.getUseItem();
        if (!(stack.getItem() instanceof ItemMekaGun)) {
            return null;
        }

        Vec3 from = ItemMekaGun.getMuzzlePosition(player, partialTick);
        Vec3 to = ItemMekaGun.traceLaser(level, player, partialTick).hitPosition();
        Color color = getBeamColor(stack);
        return new BeamData(from, to, color.rf(), color.gf(), color.bf(), DEFAULT_BEAM_COLOR.af());
    }

    private static Color getBeamColor(ItemStack stack) {
        IModule<ModuleColorModulationUnit> colorUnit = IModuleHelper.INSTANCE.getModule(stack, MekanismModules.COLOR_MODULATION_UNIT);
        return colorUnit != null ? colorUnit.getCustomInstance().color() : DEFAULT_BEAM_COLOR;
    }
}
