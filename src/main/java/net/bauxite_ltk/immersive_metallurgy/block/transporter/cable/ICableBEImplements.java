package net.bauxite_ltk.immersive_metallurgy.block.transporter.cable;

import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.CableConnectionKey;
import net.minecraft.core.BlockPos;

public interface ICableBEImplements {
    void notifiedStraight(BlockPos changedPos);

    void notifiedBackCorner(BlockPos changedPos);

    CableConnectionKey getConnectionKey();
}
