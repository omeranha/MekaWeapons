package meranha.mekaweapons;

import java.util.HashMap;
import java.util.Map;

import mekanism.api.functions.ConstantPredicates;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.common.content.gear.shared.ModuleColorModulationUnit;
import mekanism.common.lib.Color;
import mekanism.common.lib.Version;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.impl.*;
import meranha.mekaweapons.client.*;
import meranha.mekaweapons.items.*;
import meranha.mekaweapons.items.modules.*;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import mekanism.api.MekanismIMC;
import mekanism.client.ClientRegistrationUtil;
import mekanism.common.Mekanism;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.containers.energy.ComponentBackedNoClampEnergyContainer;
import mekanism.common.attachments.containers.energy.EnergyContainersBuilder;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.config.IMekanismConfig;
import mekanism.common.config.MekanismConfigHelper;
import mekanism.common.content.gear.shared.ModuleEnergyUnit;
import mekanism.common.item.ItemModule;
import mekanism.common.registries.MekanismCreativeTabs;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.Rarity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.living.LivingGetProjectileEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@SuppressWarnings({"Convert2MethodRef", "unused", "forremoval"})
@Mod(MekaWeapons.MODID)
public class MekaWeapons {
    public static final String MODID = "mekaweapons";
    public static final Logger logger = LogUtils.getLogger();
    public static final WeaponsConfig general = new WeaponsConfig();
    private static final Map<IConfigSpec, IMekanismConfig> KNOWN_CONFIGS = new HashMap<>();

