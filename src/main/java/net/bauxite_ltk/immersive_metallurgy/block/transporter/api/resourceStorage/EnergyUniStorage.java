package net.bauxite_ltk.immersive_metallurgy.block.transporter.api.resourceStorage;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class EnergyUniStorage implements ICompactUniStorage<Integer, IEnergyStorage>{
    EnergyStorage energyStorage;

    public EnergyUniStorage(EnergyStorage energyStorage){
        this.energyStorage = energyStorage;
    }

    public EnergyUniStorage(int capacity){
        this.energyStorage = new EnergyStorage(capacity);
    }

    @Override
    public IEnergyStorage getCompactParent() {
        return energyStorage;
    }

    @Override
    public CompoundTag toNBT(HolderLookup.Provider provider){
        CompoundTag tag = new CompoundTag();
        tag.put("parent_handler", energyStorage.serializeNBT(provider));
        return tag;
    }

    @Override
    public void loadFromNBT(CompoundTag tag, HolderLookup.Provider provider) {
        energyStorage.deserializeNBT(provider, tag.get("parent_handler"));
    }

    @Override
    public int receiveInStorage(Integer i, boolean simulate) {
        return energyStorage.receiveEnergy(i,simulate);
    }

    @Override
    public Integer extractFromStorage(Integer i, boolean simulate) {
        return energyStorage.extractEnergy(i, simulate);
    }

    @Override
    public Integer extractFromStorage(int amount, boolean simulate) {
        return energyStorage.extractEnergy(amount, simulate);
    }

    @Override
    public int getInStorageAmount() {
        return energyStorage.getEnergyStored();
    }

    @Override
    public int getStorageCapacity() {
        return energyStorage.getMaxEnergyStored();
    }

    @Override
    public Integer getResourceInStorage() {
        return energyStorage.getEnergyStored();
    }
}
