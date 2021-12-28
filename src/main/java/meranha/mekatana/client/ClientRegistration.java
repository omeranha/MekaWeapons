package meranha.mekatana.client;

import mekanism.client.ClientRegistrationUtil;
import mekanism.common.Mekanism;
import meranha.mekatana.items.ModItems;
import meranha.mekatana.MekaWeapons;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = MekaWeapons.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientRegistration {

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        Keybinds.registerKeybindings();

        event.enqueueWork(() -> {
            ClientRegistrationUtil.setPropertyOverride(ModItems.MEKA_BOW, Mekanism.rl("pull"),
                    (stack, world, entity) -> entity != null && entity.getUseItem() == stack ? (stack.getUseDuration() - entity.getUseItemRemainingTicks()) / 20.0F : 0);
            ClientRegistrationUtil.setPropertyOverride(ModItems.MEKA_BOW, Mekanism.rl("pulling"),
                    (stack, world, entity) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
        });
    }
}
