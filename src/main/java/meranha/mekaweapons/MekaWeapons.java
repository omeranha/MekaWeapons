package meranha.mekaweapons;

import java.util.HashMap;
import java.util.Map;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.attachments.FrequencyAware;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.integration.curios.CuriosIntegration;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.registration.impl.*;
import meranha.mekaweapons.client.GuiMagnetizer;
import meranha.mekaweapons.client.MagnetizerContainer;
import meranha.mekaweapons.client.MekaArrowRenderer;
import meranha.mekaweapons.client.WeaponsRenderer;
import meranha.mekaweapons.items.*;
import meranha.mekaweapons.items.modules.*;
import net.minecraft.core.Holder;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.items.IItemHandler;
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
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.type.capability.ICurio;

@SuppressWarnings({"Convert2MethodRef", "unused", "forremoval"})
@Mod(MekaWeapons.MODID)
public class MekaWeapons {
    public static final String MODID = "mekaweapons";
    public static final Logger logger = LogUtils.getLogger();
    public static final WeaponsConfig general = new WeaponsConfig();
    private static final Map<IConfigSpec, IMekanismConfig> KNOWN_CONFIGS = new HashMap<>();

    public static final ItemDeferredRegister ITEMS = new ItemDeferredRegister(MekaWeapons.MODID);
    public static final ItemRegistryObject<ItemMekaTana> MEKA_TANA = ITEMS.registerUnburnable("meka_tana", ItemMekaTana::new)
    .addAttachedContainerCapabilities(ContainerType.ENERGY, () -> EnergyContainersBuilder.builder()
        .addContainer((type, attachedTo, containerIndex) -> new ComponentBackedNoClampEnergyContainer(attachedTo, containerIndex, BasicEnergyContainer.manualOnly,
            ConstantPredicates.alwaysTrue(), () -> ModuleEnergyUnit.getChargeRate(attachedTo, MekaWeapons.general.mekaTanaBaseChargeRate),
            () -> ModuleEnergyUnit.getEnergyCapacity(attachedTo, MekaWeapons.general.mekaTanaBaseEnergyCapacity)))
    .build(), MekaWeapons.general);
    public static final ItemRegistryObject<ItemMekaBow> MEKA_BOW = ITEMS.registerUnburnable("meka_bow", ItemMekaBow::new)
    .addAttachedContainerCapabilities(ContainerType.ENERGY, () -> EnergyContainersBuilder.builder()
          .addContainer((type, attachedTo, containerIndex) -> new ComponentBackedNoClampEnergyContainer(attachedTo, containerIndex, BasicEnergyContainer.manualOnly,
                  ConstantPredicates.alwaysTrue(), () -> ModuleEnergyUnit.getChargeRate(attachedTo, MekaWeapons.general.mekaBowBaseChargeRate),
                () -> ModuleEnergyUnit.getEnergyCapacity(attachedTo, MekaWeapons.general.mekaBowBaseEnergyCapacity)))
    .build(), MekaWeapons.general);

    public static final ItemRegistryObject<ItemMagnetizer> MAGNETIZER = ITEMS.registerUnburnable("magnetizer", ItemMagnetizer::new);
    public static final ItemRegistryObject<Item> KATANA_BLADE = ITEMS.register("katana_blade");
    public static final ItemRegistryObject<Item> BOW_RISER = ITEMS.register("bow_riser");
    public static final ItemRegistryObject<Item> BOW_LIMB = ITEMS.register("bow_limb");
    public static final ItemRegistryObject<ItemModule> MODULE_ARROWENERGY = ITEMS.registerModule(WeaponsModules.ARROWENERGY_UNIT, Rarity.RARE);
    public static final ItemRegistryObject<ItemModule> MODULE_AUTOFIRE = ITEMS.registerModule(WeaponsModules.AUTOFIRE_UNIT, Rarity.RARE);
    public static final ItemRegistryObject<ItemModule> MODULE_DRAWSPEED = ITEMS.registerModule(WeaponsModules.DRAWSPEED_UNIT, Rarity.RARE);
    public static final ItemRegistryObject<ItemModule> MODULE_GRAVITYDAMPENER = ITEMS.registerModule(WeaponsModules.GRAVITYDAMPENER_UNIT, Rarity.EPIC);
    //public static final ItemRegistryObject<ItemModule> MODULE_ARROWVELOCITY = ITEMS.registerModule(MekaWeapons.ARROWVELOCITY_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_ATTACKAMPLIFICATION = ITEMS.registerModule(WeaponsModules.ATTACKAMPLIFICATION_UNIT, Rarity.UNCOMMON);
    public static final ItemRegistryObject<ItemModule> MODULE_SWEEPING = ITEMS.registerModule(WeaponsModules.SWEEPING_UNIT, Rarity.UNCOMMON);
    public static final ItemRegistryObject<ItemModule> MODULE_LOOTING = ITEMS.registerModule(WeaponsModules.LOOTING_UNIT, Rarity.UNCOMMON);

