package net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.connection.controller;

import com.google.common.collect.ImmutableList;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.connection.INodeConnection;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.node.INode;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import java.util.Iterator;
import java.util.List;

public interface IConnectionController<C extends INodeConnection<? extends INode>>{

    void add(C connection);

    void remove(C connection);

    void clear();

    CompoundTag toNBT();

    void loadFromNBT(CompoundTag compoundTag);

    interface IConnectionControllerBuilder<C extends INodeConnection<? extends INode>>{
        IConnectionController<C> buildFromNBT(CompoundTag compoundTag);
    }


}
