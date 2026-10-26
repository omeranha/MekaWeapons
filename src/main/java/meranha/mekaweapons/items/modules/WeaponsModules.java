package meranha.mekaweapons.items.modules;

import mekanism.api.gear.config.ModuleBooleanConfig;
import mekanism.api.gear.config.ModuleEnumConfig;
import mekanism.common.registration.impl.ModuleDeferredRegister;
import mekanism.common.registration.impl.ModuleRegistryObject;
import meranha.mekaweapons.MekaWeapons;
import meranha.mekaweapons.WeaponsItems;
import net.minecraft.world.item.enchantment.Enchantments;

public class WeaponsModules {
    private WeaponsModules() {
    }

    public static final ModuleDeferredRegister MODULES =  new ModuleDeferredRegister(MekaWeapons.MODID);

    public static final ModuleRegistryObject<AdvancedAttackAmplificationUnit> ATTACKAMPLIFICATION_UNIT = MODULES.register("attackamplification_unit", AdvancedAttackAmplificationUnit::new, () -> WeaponsItems.MODULE_ATTACKAMPLIFICATION,
            builder -> builder.maxStackSize(MekaWeapons.startup.maxAttackAmplificationUnits.getOrDefault()).handlesModeChange().rendersHUD().addInstalledCountConfig(
                    installed -> ModuleEnumConfig.createBounded(AdvancedAttackAmplificationUnit.ATTACK_DAMAGE, AdvancedAttackAmplificationUnit.AttackDamage.MED, AdvancedAttackAmplificationUnit.boundFor(installed)),
                    installed -> ModuleEnumConfig.codec(AdvancedAttackAmplificationUnit.AttackDamage.CODEC, AdvancedAttackAmplificationUnit.AttackDamage.class, AdvancedAttackAmplificationUnit.boundFor(installed)),
                    installed -> ModuleEnumConfig.streamCodec(AdvancedAttackAmplificationUnit.AttackDamage.STREAM_CODEC, AdvancedAttackAmplificationUnit.AttackDamage.class, AdvancedAttackAmplificationUnit.boundFor(installed))
            )
    );
    public static final ModuleRegistryObject<LootingUnit> LOOTING_UNIT = MODULES.registerInstanced("looting_unit", LootingUnit::new, () -> WeaponsItems.MODULE_LOOTING, builder -> builder.maxStackSize(MekaWeapons.startup.maxLootingUnits.getOrDefault()));

    public static final ModuleRegistryObject<?> ARROWENERGY_UNIT = MODULES.registerMarker("arrowenergy_unit", () -> WeaponsItems.MODULE_ARROWENERGY,
            builder -> builder.handlesModeChange().rendersHUD());
    public static final ModuleRegistryObject<?> AUTOFIRE_UNIT = MODULES.registerMarker("autofire_unit", () -> WeaponsItems.MODULE_AUTOFIRE,
            builder -> builder.handlesModeChange().rendersHUD());
    public static final ModuleRegistryObject<DrawSpeedUnit> DRAWSPEED_UNIT = MODULES.register("drawspeed_unit", DrawSpeedUnit::new, () -> WeaponsItems.MODULE_DRAWSPEED,
            builder -> builder.maxStackSize(MekaWeapons.startup.maxDrawSpeedUnits.getOrDefault()).handlesModeChange().rendersHUD().addInstalledCountConfig(
                    installed -> ModuleEnumConfig.createBounded(DrawSpeedUnit.DRAWSPEED, DrawSpeedUnit.DrawSpeed.LOW, DrawSpeedUnit.boundFor(installed)),
                    installed -> ModuleEnumConfig.codec(DrawSpeedUnit.DrawSpeed.CODEC, DrawSpeedUnit.DrawSpeed.class, DrawSpeedUnit.boundFor(installed)),
                    installed -> ModuleEnumConfig.streamCodec(DrawSpeedUnit.DrawSpeed.STREAM_CODEC, DrawSpeedUnit.DrawSpeed.class, DrawSpeedUnit.boundFor(installed))
            ));
    public static final ModuleRegistryObject<?> GRAVITYDAMPENER_UNIT = MODULES.registerMarker("gravitydampener_unit", () -> WeaponsItems.MODULE_GRAVITYDAMPENER);
    //public static final ModuleRegistryObject<?> ARROWVELOCITY_UNIT = MODULES.registerMarker("arrowvelocity_unit", () -> MekaWeapons.MODULE_ARROWVELOCITY.asItem(), builder -> builder.maxStackSize(8));

    public static final ModuleRegistryObject<?> SWEEPING_UNIT = MODULES.registerMarker("sweeping_unit", () -> WeaponsItems.MODULE_SWEEPING);

    public static final ModuleRegistryObject<?> PUNCH_UNIT = MODULES.registerEnchantBased("punch_unit", Enchantments.PUNCH, () -> WeaponsItems.MODULE_PUNCH,
            builder -> builder.maxStackSize(MekaWeapons.startup.maxPunchUnits.getOrDefault()));
    public static final ModuleRegistryObject<?> FIREASPECT_UNIT = MODULES.registerEnchantBased("fireaspect_unit", Enchantments.FIRE_ASPECT, () -> WeaponsItems.MODULE_FIREASPECT,
            builder -> builder.maxStackSize(MekaWeapons.startup.maxFireAspectUnits.getOrDefault()));
    public static final ModuleRegistryObject<?> KNOCKBACK_UNIT = MODULES.registerEnchantBased("knockback_unit", Enchantments.KNOCKBACK, () -> WeaponsItems.MODULE_KNOCKBACK,
            builder -> builder.maxStackSize(MekaWeapons.startup.maxKnockbackUnits.getOrDefault()));

    public static final ModuleRegistryObject<?> COOLING_UNIT = MODULES.registerMarker("cooling_unit", () -> WeaponsItems.MODULE_COOLING,
            builder -> builder.maxStackSize(MekaWeapons.startup.maxCoolingUnits.getOrDefault()));
    public static final ModuleRegistryObject<ChemicalArrowUnit> CHEMICALARROW_UNIT = MODULES.register("chemicalarrow_unit", module -> new ChemicalArrowUnit(), () -> WeaponsItems.MODULE_CHEMICALARROW,
            builder -> builder.addConfig(ModuleBooleanConfig.create(ChemicalArrowUnit.DUMP, false)));

    public static final ModuleRegistryObject<EnchantmentUnit> ENCHANTMENT_UNIT = MODULES.register("enchantment_unit", EnchantmentUnit::new, () -> WeaponsItems.MODULE_ENCHANTMENT,
            builder -> builder.addConfig(ModuleEnchantmentsConfig.create(EnchantmentUnit.ENCHANTMENTS), ModuleEnchantmentsConfig.CODEC, ModuleEnchantmentsConfig.STREAM_CODEC));
}
