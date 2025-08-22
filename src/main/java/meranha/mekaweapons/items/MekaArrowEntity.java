package meranha.mekaweapons.items;

import static meranha.mekaweapons.MekaWeaponsUtils.*;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import meranha.mekaweapons.items.modules.WeaponsModules;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

import meranha.mekaweapons.MekaWeapons;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.List;

public class MekaArrowEntity extends AbstractArrow {
    private IntOpenHashSet piercingIgnoreEntityIds;
    private List<Entity> piercedAndKilledEntities;

    public MekaArrowEntity(EntityType<? extends MekaArrowEntity> entityType, Level level, ItemStack itemStack) {
        super(entityType, level);
    }

    public MekaArrowEntity(Level level, AbstractArrow arrow, ItemStack projectileStack, ItemStack weaponStack, int damage) {
        super(MekaWeapons.MEKA_ARROW.get(), arrow.getX(), arrow.getY(), arrow.getZ(), level, projectileStack, null);
        this.setPickup(!isModuleEnabled(weaponStack, WeaponsModules.ARROWENERGY_UNIT));
        this.setNoGravity(isModuleEnabled(weaponStack, WeaponsModules.GRAVITYDAMPENER_UNIT));
        this.setBaseDamage(damage);
        this.setOwner(arrow.getOwner());
    }

    public MekaArrowEntity(EntityType<MekaArrowEntity> entityType, Level level) {
        this(entityType, level, new ItemStack(Items.ARROW));
    }

    public void tick() {
        super.tick();
        // 5 seconds (10 ticks) should be enough to hit something
        if (this.tickCount > 100 && !this.inGround) {
            this.setNoGravity(false);
        }
    }

    @Override
    @SuppressWarnings("ConstantValue")
    public void onHitEntity(EntityHitResult result) {
        Entity entity = result.getEntity();
        double baseDamage = this.getBaseDamage();
        Entity player = this.getOwner();
        DamageSource damagesource = this.damageSources().arrow(this, (player != null) ? player : this);
        Level level = this.level();
        if (getWeaponItem() != null) {
            if (level instanceof ServerLevel serverlevel) {
                baseDamage = EnchantmentHelper.modifyDamage(serverlevel, this.getWeaponItem(), entity, damagesource, (float)baseDamage);
            }
        }

        if (this.getPierceLevel() > 0) {
            if (this.piercingIgnoreEntityIds == null) {
                this.piercingIgnoreEntityIds = new IntOpenHashSet(5);
            }

            if (this.piercedAndKilledEntities == null) {
                this.piercedAndKilledEntities = Lists.newArrayListWithCapacity(5);
            }

            if (this.piercingIgnoreEntityIds.size() >= this.getPierceLevel() + 1) {
                this.discard();
                return;
            }

            this.piercingIgnoreEntityIds.add(entity.getId());
        }

        if (player instanceof LivingEntity livingEntity) {
            livingEntity.setLastHurtMob(entity);
        }

        boolean isEnderman = entity.getType() == EntityType.ENDERMAN;
        int i = entity.getRemainingFireTicks();
        if (this.isOnFire() && !isEnderman) {
            entity.igniteForSeconds(5.0F);
        }

        if (entity.hurt(damagesource, (float)baseDamage)) {
            if (isEnderman) {
                return;
            }

            if (entity instanceof LivingEntity livingentity) {
                if (!level.isClientSide && this.getPierceLevel() <= 0) {
                    livingentity.setArrowCount(livingentity.getArrowCount() + 1);
                }

                this.doKnockback(livingentity, damagesource);
                if (level instanceof ServerLevel serverLevel) {
                    EnchantmentHelper.doPostAttackEffectsWithItemSource(serverLevel, livingentity, damagesource, this.getWeaponItem());
                }

                this.doPostHurtEffects(livingentity);
                if (livingentity != player && livingentity instanceof Player && player instanceof ServerPlayer && !this.isSilent()) {
                    ((ServerPlayer) player).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
                }

                if (!entity.isAlive() && this.piercedAndKilledEntities != null) {
                    this.piercedAndKilledEntities.add(livingentity);
                }

                if (!level.isClientSide && player instanceof ServerPlayer serverplayer) {
                    if (this.piercedAndKilledEntities != null && this.shotFromCrossbow()) {
                        CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverplayer, this.piercedAndKilledEntities);
                    } else if (!entity.isAlive() && this.shotFromCrossbow()) {
                        CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverplayer, List.of(entity));
                    }
                }
            }

            this.playSound(this.getHitGroundSoundEvent(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
            if (this.getPierceLevel() <= 0) {
                this.discard();
            }
        } else {
            entity.setRemainingFireTicks(i);
            this.deflect(ProjectileDeflection.REVERSE, entity, this.getOwner(), false);
            this.setDeltaMovement(this.getDeltaMovement().scale(0.2));
            if (!this.level().isClientSide && this.getDeltaMovement().lengthSqr() < 1.0E-7) {
                if (this.pickup == AbstractArrow.Pickup.ALLOWED) {
                    this.spawnAtLocation(this.getPickupItem(), 0.1F);
                }

                this.discard();
            }
        }
    }

    public void setPickup(boolean pickup) {
        this.pickup = pickup ? Pickup.ALLOWED : Pickup.CREATIVE_ONLY;
    }

    @NotNull
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}
