package meranha.mekaweapons.items.modules;

import mekanism.api.gear.config.ModuleEnumConfig;
import mekanism.common.registration.impl.ModuleDeferredRegister;
import mekanism.common.registration.impl.ModuleRegistryObject;
import meranha.mekaweapons.MekaWeapons;

public class WeaponsModules {
    private WeaponsModules() {
    }

    public static final ModuleDeferredRegister MODULES =  new ModuleDeferredRegister(MekaWeapons.MODID);

    public static final ModuleRegistryObject<WeaponAttackAmplificationUnit> ATTACKAMPLIFICATION_UNIT = MODULES.register("attackamplification_unit", WeaponAttackAmplificationUnit::new, () -> MekaWeapons.MODULE_ATTACKAMPLIFICATION,
            builder -> builder.maxStackSize(4).handlesModeChange().rendersHUD().addInstalledCountConfig(
                    installed -> ModuleEnumConfig.createBounded(WeaponAttackAmplificationUnit.ATTACK_DAMAGE, WeaponAttackAmplificationUnit.AttackDamage.MED, installed + 2),
                    installed -> ModuleEnumConfig.codec(WeaponAttackAmplificationUnit.AttackDamage.CODEC, WeaponAttackAmplificationUnit.AttackDamage.class, installed + 2),
                    installed -> ModuleEnumConfig.streamCodec(WeaponAttackAmplificationUnit.AttackDamage.STREAM_CODEC, WeaponAttackAmplificationUnit.AttackDamage.class, installed + 2)
            )
    );
    public static final ModuleRegistryObject<LootingUnit> LOOTING_UNIT = MODULES.registerInstanced("looting_unit", LootingUnit::new, () -> MekaWeapons.MODULE_LOOTING, builder -> builder.maxStackSize(5));

    public static final ModuleRegistryObject<?> ARROWENERGY_UNIT = MODULES.registerMarker("arrowenergy_unit", () -> MekaWeapons.MODULE_ARROWENERGY,
            builder -> builder.handlesModeChange().rendersHUD());
    public static final ModuleRegistryObject<?> AUTOFIRE_UNIT = MODULES.registerMarker("autofire_unit", () -> MekaWeapons.MODULE_AUTOFIRE,
            builder -> builder.handlesModeChange().rendersHUD());
    public static final ModuleRegistryObject<DrawSpeedUnit> DRAWSPEED_UNIT = MODULES.register("drawspeed_unit", DrawSpeedUnit::new, () -> MekaWeapons.MODULE_DRAWSPEED,
            builder -> builder.maxStackSize(3).handlesModeChange().rendersHUD().addInstalledCountConfig(
                    installed -> ModuleEnumConfig.createBounded(DrawSpeedUnit.DRAWSPEED, DrawSpeedUnit.DrawSpeed.LOW, installed + 1),
                    installed -> ModuleEnumConfig.codec(DrawSpeedUnit.DrawSpeed.CODEC, DrawSpeedUnit.DrawSpeed.class, installed + 1),
                    installed -> ModuleEnumConfig.streamCodec(DrawSpeedUnit.DrawSpeed.STREAM_CODEC, DrawSpeedUnit.DrawSpeed.class, installed + 1)
            ));
    public static final ModuleRegistryObject<?> GRAVITYDAMPENER_UNIT = MODULES.registerMarker("gravitydampener_unit", () -> MekaWeapons.MODULE_GRAVITYDAMPENER);
    //public static final ModuleRegistryObject<?> ARROWVELOCITY_UNIT = MODULES.registerMarker("arrowvelocity_unit", () -> MekaWeapons.MODULE_ARROWVELOCITY.asItem(), builder -> builder.maxStackSize(8));

    public static final ModuleRegistryObject<?> SWEEPING_UNIT = MODULES.registerMarker("sweeping_unit", () -> MekaWeapons.MODULE_SWEEPING);
}
