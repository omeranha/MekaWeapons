package meranha.mekaweapons.items;

import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mekanism.api.math.FloatingLong;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.api.radial.mode.NestedRadialMode;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.Module;
import mekanism.common.item.ItemEnergized;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.lasers.ILaserDissipation;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.client.sound.SoundHandler;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.lib.attribute.AttributeCache;
import mekanism.common.lib.radial.IGenericRadialModeItem;
import mekanism.common.lib.radial.data.NestingRadialData;
import mekanism.common.registries.MekanismDamageTypes;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.util.StorageUtils;
import meranha.mekaweapons.MekaWeapons;
import meranha.mekaweapons.MekaWeaponsUtils;
import meranha.mekaweapons.client.WeaponsLang;
import meranha.mekaweapons.items.modules.WeaponAttackAmplificationUnit;
import meranha.mekaweapons.particle.MekaGunLaserParticleData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static meranha.mekaweapons.MekaWeaponsUtils.*;

public class ItemMekaGun extends ItemEnergized implements IModuleContainerItem, IGenericRadialModeItem {
    private static final ResourceLocation RADIAL_ID = MekaWeapons.rl("meka_gun");
    private final Int2ObjectMap<AttributeCache> attributeCaches = new Int2ObjectArrayMap<>(WeaponAttackAmplificationUnit.AttackDamage.values().length - 2);
    private static final double MUZZLE_SIDE_OFFSET = 0.02D;
    private static final double MUZZLE_FORWARD_OFFSET = 0.05D;
    private static final double MUZZLE_DOWN_OFFSET = -0.02D;
    private static final double MIN_BEAM_LENGTH = 0.35D;
    private static final String HEAT_TAG = "heat";
    private static final String LAST_FIRE_TICK_TAG = "lastFireTick";

    public ItemMekaGun(@NotNull Properties properties) {
        super(MekaWeapons.general.mekaGunBaseChargeRate, MekaWeapons.general.mekaGunBaseEnergyCapacity, properties.rarity(Rarity.EPIC).setNoRepair().stacksTo(1));
    }

    public void appendHoverText(@NotNull ItemStack stack, @NotNull Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
            addModuleDetails(stack, tooltip);
            return;
        }

        StorageUtils.addStoredEnergy(stack, tooltip, true);
        tooltip.add(MekanismLang.HOLD_FOR_MODULES.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
    }

