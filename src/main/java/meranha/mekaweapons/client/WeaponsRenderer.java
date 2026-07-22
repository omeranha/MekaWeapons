package meranha.mekaweapons.client;

import meranha.mekaweapons.MekaWeapons;
import meranha.mekaweapons.WeaponsItems;
import net.minecraft.core.NonNullList;
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

import java.util.function.Consumer;

public class WeaponsRenderer implements ICurioRenderer {
    private static final Item MekaTana = WeaponsItems.MEKA_TANA.get();
    private static final Item MekaBow = WeaponsItems.MEKA_BOW.get();
    private static final Item MekaGun = WeaponsItems.MEKA_GUN.get();

    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, @NotNull SlotContext slotContext, PoseStack ms, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource buffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(slotContext.entity() instanceof Player player)) {
            return;
        }

        var playerInventory = player.getInventory().items;
        renderItem(stack, ms, buffer, player, 0, 0, 180, 0, 0.25, 0.15, 0.5F, 0.5F, 0.5F, light);
        if (stack.getOrDefault(MekaWeapons.TOGGLE_RENDER_MEKATANA.get(), true)) {
            renderIfPresent(player, playerInventory, MekaTana, mekatana -> renderItem(mekatana, ms, buffer, player, 0, 0, 45, 0.4, 0.7, 0.2, 1.15F, 1.15F, 1.15F, light));
        }

        if (stack.getOrDefault(MekaWeapons.TOGGLE_RENDER_MEKABOW.get(), true)) {
            renderIfPresent(player, playerInventory, MekaBow, mekabow -> renderItem(mekabow, ms, buffer, player, 0, -50, 25, -0.15, 0, 0, 1.15F, 1.15F, 1.15F, light));
        }

        if (stack.getOrDefault(MekaWeapons.TOGGLE_RENDER_MEKAGUN.get(), true)) {
            renderIfPresent(player, playerInventory, MekaGun, mekagun -> renderItem(mekagun, ms, buffer, player, 90, 0, 0, 0.15, 0.4, 0.7, 1.6F, 1.6F, 1.6F, light));
        }
    }

    private void renderItem(ItemStack stack, @NotNull PoseStack ms, MultiBufferSource buffer, @NotNull LivingEntity player, float rotationX, float rotationY, float rotationZ, double translateX, double translateY, double translateZ, float scaleX, float scaleY, float scaleZ, int light) {
        ms.pushPose();
        ms.translate(translateX, translateY, translateZ);
        ms.mulPose(Axis.YP.rotationDegrees(rotationY));
        ms.mulPose(Axis.ZN.rotationDegrees(rotationZ));
        ms.mulPose(Axis.XP.rotationDegrees(rotationX));
        ms.scale(scaleX, scaleY, scaleZ);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.NONE, light, OverlayTexture.NO_OVERLAY, ms, buffer, player.level(), 1);
        ms.popPose();
    }

    private void renderIfPresent(Player player, NonNullList<ItemStack> inventory, Item item, Consumer<ItemStack> renderer) {
        if (!isNotHolding(player, item)) {
            return;
        }

        inventory.stream().filter(stack -> stack.is(item)).findAny().ifPresent(renderer);
    }

    private boolean isNotHolding(@NotNull Player player, Item item) {
        return !player.getMainHandItem().is(item) && !player.getOffhandItem().is(item);
    }
}
