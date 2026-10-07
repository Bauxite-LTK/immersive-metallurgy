package net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.global;

import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.blockface.BlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.*;

public class GlobalRFCableConnectedComponent {
    HashMap<BlockFace, Vertex> vertexMap = new HashMap<>();
    Set<BlockFace> energyStorageBlockFaceSet = new HashSet<>();
    long gameTimeLastTransfer = 0;
    Map<IEnergyStorage, Integer> tickTransferRecord = new HashMap<>();
    boolean invalid = false;

    public boolean contains(BlockFace face){
        return vertexMap.containsKey(face);
    }

    public boolean containsPos(BlockPos pos){
        return vertexMap.keySet().stream().anyMatch(bf -> bf.pos().equals(pos));
    }

    public void addHandler(BlockFace energyStorageFace){
        if(!contains(energyStorageFace.opposite())) return;
        else energyStorageBlockFaceSet.add(energyStorageFace);
    }

    public void removeHandler(BlockFace energyStorageFace){
        if(!contains(energyStorageFace.opposite())) return;
        energyStorageBlockFaceSet.remove(energyStorageFace);
    }

    public int allocateEnergyToEveryHandlers(Level level, BlockFace inputEnergyStorageFace, int energy, int limit, long gameTime, boolean simulate){
        if(gameTime != gameTimeLastTransfer){
            tickTransferRecord.clear();
            gameTimeLastTransfer = gameTime;
        }
        if(!energyStorageBlockFaceSet.contains(inputEnergyStorageFace)) return 0;
        List<Pair<IEnergyStorage, Integer>> consumeCapacity = new ArrayList<>();
        for(BlockFace energyStorageFace : energyStorageBlockFaceSet){
            if(energyStorageFace.equals(inputEnergyStorageFace)) continue;
            IEnergyStorage energyStorageToReceive = getLoadedEnergyStorage(level, energyStorageFace);
            if(energyStorageToReceive == null) continue;
            int canConsume = energyStorageToReceive.receiveEnergy(energy, true);
            tickTransferRecord.putIfAbsent(energyStorageToReceive, limit);
            int consumeLimit = tickTransferRecord.get(energyStorageToReceive);
            if(Math.min(canConsume, consumeLimit)>0) consumeCapacity.add(Pair.of(energyStorageToReceive, Math.min(canConsume, consumeLimit)));
        }
        consumeCapacity.sort(Comparator.comparingInt(Pair::value));

        int baseline = 0;
        int remainEnergy = energy;
        int remainHandlers = consumeCapacity.size();
        for(var p : consumeCapacity){
            var handler = p.key();
            int thisCanConsume = p.value();
            if(thisCanConsume - baseline <= remainEnergy/remainHandlers){
                int actualConsume = handler.receiveEnergy(thisCanConsume, simulate);
                remainEnergy -= actualConsume;
                baseline = actualConsume;

                tickTransferRecord.compute(handler, (h,canConsumeLimit) -> canConsumeLimit - actualConsume);
            }
            else{
                int actualConsume = handler.receiveEnergy( baseline + remainEnergy/remainHandlers, simulate);
                remainEnergy -= actualConsume;
                tickTransferRecord.compute(handler, (h,canConsumeLimit) -> canConsumeLimit - actualConsume);
            }
            remainHandlers--;
        }
        return energy-remainEnergy;

    }

    public int tryExtractEnergy(Level level, int limit){
        int canExtract = 0;
        for(BlockFace handlerFace : energyStorageBlockFaceSet){
            if(!SafeChunkUtils.isChunkSafe(level, handlerFace.pos())) continue;
            IEnergyStorage energyStorage = level.getCapability(Capabilities.EnergyStorage.BLOCK, handlerFace.pos(), handlerFace.attach());
            if(energyStorage == null) continue;
            canExtract += energyStorage.extractEnergy(limit, false);
            if(canExtract >= limit) return limit;
        }
        return canExtract;
    }

    public void addOrUpdateVertexNeighbor(BlockFace main, List<BlockFace> neighbors){
        if(invalid) return;
        if(vertexMap.get(main) != null){
            UpdateVertex(main, neighbors);
            return;
        }
        Vertex curVertex = new Vertex(main);
        vertexMap.put(main, curVertex);
        for(BlockFace neighbor : neighbors){
            if(neighbor.equals(main)) continue;
            Vertex neighborVertex = vertexMap.computeIfAbsent(neighbor, Vertex::new);
            curVertex.addNeighbor(neighborVertex);
            neighborVertex.addNeighbor(curVertex);
        }
    }

