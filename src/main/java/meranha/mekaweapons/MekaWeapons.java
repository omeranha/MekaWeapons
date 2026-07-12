package meranha.mekaweapons;

import mekanism.api.gear.IModule;
import mekanism.common.content.gear.shared.ModuleColorModulationUnit;
import mekanism.common.lib.Color;
import mekanism.common.registration.impl.*;
import meranha.mekaweapons.client.*;
import meranha.mekaweapons.items.*;
import meranha.mekaweapons.items.modules.WeaponsModules;
import meranha.mekaweapons.particle.MekaGunLaserParticleData;
import meranha.mekaweapons.particle.MekaGunLaserParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import mekanism.api.providers.IItemProvider;
import mekanism.api.providers.IModuleDataProvider;
import mekanism.client.ClientRegistrationUtil;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfigHelper;
import mekanism.common.item.ItemModule;
import mekanism.common.registries.MekanismCreativeTabs;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@Mod(MekaWeapons.MODID)
public class MekaWeapons {
    public static final String MODID = "mekaweapons";
    public static final Logger logger = LogUtils.getLogger();

    public static final WeaponsConfig general = new WeaponsConfig();

    public static final ItemDeferredRegister ITEMS = new ItemDeferredRegister(MekaWeapons.MODID);
    public static final ItemRegistryObject<ItemMekaTana> MEKA_TANA = ITEMS.registerUnburnable("mekatana", ItemMekaTana::new);
    public static final ItemRegistryObject<ItemMekaBow> MEKA_BOW = ITEMS.registerUnburnable("mekabow", ItemMekaBow::new);
    public static final ItemRegistryObject<ItemMekaGun> MEKA_GUN = ITEMS.registerUnburnable("meka_gun", ItemMekaGun::new);
    public static final ItemRegistryObject<Item> MAGNETIZER = ITEMS.registerUnburnable("magnetizer", ItemMagnetizer::new);
    public static final ItemRegistryObject<Item> KATANA_BLADE = ITEMS.register("katana_blade");
    public static final ItemRegistryObject<Item> BOW_RISER = ITEMS.register("bow_riser");
    public static final ItemRegistryObject<Item> BOW_LIMB = ITEMS.register("bow_limb");
    public static final ItemRegistryObject<ItemModule> MODULE_LOOTING = ITEMS.registerModule(WeaponsModules.LOOTING_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_DRAWSPEED = ITEMS.registerModule(WeaponsModules.DRAWSPEED_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_SWEEPING = ITEMS.registerModule(WeaponsModules.SWEEPING_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_ARROWENERGY = ITEMS.registerModule(WeaponsModules.ARROWENERGY_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_AUTOFIRE = ITEMS.registerModule(WeaponsModules.AUTOFIRE_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_GRAVITYDAMPENER = ITEMS.registerModule(WeaponsModules.GRAVITYDAMPENER_UNIT);
    //public static final ItemRegistryObject<ItemModule> MODULE_ARROWVELOCITY = ITEMS.registerModule(MekaWeapons.ARROWVELOCITY_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_ATTACKAMPLIFICATION = ITEMS.registerModule(WeaponsModules.ATTACKAMPLIFICATION_UNIT);

    public static final EntityTypeDeferredRegister ENTITY_TYPES = new EntityTypeDeferredRegister(MekaWeapons.MODID);
    public static final EntityTypeRegistryObject<MekaArrowEntity> MEKA_ARROW = ENTITY_TYPES.register("meka_arrow", EntityType.Builder.<MekaArrowEntity>of(MekaArrowEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(20));

    public static final ContainerTypeDeferredRegister CONTAINER_TYPES = new ContainerTypeDeferredRegister(MODID);
    public static final ContainerTypeRegistryObject<MagnetizerContainer> MAGNETIZER_CONTAINER = CONTAINER_TYPES.register(MekaWeapons.MAGNETIZER, ItemMagnetizer.class, MagnetizerContainer::new);

    public static final ParticleTypeDeferredRegister PARTICLE_TYPES = new ParticleTypeDeferredRegister(MODID);
    public static final ParticleTypeRegistryObject<MekaGunLaserParticleData, MekaGunLaserParticleType> MEKA_GUN_LASER = PARTICLE_TYPES.register("meka_gun_laser", MekaGunLaserParticleType::new);

    public static final String ADD_MEKATANA_MODULES = "add_mekatana_modules";
    public static final String ADD_MEKABOW_MODULES = "add_mekabow_modules";
    public static final String ADD_MEKAGUN_MODULES = "add_mekagun_modules";
    public static final String ADD_MAGNETIZER_MODULES = "add_magnetizer_modules";

    @SuppressWarnings("removal")
    public MekaWeapons() {
        this(FMLJavaModLoadingContext.get().getModEventBus(), ModLoadingContext.get().getActiveContainer());
    }

    public MekaWeapons(IEventBus modEventBus, ModContainer modContainer) {
        MekaWeapons.ITEMS.register(modEventBus);
        MekaWeapons.ENTITY_TYPES.register(modEventBus);
        MekaWeapons.CONTAINER_TYPES.register(modEventBus);
        MekaWeapons.PARTICLE_TYPES.register(modEventBus);
        MekanismConfigHelper.registerConfig(modContainer, general);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::buildCreativeModeTabContents);
        modEventBus.addListener(this::enqueueIMC);
        modEventBus.addListener(this::registerRenderers);
        MinecraftForge.EVENT_BUS.addListener(this::mekaBowEnergyArrows);
        MinecraftForge.EVENT_BUS.addListener(this::MekaWeaponsAttackEvent);
        WeaponsModules.MODULES.register(modEventBus);
    }

    @NotNull
    @Contract("_ -> new")
    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MekaWeapons.MODID, path);
    }

    @NotNull
    @Contract("_, _ -> new")
    public static ResourceLocation getResource(@NotNull ResourceType guiRadial, String name) {
        return MekaWeapons.rl(guiRadial.getPrefix() + name);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        MekaWeapons.logger.info("Loaded 'Mekanism: Weapons' module.");
    }

    private void buildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == MekanismCreativeTabs.MEKANISM.get()) {
            MekaWeapons.ITEMS.getAllItems().forEach(event::accept);
        }
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        addModules(ADD_MEKATANA_MODULES, MekanismModules.ENERGY_UNIT, WeaponsModules.ATTACKAMPLIFICATION_UNIT, MekanismModules.TELEPORTATION_UNIT, WeaponsModules.SWEEPING_UNIT, WeaponsModules.LOOTING_UNIT, MekanismModules.COLOR_MODULATION_UNIT);
        addModules(ADD_MEKABOW_MODULES, MekanismModules.ENERGY_UNIT, WeaponsModules.ATTACKAMPLIFICATION_UNIT, WeaponsModules.AUTOFIRE_UNIT, WeaponsModules.ARROWENERGY_UNIT, WeaponsModules.DRAWSPEED_UNIT, WeaponsModules.GRAVITYDAMPENER_UNIT, WeaponsModules.LOOTING_UNIT, MekanismModules.COLOR_MODULATION_UNIT);
        addModules(ADD_MEKAGUN_MODULES, MekanismModules.ENERGY_UNIT, WeaponsModules.ATTACKAMPLIFICATION_UNIT, MekanismModules.COLOR_MODULATION_UNIT);
        addModules(ADD_MAGNETIZER_MODULES, MekanismModules.COLOR_MODULATION_UNIT);
    }

    public static void addModules(String method, IModuleDataProvider<?>... moduleDataProviders) {
        sendModuleIMC(method, moduleDataProviders);
    }

    private static void sendModuleIMC(String method, IModuleDataProvider<?>... moduleDataProviders) {
        if (moduleDataProviders == null || moduleDataProviders.length == 0) {
            throw new IllegalArgumentException("No module data providers given.");
        }
        InterModComms.sendTo(Mekanism.MODID, method, () -> moduleDataProviders);
    }

    public void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MekaWeapons.MEKA_ARROW.get(), MekaArrowRenderer::new);
    }

    private void mekaBowEnergyArrows(final ArrowNockEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel)) {
            return;
        }

        ItemStack stack = event.getBow();
        if (stack.getItem() instanceof ProjectileWeaponItem && stack.getItem() instanceof ItemMekaBow mekaBow) {
            if (mekaBow.isModuleEnabled(stack, WeaponsModules.ARROWENERGY_UNIT)) {
                event.getEntity().startUsingItem(event.getHand());
                event.setAction(InteractionResultHolder.success(event.getBow()));
            }
        }
    }


    private void MekaWeaponsAttackEvent(@NotNull AttackEntityEvent event) {
        Player player = event.getEntity();
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        var stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        var item = stack.getItem();
        // small trick to prevent players from using the meka-bow to attack entities. This allows the tooltip to show attack damage without enabling actual damage.
        if (item instanceof ItemMekaBow) {
            event.setCanceled(true);
            return;
        }

        // another small trick to emulate Minecraft's sweep attack. Vanilla applies sweep damage through enchantment helper directly in attack function, so manually damage nearby entities to support the Meka-Tana.
        if (item instanceof ItemMekaTana) {
            if (!MekaWeaponsUtils.isModuleEnabled(stack, WeaponsModules.SWEEPING_UNIT)) {
                return;
            }

            var target = event.getTarget();
            var damage = MekaWeaponsUtils.getTotalDamage(stack);
            for(LivingEntity livingentity : level.getEntitiesOfClass(LivingEntity.class, player.getItemInHand(InteractionHand.MAIN_HAND).getSweepHitBox(player, target))) {
                double entityReachSq = Mth.square(player.getEntityReach());
                if (livingentity != player && livingentity != target && !player.isAlliedTo(livingentity) && (!(livingentity instanceof ArmorStand) || !((ArmorStand)livingentity).isMarker()) && player.distanceToSqr(livingentity) < entityReachSq) {
                    livingentity.hurt(player.damageSources().playerAttack(player), damage);
                }
            }
        }
    }

    @EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            if (ModList.get().isLoaded("curios")) {
                IItemProvider itemProvider = MekaWeapons.MAGNETIZER::get;
                CuriosRendererRegistry.register(itemProvider.asItem(), WeaponsRenderer::new);
            }

            event.enqueueWork(() -> {
                ClientRegistrationUtil.setPropertyOverride(MekaWeapons.MEKA_BOW, Mekanism.rl("pull"), (stack, world, entity, seed) -> {
                    if (entity != null && entity.getUseItem() == stack && stack.getItem() instanceof ItemMekaBow bow) {
                        return (stack.getUseDuration() - entity.getUseItemRemainingTicks()) / bow.getUseTick(stack);
                    }
                    return 0;
                });
                ClientRegistrationUtil.setPropertyOverride(MekaWeapons.MEKA_BOW, Mekanism.rl("pulling"), (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            });
        }

        @SubscribeEvent(priority = EventPriority.LOW)
        public static void registerScreens(RegisterEvent event) {
            event.register(Registries.MENU, helper -> ClientRegistrationUtil.registerScreen(MekaWeapons.MAGNETIZER_CONTAINER, GuiMagnetizer::new));
        }

        @SubscribeEvent
        public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
            event.register((stack, tintIndex) -> {
                if (tintIndex == 0) {
                    IModule<ModuleColorModulationUnit> colorUnit = MekaWeaponsUtils.getEnabledModule(stack, MekanismModules.COLOR_MODULATION_UNIT);
                    return colorUnit != null ? colorUnit.getCustomInstance().getColor().argb() : Color.WHITE.argb();
                }
                return 0xFFFFFFFF;
            }, MekaWeapons.MEKA_TANA.get(), MekaWeapons.MEKA_BOW.get(), MekaWeapons.MAGNETIZER.get(), MekaWeapons.MEKA_GUN.get());
        }

        @SubscribeEvent
        public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(MekaWeapons.MEKA_GUN_LASER.get(), MekaGunLaserParticle.Factory::new);
        }
    }
}
