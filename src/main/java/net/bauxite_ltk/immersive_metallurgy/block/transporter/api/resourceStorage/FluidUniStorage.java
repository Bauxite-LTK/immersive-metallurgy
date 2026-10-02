package net.bauxite_ltk.immersive_metallurgy.block.transporter.api.resourceStorage;

import net.bauxite_ltk.immersive_metallurgy.block.transporter.api.resourceHandler.IUniHandler;
import net.bauxite_ltk.immersive_metallurgy.util.IMUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class FluidUniStorage implements ICompactUniStorage<FluidStack, IFluidHandler>{
    FluidTank tank;

    public FluidUniStorage(int initialCapacity){
        this.tank = new FluidTank(initialCapacity);
    }

    public FluidUniStorage(FluidTank tank){
        this.tank = tank;
    }

    @Override
    public int receiveInStorage(FluidStack fluidStack, boolean simulate) {
        return tank.fill(fluidStack, simulate? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
    }

    @Override
    public IFluidHandler getCompactParent() {
        return tank;
    }

    @Override
    public FluidStack extractFromStorage(FluidStack fluidStack, boolean simulate) {
        return extractFromStorage(fluidStack.getAmount(), simulate);
    }

    @Override
    public FluidStack extractFromStorage(int amount, boolean simulate) {
        return tank.drain(amount, simulate? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
    }

    @Override
    public int getResourceAmount(int storageId) {
        return getInStorageAmount();
    }

    @Override
    public int getCapacity(int storageId) {
        return getStorageCapacity();
    }

    @Override
    public int getStoragesCount() {
        return getTanks();
    }

    @Override
    public FluidStack getResource(int storageId) {
        return getResourceInStorage();
    }

    @Override
    public CompoundTag toNBT(HolderLookup.Provider provider) {
        return tank.writeToNBT(provider, new CompoundTag());
    }

    @Override
    public void loadFromNBT(CompoundTag tag, HolderLookup.Provider provider) {
        tank.readFromNBT(provider, tag);
    }


    @Override
    public int getInStorageAmount() {
        return tank.getFluidAmount();
    }

    @Override
    public int getStorageCapacity() {
        return tank.getCapacity();
    }

    @Override
    public FluidStack getResourceInStorage() {
        return tank.getFluid();
    }

    public int getTanks(){
        return 1;
    }

    public FluidStack getFluid(){
        return tank.getFluid();
    }


    public int setFluidAmount(Fluid fluid, int amount){
        int thisAmount = tank.getFluidAmount();
        if(amount>tank.getCapacity()) IMUtils.LOGGER.warn("Pressure Pipe Set Fluid Amount: Larger Than Capacity!");
        if(amount == thisAmount) return tank.getFluidAmount();
        if(!tank.getFluid().isEmpty() && !fluid.isSame(tank.getFluid().getFluid())) return 0;
        if(amount > thisAmount){
            int fillResult = tank.fill(new FluidStack(fluid, amount-thisAmount), IFluidHandler.FluidAction.EXECUTE);
            if(fillResult != amount - thisAmount) IMUtils.LOGGER.error("Pressure Pipe Set Fluid Amount: Unexpected Fill Amount!");
            return tank.getFluidAmount();
        }
        else {
            int drainResult = tank.drain(thisAmount - amount, IFluidHandler.FluidAction.EXECUTE).getAmount();
            if(drainResult != thisAmount - amount) IMUtils.LOGGER.error("Pressure Pipe Set Fluid Amount: Unexpected Drain Amount!");
            return tank.getFluidAmount();
        }
    }

    public FluidTank readFromNBT(HolderLookup.Provider lookupProvider, CompoundTag nbt){
        return tank.readFromNBT(lookupProvider,nbt);
    }

    public CompoundTag writeToNBT(HolderLookup.Provider lookupProvider, CompoundTag nbt){
        return tank.writeToNBT(lookupProvider,nbt);
    }
}