    public void UpdateVertex(BlockFace main, List<BlockFace> neighbors){
        Vertex thisVertex = vertexMap.get(main);
        for(BlockFace preNeighbor : thisVertex.neighbors){
            Vertex preNeighborVertex = vertexMap.get(preNeighbor);
            if(preNeighborVertex == null) continue;
            preNeighborVertex.neighbors.remove(main);
        }
        thisVertex.neighbors.clear();
        for(BlockFace neighbor : neighbors){
            if(neighbor.equals(main)) continue;
            Vertex neighborVertex = vertexMap.computeIfAbsent(neighbor, Vertex::new);
            thisVertex.addNeighbor(neighborVertex);
            neighborVertex.addNeighbor(thisVertex);
        }
    }

    public List<GlobalRFCableConnectedComponent> removeVertexThenTrySplit(BlockFace main){
        Vertex thisVertex = vertexMap.get(main);
        if(thisVertex == null) return null;
        for(BlockFace preNeighbor : thisVertex.neighbors){
            Vertex preNeighborVertex = vertexMap.get(preNeighbor);
            if(preNeighborVertex == null) continue;
            preNeighborVertex.neighbors.remove(main);
        }
        vertexMap.remove(main);
        energyStorageBlockFaceSet.remove(main.opposite());
        return GlobalRFCableConnectedComponent.splitIfDivided(this);
    }

    public List<GlobalRFCableConnectedComponent> removeConnectionThenTrySplit(BlockFace from, BlockFace to){
        Vertex fromV = vertexMap.get(from);
        if(fromV == null) return null;
        Vertex toV = vertexMap.get(to);
        if(toV == null) return null;
        fromV.neighbors.remove(to);
        toV.neighbors.remove(from);
        return GlobalRFCableConnectedComponent.splitIfDivided(this);
    }

    public void addOrMergeVertexNeighbor(BlockFace main, Set<BlockFace> neighbors){
        if(invalid) return;
        Vertex curVertex = vertexMap.computeIfAbsent(main, Vertex::new);
        for(var neighbor : neighbors){
            Vertex neighborVertex = vertexMap.computeIfAbsent(neighbor, Vertex::new);
            curVertex.addNeighbor(neighborVertex);
            neighborVertex.addNeighbor(curVertex);
        }
    }

    public static GlobalRFCableConnectedComponent merge(GlobalRFCableConnectedComponent component1, GlobalRFCableConnectedComponent component2){
        if(component1 == component2) return component1;
        GlobalRFCableConnectedComponent smallerOne;
        GlobalRFCableConnectedComponent biggerOne;
        if(component2.vertexMap.size() <= component1.vertexMap.size()){
            smallerOne = component2;
            biggerOne = component1;
        }
        else{
            smallerOne = component1;
            biggerOne = component2;
        }
        for(var e : smallerOne.vertexMap.entrySet()){
            BlockFace key = e.getKey();
            Vertex v = e.getValue();
            biggerOne.addOrMergeVertexNeighbor(key, v.neighbors);
        }
        for(var energyStorageFace : smallerOne.energyStorageBlockFaceSet){
            biggerOne.addHandler(energyStorageFace);
        }
        smallerOne.invalidate();
        return biggerOne;
    }

    public static List<GlobalRFCableConnectedComponent> splitIfDivided(
            GlobalRFCableConnectedComponent component)
    {
        List<GlobalRFCableConnectedComponent> splitResult = new ArrayList<>();
        Set<BlockFace> close = new HashSet<>();
        Queue<BlockFace> open = new ArrayDeque<>();
        for(BlockFace start : component.vertexMap.keySet()){
            GlobalRFCableConnectedComponent aSplit = new GlobalRFCableConnectedComponent();
            open.clear();
            open.add(start);
            for(int i = 0; i < component.vertexMap.size()*6+1 && !open.isEmpty(); i++){
                BlockFace polledCableFace = open.poll();
                if (!close.add(polledCableFace)) continue;
                open.addAll(component.vertexMap.get(polledCableFace).neighbors);
                aSplit.addOrMergeVertexNeighbor(polledCableFace, component.vertexMap.get(polledCableFace).neighbors);

                BlockFace possibleEnergyStorageFace = polledCableFace.opposite();
                if(component.energyStorageBlockFaceSet.contains(possibleEnergyStorageFace))
                    aSplit.addHandler(possibleEnergyStorageFace);
            }
            if(!aSplit.vertexMap.isEmpty())
                splitResult.add(aSplit);
        }
        if(splitResult.size() == 1) return List.of(component);
        else return splitResult;
    }

