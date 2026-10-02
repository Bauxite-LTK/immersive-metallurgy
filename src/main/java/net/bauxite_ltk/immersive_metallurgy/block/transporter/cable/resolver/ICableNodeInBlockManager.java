package net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.resolver;

import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.CableConnectionKey;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.blockface.BlockFace;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public interface ICableNodeInBlockManager<R> {

    int handleResourceInput(R resource, BlockFace inputFace, boolean simulate);

    R handleResourceOutput(R resource, BlockFace outputFace, boolean simulate);
    R handleResourceOutput(int amount, BlockFace outputFace, boolean simulate);

    CableConnectionKey getCableConnectionKey(long gameTime);

    CompoundTag toNBT(HolderLookup.Provider provider);
    void loadFromNBT(CompoundTag nbt, HolderLookup.Provider provider);
}