    public static final EntityTypeDeferredRegister ENTITY_TYPES = new EntityTypeDeferredRegister(MODID);
    public static final DeferredHolder<EntityType<?>, EntityType<MekaArrowEntity>> MEKA_ARROW = ENTITY_TYPES.register("meka_arrow", () -> EntityType.Builder.<MekaArrowEntity>of(MekaArrowEntity::new,
            MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(20).build(MODID + ":meka_arrow"));

    public static final ContainerTypeDeferredRegister CONTAINER_TYPES = new ContainerTypeDeferredRegister(MODID);
    public static final ContainerTypeRegistryObject<MagnetizerContainer> MAGNETIZER_CONTAINER = CONTAINER_TYPES.register(MekaWeapons.MAGNETIZER, ItemMagnetizer.class, MagnetizerContainer::new);

    public MekaWeapons(IEventBus modEventBus, ModContainer modContainer) {
        MekaWeapons.ITEMS.register(modEventBus);
        WeaponsModules.MODULES.register(modEventBus);
        MekaWeapons.ENTITY_TYPES.register(modEventBus);
        MekaWeapons.CONTAINER_TYPES.register(modEventBus);
        MekanismConfigHelper.registerConfig(KNOWN_CONFIGS, modContainer, general);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::buildCreativeModeTabContents);
        modEventBus.addListener(this::sendCustomModules);
        modEventBus.addListener(this::registerRenderers);
        NeoForge.EVENT_BUS.addListener(this::mekaBowEnergyArrows);
        NeoForge.EVENT_BUS.addListener(this::disableMekaBowAttack);
        modEventBus.addListener(this::registerCapabilities);
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
        MekanismIMC.addModuleContainer((Holder<Item>)MekaWeapons.MEKA_TANA, ADD_MEKA_TANA_MODULES);
        MekanismIMC.addModuleContainer((Holder<Item>)MekaWeapons.MEKA_BOW, ADD_MEKA_BOW_MODULES);
        MekanismIMC.sendModuleIMC(ADD_MEKA_TANA_MODULES, MekanismModules.ENERGY_UNIT, WeaponsModules.ATTACKAMPLIFICATION_UNIT, MekanismModules.TELEPORTATION_UNIT,
                WeaponsModules.SWEEPING_UNIT, WeaponsModules.LOOTING_UNIT);
        MekanismIMC.sendModuleIMC(ADD_MEKA_BOW_MODULES, MekanismModules.ENERGY_UNIT, WeaponsModules.ATTACKAMPLIFICATION_UNIT, WeaponsModules.AUTOFIRE_UNIT,
                WeaponsModules.ARROWENERGY_UNIT, WeaponsModules.DRAWSPEED_UNIT, WeaponsModules.GRAVITYDAMPENER_UNIT, WeaponsModules.LOOTING_UNIT);
    }

    private void mekaBowEnergyArrows(final @NotNull LivingGetProjectileEvent event) {
        if (!(event.getEntity() instanceof Player player) || !(player.level() instanceof ServerLevel)) {
            return;
        }

        ItemStack stack = event.getProjectileWeaponItemStack();
        if (stack.getItem() instanceof ProjectileWeaponItem bow && stack.getItem() instanceof ItemMekaBow mekaBow && mekaBow.isModuleEnabled(stack, WeaponsModules.ARROWENERGY_UNIT)) {
            ItemStack defaultCreativeAmmo = bow.getDefaultCreativeAmmo(player, stack);
            event.setProjectileItemStack(defaultCreativeAmmo);
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

    public void registerCapabilities(final RegisterCapabilitiesEvent evt) {
        evt.registerItem(CuriosCapability.ITEM, (stack, context) -> new ICurio() {
                @Override
                public ItemStack getStack() {
                    return stack;
                }

                @Override
                public void curioTick(SlotContext slotContext) {
                    if (!(getStack().getItem() instanceof ItemMagnetizer magnetizer) || !(slotContext.entity() instanceof Player player)) return;

                    FrequencyAware<InventoryFrequency> frequencyAware = stack.get(magnetizer.getFrequencyComponent());
                    if (frequencyAware == null || !(frequencyAware.getFrequency(stack, magnetizer.getFrequencyComponent()) instanceof InventoryFrequency frequency)) return;
                    long toCharge = Math.min(MekaWeapons.general.wirelessChargerEnergyRate.get(), frequency.storedEnergy.getEnergy());
                    if (toCharge == 0L) return;
                    for (ItemStack stack : player.getInventory().items) {
                        toCharge = charge(frequency.storedEnergy, stack, toCharge);
                            if (toCharge == 0L) {
                                return;
                            }
                    }

                    IItemHandler handler = CuriosIntegration.getCuriosInventory(player);
                    if (handler != null) {
                        for (int slot = 0, slots = handler.getSlots(); slot < slots; slot++) {
                            toCharge = charge(frequency.storedEnergy, handler.getStackInSlot(slot), toCharge);
                            if (toCharge == 0L) {
                                return;
                            }
                        }
                    }
                }

                private long charge(IEnergyContainer energyContainer, ItemStack stack, long amount) {
                    if (!stack.isEmpty() && amount > 0L) {
                        IStrictEnergyHandler handler = EnergyCompatUtils.getStrictEnergyHandler(stack);
                        if (handler != null) {
                            long remaining = handler.insertEnergy(amount, Action.SIMULATE);
                            if (remaining < amount) {
                                long toExtract = amount - remaining;
                                long extracted = energyContainer.extract(toExtract, Action.EXECUTE, AutomationType.MANUAL);
                                long inserted = handler.insertEnergy(extracted, Action.EXECUTE);
                                return inserted + remaining;
                            }
                        }
                    }
                    return amount;
                }
        }, MekaWeapons.MAGNETIZER);
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
                ClientRegistrationUtil.setPropertyOverride(MekaWeapons.MEKA_BOW, Mekanism.rl("pulling"), (stack, world, entity, seed) ->
                        entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            });
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            ClientRegistrationUtil.registerScreen(event, MekaWeapons.MAGNETIZER_CONTAINER, GuiMagnetizer::new);
        }
    }
}
