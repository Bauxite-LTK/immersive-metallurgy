package net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.resolver.handler;

import net.bauxite_ltk.immersive_metallurgy.block.transporter.api.resourceHandler.ICompactUniHandler;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.blockface.BlockFace;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.resolver.ICableNodeInBlockManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public class CableHandler<R, H> implements ICompactUniHandler<R, H>{
    final ICompactUniHandler<R, H> parent;
    final ICableNodeInBlockManager<R> manager;
    final BlockFace face;

    public CableHandler(ICompactUniHandler<R, H> parent, ICableNodeInBlockManager<R> manager, BlockFace atFace){
        this.parent = parent;
        this.manager = manager;
        this.face = atFace;
    }

    @Override
    public H getCompactParent() {
        return parent.getCompactParent();
    }

    public ICableNodeInBlockManager<R> getManager(){
        return manager;
    }

    @Override
    public CompoundTag toNBT(HolderLookup.Provider provider){
        return parent.toNBT(provider);
    }

    @Override
    public void loadFromNBT(CompoundTag tag, HolderLookup.Provider provider) {
        parent.loadFromNBT(tag,provider);
    }



    @Override
    public int receiveResource(R resource, boolean simulate) {
        return manager.handleResourceInput(resource, face, simulate);
    }

    @Override
    public R extractResource(R resource, boolean simulate) {
        return manager.handleResourceOutput(resource, face, simulate);
    }

    @Override
    public R extractResource(int amount, boolean simulate) {
        return manager.handleResourceOutput(amount, face, simulate);
    }




    @Override
    public int getResourceAmount(int storageId) {
        return parent.getResourceAmount(storageId);
    }

    @Override
    public int getCapacity(int storageId) {
        return parent.getCapacity(storageId);
    }

    @Override
    public int getStoragesCount() {
        return parent.getStoragesCount();
    }

    @Override
    public R getResource(int storageId) {
        return parent.getResource(storageId);
    }




}
