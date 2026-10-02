package net.bauxite_ltk.immersive_metallurgy.block.transporter.api.resourceHandler.blt;

import net.bauxite_ltk.immersive_metallurgy.block.transporter.api.BlocklikeFluidTransporterBE;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.api.resourceHandler.FluidUniHandler;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class BLTSingleFluidUniHandler extends FluidUniHandler implements IFluidHandler {
    final FluidUniHandler fluidUniHandler;
    final BlocklikeFluidTransporterBE be;
    final Direction facing;

    public BLTSingleFluidUniHandler(@Nonnull FluidUniHandler fluidUniHandler, BlocklikeFluidTransporterBE instance, Direction facing) {
        super(fluidUniHandler.getCompactParent());
        this.fluidUniHandler = fluidUniHandler;
        this.be = instance;
        this.facing = facing;
    }

    @Override
    public int getTanks() {
        return fluidUniHandler.getStoragesCount();
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int i) {
        return fluidUniHandler.getResource(i);
    }

    @Override
    public int getTankCapacity(int i) {
        return fluidUniHandler.getCapacity(i);
    }

    @Override
    public boolean isFluidValid(int i, @NotNull FluidStack fluidStack) {
        return fluidUniHandler.getCompactParent().isFluidValid(i, fluidStack);
    }


    @Override
    public int fill(@NotNull FluidStack fluidStack, FluidAction fluidAction) {
        be.trySetSource(facing);
        int i = receiveResource(fluidStack, fluidAction.simulate());
        FluidStack last = fluidStack.copyWithAmount(fluidStack.getAmount() - i);
        if(i < fluidStack.getAmount() && be.isDirectionSourceFace(facing) && be.forceAllocatedExecuteCount < 8){
            //avoid execute infinite times
            be.forceAllocatedExecuteCount++;
            i += be.startAllocateResourceGlobal(facing, last, last.getAmount(), fluidAction.simulate());
        }
        be.setChanged();
        be.invalidateCapabilities();

        return i;
    }

    @Override
    public @NotNull FluidStack drain(@NotNull FluidStack fluidStack, FluidAction fluidAction) {
        FluidStack fs = extractResource(fluidStack, fluidAction.simulate());
        be.setChanged();
        be.invalidateCapabilities();
        return fs;
    }

    @Override
    public @NotNull FluidStack drain(int i, FluidAction fluidAction) {
        FluidStack fs = extractResource(i, fluidAction.simulate());
        be.setChanged();
        be.invalidateCapabilities();
        return fs;
    }

    public int setFluidAmount(Fluid fluid, int amount){
        int i = fluidUniHandler.setFluidAmount(fluid,amount);
        be.setChanged();
        be.invalidateCapabilities();
        return i;
    }
}
