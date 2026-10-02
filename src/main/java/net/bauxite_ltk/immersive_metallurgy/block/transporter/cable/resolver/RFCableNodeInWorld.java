package net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.resolver;

import com.google.common.base.Predicate;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.api.resourceStorage.EnergyUniStorage;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.blockface.BlockFace;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.connection.ICableConnection;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.node.CableNode;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.resolver.handler.CableHandler;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.ArrayList;
import java.util.List;

public class RFCableNodeInWorld extends CableNode{
    final CableHandler<Integer, IEnergyStorage> energyStorage;
    final CableBlockEnergyManager manager;

    protected RFCableNodeInWorld(BlockFace blockFace, CableBlockEnergyManager manager, CableHandler<Integer, IEnergyStorage> energyStorage) {
        super(blockFace);
        this.energyStorage = energyStorage;
        this.manager = manager;
    }


    public boolean tryConnect(CableNode other, ICableConnection.ICableConnectionBuilder cBuilder, long gameTime){
        if(!cBuilder.acquire(this, other))
            return false;
        if(!cBuilder.canBuildConnection(this, other)){
            cBuilder.release(this, other);
            return false;
        }

        ICableConnection connection = cBuilder.build(this, other, gameTime);

        if(!this.canConfirmConnection(connection) || !other.canConfirmConnection(connection)){
            cBuilder.release(this, other);
            return false;
        }
        this.confirmConnect(connection);
        other.confirmConnect(connection);

        cBuilder.release(this, other);
        return true;
    }

    /**
     * @param direction the direction of connection to be removed
     * @return the other-side node of the connection to be removed
     */
    public List<INodeIdentifyData<BlockFace>> tryDisconnectOfDir(Direction direction){
        return tryDisconnect(c -> {
            assert c != null;
            return c.relativeDirection(this).equals(direction);
        });
    }

    /**
     * @param removeCondition the predicate for removal
     * @return the other-side node of the connection to be removed
     */
    public List<INodeIdentifyData<BlockFace>> tryDisconnect(Predicate<ICableConnection> removeCondition){
        List<Integer> removeList = new ArrayList<>();
        List<INodeIdentifyData<BlockFace>> returnDataList = new ArrayList<>();
        for(int i = 0; i < connectionController.list().size(); i++){
            ICableConnection connection = connectionController.list().get(i);
            if(removeCondition.test(connection)){
                removeList.add(i);
                returnDataList.add(connection.other(this));
            }
        }
        for(Integer index : removeList){
            connectionController.list().remove(index.intValue());
        }
        return returnDataList;
    }

//    public List<ICableConnection> checkInvalidConnections(){
//        List<ICableConnection> invalidConnections = new ArrayList<>();
//        for(var connection : connectionController.list()){
//            BlockFace otherCableFace =  connection.other(this).data();
//            RFCableNodeInWorld node = CableBlockEnergyManager.getRFCableNodeInWorld(otherCableFace, manager.parentBE.getLevel());
//            if(node == null || !node.hasConnection(connection))
//                invalidConnections.add(connection);
//        }
//        return invalidConnections;
//    }


    @Override
    public CompoundTag toNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.put("energy", energyStorage.toNBT(provider));
        tag.put("node", super.toNBT(provider));
        return tag;
    }

    @Override
    public void loadFromNBT(CompoundTag tag, HolderLookup.Provider provider) {
        this.energyStorage.loadFromNBT(tag.getCompound("energy"), provider);
        super.loadFromNBT(tag.getCompound("node"), provider);
    }

    @Override
    public void loadFromNBT(Direction previousNorthNowPointTo, Vec3i translation, CompoundTag tag, HolderLookup.Provider provider) {
        this.energyStorage.loadFromNBT(tag.getCompound("energy"), provider);
        super.loadFromNBT(previousNorthNowPointTo, translation, tag.getCompound("node"), provider);
    }
}
