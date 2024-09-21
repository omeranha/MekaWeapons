package meranha.mekaweapons.items;

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

    public MekaArrowEntity(Level level, double x, double y, double z, ItemStack itemStack, boolean noGravity, int damage) {
        super(MekaWeapons.MEKA_ARROW.get(), x, y, z, level, itemStack, null);
        this.setNoGravity(noGravity);
        this.setBaseDamage(damage);
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

    @NotNull
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}
