package meranha.mekaweapons.mixin;

import com.google.common.collect.ImmutableSet;
import mekanism.api.gear.ModuleData;
import mekanism.api.providers.IItemProvider;
import mekanism.common.Mekanism;
import mekanism.common.content.gear.ModuleHelper;
import meranha.mekaweapons.MekaWeapons;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;


@Mixin(value = ModuleHelper.class, remap = false)
public abstract class ModuleHelperMixin {
    @Shadow
    protected abstract void mapSupportedModules(InterModProcessEvent event, String imcMethod, IItemProvider moduleContainer, Map<ModuleData<?>, ImmutableSet.Builder<Item>> supportedContainersBuilderMap);

    @Inject(
        method = "processIMC",
        at = @At(
            value = "INVOKE",
            ordinal = 4,
            shift = At.Shift.AFTER,
            target = "Lmekanism/common/content/gear/ModuleHelper;mapSupportedModules(Lnet/minecraftforge/fml/event/lifecycle/InterModProcessEvent;Ljava/lang/String;Lmekanism/api/providers/IItemProvider;Ljava/util/Map;)V"
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    void processIMC(InterModProcessEvent event, CallbackInfo ci, Map<ModuleData<?>, ImmutableSet.Builder<Item>> supportedContainersBuilderMap) {
        Mekanism.logger.debug("Weapons: Injected processIMC");

        mapSupportedModules(event, MekaWeapons.ADD_MEKA_BOW_MODULES, MekaWeapons.MEKA_BOW, supportedContainersBuilderMap);
        mapSupportedModules(event, MekaWeapons.ADD_MEKATANA_MODULES, MekaWeapons.MEKA_TANA, supportedContainersBuilderMap);
    }
}
