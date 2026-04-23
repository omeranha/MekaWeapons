package meranha.mekaweapons.client;

import meranha.mekaweapons.MekaWeapons;
import net.minecraft.world.item.Item;
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
    private static final int ROTATION_ZN_DEFAULT = -180;
    private static final int ROTATION_XP_DEFAULT = 180;
    private static final int ROTATION_XP_KATANA_BOW = 180;
    private static final int ROTATION_ZN_KATANA_BOW = 45;
    private static final float SCALE_X = 1f;
    private static final float SCALE_Y = -1f;
    private static final float SCALE_Z = -1f;
    private static final float TRANSLATION_Z = -0.2f;

    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, @NotNull SlotContext slotContext, PoseStack ms, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource buffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(slotContext.entity() instanceof Player player)) {
            return;
        }

        renderItem(stack, ms, buffer, player, ROTATION_ZN_DEFAULT, ROTATION_XP_DEFAULT, 0, 0.25, -0.1, 0.60f, -0.60f, -2f); // magnetizer
        boolean renderWeapons = stack.getOrDefault(MekaWeapons.TOGGLE_RENDER.get(), false);
        if (!renderWeapons) {
            return;
        }

        ItemStack mekatanaOnPlayer = player.getInventory().items.stream().filter(s -> s.is(MekaWeapons.MEKA_TANA.get())).findFirst().orElse(ItemStack.EMPTY);
        ItemStack mekabowOnPlayer = player.getInventory().items.stream().filter(s -> s.is(MekaWeapons.MEKA_BOW.get())).findFirst().orElse(ItemStack.EMPTY);
        if (!mekatanaOnPlayer.isEmpty() && !isHolding(player, MekaWeapons.MEKA_TANA.get())){
            renderItem(mekatanaOnPlayer, ms, buffer, player, ROTATION_ZN_KATANA_BOW, ROTATION_XP_KATANA_BOW, -0.10, -0.70, TRANSLATION_Z, SCALE_X, SCALE_Y, SCALE_Z);
        }

        if (!mekabowOnPlayer.isEmpty() && !isHolding(player, MekaWeapons.MEKA_BOW.get())) {
            renderItem(mekabowOnPlayer, ms, buffer, player, ROTATION_ZN_KATANA_BOW, ROTATION_XP_KATANA_BOW, -0.30, -0.10, TRANSLATION_Z, SCALE_X, SCALE_Y, SCALE_Z);
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

    private boolean isHolding(@NotNull Player player, Item item) {
        return player.getMainHandItem().is(item) || player.getOffhandItem().is(item);
    }
}
