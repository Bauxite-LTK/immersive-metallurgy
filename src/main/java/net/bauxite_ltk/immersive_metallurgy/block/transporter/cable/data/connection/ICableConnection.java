package net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.connection;

import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.blockface.BlockFace;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.node.CableNode;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.node.INode;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public interface ICableConnection extends INodeConnection<CableNode>{

    Direction relativeDirection(CableNode node);

    INode.INodeIdentifyData<BlockFace> from();

    INode.INodeIdentifyData<BlockFace> to();

    INode.INodeIdentifyData<BlockFace> other(CableNode node);


    abstract class ICableConnectionBuilder implements INodeConnectionBuilder<CableNode>{
        Set<BuildingSemaphore> semaphores = new HashSet<>();

        @Override
        public abstract boolean canBuildConnection(CableNode from, CableNode to);

        @Override
        public abstract @NotNull ICableConnection build(CableNode from, CableNode to, long createTime);

        @Override
        public abstract ICableConnection fromNBT(CompoundTag content);

        public boolean acquire(CableNode from, CableNode to){
            BuildingSemaphore s = BuildingSemaphore.of(from,to);
            return acquire(s);
        }

        public boolean acquire(BuildingSemaphore s){
            return semaphores.add(s);
        }

        public boolean release(CableNode from, CableNode to){
            BuildingSemaphore s = BuildingSemaphore.of(from,to);
            return release(s);
        }

        public boolean release(BuildingSemaphore s){
            return semaphores.remove(s);
        }
    }

    record BuildingSemaphore(INode node1, INode node2){

        public static BuildingSemaphore
        of(INode node1, INode node2){
            return new BuildingSemaphore(node1, node2);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof BuildingSemaphore s && this.hashCode() == s.hashCode();
        }

        @Override
        public int hashCode() {
            String str1 = node1.getIdentifyData().data().toString();
            String str2 = node2.getIdentifyData().data().toString();
            return str1.hashCode() + str2.hashCode();
        }
    }

}
