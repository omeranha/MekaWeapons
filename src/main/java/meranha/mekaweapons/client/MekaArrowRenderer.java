package meranha.mekaweapons.client;

import javax.annotation.Nonnull;

import meranha.mekaweapons.items.MekaArrowEntity;
import org.jetbrains.annotations.NotNull;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class MekaArrowRenderer extends ArrowRenderer<MekaArrowEntity> {
    public MekaArrowRenderer(Context context) {
        super(context);
    }

    @NotNull
    public ResourceLocation getTextureLocation(@NotNull @Nonnull MekaArrowEntity entity) {
        return new ResourceLocation("mekaweapons:textures/entity/meka_arrow.png");
    }
}
