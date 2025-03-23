package meranha.mekaweapons.items;

import static meranha.mekaweapons.MekaWeaponsUtils.*;

import org.jetbrains.annotations.NotNull;

import meranha.mekaweapons.MekaWeapons;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class MekaArrowEntity extends AbstractArrow {
    public MekaArrowEntity(EntityType<? extends MekaArrowEntity> entityType, Level level, ItemStack itemStack) {
        super(entityType, level);
    }

    public MekaArrowEntity(Level level, AbstractArrow arrow, ItemStack projectileStack, ItemStack weaponStack, int damage) {
        super(MekaWeapons.MEKA_ARROW.get(), arrow.getX(), arrow.getY(), arrow.getZ(), level, projectileStack, null);
        this.setPickup(!isModuleEnabled(weaponStack, MekaWeapons.ARROWENERGY_UNIT));
        this.setNoGravity(isModuleEnabled(weaponStack, MekaWeapons.GRAVITYDAMPENER_UNIT));
        this.setBaseDamage(damage);
        this.setOwner(arrow.getOwner());
    }

    public MekaArrowEntity(EntityType<MekaArrowEntity> entityType, Level level) {
        this(entityType, level, new ItemStack(Items.ARROW));
    }

    public void tick() {
        super.tick();
        // 10 seconds (200 ticks) should be enough to hit something
        if (this.tickCount > 200 && !this.inGround) {
            this.setNoGravity(false);
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
