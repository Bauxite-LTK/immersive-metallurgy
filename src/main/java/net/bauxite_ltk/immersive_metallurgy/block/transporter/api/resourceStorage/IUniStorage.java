package net.bauxite_ltk.immersive_metallurgy.block.transporter.api.resourceStorage;

public interface IUniStorage<R> {
    int receiveInStorage(R resource, boolean simulate);

    R extractFromStorage(R resource, boolean simulate);

    R extractFromStorage(int amount, boolean simulate);

    int getInStorageAmount();

    int getStorageCapacity();

    R getResourceInStorage();

    default boolean isEmpty(){
        return getInStorageAmount() == 0;
    }
}
