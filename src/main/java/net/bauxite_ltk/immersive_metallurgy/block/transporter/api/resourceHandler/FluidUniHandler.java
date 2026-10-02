package net.bauxite_ltk.immersive_metallurgy.block.transporter.api.resourceHandler;

import blusunrize.immersiveengineering.common.fluids.ArrayFluidHandler;
import net.bauxite_ltk.immersive_metallurgy.util.IMUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class FluidUniHandler implements ICompactUniHandler<FluidStack, IFluidHandler> {
    IFluidHandler parentHandler;

    protected FluidUniHandler(IFluidHandler parentHandler){
        this.parentHandler = parentHandler;
    }

    public static FluidUniHandler cast(IFluidHandler parentHandler){
        return new FluidUniHandler(parentHandler);
    }

    @Override
    public int receiveResource(FluidStack fluidStack, boolean simulate) {
        return parentHandler.fill(fluidStack, toAction(simulate));
    }

    @Override
    public int receiveResource(FluidStack fluidStack, int index, boolean simulate) {
        return parentHandler.fill(fluidStack.copyWithAmount(index), toAction(simulate));
    }

    @Override
    public FluidStack extractResource(FluidStack resource, boolean simulate) {
        return parentHandler.drain(resource, toAction(simulate));
    }

    @Override
    public FluidStack extractResource(int amount, boolean simulate) {
        return parentHandler.drain(amount, toAction(simulate));
    }

    @Override
    public int getResourceAmount(int storageId) {
        return parentHandler.getFluidInTank(storageId).getAmount();
    }

    @Override
    public int getCapacity(int storageId) {
        return parentHandler.getTankCapacity(storageId);
    }

    @Override
    public int getStoragesCount() {
        return parentHandler.getTanks();
    }

    @Override
    public FluidStack getResource(int storageId) {
        return parentHandler.getFluidInTank(storageId);
    }

    @Override
    public CompoundTag toNBT(HolderLookup.Provider provider) {
        return null;
    }

    @Override
    public void loadFromNBT(CompoundTag tag, HolderLookup.Provider provider) {
    }


    public int getFluidAmount(int i){
        return getResourceAmount(i);
    }


    public int setFluidAmount(Fluid fluid, int amount){
        for(int i = 0; i < getStoragesCount(); i++){
            if(getResource(i).getFluid().isSame(fluid) || getResource(i).isEmpty()){
                return setTankFluidAmount(i, fluid, amount);
            }
        }
        return 0;
    }

    private int setTankFluidAmount(int i, Fluid fluid, int amount){
        int thisAmount = getFluidAmount(i);
        if(amount > getCapacity(i)) IMUtils.LOGGER.warn("FluidUniHandler Set Fluid Amount: Larger Than Capacity!");
        if(amount == thisAmount) return getFluidAmount(i);
        if(amount > thisAmount){
            int fillResult = receiveResource(new FluidStack(fluid, amount-thisAmount), false);
            if(fillResult != amount - thisAmount) IMUtils.LOGGER.error("FluidUniHandler Set Fluid Amount: Unexpected Fill Amount!");
            return getFluidAmount(i);
        }
        else {
            int drainResult = extractResource(thisAmount - amount, false).getAmount();
            if(drainResult != thisAmount - amount) IMUtils.LOGGER.error("FluidUniHandler Set Fluid Amount: Unexpected Drain Amount!");
            return getFluidAmount(i);
        }
    }

    @Override
    public IFluidHandler getCompactParent() {
        return parentHandler;
    }

    public static IFluidHandler.FluidAction toAction(boolean b){
        return b? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE;
    }
}