    public static final EntityTypeDeferredRegister ENTITY_TYPES = new EntityTypeDeferredRegister(MODID);
    public static final DeferredHolder<EntityType<?>, EntityType<MekaArrowEntity>> MEKA_ARROW = ENTITY_TYPES.register("meka_arrow", () -> EntityType.Builder.<MekaArrowEntity>of(MekaArrowEntity::new,
            MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(20).build(MODID + ":meka_arrow"));

    public static final ContainerTypeDeferredRegister CONTAINER_TYPES = new ContainerTypeDeferredRegister(MODID);
    public static final ContainerTypeRegistryObject<MagnetizerContainer> MAGNETIZER_CONTAINER = CONTAINER_TYPES.register(WeaponsItems.MAGNETIZER, ItemMagnetizer.class, MagnetizerContainer::new);

    public static final DataComponentDeferredRegister DATA_COMPONENTS = new DataComponentDeferredRegister(MODID);
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> TOGGLE_RENDER_MEKATANA = DATA_COMPONENTS.registerBoolean("render_mekatana");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> TOGGLE_RENDER_MEKABOW = DATA_COMPONENTS.registerBoolean("render_mekabow");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> TOGGLE_RENDER_MEKAGUN = DATA_COMPONENTS.registerBoolean("render_mekagun");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Integer>> HEAT = DATA_COMPONENTS.registerInt("heat");
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Long>> LAST_FIRE_TICK = DATA_COMPONENTS.registerNonNegativeLong("last_fire_tick");

    public MekaWeapons(IEventBus modEventBus, ModContainer modContainer) {
        WeaponsItems.ITEMS.register(modEventBus);
        WeaponsModules.MODULES.register(modEventBus);
        MekaWeapons.ENTITY_TYPES.register(modEventBus);
        MekaWeapons.CONTAINER_TYPES.register(modEventBus);
        MekaWeapons.DATA_COMPONENTS.register(modEventBus);

        MekanismConfigHelper.registerConfig(KNOWN_CONFIGS, modContainer, general);
        modEventBus.addListener(this::buildCreativeModeTabContents);
        modEventBus.addListener(this::sendCustomModules);
        modEventBus.addListener(this::registerRenderers);

        NeoForge.EVENT_BUS.addListener(this::disableWeaponsAttack);

        Version versionNumber = new Version(modContainer);
        WeaponsPacketHandler packetHandler = new WeaponsPacketHandler(modEventBus, versionNumber);
    }

    @NotNull
    @Contract("_ -> new")
    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MekaWeapons.MODID, path);
    }

    @NotNull
    @Contract("_, _ -> new")
    public static ResourceLocation getResource(@NotNull ResourceType type, String name) {
        return MekaWeapons.rl(type.getPrefix() + name);
    }

    private void buildCreativeModeTabContents(@NotNull BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == MekanismCreativeTabs.MEKANISM.get()) {
            WeaponsItems.ITEMS.getEntries().forEach(entry -> event.accept(entry.get()));
        }
    }

    private void sendCustomModules(InterModEnqueueEvent event) {
        final String ADD_MEKA_TANA_MODULES = "add_meka_tana_modules";
        final String ADD_MEKA_BOW_MODULES = "add_meka_bow_modules";
        final String ADD_MEKA_GUN_MODULES = "add_meka_gun_modules";
        final String ADD_MAGNETIZER_MODULE = "add_magnetizer_module";
        MekanismIMC.addModuleContainer((Holder<Item>)WeaponsItems.MEKA_TANA, ADD_MEKA_TANA_MODULES);
        MekanismIMC.addModuleContainer((Holder<Item>)WeaponsItems.MEKA_BOW, ADD_MEKA_BOW_MODULES);
        MekanismIMC.addModuleContainer((Holder<Item>)WeaponsItems.MEKA_GUN, ADD_MEKA_GUN_MODULES);
        MekanismIMC.addModuleContainer((Holder<Item>)WeaponsItems.MAGNETIZER, ADD_MAGNETIZER_MODULE);
        MekanismIMC.sendModuleIMC(ADD_MEKA_TANA_MODULES, MekanismModules.ENERGY_UNIT, WeaponsModules.ATTACKAMPLIFICATION_UNIT, MekanismModules.TELEPORTATION_UNIT, WeaponsModules.SWEEPING_UNIT, WeaponsModules.LOOTING_UNIT, MekanismModules.COLOR_MODULATION_UNIT);
        MekanismIMC.sendModuleIMC(ADD_MEKA_BOW_MODULES, MekanismModules.ENERGY_UNIT, WeaponsModules.ATTACKAMPLIFICATION_UNIT, WeaponsModules.AUTOFIRE_UNIT, WeaponsModules.ARROWENERGY_UNIT, WeaponsModules.DRAWSPEED_UNIT, WeaponsModules.GRAVITYDAMPENER_UNIT, WeaponsModules.LOOTING_UNIT, MekanismModules.COLOR_MODULATION_UNIT);
        MekanismIMC.sendModuleIMC(ADD_MEKA_GUN_MODULES, MekanismModules.ENERGY_UNIT, WeaponsModules.ATTACKAMPLIFICATION_UNIT, MekanismModules.COLOR_MODULATION_UNIT);
        MekanismIMC.sendModuleIMC(ADD_MAGNETIZER_MODULE, MekanismModules.COLOR_MODULATION_UNIT);
    }

    // small trick to prevent players from using meka-bow and meka-gun to attack entities. This allows the tooltip to show attack damage without enabling actual damage.
    private void disableWeaponsAttack(@NotNull AttackEntityEvent event) {
        Player player = event.getEntity();
        if (!(player.level() instanceof ServerLevel)) {
            return;
        }

        var item = player.getItemInHand(InteractionHand.MAIN_HAND).getItem();
        if (item instanceof ItemMekaBow || item instanceof ItemMekaGun) {
            event.setCanceled(true);
        }
    }

    public void registerRenderers(@NotNull RegisterRenderers event) {
        event.registerEntityRenderer(MekaWeapons.MEKA_ARROW.get(), MekaArrowRenderer::new);
    }

    @EventBusSubscriber(modid = MekaWeapons.MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(final FMLClientSetupEvent event) {
            if (ModList.get().isLoaded("curios")) {
                CuriosRendererRegistry.register(WeaponsItems.MAGNETIZER.get(), WeaponsRenderer::new);
            }

            event.enqueueWork(() -> {
                ClientRegistrationUtil.setPropertyOverride(WeaponsItems.MEKA_BOW, Mekanism.rl("pull"), (stack, world, entity, seed) -> {
                    if (entity != null && entity.getUseItem() == stack && stack.getItem() instanceof ItemMekaBow bow) {
                        return (stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) / bow.getUseTick(stack);
                    }
                    return 0;
                });
                ClientRegistrationUtil.setPropertyOverride(WeaponsItems.MEKA_BOW, Mekanism.rl("pulling"), (stack, world, entity, seed) ->
                        entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            });
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            ClientRegistrationUtil.registerScreen(event, MekaWeapons.MAGNETIZER_CONTAINER, GuiMagnetizer::new);
        }

        @SubscribeEvent
        public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
            event.register((stack, tintIndex) -> {
                if (tintIndex == 0) {
                    IModule<ModuleColorModulationUnit> colorUnit = IModuleHelper.INSTANCE.getModule(stack, MekanismModules.COLOR_MODULATION_UNIT);
                    return colorUnit != null ? colorUnit.getCustomInstance().color().argb() : Color.WHITE.argb();
                }
                return 0xFFFFFFFF;
            }, WeaponsItems.MEKA_TANA.get(), WeaponsItems.MEKA_BOW.get(), WeaponsItems.MEKA_GUN.get(), WeaponsItems.MAGNETIZER.get());
        }
    }
}
