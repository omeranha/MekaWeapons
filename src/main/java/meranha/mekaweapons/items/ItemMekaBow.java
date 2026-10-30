package meranha.mekaweapons.items;

import static meranha.mekaweapons.MekaWeaponsUtils.*;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.chemical.IChemicalTank;
import mekanism.api.gear.ModuleData;
import mekanism.api.text.TextComponentUtil;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import mekanism.common.attachments.IAttachmentAware;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.containers.chemical.ChemicalTanksBuilder;
import mekanism.common.attachments.containers.chemical.ComponentBackedChemicalTank;
import mekanism.common.capabilities.chemical.item.ChemicalTankSpec;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismChemicals;
import meranha.mekaweapons.client.WeaponsLang;
import meranha.mekaweapons.items.modules.ChemicalArrowEffect;
import meranha.mekaweapons.items.modules.DrawSpeedUnit;
import meranha.mekaweapons.items.modules.EnchantmentUnit;
import meranha.mekaweapons.items.modules.WeaponsModules;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.IRadialModuleContainerItem;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.ModuleContainer;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.util.StorageUtils;
import meranha.mekaweapons.MekaWeapons;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

public class ItemMekaBow extends BowItem implements IRadialModuleContainerItem, IAttachmentAware {
    private static final ResourceLocation RADIAL_ID = MekaWeapons.rl("meka_bow");
    private static final float VANILLA_DRAW_TICKS = 20.0F;

    private final List<ChemicalTankSpec> chemicalTankSpecs = new ArrayList<>();

    public ItemMekaBow(@NotNull Properties properties) {
        super(IModuleHelper.INSTANCE.applyModuleContainerProperties(properties.rarity(Rarity.EPIC).setNoRepair().stacksTo(1)));
        chemicalTankSpecs.add(ChemicalTankSpec.createFillOnly(MekaWeapons.general.chemicalArrowsTransferRate, MekaWeapons.general.chemicalArrowsCapacity,
                chemical -> chemical.is(MekanismChemicals.HYDROGEN) || chemical.is(MekanismChemicals.SULFURIC_ACID)
                        || chemical.is(MekanismChemicals.HYDROGEN_CHLORIDE) || chemical.is(MekanismChemicals.WATER_VAPOR),
                stack -> hasModule(stack, WeaponsModules.CHEMICALARROW_UNIT)));
    }

