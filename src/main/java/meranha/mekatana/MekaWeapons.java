package meranha.mekatana;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MekaWeapons.MOD_ID)
public class MekaWeapons {

    public static final String MOD_ID = "mekaweapons";

    public MekaWeapons() {
        ModConfig.registerConfigs(ModLoadingContext.get());
        Items.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
