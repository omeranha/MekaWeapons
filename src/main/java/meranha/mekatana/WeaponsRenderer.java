package meranha.mekatana;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class WeaponsRenderer implements ICurioRenderer {
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack ms, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource buffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (slotContext.entity() instanceof Player player) {
            if (!stack.isEmpty()) {
                ms.pushPose();
                ms.mulPose(Vector3f.ZN.rotationDegrees(-180));
                ms.mulPose(Vector3f.XP.rotationDegrees(180));
                ms.translate(0, 0.25, -0.1);
                ms.scale(0.60f, -0.60f, -2f);
                Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.NONE, 0xF000F0, OverlayTexture.NO_OVERLAY, ms, buffer, 1);
                ms.popPose();
            }

            ItemStack katana = MekaWeapons.MEKA_TANA.getItemStack();
            if (player.getInventory().contains(katana) && !player.isHolding(MekaWeapons.MEKA_TANA.get())) {
                ms.pushPose();
                ms.mulPose(Vector3f.ZN.rotationDegrees(45));
                ms.mulPose(Vector3f.XP.rotationDegrees(180));
                ms.translate(-0.2, -0.75, -0.2);
                ms.scale(1f, -1f, -1f);
                Minecraft.getInstance().getItemRenderer().renderStatic(katana, ItemTransforms.TransformType.NONE, 0xF000F0, OverlayTexture.NO_OVERLAY, ms, buffer, 1);
                ms.popPose();
            }

            ItemStack bow = MekaWeapons.MEKA_BOW.getItemStack();
            if (player.getInventory().contains(bow) && !player.isHolding(MekaWeapons.MEKA_BOW.get())) {
                ms.pushPose();
                ms.mulPose(Vector3f.ZN.rotationDegrees(45));
                ms.mulPose(Vector3f.XP.rotationDegrees(180));
                ms.translate(-0.3, -0.07, -0.2);
                ms.scale(1f, -1f, -1f);
                Minecraft.getInstance().getItemRenderer().renderStatic(bow, ItemTransforms.TransformType.NONE, 0xF000F0, OverlayTexture.NO_OVERLAY, ms, buffer, 1);
                ms.popPose();
            }
        }
    }
}
