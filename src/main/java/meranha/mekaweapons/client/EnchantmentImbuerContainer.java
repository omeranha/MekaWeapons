package meranha.mekaweapons.client;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import mekanism.api.Action;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.config.ModuleConfig;
import mekanism.common.inventory.container.item.MekanismItemContainer;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.slot.BasicInventorySlot;
import meranha.mekaweapons.MekaWeapons;
import meranha.mekaweapons.items.ItemEnchantmentImbuer;
import meranha.mekaweapons.items.modules.EnchantmentUnit;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Container for the Enchantment Imbuer. The two slots are intentionally transient: they only live as long as the GUI is
 * open and their contents are handed back to the player on close, so nothing has to be persisted onto the item.
 */
public class EnchantmentImbuerContainer extends MekanismItemContainer {

    public static final int BOOK_SLOT_X = 26;
    public static final int WEAPON_SLOT_X = 134;
    public static final int SLOT_Y = 31;

    private BasicInventorySlot bookSlot;
    private BasicInventorySlot weaponSlot;
    private boolean loading;

    public EnchantmentImbuerContainer(int id, Inventory inv, InteractionHand hand, ItemStack stack) {
        super(MekaWeapons.ENCHANTMENT_IMBUER_CONTAINER, id, inv, hand, stack);
    }

    @Override
    protected void addSlots() {
        super.addSlots();
        bookSlot = BasicInventorySlot.at(EnchantmentUnit::isEnchantmentSource, this::persist, BOOK_SLOT_X, SLOT_Y);
        bookSlot.setSlotType(ContainerSlotType.INPUT);
        weaponSlot = BasicInventorySlot.at(EnchantmentUnit::canReceiveEnchantments, this::persist, WEAPON_SLOT_X, SLOT_Y);
        weaponSlot.setSlotType(ContainerSlotType.INPUT);
        addSlot(bookSlot.createContainerSlot());
        addSlot(weaponSlot.createContainerSlot());
        restore();
    }

    /**
     * Pulls anything the imbuer was holding back into the slots. This is what recovers items after a crash, where
     * {@link #removed(Player)} never ran.
     */
    private void restore() {
        ImbuerContents contents = getImbuerStack().get(MekaWeapons.IMBUER_CONTENTS.get());
        if (contents == null || contents.isEmpty()) {
            return;
        }
        loading = true;
        //Unchecked so the items come back even if their slot predicate would no longer accept them
        bookSlot.setStackUnchecked(contents.book().copy());
        weaponSlot.setStackUnchecked(contents.weapon().copy());
        loading = false;
    }

    /**
     * Mirrors the slot contents onto the imbuer stack, so they are saved with the player's inventory.
     */
    private void persist() {
        if (loading || bookSlot == null || weaponSlot == null || inv.player.level().isClientSide()) {
            return;
        }
        ItemStack imbuer = getImbuerStack();
        if (imbuer.isEmpty()) {
            return;
        }

        ImbuerContents contents = new ImbuerContents(bookSlot.getStack().copy(), weaponSlot.getStack().copy());
        if (contents.isEmpty()) {
            imbuer.remove(MekaWeapons.IMBUER_CONTENTS.get());
        } else {
            imbuer.set(MekaWeapons.IMBUER_CONTENTS.get(), contents);
        }
    }

    private ItemStack getImbuerStack() {
        ItemStack held = inv.player.getItemInHand(hand);
        return held.getItem() instanceof ItemEnchantmentImbuer ? held : stack;
    }

    @Override
    protected int getInventoryYOffset() {
        return 128;
    }

    public InteractionHand getHand() {
        return hand;
    }

    public ItemStack getBookStack() {
        return bookSlot == null ? ItemStack.EMPTY : bookSlot.getStack();
    }

    public ItemStack getWeaponStack() {
        return weaponSlot == null ? ItemStack.EMPTY : weaponSlot.getStack();
    }

