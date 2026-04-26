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
    private static final Item MekaTana = MekaWeapons.MEKA_TANA.get();
    private static final Item MekaBow = MekaWeapons.MEKA_BOW.get();
    private static final Item MekaGun = MekaWeapons.MEKA_GUN.get();

    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, @NotNull SlotContext slotContext, PoseStack ms, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource buffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(slotContext.entity() instanceof Player player)) {
            return;
        }

        renderItem(stack, ms, buffer, player, 180, 0, 0, -0.2, 0.15, 0.5f, 0.5f, 0.5f); // magnetizer
        if (stack.getOrDefault(MekaWeapons.TOGGLE_RENDER_MEKATANA.get(), true)) {
            player.getInventory().items.stream().filter(s -> s.is(MekaTana))
                    .findFirst().filter(s-> isNotHolding(player, MekaTana))
                    .ifPresent(s -> renderItem(s, ms, buffer, player, 45, 0, -0.1, 0.7, 0.2, 1f, 1f, 1f));
        }

        if (stack.getOrDefault(MekaWeapons.TOGGLE_RENDER_MEKABOW.get(), true)) {
            player.getInventory().items.stream().filter(s -> s.is(MekaBow))
                    .findFirst().filter(s-> isNotHolding(player, MekaBow))
                    .ifPresent(s -> renderItem(s, ms, buffer, player, 45, 0, -0.30, -0.15, 0.2, 1f, 1f, 1f));
        }

        if (stack.getOrDefault(MekaWeapons.TOGGLE_RENDER_MEKAGUN.get(), true)) {
            player.getInventory().items.stream().filter(s -> s.is(MekaGun))
                    .findFirst().filter(s-> isNotHolding(player, MekaGun))
                    .ifPresent(s -> renderItem(s, ms, buffer, player, 45, 90, -0.15, 0.7, -0.1, 1.5f, 1.5f, 1.5f));
        }
    }

    private void renderItem(ItemStack stack, @NotNull PoseStack ms, MultiBufferSource buffer, @NotNull LivingEntity player, float rotationZN, float rotationXP, double translateX, double translateY, double translateZ, float scaleX, float scaleY, float scaleZ) {
        ms.pushPose();
        ms.mulPose(Axis.ZN.rotationDegrees(rotationZN));
        ms.mulPose(Axis.XP.rotationDegrees(rotationXP));
        ms.translate(translateX, translateY, translateZ);
        ms.scale(scaleX, scaleY, scaleZ);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.NONE, 0xF000F0, OverlayTexture.NO_OVERLAY, ms, buffer, player.level(), 1);
        ms.popPose();
    }

    private boolean isNotHolding(@NotNull Player player, Item item) {
        return !player.getMainHandItem().is(item) && !player.getOffhandItem().is(item);
    }
}
