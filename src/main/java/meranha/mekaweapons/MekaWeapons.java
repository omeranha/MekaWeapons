package meranha.mekaweapons;

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
import mekanism.common.registration.impl.EntityTypeDeferredRegister;
import mekanism.common.registration.impl.EntityTypeRegistryObject;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.common.registration.impl.ModuleDeferredRegister;
import mekanism.common.registration.impl.ModuleRegistryObject;
import mekanism.common.registries.MekanismCreativeTabs;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.MekanismUtils.ResourceType;
import meranha.mekaweapons.items.ItemMagnetizer;
import meranha.mekaweapons.items.ItemMekaBow;
import meranha.mekaweapons.items.ItemMekaTana;
import meranha.mekaweapons.items.MekaArrowEntity;
import meranha.mekaweapons.items.MekaArrowRenderer;
import meranha.mekaweapons.items.ModuleWeaponAttackAmplificationUnit;
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
import net.minecraft.world.item.Rarity;
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

    public static final ModuleDeferredRegister MODULES =  new ModuleDeferredRegister(MekaWeapons.MODID);
    public static final ModuleRegistryObject<?> ARROWENERGY_UNIT = MODULES.registerMarker("arrowenergy_unit", () -> MekaWeapons.MODULE_ARROWENERGY.asItem(), builder -> builder.rarity(Rarity.RARE));
    public static final ModuleRegistryObject<?> AUTOFIRE_UNIT = MODULES.registerMarker("autofire_unit", () -> MekaWeapons.MODULE_AUTOFIRE.asItem(), builder -> builder.rarity(Rarity.RARE));
    public static final ModuleRegistryObject<?> DRAWSPEED_UNIT = MODULES.registerMarker("drawspeed_unit", () -> MekaWeapons.MODULE_DRAWSPEED.asItem(), builder -> builder.maxStackSize(3).rarity(Rarity.RARE));
    public static final ModuleRegistryObject<?> GRAVITYDAMPENER_UNIT = MODULES.registerMarker("gravitydampener_unit", () -> MekaWeapons.MODULE_GRAVITYDAMPENER.asItem(), builder -> builder.rarity(Rarity.EPIC));
    //public static final ModuleRegistryObject<?> ARROWVELOCITY_UNIT = MODULES.registerMarker("arrowvelocity_unit", () -> MekaWeapons.MODULE_ARROWVELOCITY.asItem(), builder -> builder.maxStackSize(8).rarity(Rarity.RARE));
    public static final ModuleRegistryObject<ModuleWeaponAttackAmplificationUnit> ATTACKAMPLIFICATION_UNIT = MODULES.register("attackamplification_unit",
        ModuleWeaponAttackAmplificationUnit::new, () -> MekaWeapons.MODULE_ATTACKAMPLIFICATION.get(), builder -> builder.maxStackSize(4).rarity(Rarity.UNCOMMON).rendersHUD()
    );

    public static final ItemDeferredRegister ITEMS = new ItemDeferredRegister(MekaWeapons.MODID);
    public static final ItemRegistryObject<ItemMekaTana> MEKA_TANA = ITEMS.registerUnburnable("mekatana", ItemMekaTana::new);
    public static final ItemRegistryObject<ItemMekaBow> MEKA_BOW = ITEMS.registerUnburnable("mekabow", ItemMekaBow::new);
    public static final ItemRegistryObject<Item> MAGNETIZER = ITEMS.registerUnburnable("magnetizer", ItemMagnetizer::new);
    public static final ItemRegistryObject<Item> KATANA_BLADE = ITEMS.register("katana_blade");
    public static final ItemRegistryObject<Item> BOW_RISER = ITEMS.register("bow_riser");
    public static final ItemRegistryObject<Item> BOW_LIMB = ITEMS.register("bow_limb");
    public static final ItemRegistryObject<ItemModule> MODULE_ARROWENERGY = ITEMS.registerModule(MekaWeapons.ARROWENERGY_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_AUTOFIRE = ITEMS.registerModule(MekaWeapons.AUTOFIRE_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_DRAWSPEED = ITEMS.registerModule(MekaWeapons.DRAWSPEED_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_GRAVITYDAMPENER = ITEMS.registerModule(MekaWeapons.GRAVITYDAMPENER_UNIT);
    //public static final ItemRegistryObject<ItemModule> MODULE_ARROWVELOCITY = ITEMS.registerModule(MekaWeapons.ARROWVELOCITY_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_ATTACKAMPLIFICATION = ITEMS.registerModule(MekaWeapons.ATTACKAMPLIFICATION_UNIT);

    public static final EntityTypeDeferredRegister ENTITY_TYPES = new EntityTypeDeferredRegister(MekaWeapons.MODID);
    public static final EntityTypeRegistryObject<MekaArrowEntity> MEKA_ARROW = ENTITY_TYPES.register("meka_arrow", EntityType.Builder.<MekaArrowEntity>of(MekaArrowEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(20));

    public static final String ADD_MEKA_BOW_MODULES = "add_meka_bow_modules";
    public static final String ADD_MEKATANA_MODULES = "add_mekatana_modules";

    public MekaWeapons() {
        this(FMLJavaModLoadingContext.get().getModEventBus(), ModLoadingContext.get().getActiveContainer());
    }

    public MekaWeapons(IEventBus modEventBus, ModContainer modContainer) {
        MekaWeapons.ITEMS.register(modEventBus);
        MekaWeapons.MODULES.register(modEventBus);
        MekaWeapons.ENTITY_TYPES.register(modEventBus);
        MekanismConfigHelper.registerConfig(modContainer, general);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::buildCreativeModeTabContents);
        modEventBus.addListener(this::enqueueIMC);
        modEventBus.addListener(this::registerRenderers);
        MinecraftForge.EVENT_BUS.addListener(this::mekaBowEnergyArrows);
        MinecraftForge.EVENT_BUS.addListener(this::disableMekaBowAttack);
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
        addModules(ADD_MEKA_BOW_MODULES, MekanismModules.ENERGY_UNIT, ATTACKAMPLIFICATION_UNIT, AUTOFIRE_UNIT, ARROWENERGY_UNIT, DRAWSPEED_UNIT, GRAVITYDAMPENER_UNIT); // ARROWVELOCITY_UNIT
        addModules(ADD_MEKATANA_MODULES, MekanismModules.ENERGY_UNIT, ATTACKAMPLIFICATION_UNIT, MekanismModules.TELEPORTATION_UNIT);
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
            if (mekaBow.isModuleEnabled(stack, ARROWENERGY_UNIT)) {
                event.getEntity().startUsingItem(event.getHand());
                event.setAction(InteractionResultHolder.success(event.getBow()));
            }
        }
    }

    // small trick to prevent players from using the meka-bow to attack entities. This allows the tooltip to show attack damage without enabling actual damage.
    private void disableMekaBowAttack(@NotNull AttackEntityEvent event) {
        Player player = event.getEntity();

        if (!(player.level() instanceof ServerLevel)) {
            return;
        }

        if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof ItemMekaBow) {
            event.setCanceled(true);
        }
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            if (ModList.get().isLoaded("curios")) {
                IItemProvider itemProvider = () -> MekaWeapons.MAGNETIZER.get();
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
    }
}