    @NotNull
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@NotNull EquipmentSlot slot, @NotNull ItemStack stack) {
        if (slot == EquipmentSlot.MAINHAND) {
            long totalDamage = MekaWeaponsUtils.getTotalDamage(stack);
            return attributeCaches.computeIfAbsent((int) totalDamage, damage -> new AttributeCache(builder -> builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", totalDamage, AttributeModifier.Operation.ADDITION)), MekaWeapons.general.mekaGunBaseDamage)).get();
        }
        return super.getAttributeModifiers(slot, stack);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        int heat = getHeat(stack);
        if (isEnergyInsufficient(stack) || heat >= MekaWeapons.general.mekaGunMaxHeat.get()) {
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
        Vec3 aimEnd = eye.add(look.scale(MekaWeapons.general.mekaGunBeamLength.get()));
        BlockHitResult result = level.clip(new ClipContext(eye, aimEnd, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));

        Vec3 to = result.getType() != HitResult.Type.MISS ? result.getLocation() : aimEnd;
        Vec3 beam = to.subtract(from);
        if (beam.length() < MIN_BEAM_LENGTH) {
            to = from.add(look.scale(MIN_BEAM_LENGTH));
        }

        Vec3 finalTo = fireLaser(level, stack, player, from, to);
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        long energy = getEnergyNeeded(stack);
        if (!player.isCreative() && energyContainer != null) {
            energyContainer.extract(FloatingLong.create(energy), Action.EXECUTE, AutomationType.MANUAL);
        }
        Vec3 finalBeam = finalTo.subtract(from);
        sendLaserDataToPlayers(level, new MekaGunLaserParticleData(finalBeam.x, finalBeam.y, finalBeam.z, 70, 242, 149), from);
        SoundHandler.playSound(MekanismSounds.LASER);
        setHeat(stack, heat + MekaWeapons.general.mekaGunHeatPerShot.get());
        setLastFireTick(stack, level.getGameTime());
        return super.use(level, player, usedHand);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, Level level, @NotNull Entity entity, int slot, boolean selected) {
        if (level.isClientSide || !(entity instanceof Player player) || player.getMainHandItem() != stack) return;

        long gameTime = level.getGameTime();
        if (gameTime - getLastFireTick(stack) < MekaWeapons.general.mekaGunCooldownDelayTicks.get() || gameTime % 20 != 0) return;

        int heat = getHeat(stack);
        if (heat <= MekaWeapons.general.mekaGunMaxHeat.get()) {
            setHeat(stack, heat - MekaWeapons.general.mekaGunHeatLossPerSecond.get());
        }
    }

    private Vec3 fireLaser(Level level, ItemStack stack, Player owner, Vec3 from, Vec3 to) {
        Vec3 beam = to.subtract(from);
        double length = beam.length();
        if (length < 1e-6) {
            return to;
        }

        Vec3 dir = beam.normalize();
        float radius = 0.1F;
        LivingEntity hitEntity = null;
        double closestDistance = Double.MAX_VALUE;
        for (LivingEntity livingEntity : level.getEntitiesOfClass(LivingEntity.class, getLaserBox(from, to, radius))) {
            if (!isEntityHit(from, dir, length, livingEntity, radius)) {
                continue;
            }

            if (livingEntity instanceof Player player && (!owner.canHarmPlayer(player) || player.is(owner))) {
                continue;
            }

            Vec3 pos = livingEntity.position();
            double distance = (pos.x - from.x) * dir.x + (pos.y - from.y) * dir.y + (pos.z - from.z) * dir.z;

            if (distance < closestDistance) {
                closestDistance = distance;
                hitEntity = livingEntity;
            }
        }

        if (hitEntity == null) {
            return to;
        }

        if (hitEntity.isInvulnerable() || hitEntity.isInvulnerableTo(MekanismDamageTypes.LASER.source(level))) {
            return projectPoint(from, dir, hitEntity.position());
        }

        float damage = getTotalDamage(stack);
        float remainingDamage = damage;
        double dissipationPercent = 0;
        double refractionPercent = 0;
        for (ItemStack armor : hitEntity.getArmorSlots()) {
            if (armor.isEmpty()) {
                continue;
            }

            Optional<ILaserDissipation> capability = armor.getCapability(Capabilities.LASER_DISSIPATION).resolve();
            if (capability.isPresent()) {
                ILaserDissipation laserDissipation = capability.get();
                dissipationPercent += laserDissipation.getDissipationPercent();
                refractionPercent += laserDissipation.getRefractionPercent();
                if (dissipationPercent >= 1) {
                    break;
                }
            }
        }

        if (dissipationPercent > 0) {
            dissipationPercent = Math.min(dissipationPercent, 1);
            remainingDamage *= (float) (1D - dissipationPercent);
            if (remainingDamage <= 0) {
                return projectPoint(from, dir, hitEntity.position());
            }
        }

        if (refractionPercent > 0) {
            refractionPercent = Math.min(refractionPercent, 1);
            double refractedDamage = remainingDamage * refractionPercent;
            damage = (float) (remainingDamage - refractedDamage);
        } else {
            damage = remainingDamage;
        }

        if (damage > 0) {
            if (!hitEntity.fireImmune()) {
                hitEntity.setSecondsOnFire((int) damage);
            }

            hitEntity.hurt(MekanismDamageTypes.LASER.source(level), damage);
        }

        return to;
    }

    private AABB getLaserBox(Vec3 from, Vec3 to, float radius) {
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

    private Vec3 projectPoint(Vec3 from, Vec3 dir, Vec3 point) {
        Vec3 toPoint = point.subtract(from);
        double t = toPoint.dot(dir);
        return from.add(dir.scale(t));
    }

    private boolean isEntityHit(Vec3 from, Vec3 dir, double length, Entity entity, double radius) {
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

    private void sendLaserDataToPlayers(Level level, MekaGunLaserParticleData data, Vec3 from) {
        if (level instanceof ServerLevel serverWorld) {
            for (ServerPlayer player : serverWorld.players()) {
                serverWorld.sendParticles(player, data, true, from.x, from.y, from.z, 1, 0, 0, 0, 0);
            }
        }
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
    public int getBarColor(@NotNull ItemStack stack) {
        return getBarCustomColor(stack);
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return false;
    }

    private static int getHeat(ItemStack stack) {
        return stack.getOrCreateTag().getInt(HEAT_TAG);
    }

    private static void setHeat(ItemStack stack, int value) {
        stack.getOrCreateTag().putInt(HEAT_TAG, Mth.clamp(value, 0, MekaWeapons.general.mekaGunMaxHeat.get()));
    }

    private static long getLastFireTick(ItemStack stack) {
        return stack.getOrCreateTag().getLong(LAST_FIRE_TICK_TAG);
    }

    private static void setLastFireTick(ItemStack stack, long tick) {
        stack.getOrCreateTag().putLong(LAST_FIRE_TICK_TAG, tick);
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
    public void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, DisplayChange displayChange) {
        for (Module<?> module : getModules(stack)) {
            if (module.handlesModeChange()) {
                module.changeMode(player, stack, shift, displayChange);
                return;
            }
        }
    }
}
