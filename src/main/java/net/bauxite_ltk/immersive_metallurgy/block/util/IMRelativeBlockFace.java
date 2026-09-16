package net.bauxite_ltk.immersive_metallurgy.block.util;

import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;

public enum IMRelativeBlockFace {

    // Every Time I Use RelativeBlockFace from IE's code, I would spend minutes on Struggling to Distinguish Every Direction.
    // That's So Annoying, I Just Can't Take It Anymore.

    XP(RelativeBlockFace.LEFT),
    XN(RelativeBlockFace.RIGHT),
    YP(RelativeBlockFace.UP),
    YN(RelativeBlockFace.DOWN),
    ZP(RelativeBlockFace.BACK),
    ZN(RelativeBlockFace.FRONT);

    final RelativeBlockFace relativeBlockFace;
    IMRelativeBlockFace(RelativeBlockFace relativeBlockFace){
        this.relativeBlockFace = relativeBlockFace;
    }

    public RelativeBlockFace toIE(){
        return relativeBlockFace;
    }
}
