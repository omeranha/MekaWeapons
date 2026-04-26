package meranha.mekaweapons.items;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.lasers.ILaserDissipation;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.client.sound.SoundHandler;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.gear.IRadialModuleContainerItem;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.item.ItemEnergized;
import mekanism.common.registries.MekanismDamageTypes;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.util.StorageUtils;
import meranha.mekaweapons.MekaWeapons;
import meranha.mekaweapons.particle.MekaGunLaserParticleData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

import static meranha.mekaweapons.MekaWeaponsUtils.*;

public class ItemMekaGun extends ItemEnergized implements IRadialModuleContainerItem {
    private static final ResourceLocation RADIAL_ID = MekaWeapons.rl("meka_gun");
    private static final double RANGE = 50.0D;
    private static final double MUZZLE_SIDE_OFFSET = 0.02D;
    private static final double MUZZLE_FORWARD_OFFSET = 0.05D;
    private static final double MUZZLE_DOWN_OFFSET = -0.02D;
    private static final double MIN_BEAM_LENGTH = 0.35D;

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
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (isEnergyInsufficient(stack)) {
            return InteractionResultHolder.fail(stack);
        }

        Vec3 look = player.getViewVector(1.0F).normalize();
        Vec3 eye = player.getEyePosition(1.0F);
        double mainHandSign = player.getMainArm() == net.minecraft.world.entity.HumanoidArm.RIGHT ? 1.0D : -1.0D;
        double handSign = (usedHand == InteractionHand.MAIN_HAND) ? mainHandSign : -mainHandSign;
        Vec3 right = look.cross(new Vec3(0, 1, 0));
        if (right.lengthSqr() < 1.0E-6) {
            right = new Vec3(1, 0, 0);
        } else {
            right = right.normalize();
        }

        Vec3 upLocal = right.cross(look).normalize();
        Vec3 from = eye.add(right.scale(MUZZLE_SIDE_OFFSET * handSign)).add(look.scale(MUZZLE_FORWARD_OFFSET)).add(upLocal.scale(MUZZLE_DOWN_OFFSET));
        Vec3 aimEnd = eye.add(look.scale(RANGE));
        BlockHitResult result = level.clip(new ClipContext(eye, aimEnd, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));

        Vec3 to = result.getType() != HitResult.Type.MISS ? result.getLocation() : aimEnd;
        Vec3 beam = to.subtract(from);
        if (beam.length() < MIN_BEAM_LENGTH) {
            to = from.add(look.scale(MIN_BEAM_LENGTH));
        }

        Vec3 finalTo = fireLaser(level, player, from, to);
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        long energy = getEnergyNeeded(stack);
        if (!player.isCreative() && energyContainer != null) {
            energyContainer.extract(energy, Action.EXECUTE, AutomationType.MANUAL);
        }
        Vec3 finalBeam = finalTo.subtract(from);
        sendLaserDataToPlayers(level, new MekaGunLaserParticleData(finalBeam.x, finalBeam.y, finalBeam.z, 70, 242, 149), from);
        SoundHandler.playSound(MekanismSounds.LASER);
        return InteractionResultHolder.pass(stack);
    }

    private Vec3 fireLaser(Level level, Player owner, Vec3 from, Vec3 to) {
        Vec3 beam = to.subtract(from);
        double length = beam.length();
        if (length < 1e-6) return to;
        Vec3 dir = beam.normalize();
        float radius = 0.1f;
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, getLaserBox(from, to, radius));
        entities.sort(Comparator.comparingDouble(e -> {
            Vec3 rel = e.position().subtract(from);
            return rel.dot(dir);
        }));

        for (LivingEntity livingEntity : entities) {
            if (!isEntityHit(from, dir, length, livingEntity, radius) || livingEntity instanceof Player player && (!owner.canHarmPlayer(player) || player.is(owner))) continue;
            if (livingEntity.isInvulnerable() || livingEntity.isInvulnerableTo(MekanismDamageTypes.LASER.source(level))) {
                return projectPoint(from, dir, livingEntity.position());
            }

            float damage = MekaWeapons.general.mekaGunBaseDamage.get();
            float remainingDamage = damage; // should shields block lasers from meka-gun?

            double dissipationPercent = 0;
            double refractionPercent = 0;
            for (ItemStack armor : livingEntity.getArmorSlots()) {
                if (!armor.isEmpty()) {
                    ILaserDissipation laserDissipation = armor.getCapability(Capabilities.LASER_DISSIPATION);
                    if (laserDissipation != null) {
                        dissipationPercent += laserDissipation.getDissipationPercent();
                        refractionPercent += laserDissipation.getRefractionPercent();
                        if (dissipationPercent >= 1) {
                            break;
                        }
                    }
                }
            }
            if (dissipationPercent > 0) {
                dissipationPercent = Math.min(dissipationPercent, 1);
                remainingDamage = (long) (remainingDamage * (1D - dissipationPercent));
                if (remainingDamage == 0L) {
                    return projectPoint(from, dir, livingEntity.position());
                }
            }

            if (refractionPercent > 0) {
                refractionPercent = Math.min(refractionPercent, 1);
                double refractedDamage = remainingDamage * refractionPercent;
                damage = (float) (remainingDamage - refractedDamage);
            }
            if (damage > 0) {
                if (!livingEntity.fireImmune()) {
                    livingEntity.igniteForTicks((int) damage);
                }

                livingEntity.hurt(MekanismDamageTypes.LASER.source(level), damage);
            }
        }

        return to;
    }

    private AABB getLaserBox(Vec3 from, Vec3 to, float radius) {
        Vec3 dir = to.subtract(from);
        if (dir.lengthSqr() < 1e-6) {
            return new AABB(from, to);
        }

        dir = dir.normalize();
        double ax = Math.abs(dir.x);
        double ay = Math.abs(dir.y);
        double az = Math.abs(dir.z);
        double inflateX = (1.0 - ax) * radius;
        double inflateY = (1.0 - ay) * radius;
        double inflateZ = (1.0 - az) * radius;
        return new AABB(from, to).inflate(inflateX, inflateY, inflateZ);
    }

    private Vec3 projectPoint(Vec3 from, Vec3 dir, Vec3 point) {
        Vec3 toPoint = point.subtract(from);
        double t = toPoint.dot(dir);
        return from.add(dir.scale(t));
    }

    private boolean isEntityHit(Vec3 from, Vec3 dir, double length, Entity entity, double radius) {
        AABB box = entity.getBoundingBox();
        Vec3 center = box.getCenter();
        Vec3 toEntity = center.subtract(from);
        double t = toEntity.dot(dir);
        if (t < 0 || t > length) return false;

        Vec3 closest = from.add(dir.scale(t));
        double dx = Math.max(box.minX - closest.x, 0);
        dx = Math.max(dx, closest.x - box.maxX);

        double dy = Math.max(box.minY - closest.y, 0);
        dy = Math.max(dy, closest.y - box.maxY);

        double dz = Math.max(box.minZ - closest.z, 0);
        dz = Math.max(dz, closest.z - box.maxZ);

        double distSqr = dx*dx + dy*dy + dz*dz;
        return distSqr <= radius * radius;
    }

    private void sendLaserDataToPlayers(Level level, MekaGunLaserParticleData data, Vec3 from) {
        if (level instanceof ServerLevel serverWorld) {
            for (ServerPlayer player : serverWorld.players()) {
                serverWorld.sendParticles(player, data, true, from.x, from.y, from.z, 1, 0, 0, 0, 0);
            }
        }
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
}
