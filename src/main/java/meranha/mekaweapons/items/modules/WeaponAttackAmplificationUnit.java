package meranha.mekaweapons.items.modules;

import static meranha.mekaweapons.MekaWeaponsUtils.*;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mekanism.api.IIncrementalEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.radial.IRadialDataHelper;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.api.radial.mode.NestedRadialMode;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import meranha.mekaweapons.MekaWeapons;
import meranha.mekaweapons.client.WeaponsLang;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.TranslatableEnum;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ParametersAreNotNullByDefault
public record WeaponAttackAmplificationUnit(AttackDamage attackDamage) implements ICustomModule<WeaponAttackAmplificationUnit> {
    public static final ResourceLocation ATTACK_DAMAGE = Mekanism.rl("bonus_attack_damage");
    private static final ResourceLocation RADIAL_ID = MekaWeapons.rl("attack_damage");
    private static final Int2ObjectMap<Lazy<NestedRadialMode>> RADIAL_DATAS = Util.make(() -> {
        int types = AttackDamage.values().length - 2;
        Int2ObjectMap<Lazy<NestedRadialMode>> map = new Int2ObjectArrayMap<>(types);
        for (int type = 1; type <= types; type++) {
            int accessibleValues = type + 2;
            map.put(type, Lazy.of(() -> new NestedRadialMode(IRadialDataHelper.INSTANCE.dataForTruncated(RADIAL_ID, accessibleValues, AttackDamage.MED),
                    WeaponsLang.RADIAL_ATTACK_DAMAGE, AttackDamage.MED.icon(), EnumColor.RED)));
        }
        return map;
    });

    public WeaponAttackAmplificationUnit(IModule<WeaponAttackAmplificationUnit> module) {
        this(module.<AttackDamage>getConfigOrThrow(ATTACK_DAMAGE).get());
    }

    public void addHUDStrings(IModule<WeaponAttackAmplificationUnit> module, IModuleContainer moduleContainer, ItemStack stack, Player player, Consumer<Component> hudStringAdder) {
        if (module.isEnabled()) {
            hudStringAdder.accept(MekanismLang.MODULE_DAMAGE.translateColored(EnumColor.DARK_GRAY, EnumColor.INDIGO, getCurrentMaxDamage(stack)));
        }
    }

    @NotNull
    private NestedRadialMode getNestedData(IModule<WeaponAttackAmplificationUnit> module) {
        return RADIAL_DATAS.get(module.getInstalledCount()).get();
    }

    @NotNull
    private RadialData<?> getRadialData(IModule<WeaponAttackAmplificationUnit> module) {
        return getNestedData(module).nestedData();
    }

    @Override
    public void addRadialModes(IModule<WeaponAttackAmplificationUnit> module, ItemStack stack, Consumer<NestedRadialMode> adder) {
        adder.accept(getNestedData(module));
    }

    public int getCurrentUnit() {
        return attackDamage.ordinal();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <MODE extends IRadialMode> MODE getMode(IModule<WeaponAttackAmplificationUnit> module, ItemStack stack, RadialData<MODE> radialData) {
        return radialData == getRadialData(module) ? (MODE) attackDamage : null;
    }

    public <MODE extends IRadialMode> boolean setMode(IModule<WeaponAttackAmplificationUnit> module, Player player, IModuleContainer moduleContainer, ItemStack stack, RadialData<MODE> radialData, MODE mode) {
        if (radialData == getRadialData(module)) {
            AttackDamage newMode = (AttackDamage) mode;
            if (attackDamage != newMode) {
                moduleContainer.replaceModuleConfig(player.registryAccess(), stack, module.getDataHolder(), module.<AttackDamage>getConfigOrThrow(ATTACK_DAMAGE).with(newMode));
                return true;
            }
        }
        return false;
    }

    @NotNull
    public Component getModeScrollComponent(IModule<WeaponAttackAmplificationUnit> module, ItemStack stack) {
        return MekanismLang.GENERIC_WITH_PARENTHESIS.translateColored(EnumColor.INDIGO, attackDamage.sliceName(), EnumColor.AQUA, getCurrentMaxDamage(stack));
    }

    public void changeMode(IModule<WeaponAttackAmplificationUnit> module, Player player, IModuleContainer moduleContainer, ItemStack stack, int shift, boolean displayChangeMessage) {
        AttackDamage newMode = attackDamage.adjust(shift, v -> v.ordinal() < module.getInstalledCount() + 2);
        if (attackDamage != newMode) {
            if (displayChangeMessage) {
                module.displayModeChange(player, MekanismLang.MODULE_EFFICIENCY.translate(), newMode);
            }
            moduleContainer.replaceModuleConfig(player.registryAccess(), stack, module.getDataHolder(), module.<AttackDamage>getConfigOrThrow(ATTACK_DAMAGE).with(newMode));
        }
    }

    @NothingNullByDefault
    public enum AttackDamage implements IIncrementalEnum<AttackDamage>, IHasTextComponent, TranslatableEnum, IRadialMode, StringRepresentable {
        OFF(WeaponsLang.RADIAL_TOGGLE_OFF, EnumColor.WHITE, "off"),
        LOW(WeaponsLang.RADIAL_ATTACK_DAMAGE_LOW, EnumColor.PINK, "damage_low"),
        MED(WeaponsLang.RADIAL_ATTACK_DAMAGE_MEDIUM, EnumColor.BRIGHT_GREEN, "damage_medium"),
        HIGH(WeaponsLang.RADIAL_ATTACK_DAMAGE_HIGH, EnumColor.YELLOW, "damage_high"),
        SUPER_HIGH(WeaponsLang.RADIAL_ATTACK_DAMAGE_SUPER, EnumColor.ORANGE, "damage_super"),
        EXTREME(WeaponsLang.RADIAL_ATTACK_DAMAGE_EXTREME, EnumColor.RED, "damage_extreme");

        public static final Codec<AttackDamage> CODEC = StringRepresentable.fromEnum(AttackDamage::values);
        public static final IntFunction<AttackDamage> BY_ID = ByIdMap.continuous(AttackDamage::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, AttackDamage> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, AttackDamage::ordinal);
        private final String serializedName;
        private final Component label;
        private final ResourceLocation icon;
        private final Component sliceNamePreCalc;

        AttackDamage(ILangEntry langEntry, EnumColor color, String texture) {
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

        public Component getTranslatedName() {
            return sliceName();
        }

        public Component sliceName() {
            return sliceNamePreCalc;
        }

        public ResourceLocation icon() {
            return icon;
        }

        public AttackDamage byIndex(int index) {
            return BY_ID.apply(index);
        }
    }

    private int getCurrentMaxDamage(ItemStack stack) {
        return getBaseDamage(stack) * getCurrentUnit();
    }
}
