package meranha.mekaweapons.client;

import meranha.mekaweapons.items.MekaArrowEntity;
import org.jetbrains.annotations.NotNull;

import meranha.mekaweapons.MekaWeapons;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class MekaArrowRenderer extends ArrowRenderer<MekaArrowEntity> {
    public MekaArrowRenderer(Context context) {
        super(context);
    }

    @NotNull
    public ResourceLocation getTextureLocation(@NotNull MekaArrowEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(MekaWeapons.MODID, "textures/entity/meka_arrow.png");
    }
}
