package meranha.mekaweapons.items.modules;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import meranha.mekaweapons.MekaWeapons;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Generic enchantment module. Instead of being tied to a single enchantment like Mekanism's enchantment based modules,
 * it stores whatever enchantments were infused into it, up to {@link #MAX_ENCHANTMENTS}.
 */
@ParametersAreNotNullByDefault
public record EnchantmentUnit(ItemEnchantments enchantments) implements ICustomModule<EnchantmentUnit> {

    public static final ResourceLocation ENCHANTMENTS = MekaWeapons.rl("enchantments");
    public static final int MAX_ENCHANTMENTS = 4;

    public EnchantmentUnit(IModule<EnchantmentUnit> module) {
        this(module.<ItemEnchantments>getConfigOrThrow(ENCHANTMENTS).get());
    }

    /**
     * Enchantments currently infused into the unit installed on the given stack, or empty when there is no unit or it is
     * disabled.
     */
    @NotNull
    public static ItemEnchantments getInfused(ItemStack stack) {
        IModule<EnchantmentUnit> module = getModule(stack);
        if (module == null || !module.isEnabled()) {
            return ItemEnchantments.EMPTY;
        }
        return module.getCustomInstance().enchantments();
    }

    @Nullable
    public static IModule<EnchantmentUnit> getModule(ItemStack stack) {
        return IModuleHelper.INSTANCE.getModule(stack, WeaponsModules.ENCHANTMENT_UNIT);
    }

    /**
     * Reads the enchantments an item can donate, the same way the anvil does: enchanted books keep theirs in a separate
     * component from regular enchanted gear.
     */
    @NotNull
    public static ItemEnchantments readEnchantments(ItemStack stack) {
        return EnchantmentHelper.getEnchantmentsForCrafting(stack);
    }

    public static boolean isEnchantmentSource(ItemStack stack) {
        return !readEnchantments(stack).isEmpty();
    }

    public static boolean canReceiveEnchantments(ItemStack stack) {
        return getModule(stack) != null;
    }

    public static boolean isFull(ItemStack target) {
        return getInfused(target).size() >= MAX_ENCHANTMENTS;
    }

    /**
     * Merges {@code incoming} into {@code current} following the same rules the anvil applies in
     * {@code AnvilMenu#createResult}: an enchantment has to be applicable to the target, it must not be mutually
     * exclusive with anything already present, two equal levels combine into the next one up, and the result is clamped
     * to the enchantment's max level. On top of that the unit's capacity is respected.
     */
    @NotNull
    public static ItemEnchantments merge(ItemStack target, ItemEnchantments current, ItemEnchantments incoming) {
        ItemEnchantments.Mutable merged = new ItemEnchantments.Mutable(current);
        int distinct = current.size();
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : incoming.entrySet()) {
            Holder<Enchantment> enchantment = entry.getKey();
            if (!canApply(target, enchantment) || !isCompatible(merged.keySet(), enchantment)) {
                continue;
            }

            int existing = merged.getLevel(enchantment);
            int incomingLevel = entry.getIntValue();
            //Anvil rule: combining two of the same level bumps it by one, otherwise the higher level wins
            int level = existing == incomingLevel ? incomingLevel + 1 : Math.max(incomingLevel, existing);
            level = Math.min(level, enchantment.value().getMaxLevel());
            if (level <= existing) {
                continue;
            } else if (existing == 0) {
                if (distinct >= MAX_ENCHANTMENTS) {
                    continue;
                }
                distinct++;
            }
            merged.set(enchantment, level);
        }
        return merged.toImmutable();
    }

    /**
     * The anvil's mutual exclusivity rule: an enchantment cannot be added when anything already present declares it in
     * its exclusive set, or vice versa. This is what stops Sharpness from sitting next to Smite.
     */
    public static boolean isCompatible(Iterable<Holder<Enchantment>> present, Holder<Enchantment> enchantment) {
        for (Holder<Enchantment> other : present) {
            if (!other.equals(enchantment) && !Enchantment.areCompatible(enchantment, other)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Whether the enchantment is valid for the target, using the same check the anvil does: the enchantment's
     * {@code supported_items} tag has to contain the item.
     * <p>
     * The Meka weapons are added to the relevant vanilla {@code enchantable/*} tags by this mod's datapack, which is
     * what makes this resolve correctly. Those tags are the only thing deciding what each weapon accepts, so editing
     * them is how the allowed set is tuned.
     */
    public static boolean canApply(ItemStack target, Holder<Enchantment> enchantment) {
        return !enchantment.value().effects().isEmpty() && target.supportsEnchantment(enchantment);
    }
}
