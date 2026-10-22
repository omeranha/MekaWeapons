package meranha.mekaweapons.items;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.lasers.ILaserDissipation;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.gear.IRadialModuleContainerItem;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.item.ItemEnergized;
import mekanism.common.registries.MekanismDamageTypes;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.util.StorageUtils;
import meranha.mekaweapons.MekaWeapons;
import meranha.mekaweapons.client.WeaponsLang;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static meranha.mekaweapons.MekaWeaponsUtils.*;

public class ItemMekaGun extends ItemEnergized implements IRadialModuleContainerItem {
    public record LaserHit(@Nullable LivingEntity entity, Vec3 hitPosition) {}

    private static final ResourceLocation RADIAL_ID = MekaWeapons.rl("meka_gun");
    private static final double MUZZLE_SIDE_OFFSET = 0.02D;
    private static final double MUZZLE_FORWARD_OFFSET = 0.05D;
    private static final double MUZZLE_DOWN_OFFSET = -0.02D;
    private static final double MIN_BEAM_LENGTH = 0.35D;
    private static final float LASER_RADIUS = 0.1F;

    public ItemMekaGun(Properties properties) {
        super(IModuleHelper.INSTANCE.applyModuleContainerProperties(properties.rarity(Rarity.EPIC).setNoRepair().stacksTo(1)));
    }

    @Override
    public void onDestroyed(@NotNull ItemEntity item, @NotNull DamageSource damageSource) {
        ModuleHelper.INSTANCE.dropModuleContainerContents(item, damageSource);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
            addModuleDetails(stack, tooltip);
            return;
        }

        StorageUtils.addStoredEnergy(stack, tooltip, true);
        tooltip.add(MekanismLang.HOLD_FOR_MODULES.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
    }

    @Override
    public void adjustAttributes(@NotNull ItemAttributeModifierEvent event) {
        long totalDamage = getTotalDamage(event.getItemStack());
        event.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, totalDamage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        IRadialModuleContainerItem.super.adjustAttributes(event);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.fail(player.getItemInHand(hand));
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity livingEntity, @NotNull ItemStack stack, int remainingUseTicks) {
        // The laser beam is rendered client side from the synced item use state, so all the firing logic stays on the server.
        if (level.isClientSide() || !(livingEntity instanceof Player player)) {
            return;
        }

        int heat = getHeat(stack);
        if (isEnergyInsufficient(stack) || heat >= MekaWeapons.general.mekaGunMaxHeat.get()) {
            player.releaseUsingItem();
            return;
        }

        long gameTime = level.getGameTime();
        if (gameTime - getLastFireTick(stack) < MekaWeapons.general.mekaGunFireInterval.get()) {
            return;
        }

        IEnergyContainer energy = StorageUtils.getEnergyContainer(stack, 0);
        if (!player.isCreative() && energy != null) {
            energy.extract(getEnergyNeeded(stack), Action.EXECUTE, AutomationType.MANUAL);
        }

        setHeat(stack, heat + MekaWeapons.general.mekaGunHeatPerShot.get());
        setLastFireTick(stack, gameTime);
        // TODO: start and stop laser sound in use and release using to avoid sound spam each shot
        level.playSound(null, player.getX(), player.getY(), player.getZ(), MekanismSounds.LASER.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

        LaserHit hit = traceLaser(level, player, 1.0F);
        if (hit.entity() != null) {
            damageLaserHit(level, stack, hit.entity());
        }
    }

    /**
     * Position the beam is emitted from, slightly offset from the eyes towards the hand holding the gun.
     */
    public static Vec3 getMuzzlePosition(@NotNull Player player, float partialTick) {
        Vec3 look = player.getViewVector(partialTick).normalize();
        Vec3 eye = player.getEyePosition(partialTick);
        double mainHandSign = player.getMainArm() == HumanoidArm.RIGHT ? 1.0D : -1.0D;
        double handSign = player.getUsedItemHand() == InteractionHand.MAIN_HAND ? mainHandSign : -mainHandSign;
        Vec3 right = look.cross(new Vec3(0, 1, 0));
        right = right.lengthSqr() < 1.0E-6 ? new Vec3(1, 0, 0) : right.normalize();

        Vec3 up = right.cross(look).normalize();
        return eye.add(right.scale(MUZZLE_SIDE_OFFSET * handSign)).add(look.scale(MUZZLE_FORWARD_OFFSET)).add(up.scale(MUZZLE_DOWN_OFFSET));
    }

    /**
     * Traces the beam from the muzzle up to the first block or living entity in the way.
     */
    public static LaserHit traceLaser(@NotNull Level level, @NotNull Player player, float partialTick) {
        Vec3 look = player.getViewVector(partialTick).normalize();
        Vec3 eye = player.getEyePosition(partialTick);
        Vec3 from = getMuzzlePosition(player, partialTick);

        Vec3 aimEnd = eye.add(look.scale(MekaWeapons.general.mekaGunBeamLength.get()));
        BlockHitResult blockHitResult = level.clip(new ClipContext(eye, aimEnd, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        Vec3 to = blockHitResult.getType() != HitResult.Type.MISS ? blockHitResult.getLocation() : aimEnd;
        if (to.subtract(from).length() < MIN_BEAM_LENGTH) {
            to = from.add(look.scale(MIN_BEAM_LENGTH));
        }
        return traceLaser(level, player, from, to);
    }

    private static LaserHit traceLaser(Level level, Player owner, Vec3 from, Vec3 to) {
        Vec3 beam = to.subtract(from);
        double length = beam.length();
        if (length < 1e-6) {
            return new LaserHit(null, to);
        }

        Vec3 dir = beam.normalize();
        LivingEntity closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, getLaserBox(from, to, LASER_RADIUS))) {
            if (!isEntityHit(from, dir, length, entity, LASER_RADIUS)) {
                continue;
            }

            if (entity instanceof Player player && (!owner.canHarmPlayer(player) || player == owner)) {
                continue;
            }

            Vec3 pos = entity.position();
            double distance = (pos.x - from.x) * dir.x + (pos.y - from.y) * dir.y + (pos.z - from.z) * dir.z;
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = entity;
            }
        }

        if (closest == null) {
            return new LaserHit(null, to);
        }
        return new LaserHit(closest, projectPoint(from, dir, closest.position()));
    }

