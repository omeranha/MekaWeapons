package meranha.mekaweapons.items;

import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.common.item.interfaces.IGuiItem;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import meranha.mekaweapons.MekaWeapons;
import meranha.mekaweapons.client.InfuserContents;
import meranha.mekaweapons.client.WeaponsLang;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Handheld station that transfers the enchantments of an enchanted book into the Enchantment Unit installed on a
 * weapon.
 */
public class ItemEnchantmentInfuser extends Item implements IGuiItem {

    public ItemEnchantmentInfuser(@NotNull Properties properties) {
        super(properties.rarity(Rarity.RARE).stacksTo(1));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(WeaponsLang.INFUSER.translateColored(EnumColor.WHITE));
        InfuserContents contents = stack.get(MekaWeapons.INFUSER_CONTENTS.get());
        if (contents != null && !contents.isEmpty()) {
            tooltip.add(WeaponsLang.INFUSER_HOLDING.translateColored(EnumColor.YELLOW));
            for (ItemStack held : contents.nonEmpty()) {
                tooltip.add(Component.literal(held.getCount() + "x ").append(held.getHoverName()).withStyle(ChatFormatting.GRAY));
            }
        }
    }

    @Override
    public void onDestroyed(@NotNull ItemEntity item, @NotNull DamageSource damageSource) {
        InfuserContents contents = item.getItem().get(MekaWeapons.INFUSER_CONTENTS.get());
        if (contents != null && !contents.isEmpty()) {
            ItemUtils.onContainerDestroyed(item, contents.nonEmpty());
        }
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level world, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            getContainerType().tryOpenGui(serverPlayer, hand, stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
    }

    @Override
    public ContainerTypeRegistryObject<?> getContainerType() {
        return MekaWeapons.ENCHANTMENT_INFUSER_CONTAINER;
    }

    @Override
    public boolean shouldCauseReequipAnimation(@NotNull ItemStack oldStack, @NotNull ItemStack newStack, boolean slotChanged) {
        //Stashing items writes a component onto the held stack, which would otherwise flicker the held item
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }
}
