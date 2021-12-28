package meranha.mekatana;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.gear.*;
import mekanism.api.gear.IHUDElement.HUDColor;
import mekanism.api.providers.IItemProvider;
import mekanism.api.providers.IModuleDataProvider;
import mekanism.common.Mekanism;
import mekanism.common.content.gear.HUDElement;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.Module;
import mekanism.common.item.ItemModule;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.common.util.text.TextUtils;
import meranha.mekatana.items.ModItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.InterModComms;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WeaponsModuleHelper implements IModuleHelper {

    public static final WeaponsModuleHelper WEAPONSINSTANCE = new WeaponsModuleHelper();

    private final Map<String, ModuleData<?>> legacyModuleLookup = new Object2ObjectOpenHashMap<>();
    private final Map<Item, Set<ModuleData<?>>> supportedModules = new Object2ObjectOpenHashMap<>(5);
    private final Map<ModuleData<?>, Set<Item>> supportedContainers = new Object2ObjectOpenHashMap<>();

    @Deprecated//TODO - 1.18: Remove this
    public void gatherLegacyModules() {
        for (ModuleData<?> moduleData : MekanismAPI.moduleRegistry()) {
            String legacyName = moduleData.getLegacyName();
            if (legacyName != null) {
                legacyModuleLookup.put(legacyName, moduleData);
            }
        }
    }

    public void processIMC() {
        Map<ModuleData<?>, ImmutableSet.Builder<Item>> supportedContainersBuilderMap = new Object2ObjectOpenHashMap<>();
        mapSupportedModules(WeaponsIMC.ADD_MEKA_TANA_MODULES, ModItems.MEKA_TANA, supportedContainersBuilderMap);
        mapSupportedModules(WeaponsIMC.ADD_MEKA_BOW_MODULES, ModItems.MEKA_BOW, supportedContainersBuilderMap);
        for (Map.Entry<ModuleData<?>, ImmutableSet.Builder<Item>> entry : supportedContainersBuilderMap.entrySet()) {
            supportedContainers.put(entry.getKey(), entry.getValue().build());
        }
    }

    private void mapSupportedModules(String imcMethod, IItemProvider moduleContainer, Map<ModuleData<?>, ImmutableSet.Builder<Item>> supportedContainersBuilderMap) {
        ImmutableSet.Builder<ModuleData<?>> supportedModulesBuilder = ImmutableSet.builder();
        InterModComms.getMessages(MekaWeapons.MOD_ID, imcMethod::equals).forEach(message -> {
            Object body = message.getMessageSupplier().get();
            if (body instanceof IModuleDataProvider) {
                IModuleDataProvider<?> moduleDataProvider = (IModuleDataProvider<?>) body;
                supportedModulesBuilder.add(moduleDataProvider.getModuleData());
                logDebugReceivedIMC(imcMethod, message.getSenderModId(), moduleDataProvider);
            } else if (body instanceof IModuleDataProvider[]) {
                for (IModuleDataProvider<?> moduleDataProvider : ((IModuleDataProvider<?>[]) body)) {
                    supportedModulesBuilder.add(moduleDataProvider.getModuleData());
                    logDebugReceivedIMC(imcMethod, message.getSenderModId(), moduleDataProvider);
                }
            } else {
                Mekanism.logger.warn("Received IMC message for '{}' from mod '{}' with an invalid body.", imcMethod, message.getSenderModId());
            }
        });
        Set<ModuleData<?>> supported = supportedModulesBuilder.build();
        if (!supported.isEmpty()) {
            Item item = moduleContainer.getItem();
            supportedModules.put(item, supported);
            for (ModuleData<?> data : supported) {
                supportedContainersBuilderMap.computeIfAbsent(data, d -> ImmutableSet.builder()).add(item);
            }
        }
    }

    private void logDebugReceivedIMC(String imcMethod, String senderModId, IModuleDataProvider<?> moduleDataProvider) {
        Mekanism.logger.debug("Received '{}' IMC message from '{}' for module ''{}.", imcMethod, senderModId, moduleDataProvider.getRegistryName());
    }

    @Override
    public ItemModule createModuleItem(IModuleDataProvider<?> moduleDataProvider, Item.Properties properties) {
        return new ItemModule(moduleDataProvider, properties);
    }

    @Override
    public Set<ModuleData<?>> getSupported(ItemStack container) {
        return supportedModules.getOrDefault(container.getItem(), Collections.emptySet());
    }

    @Override
    public Set<Item> getSupported(IModuleDataProvider<?> typeProvider) {
        return supportedContainers.getOrDefault(typeProvider.getModuleData(), Collections.emptySet());
    }

    @Override
    public boolean isEnabled(ItemStack container, IModuleDataProvider<?> typeProvider) {
        IModule<?> m = load(container, typeProvider);
        return m != null && m.isEnabled();
    }

    @Nullable
    @Override
    public <MODULE extends ICustomModule<MODULE>> Module<MODULE> load(ItemStack container, IModuleDataProvider<MODULE> typeProvider) {
        if (container.getItem() instanceof IModuleContainerItem) {
            CompoundNBT modulesTag = ItemDataUtils.getCompound(container, NBTConstants.MODULES);
            return load(container, typeProvider.getModuleData(), modulesTag, null);
        }
        return null;
    }

    @Override
    public List<Module<?>> loadAll(ItemStack container) {
        if (container.getItem() instanceof IModuleContainerItem) {
            List<Module<?>> modules = new ArrayList<>();
            CompoundNBT modulesTag = ItemDataUtils.getCompound(container, NBTConstants.MODULES);
            for (ModuleData<?> moduleType : loadAllTypes(modulesTag)) {
                Module<?> module = load(container, moduleType, modulesTag, null);
                if (module != null) {
                    modules.add(module);
                }
            }
            return modules;
        }
        return Collections.emptyList();
    }

    @Override
    public <MODULE extends ICustomModule<?>> List<Module<? extends MODULE>> loadAll(ItemStack container, Class<MODULE> moduleClass) {
        if (container.getItem() instanceof IModuleContainerItem) {
            List<Module<? extends MODULE>> modules = new ArrayList<>();
            CompoundNBT modulesTag = ItemDataUtils.getCompound(container, NBTConstants.MODULES);
            for (ModuleData<?> moduleType : loadAllTypes(modulesTag)) {
                Module<?> module = load(container, moduleType, modulesTag, moduleClass);
                if (module != null) {
                    modules.add((Module<? extends MODULE>) module);
                }
            }
            return modules;
        }
        return Collections.emptyList();
    }

    @Override
    public Collection<ModuleData<?>> loadAllTypes(ItemStack container) {
        if (container.getItem() instanceof IModuleContainerItem) {
            return loadAllTypes(ItemDataUtils.getCompound(container, NBTConstants.MODULES));
        }
        return Collections.emptyList();
    }

    private Set<ModuleData<?>> loadAllTypes(CompoundNBT modulesTag) {
        Set<ModuleData<?>> moduleTypes = new HashSet<>();
        for (String name : modulesTag.getAllKeys()) {
            ModuleData<?> moduleType = getModuleTypeFromName(name);
            if (moduleType != null) {
                moduleTypes.add(moduleType);
            }
        }
        return moduleTypes;
    }

    @Nullable
    private ModuleData<?> getModuleTypeFromName(String name) {
        ModuleData<?> legacy = legacyModuleLookup.get(name);
        if (legacy != null) {
            return legacy;
        }
        ResourceLocation registryName = ResourceLocation.tryParse(name);
        return registryName == null ? null : MekanismAPI.moduleRegistry().getValue(registryName);
    }

    @Nullable
    private <MODULE extends ICustomModule<MODULE>> Module<MODULE> load(ItemStack container, ModuleData<MODULE> type, CompoundNBT modulesTag, @Nullable Class<? extends ICustomModule<?>> typeFilter) {
        String registryName = type.getRegistryName().toString();
        if (modulesTag.contains(registryName, NBT.TAG_COMPOUND)) {
            return load(type, container, modulesTag, registryName, typeFilter);
        }
        String legacyName = type.getLegacyName();
        if (legacyName != null && modulesTag.contains(legacyName, NBT.TAG_COMPOUND)) {
            return load(type, container, modulesTag, legacyName, typeFilter);
        }
        return null;
    }

    @Nullable
    private <MODULE extends ICustomModule<MODULE>> Module<MODULE> load(ModuleData<MODULE> type, ItemStack container, CompoundNBT modulesTag, String key, @Nullable Class<? extends ICustomModule<?>> typeFilter) {
        Module<MODULE> module = new Module<>(type, container);
        if (typeFilter == null || typeFilter.isInstance(module.getCustomInstance())) {
            module.read(modulesTag.getCompound(key));
            return module;
        }
        return null;
    }

    @Override
    public IHUDElement hudElementEnabled(ResourceLocation icon, boolean enabled) {
        return hudElement(icon, OnOff.caps(enabled, false).getTextComponent(), enabled ? HUDColor.REGULAR : HUDColor.FADED);
    }

    @Override
    public IHUDElement hudElementPercent(ResourceLocation icon, double ratio) {
        return hudElement(icon, TextUtils.getPercent(ratio), ratio > 0.2 ? HUDColor.REGULAR : (ratio > 0.1 ? HUDColor.WARNING : HUDColor.DANGER));
    }

    @Override
    public IHUDElement hudElement(ResourceLocation icon, ITextComponent text, HUDColor color) {
        return HUDElement.of(icon, text, HUDElement.HUDColor.from(color));
    }
}