    public static IEnergyStorage getLoadedEnergyStorage(Level level, BlockFace energyStorageFace){
        if(!SafeChunkUtils.isChunkSafe(level, energyStorageFace.pos())) return null;
        return level.getCapability(Capabilities.EnergyStorage.BLOCK, energyStorageFace.pos(), energyStorageFace.attach());
    }

    public boolean hasVertex(BlockFace blockFace){
        return vertexMap.containsKey(blockFace);
    }

    public boolean hasAllVertexes(BlockFace... blockFaces){
        for(var blockface : blockFaces){
            if(!vertexMap.containsKey(blockface)) return false;
        }
        return true;
    }

    public void invalidate(){
        vertexMap.clear();
        invalid = true;
    }

    public boolean isInvalid(){
        return invalid;
    }

    public CompoundTag toNBT(){
        CompoundTag main = new CompoundTag();

        ListTag vertexListTag = new ListTag();
        for(var v : vertexMap.values()){
            vertexListTag.add(v.toNBT());
        }
        main.put("vertex_list", vertexListTag);

        ListTag energyStorageListTag = new ListTag();
        for(var ve : energyStorageBlockFaceSet)
            energyStorageListTag.add(ve.toNBT());
        main.put("energy_storage_list", energyStorageListTag);

        return main;
    }

    public static GlobalRFCableConnectedComponent fromNBT(CompoundTag main){
        ListTag vertexListTag = main.getList("vertex_list", Tag.TAG_COMPOUND);
        GlobalRFCableConnectedComponent component = new GlobalRFCableConnectedComponent();
        for(var vertexTag : vertexListTag){
            Vertex v = Vertex.fromNBT((CompoundTag) vertexTag);
            component.vertexMap.put(v.blockFace, v);
        }

        ListTag energyStoragListTag = main.getList("energy_storage_list", Tag.TAG_COMPOUND);
        for(var energyStorageTag : energyStoragListTag){
            var energyStorageFace = BlockFace.fromNBT((CompoundTag) energyStorageTag);
            component.energyStorageBlockFaceSet.add(energyStorageFace);
        }
        return component;
    }

    public Set<BlockFace> getAllBlockFaces(){
        return vertexMap.keySet();
    }


    static class Vertex {
        public BlockFace blockFace;
        public Set<BlockFace> neighbors;

        private Vertex(BlockFace blockFace, Set<BlockFace> neighbors){
            this.blockFace = blockFace;
            this.neighbors = neighbors;
        }

        public Vertex(BlockFace blockFace){
            this(blockFace, new ObjectArraySet<>());
        }

        public void addNeighbor(Vertex other){
            neighbors.add(other.blockFace);
        }

        public boolean hasNeighbor(BlockFace otherFace){
            return neighbors.contains(otherFace);
        }

        public CompoundTag toNBT(){
            CompoundTag main = new CompoundTag();
            main.put("vertex_face", blockFace.toNBT());
            ListTag neighborList = new ListTag();
            for(var bf : neighbors){
                neighborList.add(bf.toNBT());
            }
            main.put("vertex_neighbors", neighborList);
            return main;
        }

        public static Vertex fromNBT(CompoundTag main){
            BlockFace thisFace = BlockFace.fromNBT(main.getCompound("vertex_face"));
            Set<BlockFace> neighbors = new ObjectArraySet<>();
            ListTag listTag = main.getList("vertex_neighbors", Tag.TAG_COMPOUND);
            for(var tag : listTag){
                neighbors.add(BlockFace.fromNBT((CompoundTag)tag));
            }
            return new Vertex(thisFace, neighbors);
        }
    }
}
