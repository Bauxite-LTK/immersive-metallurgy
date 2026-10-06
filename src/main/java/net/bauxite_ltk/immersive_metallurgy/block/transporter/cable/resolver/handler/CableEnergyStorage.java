package net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.resolver.handler;

import net.bauxite_ltk.immersive_metallurgy.block.transporter.api.resourceHandler.ICompactUniHandler;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.api.resourceStorage.EnergyUniStorage;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.blockface.BlockFace;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.resolver.CableBlockEnergyManager;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.resolver.ICableNodeInBlockManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class CableEnergyStorage implements IEnergyStorage {
    final CableBlockEnergyManager manager;
    final BlockFace face;

    public CableEnergyStorage(CableBlockEnergyManager manager, BlockFace atFace){
        this.manager = manager;
        this.face = atFace;
    }

    public CableBlockEnergyManager getManager(){
        return manager;
    }

    public CompoundTag toNBT(HolderLookup.Provider provider){
        return new CompoundTag();
    }

    public void loadFromNBT(CompoundTag tag, HolderLookup.Provider provider) {}


    @Override
    public int receiveEnergy(int energy, boolean simulate) {
        return manager.handleResourceInput(energy, face, simulate);
    }

    @Override
    public int extractEnergy(int energy, boolean simulate) {
        return manager.handleResourceOutput(energy, face, simulate);
    }

    @Override
    public int getEnergyStored() {
        return 0;
    }

    @Override
    public int getMaxEnergyStored() {
        return 0;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }



}
