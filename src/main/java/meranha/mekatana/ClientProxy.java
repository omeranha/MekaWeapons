package meranha.mekatana;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.Map;
import java.util.function.Supplier;

public class ClientProxy implements IProxy {
    @Override
    public void preInit(FMLCommonSetupEvent event) {

    }

    @Override
    public void postInit(FMLCommonSetupEvent event) {

    }

    @Override
    public <T extends Entity> void registerEntityRenderer(EntityType<T> entityClass, Supplier<IRenderFactory<T>> renderFactory) {
        RenderingRegistry.registerEntityRenderingHandler(entityClass, renderFactory.get());
    }
}
