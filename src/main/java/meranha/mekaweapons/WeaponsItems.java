package meranha.mekaweapons;

import mekanism.api.functions.ConstantPredicates;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.containers.energy.ComponentBackedNoClampEnergyContainer;
import mekanism.common.attachments.containers.energy.EnergyContainersBuilder;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.content.gear.shared.ModuleEnergyUnit;
import mekanism.common.item.ItemModule;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.registration.impl.ItemRegistryObject;
import meranha.mekaweapons.items.ItemMagnetizer;
import meranha.mekaweapons.items.ItemMekaBow;
import meranha.mekaweapons.items.ItemMekaGun;
import meranha.mekaweapons.items.ItemMekaTana;
import meranha.mekaweapons.items.modules.WeaponsModules;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;

public class WeaponsItems {
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

    public static final ItemRegistryObject<ItemMekaGun> MEKA_GUN = ITEMS.registerUnburnable("meka_gun", ItemMekaGun::new)
            .addAttachedContainerCapabilities(ContainerType.ENERGY, () -> EnergyContainersBuilder.builder()
                    .addContainer((type, attachedTo, containerIndex) -> new ComponentBackedNoClampEnergyContainer(attachedTo, containerIndex, BasicEnergyContainer.manualOnly,
                            ConstantPredicates.alwaysTrue(), () -> ModuleEnergyUnit.getChargeRate(attachedTo, MekaWeapons.general.mekaGunBaseChargeRate),
                            () -> ModuleEnergyUnit.getEnergyCapacity(attachedTo, MekaWeapons.general.mekaGunBaseEnergyCapacity)))
                    .build(), MekaWeapons.general);

    public static final ItemRegistryObject<ItemMagnetizer> MAGNETIZER = ITEMS.registerUnburnable("magnetizer", ItemMagnetizer::new);
    public static final ItemRegistryObject<Item> KATANA_BLADE = ITEMS.register("katana_blade");
    public static final ItemRegistryObject<Item> BOW_RISER = ITEMS.register("bow_riser");
    public static final ItemRegistryObject<Item> BOW_LIMB = ITEMS.register("bow_limb");
    public static final ItemRegistryObject<Item> FIBER_OPTIC_GLASS = ITEMS.register("fiber_optic_glass");
    public static final ItemRegistryObject<ItemModule> MODULE_ARROWENERGY = ITEMS.registerModule(WeaponsModules.ARROWENERGY_UNIT, Rarity.RARE);
    public static final ItemRegistryObject<ItemModule> MODULE_AUTOFIRE = ITEMS.registerModule(WeaponsModules.AUTOFIRE_UNIT, Rarity.RARE);
    public static final ItemRegistryObject<ItemModule> MODULE_DRAWSPEED = ITEMS.registerModule(WeaponsModules.DRAWSPEED_UNIT, Rarity.RARE);
    public static final ItemRegistryObject<ItemModule> MODULE_GRAVITYDAMPENER = ITEMS.registerModule(WeaponsModules.GRAVITYDAMPENER_UNIT, Rarity.EPIC);
    //public static final ItemRegistryObject<ItemModule> MODULE_ARROWVELOCITY = ITEMS.registerModule(MekaWeapons.ARROWVELOCITY_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_ATTACKAMPLIFICATION = ITEMS.registerModule(WeaponsModules.ATTACKAMPLIFICATION_UNIT, Rarity.UNCOMMON);
    public static final ItemRegistryObject<ItemModule> MODULE_SWEEPING = ITEMS.registerModule(WeaponsModules.SWEEPING_UNIT, Rarity.UNCOMMON);
    public static final ItemRegistryObject<ItemModule> MODULE_LOOTING = ITEMS.registerModule(WeaponsModules.LOOTING_UNIT, Rarity.UNCOMMON);
}
