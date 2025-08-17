package meranha.mekaweapons.items;

import static meranha.mekaweapons.MekaWeaponsUtils.*;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import meranha.mekaweapons.MekaWeapons;
import meranha.mekaweapons.WeaponsLang;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@ParametersAreNotNullByDefault
public record ModuleWeaponAttackAmplificationUnit(AttackDamage attackDamage) implements ICustomModule<ModuleWeaponAttackAmplificationUnit> {
    public static final ResourceLocation ATTACK_DAMAGE = Mekanism.rl("bonus_attack_damage");

    public ModuleWeaponAttackAmplificationUnit(IModule<ModuleWeaponAttackAmplificationUnit> module) {
        this(module.<AttackDamage>getConfigOrThrow(ATTACK_DAMAGE).get());
    }

    public void addHUDStrings(IModule<ModuleWeaponAttackAmplificationUnit> module, IModuleContainer moduleContainer, ItemStack stack, Player player, Consumer<Component> hudStringAdder) {
        if (module.isEnabled()) {
            hudStringAdder.accept(MekanismLang.MODULE_DAMAGE.translateColored(EnumColor.DARK_GRAY, EnumColor.INDIGO, getCurrentMaxDamage(stack)));
        }
    }

    public int getCurrentUnit() {
        return attackDamage.ordinal();
    }

    @NothingNullByDefault
    public enum AttackDamage implements IHasTextComponent, StringRepresentable {
        OFF(WeaponsLang.RADIAL_ATTACK_DAMAGE_OFF, EnumColor.WHITE, "damage_off"),
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

        AttackDamage(ILangEntry langEntry, EnumColor color, String texture) {
            this.serializedName = name().toLowerCase(Locale.ROOT);
            this.label = TextComponentUtil.getString(Integer.toString(this.ordinal()));
        }

        public Component getTextComponent() {
            return label;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }

    private int getCurrentMaxDamage(ItemStack stack) {
        return getBaseDamage(stack) * getCurrentUnit();
    }
}
