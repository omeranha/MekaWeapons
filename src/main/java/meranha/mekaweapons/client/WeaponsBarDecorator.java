package meranha.mekaweapons.client;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.lang.reflect.Method;
import java.util.List;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.client.render.item.ChemicalFluidBarDecorator;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.GenericTankSpec;
import mekanism.common.capabilities.chemical.item.ChemicalTankSpec;
import meranha.mekaweapons.items.ItemMekaBow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;

public class WeaponsBarDecorator implements IItemDecorator {

    public static final WeaponsBarDecorator INSTANCE = new WeaponsBarDecorator();

    private static final Method RENDER_BAR_CHEMICAL = findRenderBar(IChemicalTank.class);
    private static final Method RENDER_BAR_FLUID = findRenderBar(IExtendedFluidTank.class);
    private static final Method RENDER_BAR_RAW = findRenderBarRaw();
    private static final Method GET_DISPLAY_TANK = findGetDisplayTank();

    private WeaponsBarDecorator() {
    }

    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemMekaBow bow)) {
            return false;
        }
        yOffset += 12;

        if (tryRender(guiGraphics, stack, xOffset, yOffset, bow.getChemicalTankSpecs())) {
            yOffset--;
        }
        return true;
    }

    private boolean tryRender(GuiGraphics guiGraphics, ItemStack stack, int xOffset, int yOffset, List<ChemicalTankSpec> chemicalTankSpecs) {
        if (!chemicalTankSpecs.isEmpty()) {
            List<IChemicalTank> tanks = ContainerType.CHEMICAL.getAttachmentContainersIfPresent(stack);
            int tank = getDisplayTank(chemicalTankSpecs, stack, tanks.size());
            if (tank != -1) {
                renderBar(RENDER_BAR_CHEMICAL, guiGraphics, xOffset, yOffset, tanks.get(tank));
            } else if (tanks.isEmpty()) {
                renderBarRaw(guiGraphics, xOffset, yOffset, 0, 1, 0xFFFFFFFF);
            }
            return true;
        }
        return false;
    }

    private static <TYPE> int getDisplayTank(List<? extends GenericTankSpec<TYPE>> tankSpecs, ItemStack stack, int tanks) {
        if (tanks == 0) {
            return -1;
        } else if (tanks > 1 && tanks == tankSpecs.size() && Minecraft.getInstance().level != null) {
            IntList tankIndices = new IntArrayList(tanks);
            for (int i = 0; i < tanks; i++) {
                if (tankSpecs.get(i).supportsStack(stack)) {
                    tankIndices.add(i);
                }
            }
            int displayTank = invokeGetDisplayTank(tankIndices.size());
            return displayTank == -1 ? -1 : tankIndices.getInt(displayTank);
        }
        for (int i = 0; i < tanks && i < tankSpecs.size(); i++) {
            if (tankSpecs.get(i).supportsStack(stack)) {
                return i;
            }
        }
        return -1;
    }

    private static void renderBar(Method method, GuiGraphics guiGraphics, int xOffset, int yOffset, Object tank) {
        try {
            method.invoke(null, guiGraphics, xOffset, yOffset, tank);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to invoke ChemicalFluidBarDecorator.renderBar", e);
        }
    }

    private static void renderBarRaw(GuiGraphics guiGraphics, int xOffset, int yOffset, long stored, long capacity, int color) {
        try {
            RENDER_BAR_RAW.invoke(null, guiGraphics, xOffset, yOffset, stored, capacity, color);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to invoke ChemicalFluidBarDecorator.renderBar", e);
        }
    }

    private static int invokeGetDisplayTank(int tanks) {
        try {
            return (int) GET_DISPLAY_TANK.invoke(null, tanks);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to invoke ChemicalFluidBarDecorator.getDisplayTank", e);
        }
    }

    private static Method findRenderBar(Class<?> tankType) {
        try {
            Method method = ChemicalFluidBarDecorator.class.getDeclaredMethod("renderBar", GuiGraphics.class, int.class, int.class, tankType);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Could not find ChemicalFluidBarDecorator.renderBar for " + tankType, e);
        }
    }

    private static Method findRenderBarRaw() {
        try {
            Method method = ChemicalFluidBarDecorator.class.getDeclaredMethod("renderBar", GuiGraphics.class, int.class, int.class, long.class, long.class, int.class);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Could not find ChemicalFluidBarDecorator.renderBar(long, long, int)", e);
        }
    }

    private static Method findGetDisplayTank() {
        try {
            Method method = ChemicalFluidBarDecorator.class.getDeclaredMethod("getDisplayTank", int.class);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Could not find ChemicalFluidBarDecorator.getDisplayTank", e);
        }
    }
}
