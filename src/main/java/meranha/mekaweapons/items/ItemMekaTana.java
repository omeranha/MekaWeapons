package meranha.mekaweapons.items;

import static meranha.mekaweapons.MekaWeaponsUtils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import mekanism.common.content.gear.shared.ModuleEnergyUnit;
import meranha.mekaweapons.items.modules.WeaponAttackAmplificationUnit;
import meranha.mekaweapons.items.modules.WeaponsModules;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Multimap;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.event.MekanismTeleportEvent;
import mekanism.api.gear.IModule;
import mekanism.api.math.FloatingLong;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.api.radial.mode.NestedRadialMode;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.mekatool.ModuleTeleportationUnit;
import mekanism.common.item.ItemEnergized;
import mekanism.common.lib.attribute.AttributeCache;
import mekanism.common.lib.radial.IGenericRadialModeItem;
import mekanism.common.lib.radial.data.NestingRadialData;
import mekanism.common.network.to_client.PacketPortalFX;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import meranha.mekaweapons.MekaWeapons;
import meranha.mekaweapons.MekaWeaponsUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.MinecraftForge;

public class ItemMekaTana extends ItemEnergized implements IModuleContainerItem, IGenericRadialModeItem {

    private static final ResourceLocation RADIAL_ID = MekaWeapons.rl("mekatana");
    private final Int2ObjectMap<AttributeCache> attributeCaches = new Int2ObjectArrayMap<>(WeaponAttackAmplificationUnit.AttackDamage.values().length - 2);

    public ItemMekaTana(@NotNull Properties properties) {
        super(MekaWeapons.general.mekaTanaBaseChargeRate, MekaWeapons.general.mekaTanaBaseEnergyCapacity, properties.rarity(Rarity.EPIC).setNoRepair().stacksTo(1));
    }

    public void appendHoverText(@NotNull ItemStack stack, @NotNull Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
            addModuleDetails(stack, tooltip);
            return;
        }

        StorageUtils.addStoredEnergy(stack, tooltip, true);
        tooltip.add(MekanismLang.HOLD_FOR_MODULES.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
    }

    @Override
    public boolean canPerformAction(ItemStack stack, net.minecraftforge.common.ToolAction toolAction) {
        if (isModuleEnabled(stack, WeaponsModules.SWEEPING_UNIT)) {
            return net.minecraftforge.common.ToolActions.DEFAULT_SWORD_ACTIONS.contains(toolAction);
        }
        return false;
    }

    public boolean hurtEnemy(@NotNull @Nonnull ItemStack stack, @NotNull @Nonnull LivingEntity target, @NotNull @Nonnull LivingEntity attacker) {
        if (attacker instanceof Player player && !player.isCreative()) {
            long energyNeeded = getEnergyNeeded(stack);
            IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
            if (energyContainer != null) {
                energyContainer.extract(FloatingLong.create(energyNeeded), Action.EXECUTE, AutomationType.MANUAL);
                return true;
            }
        }
        return false;
    }

