package meranha.mekatana.client;

import net.minecraft.entity.Entity;
import javax.annotation.Nonnull;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;

public class QuiverModel<T extends LivingEntity> extends EntityModel<T> {
    private final ModelRenderer bone;

    public QuiverModel() {
        this.texWidth = 32;
        this.texHeight = 32;
        (this.bone = new ModelRenderer((Model)this)).setPos(0.0f, 24.0f, 0.0f);
        this.setRotationAngle(this.bone, 0.0f, 0.0f, -0.6981f);
        this.bone.texOffs(10, 0).addBox(10.0f, -20.0f, 2.0f, 4.0f, 12.0f, 3.0f, 0.0f, false);
        this.bone.texOffs(0, 0).addBox(11.5f, -20.75f, -2.0f, 1.0f, 13.0f, 4.0f, 0.1f, false);
    }

    public void renderToBuffer(final MatrixStack matrixStack, @Nonnull final IVertexBuilder buffer, final int packedLight, final int packedOverlay, final float red, final float green, final float blue, final float alpha) {
        this.bone.render(matrixStack, buffer, packedLight, packedOverlay);
    }

    public void setupAnim(final T entityIn, final float limbSwing, final float limbSwingAmount, final float ageInTicks, final float netHeadYaw, final float headPitch) { }

    public void setRotationAngle(final ModelRenderer modelRenderer, final float x, final float y, final float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
