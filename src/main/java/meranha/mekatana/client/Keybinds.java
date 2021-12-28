package meranha.mekatana.client;

import mekanism.client.ClientRegistrationUtil;
import mekanism.client.MekanismClient;
import mekanism.client.key.MekKeyBindingBuilder;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.ModuleTweakerContainer;
import mekanism.common.inventory.container.item.PersonalChestItemContainer;
import mekanism.common.network.to_server.PacketOpenGui;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registries.MekanismContainerTypes;
import meranha.mekatana.WeaponsLang;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public class Keybinds {
    //public static final KeyBinding PersonalChestKey = new MekKeyBindingBuilder().description(WeaponsLang.KEY_PERSONAL).conflictInGame().keyCode(GLFW.GLFW_KEY_O)
            //.onKeyDown((kb, isRepeat) -> {
                //PlayerEntity player = Minecraft.getInstance().player;
    //Hand hand = null;
    //ItemStack stack = player.getItemInHand(hand);
    //if (!player.isSleeping()) {
    //getContainerType().tryOpenGui((ServerPlayerEntity) player, hand, stack);
    //}
    //}).build();
    public static void registerKeybindings() {
        ClientRegistrationUtil.registerKeyBindings();
    }
}
