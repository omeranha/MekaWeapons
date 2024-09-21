package meranha.mekaweapons;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import mekanism.api.MekanismIMC;
import mekanism.api.gear.config.ModuleEnumConfig;
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
import mekanism.common.registration.impl.EntityTypeDeferredRegister;
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
import meranha.mekaweapons.items.ModuleWeaponAttackAmplificationUnit.AttackDamage;
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

    public static final ModuleDeferredRegister MODULES =  new ModuleDeferredRegister(MekaWeapons.MODID);
    public static final ModuleRegistryObject<?> ARROWENERGY_UNIT = MODULES.registerMarker("arrowenergy_unit", () -> MekaWeapons.MODULE_ARROWENERGY.asItem());
    public static final ModuleRegistryObject<?> AUTOFIRE_UNIT = MODULES.registerMarker("autofire_unit", () -> MekaWeapons.MODULE_AUTOFIRE.asItem());
    public static final ModuleRegistryObject<?> DRAWSPEED_UNIT = MODULES.registerMarker("drawspeed_unit", () -> MekaWeapons.MODULE_DRAWSPEED.asItem(), builder -> builder.maxStackSize(3));
    public static final ModuleRegistryObject<?> GRAVITYDAMPENER_UNIT = MODULES.registerMarker("gravitydampener_unit", () -> MekaWeapons.MODULE_GRAVITYDAMPENER.asItem());
    //public static final ModuleRegistryObject<?> ARROWVELOCITY_UNIT = MODULES.registerMarker("arrowvelocity_unit", () -> MekaWeapons.MODULE_ARROWVELOCITY.asItem(), builder -> builder.maxStackSize(8));
    public static final ModuleRegistryObject<ModuleWeaponAttackAmplificationUnit> ATTACKAMPLIFICATION_UNIT = MODULES.register("attackamplification_unit",
            ModuleWeaponAttackAmplificationUnit::new, () -> MekaWeapons.MODULE_ATTACKAMPLIFICATION.asItem(), builder -> builder.maxStackSize(AttackDamage.values().length - 2).handlesModeChange().rendersHUD()
                    .addInstalledCountConfig(
                            installed -> ModuleEnumConfig.createBounded(ModuleWeaponAttackAmplificationUnit.ATTACK_DAMAGE, AttackDamage.MED, installed + 2),
                            installed -> ModuleEnumConfig.codec(AttackDamage.CODEC, AttackDamage.class, installed + 2),
                            installed -> ModuleEnumConfig.streamCodec(AttackDamage.STREAM_CODEC, AttackDamage.class, installed + 2)
                    )
    );

    public static final ItemDeferredRegister ITEMS = new ItemDeferredRegister(MekaWeapons.MODID);
    public static final ItemRegistryObject<ItemMekaTana> MEKA_TANA = ITEMS.registerUnburnable("meka_tana", ItemMekaTana::new)
    .addAttachedContainerCapabilities(ContainerType.ENERGY, () -> EnergyContainersBuilder.builder()
        .addContainer((type, attachedTo, containerIndex) -> new ComponentBackedNoClampEnergyContainer(attachedTo, containerIndex, BasicEnergyContainer.manualOnly,
            BasicEnergyContainer.alwaysTrue, () -> ModuleEnergyUnit.getChargeRate(attachedTo, MekaWeapons.general.mekaTanaBaseChargeRate),
            () -> ModuleEnergyUnit.getEnergyCapacity(attachedTo, MekaWeapons.general.mekaTanaBaseEnergyCapacity)))
    .build(), MekaWeapons.general);

    public static final ItemRegistryObject<ItemMekaBow> MEKA_BOW = ITEMS.registerUnburnable("meka_bow", ItemMekaBow::new)
    .addAttachedContainerCapabilities(ContainerType.ENERGY, () -> EnergyContainersBuilder.builder()
          .addContainer((type, attachedTo, containerIndex) -> new ComponentBackedNoClampEnergyContainer(attachedTo, containerIndex, BasicEnergyContainer.manualOnly,
                BasicEnergyContainer.alwaysTrue, () -> ModuleEnergyUnit.getChargeRate(attachedTo, MekaWeapons.general.mekaBowBaseChargeRate),
                () -> ModuleEnergyUnit.getEnergyCapacity(attachedTo, MekaWeapons.general.mekaBowBaseEnergyCapacity)))
    .build(), MekaWeapons.general);

    public static final ItemRegistryObject<ItemMagnetizer> MAGNETIZER = ITEMS.registerUnburnable("magnetizer", ItemMagnetizer::new);
    public static final ItemRegistryObject<Item> KATANA_BLADE = ITEMS.register("katana_blade");
    public static final ItemRegistryObject<Item> BOW_RISER = ITEMS.register("bow_riser");
    public static final ItemRegistryObject<Item> BOW_LIMB = ITEMS.register("bow_limb");
    public static final ItemRegistryObject<ItemModule> MODULE_ARROWENERGY = ITEMS.registerModule(MekaWeapons.ARROWENERGY_UNIT, Rarity.RARE);
    public static final ItemRegistryObject<ItemModule> MODULE_AUTOFIRE = ITEMS.registerModule(MekaWeapons.AUTOFIRE_UNIT, Rarity.RARE);
    public static final ItemRegistryObject<ItemModule> MODULE_DRAWSPEED = ITEMS.registerModule(MekaWeapons.DRAWSPEED_UNIT, Rarity.RARE);
    public static final ItemRegistryObject<ItemModule> MODULE_GRAVITYDAMPENER = ITEMS.registerModule(MekaWeapons.GRAVITYDAMPENER_UNIT, Rarity.EPIC);
    //public static final ItemRegistryObject<ItemModule> MODULE_ARROWVELOCITY = ITEMS.registerModule(MekaWeapons.ARROWVELOCITY_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_ATTACKAMPLIFICATION = ITEMS.registerModule(MekaWeapons.ATTACKAMPLIFICATION_UNIT, Rarity.UNCOMMON);
    // todo add looting for meka-tana?

    public static final EntityTypeDeferredRegister ENTITY_TYPES = new EntityTypeDeferredRegister(MODID);
    public static final DeferredHolder<EntityType<?>, EntityType<MekaArrowEntity>> MEKA_ARROW = ENTITY_TYPES.register("meka_arrow", () -> EntityType.Builder.<MekaArrowEntity>of(MekaArrowEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(20).build(MODID + ":meka_arrow"));

    public MekaWeapons(IEventBus modEventBus, ModContainer modContainer) {
        MekaWeapons.ITEMS.register(modEventBus);
        MekaWeapons.MODULES.register(modEventBus);
        MekaWeapons.ENTITY_TYPES.register(modEventBus);
        MekanismConfigHelper.registerConfig(KNOWN_CONFIGS, modContainer, general);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::buildCreativeModeTabContents);
        modEventBus.addListener(this::sendCustomModules);
        modEventBus.addListener(this::registerRenderers);
        NeoForge.EVENT_BUS.addListener(this::mekaBowEnergyArrows);
        NeoForge.EVENT_BUS.addListener(this::disableMekaBowAttack);
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

    private void commonSetup(FMLCommonSetupEvent event) {
        MekaWeapons.logger.info("Loaded 'Mekanism: Weapons' module.");
    }

    private void buildCreativeModeTabContents(@NotNull BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == MekanismCreativeTabs.MEKANISM.get()) {
            ITEMS.getEntries().forEach(entry -> event.accept(entry.get()));
        }
    }

    private void sendCustomModules(InterModEnqueueEvent event) {
        final String ADD_MEKA_TANA_MODULES = "add_meka_tana_modules";
        final String ADD_MEKA_BOW_MODULES = "add_meka_bow_modules";
        MekanismIMC.addModuleContainer(MekaWeapons.MEKA_TANA, ADD_MEKA_TANA_MODULES);
        MekanismIMC.addModuleContainer(MekaWeapons.MEKA_BOW, ADD_MEKA_BOW_MODULES);
        MekanismIMC.sendModuleIMC(ADD_MEKA_TANA_MODULES, MekanismModules.ENERGY_UNIT, ATTACKAMPLIFICATION_UNIT, MekanismModules.TELEPORTATION_UNIT);
        MekanismIMC.sendModuleIMC(ADD_MEKA_BOW_MODULES, MekanismModules.ENERGY_UNIT, ATTACKAMPLIFICATION_UNIT, AUTOFIRE_UNIT, ARROWENERGY_UNIT, DRAWSPEED_UNIT, GRAVITYDAMPENER_UNIT);
    }

    private void mekaBowEnergyArrows(final @NotNull LivingGetProjectileEvent event) {
        if (!(event.getEntity() instanceof Player player) || !(player.level() instanceof ServerLevel)) {
            return;
        }

        ItemStack stack = event.getProjectileWeaponItemStack();
        if (stack.getItem() instanceof ProjectileWeaponItem bow && stack.getItem() instanceof ItemMekaBow mekaBow) {
            if (mekaBow.isModuleEnabled(stack, ARROWENERGY_UNIT)) {
                ItemStack defaultCreativeAmmo = bow.getDefaultCreativeAmmo(player, stack);
                event.setProjectileItemStack(defaultCreativeAmmo);
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

    public void registerRenderers(@NotNull RegisterRenderers event) {
        event.registerEntityRenderer(MekaWeapons.MEKA_ARROW.get(), MekaArrowRenderer::new);
    }

    @EventBusSubscriber(modid = MekaWeapons.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            if (ModList.get().isLoaded("curios")) {
                CuriosRendererRegistry.register(MekaWeapons.MAGNETIZER.get(), WeaponsRenderer::new);
            }

            event.enqueueWork(() -> {
                ClientRegistrationUtil.setPropertyOverride(MekaWeapons.MEKA_BOW, Mekanism.rl("pull"), (stack, world, entity, seed) -> {
                    if (entity != null && entity.getUseItem() == stack && stack.getItem() instanceof ItemMekaBow bow) {
                        return (stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) / bow.getUseTick(stack);
                    }
                    return 0;
                });
                ClientRegistrationUtil.setPropertyOverride(MekaWeapons.MEKA_BOW, Mekanism.rl("pulling"), (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            });
        }
    }
}
