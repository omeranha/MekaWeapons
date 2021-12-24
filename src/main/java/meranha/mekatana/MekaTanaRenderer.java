package meranha.mekatana;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MekaTanaRenderer extends LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> {
    public MekaTanaRenderer(IEntityRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> playerModel) {
        super(playerModel);
    }

    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!player.isInvisible()) {
            matrixStack.pushPose();
            getEntityModel().bipedBody.translateRotate(matrixStack);
            matrixStack.translate(0, 0.25, 0.2);
            matrixStack.scale(1f, -1f, -0.25f);
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(180));
            renderItem(player, Items.MEKA_TANA.get().getDefaultInstance(), ItemCameraTransforms.TransformType.NONE,
                    false, matrixStack, buffer, player.world, 0xF000F0, OverlayTexture.NO_OVERLAY);
            matrixStack.popPose();
        }
    }
}
