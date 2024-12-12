package meranha.mekaweapons;

import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class WeaponsRenderer implements ICurioRenderer {
    public boolean contains(@NotNull Player player, ItemStack stack) {
        return player.getInventory().items.stream().anyMatch(item -> !item.isEmpty() && ItemStack.isSameItem(stack, item));
    }

    final ItemStack katana = MekaWeapons.MEKA_TANA.getItemStack();
    final ItemStack bow = MekaWeapons.MEKA_BOW.getItemStack();

    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, @NotNull SlotContext slotContext, PoseStack ms, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource buffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(slotContext.entity() instanceof Player player)) {
            return;
        }

        renderItem(stack, ms, buffer, player, -180, 180, 0, 0.25, -0.1, 0.60f, -0.60f, -2f); // magnetizer
        if (!player.isHolding(katana.getItem()) && contains(player, katana)) {
            renderItem(katana, ms, buffer, player, 45, 180, -0.2, -0.75, -0.2, 1f, -1f, -1f);
        }

        if (!player.isHolding(bow.getItem()) && contains(player, bow)) {
            renderItem(bow, ms, buffer, player, 45, 180, -0.3, -0.07, -0.2, 1f, -1f, -1f);
        }
    }

    private void renderItem(ItemStack stack, @NotNull PoseStack ms, MultiBufferSource buffer, @NotNull LivingEntity player, int rotationZN, int rotationXP, double translateX, double translateY, double translateZ, float scaleX, float scaleY, float scaleZ) {
        ms.pushPose();
        ms.mulPose(Axis.ZN.rotationDegrees(rotationZN));
        ms.mulPose(Axis.XP.rotationDegrees(rotationXP));
        ms.translate(translateX, translateY, translateZ);
        ms.scale(scaleX, scaleY, scaleZ);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.NONE, 0xF000F0, OverlayTexture.NO_OVERLAY, ms, buffer, player.level(), 1);
        ms.popPose();
    }
}