    /**
     * Stored enchantments in a stable order, so a row in the GUI always refers to the same entry on both sides.
     */
    public List<StoredEnchantment> getStored() {
        ItemEnchantments stored = EnchantmentUnit.getImbued(getWeaponStack());
        List<StoredEnchantment> list = new ArrayList<>(stored.size());
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : stored.entrySet()) {
            int level = entry.getIntValue();
            entry.getKey().unwrapKey().ifPresent(key -> list.add(new StoredEnchantment(entry.getKey(), level, key.location())));
        }
        list.sort(Comparator.comparing(entry -> entry.id().toString()));
        return list;
    }

    public ImbueStatus getStatus() {
        ItemStack weapon = getWeaponStack();
        if (weapon.isEmpty()) {
            return ImbueStatus.NO_WEAPON;
        }
        ItemStack book = getBookStack();
        if (book.isEmpty()) {
            return ImbueStatus.NO_BOOK;
        }

        ItemEnchantments incoming = EnchantmentUnit.readEnchantments(book);
        ItemEnchantments current = EnchantmentUnit.getImbued(weapon);
        boolean anyApplicable = false;
        boolean anyCompatible = false;
        boolean anyNew = false;
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : incoming.entrySet()) {
            Holder<Enchantment> enchantment = entry.getKey();
            if (!EnchantmentUnit.canApply(weapon, enchantment)) {
                continue;
            }
            anyApplicable = true;
            if (!EnchantmentUnit.isCompatible(current.keySet(), enchantment)) {
                continue;
            }
            anyCompatible = true;
            if (current.getLevel(enchantment) == 0) {
                anyNew = true;
            }
        }

        if (!anyApplicable) {
            return ImbueStatus.NOT_SUPPORTED;
        } else if (!anyCompatible) {
            return ImbueStatus.INCOMPATIBLE;
        } else if (!EnchantmentUnit.merge(weapon, current, incoming).equals(current)) {
            return ImbueStatus.READY;
        } else if (anyNew && EnchantmentUnit.isFull(weapon)) {
            return ImbueStatus.FULL;
        }
        return ImbueStatus.NOTHING_NEW;
    }

    public boolean canImbue() {
        return getStatus() == ImbueStatus.READY;
    }

    /**
     * Server side: moves the book's applicable enchantments into the weapon's Enchantment Unit and consumes the book.
     */
    public void imbue(Player player) {
        if (!canImbue()) {
            return;
        }

        ItemStack weapon = weaponSlot.getStack();
        ModuleTarget target = resolve(weapon);
        if (target == null) {
            return;
        }

        ItemEnchantments result = EnchantmentUnit.merge(weapon, target.config().get(), EnchantmentUnit.readEnchantments(bookSlot.getStack()));
        apply(player, target, weapon, result);
        bookSlot.shrinkStack(1, Action.EXECUTE);
    }

    /**
     * Server side: strips a single enchantment off the unit and hands it back to the player as an enchanted book.
     */
    public void removeEnchantment(Player player, ResourceLocation id) {
        ItemStack weapon = weaponSlot == null ? ItemStack.EMPTY : weaponSlot.getStack();
        if (weapon.isEmpty()) {
            return;
        }
        ModuleTarget target = resolve(weapon);
        if (target == null) {
            return;
        }

        ItemEnchantments current = target.config().get();
        Holder<Enchantment> enchantment = null;
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : current.entrySet()) {
            if (entry.getKey().unwrapKey().filter(key -> key.location().equals(id)).isPresent()) {
                enchantment = entry.getKey();
                break;
            }
        }
        if (enchantment == null) {
            return;
        }

        int level = current.getLevel(enchantment);
        ItemEnchantments.Mutable remaining = new ItemEnchantments.Mutable(current);
        remaining.set(enchantment, 0);
        apply(player, target, weapon, remaining.toImmutable());

        ItemEnchantments.Mutable onBook = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        onBook.set(enchantment, level);
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        book.set(DataComponents.STORED_ENCHANTMENTS, onBook.toImmutable());
        giveToPlayer(player, book);
    }

    @Nullable
    private ModuleTarget resolve(ItemStack weapon) {
        IModule<EnchantmentUnit> module = EnchantmentUnit.getModule(weapon);
        IModuleContainer container = IModuleHelper.INSTANCE.getModuleContainer(weapon);
        if (module == null || container == null) {
            return null;
        }
        ModuleConfig<ItemEnchantments> config = module.getConfig(EnchantmentUnit.ENCHANTMENTS);
        return config == null ? null : new ModuleTarget(module, container, config);
    }

    private void apply(Player player, ModuleTarget target, ItemStack weapon, ItemEnchantments enchantments) {
        target.container().replaceModuleConfig(player.registryAccess(), weapon, target.module().getDataHolder(), target.config().with(enchantments));
        weaponSlot.setStack(weapon);
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        if (!player.level().isClientSide()) {
            returnToPlayer(player, bookSlot);
            returnToPlayer(player, weaponSlot);
            //Slots are empty now, so this clears the persisted copy
            persist();
        }
    }

    private void returnToPlayer(Player player, BasicInventorySlot slot) {
        if (slot == null) {
            return;
        }
        ItemStack contents = slot.getStack().copy();
        if (!contents.isEmpty()) {
            slot.setEmpty();
            giveToPlayer(player, contents);
        }
    }

    private void giveToPlayer(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    public record StoredEnchantment(Holder<Enchantment> enchantment, int level, ResourceLocation id) {
    }

    private record ModuleTarget(IModule<EnchantmentUnit> module, IModuleContainer container, ModuleConfig<ItemEnchantments> config) {
    }

    public enum ImbueStatus {
        NO_WEAPON,
        NO_BOOK,
        NOT_SUPPORTED,
        INCOMPATIBLE,
        FULL,
        NOTHING_NEW,
        READY
    }
}
