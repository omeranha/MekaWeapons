package meranha.mekaweapons.items.modules;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntFunction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mekanism.api.IIncrementalEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.api.radial.IRadialDataHelper;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.api.radial.mode.NestedRadialMode;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import meranha.mekaweapons.MekaWeapons;
import meranha.mekaweapons.WeaponsLang;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.Lazy;

@ParametersAreNotNullByDefault
public class DrawSpeedUnit implements ICustomModule<DrawSpeedUnit> {

    public static final ResourceLocation DRAWSPEED = MekaWeapons.rl("drawspeed");
    private static final Int2ObjectMap<Lazy<NestedRadialMode>> RADIAL_DATAS = Util.make(() -> {
        int types = DrawSpeed.values().length - 1;
        Int2ObjectMap<Lazy<NestedRadialMode>> map = new Int2ObjectArrayMap<>(types);
        for (int type = 1; type <= types; type++) {
            int accessibleValues = type + 1;
            map.put(type, Lazy.of(() -> new NestedRadialMode(IRadialDataHelper.INSTANCE.dataForTruncated(DRAWSPEED, accessibleValues, DrawSpeed.LOW),
                    WeaponsLang.RADIAL_DRAWSPEED_MODE, DrawSpeed.LOW.icon(), EnumColor.YELLOW)));
        }
        return map;
    });

    private IModuleConfigItem<DrawSpeed> drawSpeed;

    @Override
    public void init(IModule<DrawSpeedUnit> module, ModuleConfigItemCreator configItemCreator) {
        drawSpeed = configItemCreator.createConfigItem("drawspeed", WeaponsLang.RADIAL_DRAWSPEED_MODE,
                new ModuleEnumData<>(DrawSpeed.LOW, module.getInstalledCount() + 1));
    }

    @NotNull
    private NestedRadialMode getNestedData(IModule<DrawSpeedUnit> module) {
        return RADIAL_DATAS.get(module.getInstalledCount()).get();
    }

    @NotNull
    private RadialData<?> getRadialData(IModule<DrawSpeedUnit> module) {
        return getNestedData(module).nestedData();
    }

    public void addRadialModes(IModule<DrawSpeedUnit> module, ItemStack stack, Consumer<NestedRadialMode> adder) {
        adder.accept(getNestedData(module));
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <MODE extends IRadialMode> MODE getMode(IModule<DrawSpeedUnit> module, ItemStack stack,
                                                   RadialData<MODE> radialData) {
        return radialData == getRadialData(module) ? (MODE) drawSpeed.get() : null;
    }

    public <MODE extends IRadialMode> boolean setMode(IModule<DrawSpeedUnit> module, Player player, ItemStack stack, RadialData<MODE> radialData, MODE mode) {
        if (radialData == getRadialData(module)) {
            DrawSpeed newMode = (DrawSpeed) mode;
            if (drawSpeed.get() != newMode) {
                drawSpeed.set(newMode);
                return true;
            }
        }
        return false;
    }

    public void changeMode(IModule<DrawSpeedUnit> module, Player player, ItemStack stack, int shift, boolean displayChangeMessage) {
        DrawSpeed speed = drawSpeed.get();
        DrawSpeed newMode = speed.adjust(shift, v -> v.ordinal() < module.getInstalledCount() + 1);
        if (speed != newMode) {
            if (displayChangeMessage) {
                module.displayModeChange(player, MekanismLang.MODULE_EFFICIENCY.translate(), newMode);
            }
            drawSpeed.set(newMode);
        }
    }

    @Override
    public void addHUDStrings(IModule<DrawSpeedUnit> module, Player player, Consumer<Component> hudStringAdder) {
        if (module.isEnabled()) {
            hudStringAdder.accept(WeaponsLang.DRAWSPEED.translateColored(EnumColor.DARK_GRAY, EnumColor.INDIGO, drawSpeed.get().sliceName()));
        }
    }

    public int getDrawSpeed() {
        return drawSpeed.get().ordinal();
    }

    @NothingNullByDefault
    public enum DrawSpeed implements IIncrementalEnum<DrawSpeed>, IHasTextComponent, IRadialMode, StringRepresentable {
        OFF(WeaponsLang.RADIAL_TOGGLE_OFF, EnumColor.WHITE, "off"),
        LOW(WeaponsLang.RADIAL_ATTACK_DAMAGE_LOW, EnumColor.PINK, "damage_low"),
        MED(WeaponsLang.RADIAL_ATTACK_DAMAGE_MEDIUM, EnumColor.BRIGHT_GREEN, "damage_medium"),
        HIGH(WeaponsLang.RADIAL_ATTACK_DAMAGE_HIGH, EnumColor.YELLOW, "damage_high");

        private final String serializedName;
        private final Component label;
        private final ResourceLocation icon;
        private final Component sliceNamePreCalc;
        public static final IntFunction<DrawSpeed> BY_ID = ByIdMap.continuous(DrawSpeed::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);

        DrawSpeed(ILangEntry langEntry, EnumColor color, String texture) {
            this.serializedName = name().toLowerCase(Locale.ROOT);
            this.icon = MekaWeapons.getResource(MekanismUtils.ResourceType.GUI_RADIAL, texture + ".png");
            this.label = TextComponentUtil.getString(Integer.toString(this.ordinal()));
            this.sliceNamePreCalc = langEntry.translateColored(color);
        }

        @Override
        public Component getTextComponent() {
            return label;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }

        public Component sliceName() {
            return sliceNamePreCalc;
        }

        public ResourceLocation icon() {
            return icon;
        }

        public DrawSpeed byIndex(int index) {
            return BY_ID.apply(index);
        }
    }
}