    @NotNull
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@NotNull EquipmentSlot slot, @NotNull ItemStack stack) {
        if (slot == EquipmentSlot.MAINHAND) {
            long totalDamage = MekaWeaponsUtils.getTotalDamage(stack);
            return attributeCaches.computeIfAbsent((int) totalDamage, damage -> new AttributeCache(builder -> {
                builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier",
                        totalDamage, Operation.ADDITION));
                builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier",
                        MekaWeapons.general.mekaTanaAttackSpeed.get(), Operation.ADDITION));
            }, MekaWeapons.general.mekaTanaBaseDamage, MekaWeapons.general.mekaTanaAttackSpeed)).get();
        }
        return super.getAttributeModifiers(slot, stack);
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level world, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (world.isClientSide()) {
            return InteractionResultHolder.pass(stack);
        }

        IModule<ModuleTeleportationUnit> module = getEnabledModule(stack, MekanismModules.TELEPORTATION_UNIT);
        BlockHitResult result = MekanismUtils.rayTrace(player, MekaWeapons.general.mekaTanaMaxTeleportReach.get());
        if (module == null || module.getCustomInstance().requiresBlockTarget() && result.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(stack);
        }

        BlockPos pos = result.getBlockPos();
        double distance = player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ());
        BlockState state1 = world.getBlockState(pos.above());
        BlockState state2 = world.getBlockState(pos.above(2));
        if (!((state1.isAir() || MekanismUtils.isLiquidBlock(state1.getBlock())) && (state2.isAir() || MekanismUtils.isLiquidBlock(state2.getBlock()))) || distance < 5) {
            return InteractionResultHolder.pass(stack);
        }

        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        FloatingLong energyNeeded = MekaWeapons.general.mekaTanaTeleportUsage.get().multiply(distance / 10D);
        if (energyContainer == null || energyContainer.getEnergy().compareTo(energyNeeded) < 0) {
            return InteractionResultHolder.fail(stack);
        }

        final double targetX = pos.getX() + 0.5;
        final double targetY = pos.getY() + 1.5;
        final double targetZ = pos.getZ() + 0.5;

        MekanismTeleportEvent.MekaTool event = new MekanismTeleportEvent.MekaTool(player, targetX, targetY, targetZ, stack, result);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return InteractionResultHolder.fail(stack);
        }

        energyContainer.extract(energyNeeded, Action.EXECUTE, AutomationType.MANUAL);
        if (player.isPassenger()) {
            player.dismountTo(targetX, targetY, targetZ);
        } else {
            player.teleportTo(targetX, targetY, targetZ);
        }

        player.resetFallDistance();
        Mekanism.packetHandler().sendToAllTracking(new PacketPortalFX(pos.above()), world, pos);
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1, 1);
        return InteractionResultHolder.success(stack);
    }

    public int getBarColor(@NotNull ItemStack stack) {
        return getBarCustomColor(stack);
    }

    public boolean isEnchantable(@NotNull @Nonnull ItemStack stack) {
        return MekaWeapons.general.mekaTanaEnchantments.get();
    }

    public boolean isBookEnchantable(@NotNull ItemStack stack, @NotNull ItemStack book) {
        return isEnchantable(stack);
    }

    public boolean isFoil(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean supportsSlotType(ItemStack stack, @NotNull EquipmentSlot slotType) {
        return IGenericRadialModeItem.super.supportsSlotType(stack, slotType) && getModules(stack).stream().anyMatch(Module::handlesAnyModeChange);
    }

    @Override
    public void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, DisplayChange displayChange) {
        for (Module<?> module : getModules(stack)) {
            if (module.handlesModeChange()) {
                module.changeMode(player, stack, shift, displayChange);
                return;
            }
        }
    }

    @Nullable
    public RadialData<?> getRadialData(ItemStack stack) {
        List<NestedRadialMode> nestedModes = new ArrayList<>();
        Consumer<NestedRadialMode> adder = nestedModes::add;
        for (Module<?> module : getModules(stack)) {
            if (module.handlesRadialModeChange()) {
                module.addRadialModes(stack, adder);
            }
        }
        if (nestedModes.isEmpty()) {
            // No modes available, return that we don't actually currently support radials
            return null;
        } else if (nestedModes.size() == 1) {
            // If we only have one mode available, just return it rather than having to
            // select the singular mode
            return nestedModes.get(0).nestedData();
        }
        return new NestingRadialData(RADIAL_ID, nestedModes);
    }

    @Nullable
    public <M extends IRadialMode> M getMode(ItemStack stack, RadialData<M> radialData) {
        for (Module<?> module : getModules(stack)) {
            if (module.handlesRadialModeChange()) {
                M mode = module.getMode(stack, radialData);
                if (mode != null) {
                    return mode;
                }
            }
        }
        return null;
    }

    public <M extends IRadialMode> void setMode(ItemStack stack, Player player, RadialData<M> radialData, M mode) {
        for (Module<?> module : getModules(stack)) {
            if (module.handlesRadialModeChange() && module.setMode(player, stack, radialData, mode)) {
                return;
            }
        }
    }

    @Override
    protected FloatingLong getMaxEnergy(ItemStack stack) {
        IModule<ModuleEnergyUnit> module = getModule(stack, MekanismModules.ENERGY_UNIT);
        return module == null ? MekaWeapons.general.mekaBowBaseEnergyCapacity.get() : module.getCustomInstance().getEnergyCapacity(module);
    }

    @Override
    protected FloatingLong getChargeRate(ItemStack stack) {
        IModule<ModuleEnergyUnit> module = getModule(stack, MekanismModules.ENERGY_UNIT);
        return module == null ? MekaWeapons.general.mekaBowBaseChargeRate.get() : module.getCustomInstance().getChargeRate(module);
    }

    @Nullable
    @Override
    public Component getScrollTextComponent(@NotNull ItemStack stack) {
        return getModules(stack).stream().filter(Module::handlesModeChange).findFirst().map(module -> module.getModeScrollComponent(stack)).orElse(null);
    }
}