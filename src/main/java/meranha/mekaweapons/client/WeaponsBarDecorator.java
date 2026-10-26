package meranha.mekaweapons.client;

import java.util.List;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.math.MathUtils;
import mekanism.client.gui.GuiUtils;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.GenericTankSpec;
import mekanism.common.capabilities.chemical.item.ChemicalTankSpec;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.StorageUtils;
import meranha.mekaweapons.items.ItemMekaBow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;
import net.neoforged.neoforge.fluids.FluidStack;

public class WeaponsBarDecorator implements IItemDecorator {

    public static final WeaponsBarDecorator INSTANCE = new WeaponsBarDecorator();
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
                renderBar(guiGraphics, xOffset, yOffset, tanks.get(tank));
            } else if (tanks.isEmpty()) {
                renderBar(guiGraphics, xOffset, yOffset, 0, 1, 0xFFFFFFFF);
            }
            return true;
        }
        return false;
    }

    public static void renderBar(GuiGraphics guiGraphics, int stackXPos, int yPos, IChemicalTank tank) {
        renderBar(guiGraphics, stackXPos, yPos, tank.getStored(), tank.getCapacity(), tank.getStack().getChemicalColorRepresentation());
    }

    public static void renderBar(GuiGraphics guiGraphics, int stackXPos, int yPos, IExtendedFluidTank tank) {
        FluidStack fluid = tank.getFluid();
        renderBar(guiGraphics, stackXPos, yPos, (long)fluid.getAmount(), (long)tank.getCapacity(), FluidUtils.getRGBDurabilityForDisplay(fluid));
    }

    public static void renderBar(GuiGraphics guiGraphics, int stackXPos, int yPos, long amount, long capacity, int color) {
        int pixelWidth = convertWidth(StorageUtils.getRatio(amount, capacity));
        GuiUtils.fill(guiGraphics, RenderType.guiOverlay(), stackXPos + 2 + pixelWidth, yPos, 13 - pixelWidth, 1, -16777216);
        GuiUtils.fill(guiGraphics, RenderType.guiOverlay(), stackXPos + 2, yPos, pixelWidth, 1, color | -16777216);
    }

    private static int convertWidth(double width) {
        return MathUtils.clampToInt(Math.round((double)13.0F * width));
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
            int displayTank = getDisplayTank(tankIndices.size());
            return displayTank == -1 ? -1 : tankIndices.getInt(displayTank);
        }
        for (int i = 0; i < tanks && i < tankSpecs.size(); i++) {
            if (tankSpecs.get(i).supportsStack(stack)) {
                return i;
            }
        }
        return -1;
    }

    static int getDisplayTank(int tanks) {
        if (tanks == 0) {
            return -1;
        } else {
            return tanks > 1 ? Minecraft.getInstance().gui.getGuiTicks() / 20 % tanks : 0;
        }
    }
}