    private void damageLaserHit(Level level, ItemStack stack, LivingEntity target) {
        if (target.isInvulnerable() || target.isInvulnerableTo(MekanismDamageTypes.LASER.source(level))) {
            return;
        }

        float damage = getTotalDamage(stack);
        float remainingDamage = damage;
        double dissipation = 0;
        double refraction = 0;
        for (ItemStack armor : target.getArmorSlots()) {
            ILaserDissipation laser = armor.getCapability(Capabilities.LASER_DISSIPATION);
            if (laser == null) {
                continue;
            }

            dissipation += laser.getDissipationPercent();
            refraction += laser.getRefractionPercent();
        }

        dissipation = Math.min(dissipation, 1);
        remainingDamage *= (float) (1 - dissipation);
        if (remainingDamage <= 0) {
            return;
        }

        refraction = Math.min(refraction, 1);
        damage = (float)(remainingDamage * (1 - refraction));
        if (damage <= 0) {
            return;
        }

        if (!target.fireImmune()) {
            target.igniteForSeconds((int)damage);
        }
        target.hurt(MekanismDamageTypes.LASER.source(level), damage);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, Level level, @NotNull Entity entity, int slot, boolean selected) {
        if (level.isClientSide || !(entity instanceof Player)) return;

        long gameTime = level.getGameTime();
        if (gameTime - getLastFireTick(stack) < MekaWeapons.general.mekaGunCooldownDelayTicks.get() || gameTime % 20 != 0) return;

        int heat = getHeat(stack);
        if (heat > 0) {
            setHeat(stack, heat - MekaWeapons.general.mekaGunHeatLossPerSecond.get());
        }
    }

    private static AABB getLaserBox(Vec3 from, Vec3 to, float radius) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        double lengthSq = dx * dx + dy * dy + dz * dz;
        if (lengthSq < 1.0E-12) {
            return new AABB(from, to);
        }

        double invLength = 1.0 / Math.sqrt(lengthSq);
        return new AABB(from, to).inflate((1.0 - Math.abs(dx * invLength)) * radius, (1.0 - Math.abs(dy * invLength)) * radius, (1.0 - Math.abs(dz * invLength)) * radius);
    }

    private static Vec3 projectPoint(Vec3 from, Vec3 dir, Vec3 point) {
        Vec3 toPoint = point.subtract(from);
        double t = toPoint.dot(dir);
        return from.add(dir.scale(t));
    }

    private static boolean isEntityHit(Vec3 from, Vec3 dir, double length, Entity entity, double radius) {
        AABB box = entity.getBoundingBox();

        double centerX = (box.minX + box.maxX) * 0.5;
        double centerY = (box.minY + box.maxY) * 0.5;
        double centerZ = (box.minZ + box.maxZ) * 0.5;
        double t = (centerX - from.x) * dir.x + (centerY - from.y) * dir.y + (centerZ - from.z) * dir.z;

        if (t < 0 || t > length) {
            return false;
        }

        double closestX = from.x + dir.x * t;
        double closestY = from.y + dir.y * t;
        double closestZ = from.z + dir.z * t;
        double dx = 0;
        if (closestX < box.minX) {
            dx = box.minX - closestX;
        } else if (closestX > box.maxX) {
            dx = closestX - box.maxX;
        }

        double dy = 0;
        if (closestY < box.minY) {
            dy = box.minY - closestY;
        } else if (closestY > box.maxY) {
            dy = closestY - box.maxY;
        }

        double dz = 0;
        if (closestZ < box.minZ) {
            dz = box.minZ - closestZ;
        } else if (closestZ > box.maxZ) {
            dz = closestZ - box.maxZ;
        }
        return dx * dx + dy * dy + dz * dz <= radius * radius;
    }

    @Override
    public void addHUDStrings(List<Component> list, Player player, ItemStack stack, EquipmentSlot slotType) {
        list.add(WeaponsLang.HEAT.translateColored(EnumColor.WHITE, getHeat(stack) + "%"));
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
    public int getBarColor(@NotNull ItemStack stack) {
        return getBarCustomColor(stack);
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return false;
    }

    public ResourceLocation getRadialIdentifier() {
        return RADIAL_ID;
    }

    private static int getHeat(ItemStack stack) {
        return stack.getOrDefault(MekaWeapons.HEAT, 0);
    }

    private static void setHeat(ItemStack stack, int value) {
        stack.set(MekaWeapons.HEAT, Math.clamp(value, 0, MekaWeapons.general.mekaGunMaxHeat.get()));
    }

    private static long getLastFireTick(ItemStack stack) {
        return stack.getOrDefault(MekaWeapons.LAST_FIRE_TICK, 0L);
    }

    private static void setLastFireTick(ItemStack stack, long tick) {
        stack.set(MekaWeapons.LAST_FIRE_TICK, tick);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return 72000;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
        return MekaWeapons.general.mekaGunEnchantments.get();
    }

    @Override
    public boolean isBookEnchantable(@NotNull ItemStack stack, @NotNull ItemStack book) {
        return MekaWeapons.general.mekaGunEnchantments.get();
    }
}
