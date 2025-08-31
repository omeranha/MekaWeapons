package meranha.mekaweapons.items.modules;

import mekanism.common.registration.impl.ModuleDeferredRegister;
import mekanism.common.registration.impl.ModuleRegistryObject;
import meranha.mekaweapons.MekaWeapons;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Enchantments;

@SuppressWarnings({"Convert2MethodRef", "FunctionalExpressionCanBeFolded"})
public class WeaponsModules {
    private WeaponsModules() {
    }

    public static final ModuleDeferredRegister MODULES =  new ModuleDeferredRegister(MekaWeapons.MODID);

    public static final ModuleRegistryObject<?> LOOTING_UNIT = MODULES.registerEnchantBased("looting_unit", () -> Enchantments.MOB_LOOTING, () -> MekaWeapons.MODULE_LOOTING.asItem(), builder -> builder.maxStackSize(5));
    public static final ModuleRegistryObject<DrawSpeedUnit> DRAWSPEED_UNIT = MODULES.register("drawspeed_unit",
            DrawSpeedUnit::new, () -> MekaWeapons.MODULE_DRAWSPEED.asItem(), builder -> builder.maxStackSize(3).rarity(Rarity.UNCOMMON).rendersHUD().handlesModeChange()
    );
    public static final ModuleRegistryObject<?> SWEEPING_UNIT = MODULES.registerMarker("sweeping_unit", () -> MekaWeapons.MODULE_SWEEPING.asItem(), builder -> builder.rarity(Rarity.RARE));


    public static final ModuleRegistryObject<?> ARROWENERGY_UNIT = MODULES.registerMarker("arrowenergy_unit", () -> MekaWeapons.MODULE_ARROWENERGY.asItem(), builder -> builder.rarity(Rarity.RARE));
    public static final ModuleRegistryObject<?> AUTOFIRE_UNIT = MODULES.registerMarker("autofire_unit", () -> MekaWeapons.MODULE_AUTOFIRE.asItem(), builder -> builder.rarity(Rarity.RARE));
    public static final ModuleRegistryObject<?> GRAVITYDAMPENER_UNIT = MODULES.registerMarker("gravitydampener_unit", () -> MekaWeapons.MODULE_GRAVITYDAMPENER.asItem(), builder -> builder.rarity(Rarity.EPIC));
    //public static final ModuleRegistryObject<?> ARROWVELOCITY_UNIT = MODULES.registerMarker("arrowvelocity_unit", () -> MekaWeapons.MODULE_ARROWVELOCITY.asItem(), builder -> builder.maxStackSize(8).rarity(Rarity.RARE));
    public static final ModuleRegistryObject<WeaponAttackAmplificationUnit> ATTACKAMPLIFICATION_UNIT = MODULES.register("attackamplification_unit",
            WeaponAttackAmplificationUnit::new, () -> MekaWeapons.MODULE_ATTACKAMPLIFICATION.asItem(), builder -> builder.maxStackSize(4).rarity(Rarity.UNCOMMON).rendersHUD().handlesModeChange()
    );

}
