package meranha.mekatana;

import mekanism.api.MekanismIMC;
import mekanism.common.Mekanism;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.registries.MekanismModules;
import meranha.mekatana.client.WeaponsConfig;
import meranha.mekatana.items.ItemQuiver;
import meranha.mekatana.items.ModItems;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(MekaWeapons.MOD_ID)
public class MekaWeapons {

    private static final Logger LOGGER = LogManager.getLogger("BowInfinityFix");
    public static final String MOD_ID = "mekaweapons";
    public static final Item quiver = new ItemQuiver(new Item.Properties().tab(Mekanism.tabMekanism).stacksTo(1));
    public static final MekanismHooks hooks = new MekanismHooks();

    public MekaWeapons() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        WeaponsConfig.registerConfigs(ModLoadingContext.get());
        ModItems.ITEMS.register(modEventBus);
        modEventBus.addGenericListener(Item.class, this::item);
        modEventBus.addListener(this::enqueueIMC);
        modEventBus.addListener(this::handleIMC);
        MinecraftForge.EVENT_BUS.addListener(this::infinityFix);
        LOGGER.info("Fix Registered!");
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        hooks.sendIMCMessages(event);
        InterModComms.sendTo(CuriosApi.MODID, SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.BACK.getMessageBuilder().build());
        WeaponsIMC.addModulesToAll(MekanismModules.ENERGY_UNIT);
        WeaponsIMC.addMekaTanaModules(MekanismModules.ATTACK_AMPLIFICATION_UNIT);
        WeaponsIMC.addMekaBowModules(MekanismModules.ATTACK_AMPLIFICATION_UNIT);
    }

    private void handleIMC(InterModProcessEvent event) {
        WeaponsModuleHelper.WEAPONSINSTANCE.processIMC();
    }

    private void item(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(quiver.setRegistryName("quiver"));
    }

    private void infinityFix(final ArrowNockEvent event) {
        ItemStack quiver = CuriosApi.getCuriosHelper().findEquippedCurio(MekaWeapons.quiver, event.getPlayer()).map(ImmutableTriple::getRight).orElse(ItemStack.EMPTY);
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, event.getBow()) > 0) {
            event.getPlayer().startUsingItem(event.getHand());
            event.setAction(ActionResult.success(event.getBow()));
        }
    }
}