    @Override
    public void onDestroyed(@NotNull ItemEntity item, @NotNull DamageSource damageSource) {
        ModuleHelper.INSTANCE.dropModuleContainerContents(item, damageSource);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!level.isClientSide() && entity instanceof Player player) {
            ModuleContainer container = ModuleHelper.get().getModuleContainer(stack);
            if (container != null) {
                for (Module<?> module : container.modules()) {
                    module.tick(container, stack, player);
                }
            }
        }
    }

    @Override
    public void addModuleDetails(ItemStack stack, List<Component> tooltip) {
        for (IModule<?> module : this.getModules(stack)) {
            ModuleData<?> data = module.getUntypedData();
            if (module.getInstalledCount() > 1) {
                Component amount = MekanismLang.GENERIC_FRACTION.translate(module.getInstalledCount(), data.getMaxStackSize());
                tooltip.add(MekanismLang.GENERIC_WITH_PARENTHESIS.translateColored(EnumColor.GRAY, data, amount));
            } else {
                tooltip.add(TextComponentUtil.build(EnumColor.GRAY, data));
                if (module.getCustomInstance() instanceof EnchantmentUnit(ItemEnchantments enchantments)) {
                    for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
                        Component name = Enchantment.getFullname(entry.getKey(), entry.getIntValue());
                        tooltip.add(TextComponentUtil.build(EnumColor.DARK_GRAY, Component.literal(" ").append(name)));
                    }
                }
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
            addModuleDetails(stack, tooltip);
            return;
        }

        StorageUtils.addStoredEnergy(stack, tooltip, true);
        if (isModuleInstalled(stack, WeaponsModules.CHEMICALARROW_UNIT)) {
            StorageUtils.addStoredChemical(stack, tooltip);
        }
        tooltip.add(MekanismLang.HOLD_FOR_MODULES.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
    }

    @Override
    public void adjustAttributes(@NotNull ItemAttributeModifierEvent event) {
        long totalDamage = getTotalDamage(event.getItemStack());
        event.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, totalDamage, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        IRadialModuleContainerItem.super.adjustAttributes(event);
    }

    @Override
    public void onUseTick(@NotNull Level world, @NotNull LivingEntity player, @NotNull ItemStack stack, int timeLeft) {
        // ">=" instead of "==" because anything shortening the use duration can make the elapsed time skip over the draw time
        if (player.isAlive() && isModuleEnabled(stack, WeaponsModules.AUTOFIRE_UNIT) && getUseDuration(stack, player) - timeLeft >= getUseTick(stack)) {
            player.stopUsingItem();
            stack.releaseUsing(world, player, 0);
            player.startUsingItem(player.getUsedItemHand());
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack bow = player.getItemInHand(hand);
        if (world.isClientSide && isEnergyInsufficient(bow)) {
            return InteractionResultHolder.fail(bow);
        }

        boolean hasAmmo = !player.getProjectile(bow).isEmpty() || isModuleEnabled(bow, WeaponsModules.ARROWENERGY_UNIT);
        InteractionResultHolder<ItemStack> ret = EventHooks.onArrowNock(bow, world, player, hand, hasAmmo);
        if (ret != null) {
            return ret;
        } else if (!player.hasInfiniteMaterials() && !hasAmmo) {
            return InteractionResultHolder.fail(bow);
        } else {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(bow);
        }
    }

    @Override
    public void releaseUsing(@NotNull ItemStack bow, @NotNull Level world, @NotNull LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player) || (!player.isCreative() && isEnergyInsufficient(bow)) || !(bow.getItem() instanceof ProjectileWeaponItem projectileWeaponItem)) {
            return;
        }

        int charge = EventHooks.onArrowLoose(bow, world, player, this.getUseDuration(bow, entity) - timeLeft, true);
        if (charge < 0) {
            return;
        }

        // the Draw Speed Unit shortens the draw time, so the charge has to be scaled back to the vanilla draw time to keep full power shots
        float power = getPowerForTime(Math.round(charge * VANILLA_DRAW_TICKS / getUseTick(bow)));
        if (power < 0.1) {
            return;
        }

        ItemStack ammoStack = projectileWeaponItem.getDefaultCreativeAmmo(player, bow);
        List<ItemStack> list = draw(bow, ammoStack, player);
        if (world instanceof ServerLevel serverlevel) {
            if (!list.isEmpty()) {
                this.shoot(serverlevel, player, player.getUsedItemHand(), bow, list, power * 3.0F, 1.0F, power == 1.0F, null);
            }
        }

        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F / (world.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F);
        player.awardStat(Stats.ITEM_USED.get(this));
    }

    @Override
    protected void shoot(@NotNull ServerLevel world, @NotNull LivingEntity entity, @NotNull InteractionHand hand, @NotNull ItemStack bow, @NotNull List<ItemStack> potentialAmmo, float velocity, float inaccuracy, boolean critical, @Nullable LivingEntity target) {
        if (!(entity instanceof Player player)) {
            return;
        }

        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(bow, 0);
        if (!player.isCreative() && energyContainer != null) {
            long energyNeeded = getEnergyNeeded(bow);
            energyContainer.extract(energyNeeded, Action.EXECUTE, AutomationType.MANUAL);
        }
        super.shoot(world, entity, hand, bow, potentialAmmo, velocity, inaccuracy, critical, target);
    }

    @NotNull
    @Override
    public AbstractArrow customArrow(@NotNull AbstractArrow arrow, @NotNull ItemStack projectileStack, @NotNull ItemStack weaponStack) {
        long totalDamage = getTotalDamage(weaponStack);
        MekaArrowEntity mekaArrow = new MekaArrowEntity(arrow.level(), arrow, projectileStack, weaponStack, MathUtils.clampToInt(totalDamage));
        mekaArrow.setChemicalEffect(consumeChemical(arrow.level(), weaponStack));
        return mekaArrow;
    }

    @Override
    public void attachAttachments(IEventBus eventBus) {
        if (!chemicalTankSpecs.isEmpty()) {
            ContainerType.CHEMICAL.addDefaultCreators(eventBus, this, () -> {
                ChemicalTanksBuilder builder = ChemicalTanksBuilder.builder();
                for (ChemicalTankSpec spec : chemicalTankSpecs) {
                    spec.addTank(builder, ComponentBackedChemicalTank::new);
                }
                return builder.build();
            }, MekanismConfig.gear);
        }
    }

    /**
     * Drains one shot worth of chemical from the bow and returns the effect the arrow should apply, or null when the
     * Chemical Arrows Unit is missing, disabled or out of chemical.
     */
    @Nullable
    private ChemicalArrowEffect consumeChemical(Level level, ItemStack bow) {
        if (level.isClientSide() || !isModuleEnabled(bow, WeaponsModules.CHEMICALARROW_UNIT)) {
            return null;
        }

        IChemicalTank tank = getChemicalTank(bow);
        if (tank == null || tank.isEmpty()) {
            return null;
        }

        ChemicalArrowEffect effect = ChemicalArrowEffect.byStack(tank.getStack());
        if (effect == null) {
            return null;
        }

        long usage = MekaWeapons.general.chemicalArrowsUsage.get();
        if (tank.extract(usage, Action.SIMULATE, AutomationType.MANUAL).getAmount() < usage) {
            return null;
        }

        tank.extract(usage, Action.EXECUTE, AutomationType.MANUAL);
        return effect;
    }

    @Override
    public void addHUDStrings(List<Component> list, Player player, ItemStack stack, EquipmentSlot slotType) {
        IChemicalTank tank = getChemicalTank(stack);
        if (tank != null && isModuleEnabled(stack, WeaponsModules.CHEMICALARROW_UNIT)) {
            if (tank.isEmpty()) {
                list.add(WeaponsLang.CHEMICAL_ARROWS_EMPTY.translateColored(EnumColor.DARK_GRAY, EnumColor.RED, MekanismLang.NONE.translate()));
            } else {
                list.add(WeaponsLang.CHEMICAL_ARROWS.translateColored(EnumColor.DARK_GRAY, EnumColor.INDIGO, tank.getStack().getTextComponent(), EnumColor.AQUA, tank.getStored()));
            }
        }
    }

    @Override
    public @NotNull ItemEnchantments getAllEnchantments(@NotNull ItemStack stack, @NotNull HolderLookup.RegistryLookup<Enchantment> lookup) {
        return mergeModuleEnchantments(stack, super.getAllEnchantments(stack, lookup));
    }

    @Override
    public boolean shouldCauseReequipAnimation(@NotNull ItemStack oldStack, @NotNull ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        return StorageUtils.getEnergyBarWidth(stack);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return getBarCustomColor(stack);
    }

    @Override
    public boolean shouldCauseBlockBreakReset(@NotNull ItemStack oldStack, @NotNull ItemStack newStack) {
        return oldStack.getItem() != newStack.getItem();
    }

    public float getUseTick(@NotNull ItemStack stack) {
        float useTick = VANILLA_DRAW_TICKS;
        IModule<DrawSpeedUnit> drawSpeedUnit = getEnabledModule(stack, WeaponsModules.DRAWSPEED_UNIT);
        if (drawSpeedUnit != null) {
            useTick -= 5.0f * drawSpeedUnit.getCustomInstance().getDrawSpeed();
        }
        return Math.max(1.0F, useTick);
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
        return MekaWeapons.general.mekaBowEnchantments.get();
    }

    @Override
    public boolean isBookEnchantable(@NotNull ItemStack stack, @NotNull ItemStack book) {
        return MekaWeapons.general.mekaBowEnchantments.get();
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return false;
    }

    public ResourceLocation getRadialIdentifier() {
        return RADIAL_ID;
    }

    public List<ChemicalTankSpec> getChemicalTankSpecs() {
        return chemicalTankSpecs;
    }
}
