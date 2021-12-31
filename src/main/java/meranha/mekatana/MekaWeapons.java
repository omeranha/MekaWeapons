package meranha.mekatana;

import mekanism.client.ClientRegistrationUtil;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfigHelper;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.common.registries.MekanismModules;
import meranha.mekatana.items.ItemMekaBow;
import meranha.mekatana.items.ItemMekaTana;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod.EventBusSubscriber(modid = MekaWeapons.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
@Mod(MekaWeapons.MOD_ID)
@SuppressWarnings("unused")
public class MekaWeapons {

    public static final String MOD_ID = "mekaweapons";
    public static final MekanismHooks hooks = new MekanismHooks();
    public static final ItemDeferredRegister ITEMS = new ItemDeferredRegister("mekaweapons");
    public static final MekaWeaponsConfig general = new MekaWeaponsConfig();

    public static final ItemRegistryObject<ItemMekaTana> MEKA_TANA = ITEMS.registerUnburnable("mekatana", ItemMekaTana::new);
    public static final ItemRegistryObject<ItemMekaBow> MEKA_BOW = ITEMS.registerUnburnable("mekabow", ItemMekaBow::new);

    public MekaWeapons() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        MekaWeapons.registerConfigs(ModLoadingContext.get());
        MekaWeapons.ITEMS.register(modEventBus);
        modEventBus.addListener(this::enqueueIMC);
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        hooks.sendIMCMessages(event);
        WeaponsIMC.addModulesToAll(MekanismModules.ENERGY_UNIT);
        WeaponsIMC.addMekaTanaModules(MekanismModules.ATTACK_AMPLIFICATION_UNIT);
        WeaponsIMC.addMekaBowModules(MekanismModules.ATTACK_AMPLIFICATION_UNIT);
    }

    public static void registerConfigs(ModLoadingContext modLoadingContext) {
        ModContainer modContainer = modLoadingContext.getActiveContainer();
        MekanismConfigHelper.registerConfig(modContainer, general);
    }

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ClientRegistrationUtil.setPropertyOverride(MekaWeapons.MEKA_BOW, Mekanism.rl("pull"), (stack, world, entity) -> entity != null && entity.getUseItem() == stack ? (stack.getUseDuration() - entity.getUseItemRemainingTicks()) / 20.0F : 0);
            ClientRegistrationUtil.setPropertyOverride(MekaWeapons.MEKA_BOW, Mekanism.rl("pulling"), (stack, world, entity) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
        });
    }
}
