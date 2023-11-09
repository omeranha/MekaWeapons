package meranha.mekaweapons;

import ad_astra_giselle_addon.common.compat.mekanism.AddonMekanismModules;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import mekanism.api.MekanismIMC;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.api.providers.IItemProvider;
import mekanism.api.providers.IModuleDataProvider;
import mekanism.client.ClientRegistrationUtil;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfigHelper;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.item.ItemModule;
import mekanism.common.registration.impl.*;
import mekanism.common.registries.MekanismCreativeTabs;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismModules;
import mekanism.generators.common.registries.GeneratorsModules;
import meranha.mekaweapons.items.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"Convert2MethodRef", "unused", "forremoval"})
@Mod.EventBusSubscriber(modid = MekaWeapons.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
@Mod(MekaWeapons.MODID)
public class MekaWeapons {
    public static final String MODID = "mekaweapons";
    public static final WeaponsConfig general = new WeaponsConfig();

    public static final ModuleDeferredRegister MODULES =  new ModuleDeferredRegister(MekaWeapons.MODID);
    public static final ModuleRegistryObject<?> ARROWENERGY_UNIT = MODULES.registerMarker("arrowenergy_unit", () -> MekaWeapons.MODULE_ARROWENERGY.asItem(), builder -> builder.rarity(Rarity.RARE));
    public static final ModuleRegistryObject<?> AUTOFIRE_UNIT = MODULES.registerMarker("autofire_unit", () -> MekaWeapons.MODULE_AUTOFIRE.asItem(), builder -> builder.rarity(Rarity.RARE));
    public static final ModuleRegistryObject<?> DRAWSPEED_UNIT = MODULES.registerMarker("drawspeed_unit", () -> MekaWeapons.MODULE_DRAWSPEED.asItem(), builder -> builder.maxStackSize(3).rarity(Rarity.RARE));
    public static final ModuleRegistryObject<?> GRAVITYDAMPENER_UNIT = MODULES.registerMarker("gravitydampener_unit", () -> MekaWeapons.MODULE_GRAVITYDAMPENER.asItem(), builder -> builder.rarity(Rarity.EPIC));
    //public static final ModuleRegistryObject<?> ARROWVELOCITY_UNIT = MODULES.registerMarker("arrowvelocity_unit", () -> MekaWeapons.MODULE_ARROWVELOCITY.asItem(), builder -> builder.maxStackSize(8).rarity(Rarity.RARE));

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

    public static final EntityTypeDeferredRegister ENTITY_TYPES = new EntityTypeDeferredRegister(MekaWeapons.MODID);
    public static final EntityTypeRegistryObject<MekaArrowEntity> MEKA_ARROW = ENTITY_TYPES.register("meka_arrow", EntityType.Builder.<MekaArrowEntity>of(MekaArrowEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(20));

    private final Map<Item, Set<ModuleData<?>>> supportedModules = new Reference2ObjectArrayMap<>(7);
    private final Map<ModuleData<?>, Set<Item>> supportedContainers = new IdentityHashMap<>();
    public static final String ADD_MEKA_BOW_MODULES = "add_meka_bow_modules";
    public static final String ADD_MEKATANA_MODULES = "add_mekatana_modules";

    public MekaWeapons() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MekaWeapons.ITEMS.register(modEventBus);
        MekaWeapons.MODULES.register(modEventBus);
        MekaWeapons.ENTITY_TYPES.register(modEventBus);
        MekanismConfigHelper.registerConfig(ModLoadingContext.get().getActiveContainer(), general);
        modEventBus.addListener(this::buildCreativeModeTabContents);
        modEventBus.addListener(this::enqueueIMC);
        modEventBus.addListener(this::processIMC);
        modEventBus.addListener(this::registerRenderers);
    }

    private void buildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == MekanismCreativeTabs.MEKANISM.get()) {
            MekaWeapons.ITEMS.getAllItems().forEach(event::accept);
        }
    }

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        if (ModList.get().isLoaded("curios")) {
            CuriosRendererRegistry.register(MekaWeapons.MAGNETIZER.get(), WeaponsRenderer::new);
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

    public void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MekaWeapons.MEKA_ARROW.get(), MekaArrowRenderer::new);
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        InterModComms.sendTo(CuriosApi.MODID, SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder("magnetizer").icon(new ResourceLocation(MekaWeapons.MODID, "slot/magnetizer_slot")).build());
        addMekaToolModules(MekanismModules.ENERGY_UNIT, MekanismModules.ATTACK_AMPLIFICATION_UNIT, MekanismModules.SILK_TOUCH_UNIT, MekanismModules.FORTUNE_UNIT, MekanismModules.BLASTING_UNIT, MekanismModules.VEIN_MINING_UNIT, MekanismModules.FARMING_UNIT, MekanismModules.SHEARING_UNIT, MekanismModules.TELEPORTATION_UNIT, MekanismModules.EXCAVATION_ESCALATION_UNIT);
        addMekaBowModules(MekanismModules.ENERGY_UNIT, MekanismModules.ATTACK_AMPLIFICATION_UNIT, MekaWeapons.AUTOFIRE_UNIT, MekaWeapons.ARROWENERGY_UNIT, MekaWeapons.DRAWSPEED_UNIT, MekaWeapons.GRAVITYDAMPENER_UNIT); // MekaWeapons.ARROWVELOCITY_UNIT
        addMekaTanaModules(MekanismModules.ENERGY_UNIT, MekanismModules.ATTACK_AMPLIFICATION_UNIT, MekanismModules.TELEPORTATION_UNIT);

        addMekaSuitModules(MekanismModules.ENERGY_UNIT, MekanismModules.COLOR_MODULATION_UNIT, MekanismModules.LASER_DISSIPATION_UNIT, MekanismModules.RADIATION_SHIELDING_UNIT);
        addMekaSuitHelmetModules(MekanismModules.ELECTROLYTIC_BREATHING_UNIT, MekanismModules.INHALATION_PURIFICATION_UNIT, MekanismModules.VISION_ENHANCEMENT_UNIT, MekanismModules.NUTRITIONAL_INJECTION_UNIT);
        addMekaSuitBodyarmorModules(MekanismModules.JETPACK_UNIT, MekanismModules.GRAVITATIONAL_MODULATING_UNIT, MekanismModules.CHARGE_DISTRIBUTION_UNIT, MekanismModules.DOSIMETER_UNIT, MekanismModules.GEIGER_UNIT, MekanismModules.ELYTRA_UNIT);
        addMekaSuitPantsModules(MekanismModules.LOCOMOTIVE_BOOSTING_UNIT, MekanismModules.GYROSCOPIC_STABILIZATION_UNIT, MekanismModules.HYDROSTATIC_REPULSOR_UNIT, MekanismModules.MOTORIZED_SERVO_UNIT);
        addMekaSuitBootsModules(MekanismModules.HYDRAULIC_PROPULSION_UNIT, MekanismModules.MAGNETIC_ATTRACTION_UNIT, MekanismModules.FROST_WALKER_UNIT);
        if (ModList.get().isLoaded("ad_astra_giselle_addon")) {
            addMekaSuitHelmetModules(AddonMekanismModules.SPACE_BREATHING_UNIT);
            addMekaSuitBodyarmorModules(AddonMekanismModules.SPACE_FIRE_PROOF_UNIT);
            addMekaSuitPantsModules(AddonMekanismModules.ACID_RAIN_PROOF_UNIT);
            addMekaSuitBootsModules(AddonMekanismModules.GRAVITY_NORMALIZING_UNIT);
        }
        if (ModList.get().isLoaded("mekanismgenerators")) {
            addMekaSuitHelmetModules(GeneratorsModules.SOLAR_RECHARGING_UNIT);
            addMekaSuitPantsModules(GeneratorsModules.GEOTHERMAL_GENERATOR_UNIT);
        }
    }

    public void processIMC(InterModProcessEvent event) {
        Map<ModuleData<?>, ImmutableSet.Builder<Item>> supportedContainersBuilderMap = new IdentityHashMap<>();
        mapSupportedModules(event, MekanismIMC.ADD_MEKA_TOOL_MODULES, MekanismItems.MEKA_TOOL, supportedContainersBuilderMap);
        mapSupportedModules(event, MekanismIMC.ADD_MEKA_SUIT_HELMET_MODULES, MekanismItems.MEKASUIT_HELMET, supportedContainersBuilderMap);
        mapSupportedModules(event, MekanismIMC.ADD_MEKA_SUIT_BODYARMOR_MODULES, MekanismItems.MEKASUIT_BODYARMOR, supportedContainersBuilderMap);
        mapSupportedModules(event, MekanismIMC.ADD_MEKA_SUIT_PANTS_MODULES, MekanismItems.MEKASUIT_PANTS, supportedContainersBuilderMap);
        mapSupportedModules(event, MekanismIMC.ADD_MEKA_SUIT_BOOTS_MODULES, MekanismItems.MEKASUIT_BOOTS, supportedContainersBuilderMap);
        mapSupportedModules(event, MekaWeapons.ADD_MEKA_BOW_MODULES, MekaWeapons.MEKA_BOW, supportedContainersBuilderMap);
        mapSupportedModules(event, MekaWeapons.ADD_MEKATANA_MODULES, MekaWeapons.MEKA_TANA, supportedContainersBuilderMap);
        for (Map.Entry<ModuleData<?>, ImmutableSet.Builder<Item>> entry : supportedContainersBuilderMap.entrySet()) {
            supportedContainers.put(entry.getKey(), entry.getValue().build());
        }

        Field modulesField;
        Field containersField;
        try {
            modulesField = ModuleHelper.class.getDeclaredField("supportedModules");
            containersField = ModuleHelper.class.getDeclaredField("supportedContainers");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        modulesField.setAccessible(true);
        containersField.setAccessible(true);
        try {
            modulesField.set(ModuleHelper.INSTANCE, supportedModules);
            containersField.set(ModuleHelper.INSTANCE, supportedContainers);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void mapSupportedModules(InterModProcessEvent event, String imcMethod, IItemProvider moduleContainer, Map<ModuleData<?>, ImmutableSet.Builder<Item>> supportedContainersBuilderMap) {
        ImmutableSet.Builder<ModuleData<?>> supportedModulesBuilder = ImmutableSet.builder();
        event.getIMCStream(imcMethod::equals).forEach(message -> {
            Object body = message.messageSupplier().get();
            if (body instanceof IModuleDataProvider<?> moduleDataProvider) {
                supportedModulesBuilder.add(moduleDataProvider.getModuleData());
                Mekanism.logger.debug("Weapons: Received IMC message '{}' from '{}' for module '{}'.", imcMethod, message.senderModId(), moduleDataProvider.getRegistryName());
            } else if (body instanceof IModuleDataProvider<?>[] providers) {
                for (IModuleDataProvider<?> moduleDataProvider : providers) {
                    supportedModulesBuilder.add(moduleDataProvider.getModuleData());
                    Mekanism.logger.debug("Weapons: Received IMC message '{}' from '{}' for module '{}'.", imcMethod, message.senderModId(), moduleDataProvider.getRegistryName());
                }
            } else {
                Mekanism.logger.warn("Weapons: Received IMC message for '{}' from mod '{}' with an invalid body.", imcMethod, message.senderModId());
            }
        });
        Set<ModuleData<?>> supported = supportedModulesBuilder.build();
        if (!supported.isEmpty()) {
            Item item = moduleContainer.asItem();
            supportedModules.put(item, supported);
            for (ModuleData<?> data : supported) {
                supportedContainersBuilderMap.computeIfAbsent(data, d -> ImmutableSet.builder()).add(item);
            }
        }
    }

    private static void sendModuleIMC(String method, IModuleDataProvider<?>... moduleDataProviders) {
        if (moduleDataProviders == null || moduleDataProviders.length == 0) {
            throw new IllegalArgumentException("No module data providers given.");
        }
        InterModComms.sendTo(MekaWeapons.MODID, method, () -> moduleDataProviders);
    }

    public static void addMekaBowModules(IModuleDataProvider<?>... moduleDataProviders) {
        sendModuleIMC(MekaWeapons.ADD_MEKA_BOW_MODULES, moduleDataProviders);
    }
    public static void addMekaTanaModules(IModuleDataProvider<?>... moduleDataProviders) {
        sendModuleIMC(MekaWeapons.ADD_MEKATANA_MODULES, moduleDataProviders);
    }
    public static void addMekaSuitModules(IModuleDataProvider<?>... moduleDataProviders) {
        addMekaSuitHelmetModules(moduleDataProviders);
        addMekaSuitBodyarmorModules(moduleDataProviders);
        addMekaSuitPantsModules(moduleDataProviders);
        addMekaSuitBootsModules(moduleDataProviders);
    }
    public static void addMekaToolModules(IModuleDataProvider<?>... moduleDataProviders) {
        sendModuleIMC(MekanismIMC.ADD_MEKA_TOOL_MODULES, moduleDataProviders);
    }
    public static void addMekaSuitHelmetModules(IModuleDataProvider<?>... moduleDataProviders) {
        sendModuleIMC(MekanismIMC.ADD_MEKA_SUIT_HELMET_MODULES, moduleDataProviders);
    }
    public static void addMekaSuitBodyarmorModules(IModuleDataProvider<?>... moduleDataProviders) {
        sendModuleIMC(MekanismIMC.ADD_MEKA_SUIT_BODYARMOR_MODULES, moduleDataProviders);
    }
    public static void addMekaSuitPantsModules(IModuleDataProvider<?>... moduleDataProviders) {
        sendModuleIMC(MekanismIMC.ADD_MEKA_SUIT_PANTS_MODULES, moduleDataProviders);
    }
    public static void addMekaSuitBootsModules(IModuleDataProvider<?>... moduleDataProviders) {
        sendModuleIMC(MekanismIMC.ADD_MEKA_SUIT_BOOTS_MODULES, moduleDataProviders);
    }
}
