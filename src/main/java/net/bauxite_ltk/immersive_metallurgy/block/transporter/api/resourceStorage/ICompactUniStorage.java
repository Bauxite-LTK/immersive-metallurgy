package net.bauxite_ltk.immersive_metallurgy.block.transporter.api.resourceStorage;

import net.bauxite_ltk.immersive_metallurgy.block.transporter.api.resourceHandler.ICompactUniHandler;

public interface ICompactUniStorage<R, Handler>  extends IUniStorage<R>, ICompactUniHandler<R,Handler> {
    Handler getCompactParent();

    @Override
    default int receiveResource(R resource, int index, boolean simulate){
        return receiveInStorage(resource,simulate);
    }

    @Override
    default int getResourceAmount(int storageId){
        return getInStorageAmount();
    }

    @Override
    default int getCapacity(int storageId){
        return getStorageCapacity();
    }

    @Override
    default int getStoragesCount(){
        return 1;
    }

    @Override
    default R extractResource(int amount, boolean simulate){
        return extractFromStorage(amount, simulate);
    }

    @Override
    default R extractResource(R resource, boolean simulate){
        return extractFromStorage(resource, simulate);
    }

    @Override
    default int receiveResource(R resource, boolean simulate){
        return receiveInStorage(resource, simulate);
    }

    @Override
    default R getResource(int storageId){
        return getResourceInStorage();
    }
}
