package net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.global;

import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.blockface.BlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalRFCableConnectionData extends SavedData {

    public static GlobalRFCableConnectionData instance;
    public static String DATA_NAME = "im_global_cable_connection_data";

    public Map<ResourceKey<Level>, List<GlobalRFCableConnectedComponent>> connectedComponentsByLevel = new HashMap<>();
    //public List<GlobalRFCableConnectedComponent> connectedComponents = new ArrayList<>();

    public int testInt = 0;

    public static SavedData.Factory<GlobalRFCableConnectionData> factory() {
        return new SavedData.Factory<>(
                GlobalRFCableConnectionData::new,
                GlobalRFCableConnectionData::load,
                DataFixTypes.LEVEL);
    }

    public GlobalRFCableConnectionData(){

    }

    public static void addTestInt(int a){
        instance.testInt += a;
        makeDirty();
    }

    public static void updateVertexEnergyStorage(Level level, BlockFace inputCableFace, IEnergyStorage energyStorage){
        if(level == null) return;
        BlockFace opposingEnergyStorageFace = inputCableFace.opposite();
        List<GlobalRFCableConnectedComponent> levelCCs = instance.connectedComponentsByLevel.computeIfAbsent(level.dimension(), e->new ArrayList<>());
        for(var cc : levelCCs){
            if(cc.contains(inputCableFace)){
                if(energyStorage != null)
                    cc.addHandler(opposingEnergyStorageFace);
                else cc.removeHandler(opposingEnergyStorageFace);
            }
        }
    }

    public static int allocateEnergy(Level level, BlockFace inputCableFace, int energy, boolean simulate){
        if(level == null) return 0;
        BlockFace opposingEnergyStorageFace = inputCableFace.opposite();
        List<GlobalRFCableConnectedComponent> levelCCs = instance.connectedComponentsByLevel.computeIfAbsent(level.dimension(), e->new ArrayList<>());
        for(var cc : levelCCs){

            if(cc.contains(inputCableFace)){
                return cc.allocateEnergyToEveryHandlers(level, opposingEnergyStorageFace, energy, simulate);
            }
        }
        return 0;
    }

    public static int tryElectrocute(Level level, BlockPos blockPos, int limit){
        if(level == null) return 0;
        List<GlobalRFCableConnectedComponent> levelCCs = instance.connectedComponentsByLevel.computeIfAbsent(level.dimension(), e->new ArrayList<>());
        for(var cc : levelCCs){
            if(cc.containsPos(blockPos)){
                return cc.tryExtractEnergy(level, limit);
            }
        }
        return 0;
    }


    public static void addOrUpdateVertexes(Level level, BlockFace main, List<BlockFace> neighbors){
        //if(neighbors.isEmpty()) return;
        if(level == null) return;
        List<GlobalRFCableConnectedComponent> levelCCs = instance.connectedComponentsByLevel.computeIfAbsent(level.dimension(), e->new ArrayList<>());

        GlobalRFCableConnectedComponent chosenComponent = null;
        for(var cc : levelCCs){
            if(cc.hasVertex(main)){
                if(chosenComponent == null) chosenComponent = cc;
                else chosenComponent = GlobalRFCableConnectedComponent.merge(cc, chosenComponent);
            }
        }
        for(var bf : neighbors){
            for(var cc : levelCCs){
                if(cc.hasVertex(bf)){
                    if(chosenComponent == null) chosenComponent = cc;
                    else chosenComponent = GlobalRFCableConnectedComponent.merge(cc, chosenComponent);
                }
            }
        }
        if(chosenComponent == null){
            chosenComponent = new GlobalRFCableConnectedComponent();
            levelCCs.add(chosenComponent);
        }


        chosenComponent.addOrUpdateVertexNeighbor(main, neighbors);
        removeInvalidComponents();
        makeDirty();
    }

    public static void removeVertex(Level level, BlockFace blockFace){
        if(level == null) return;
        List<GlobalRFCableConnectedComponent> levelCCs = instance.connectedComponentsByLevel.computeIfAbsent(level.dimension(), e->new ArrayList<>());

        List<GlobalRFCableConnectedComponent> splits = new ArrayList<>();
        List<GlobalRFCableConnectedComponent> removes = new ArrayList<>();
        for(var cc : levelCCs){
            if(cc.hasVertex(blockFace)){
                splits.addAll(cc.removeVertexThenTrySplit(blockFace));
                removes.add(cc);
            }
        }
        levelCCs.removeAll(removes);
        levelCCs.addAll(splits);
        makeDirty();
    }

    public static void removeConnection(Level level, BlockFace from, BlockFace to){
        if(level == null) return;
        List<GlobalRFCableConnectedComponent> levelCCs = instance.connectedComponentsByLevel.computeIfAbsent(level.dimension(), e->new ArrayList<>());

        List<GlobalRFCableConnectedComponent> splits = new ArrayList<>();
        List<GlobalRFCableConnectedComponent> removes = new ArrayList<>();
        for(var cc : levelCCs){
            if(cc.hasVertex(from) && cc.hasVertex(to)){
                splits.addAll(cc.removeConnectionThenTrySplit(from, to));
                removes.add(cc);
            }
        }
        levelCCs.removeAll(removes);
        levelCCs.addAll(splits);
        makeDirty();
    }

    public static void removeInvalidComponents(){
        for(var levelCC : instance.connectedComponentsByLevel.values()){
            levelCC.removeIf(GlobalRFCableConnectedComponent::isInvalid);
        }
        makeDirty();
    }



    @Override
    public @NotNull CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        compoundTag.putInt("test_int", testInt);
        ListTag levelCCsListTag = new ListTag();
        for(var levelKey : connectedComponentsByLevel.keySet()){
            CompoundTag ccKeyTag = new CompoundTag();
            var levelCCs = connectedComponentsByLevel.get(levelKey);
            ListTag componentsTag = new ListTag();
            for(var cc : levelCCs){
                componentsTag.add(cc.toNBT());
            }
            ccKeyTag.put("connected_components", componentsTag);
            ccKeyTag.putString("level", levelKey.location().toString());
            levelCCsListTag.add(ccKeyTag);
        }
        compoundTag.put("connection_components_level", levelCCsListTag);
        return compoundTag;
    }

    public static GlobalRFCableConnectionData load(CompoundTag tag, HolderLookup.Provider provider){
        var instance = new GlobalRFCableConnectionData();
        instance.testInt = tag.getInt("test_int");
        ListTag levelCCsListTag = tag.getList("connection_components_level", Tag.TAG_COMPOUND);
        for(var t : levelCCsListTag){
            CompoundTag levelConnectionComponentsTag = (CompoundTag) t;

            String levelKeyString = levelConnectionComponentsTag.getString("level");
            var levelKeyRK = ResourceLocation.parse(levelKeyString);
            ResourceKey<Level> levelKey = ResourceKey.create(Registries.DIMENSION, levelKeyRK);

            var components = levelConnectionComponentsTag.getList("connected_components", Tag.TAG_COMPOUND);
            for(var ccTag : components){
                instance.connectedComponentsByLevel.computeIfAbsent(levelKey, k-> new ArrayList<>())
                        .add(GlobalRFCableConnectedComponent.fromNBT((CompoundTag) ccTag));
            }

        }

        return instance;
    }



    public static void makeDirty(){
        instance.setDirty();
    }

    public static void setInstance(GlobalRFCableConnectionData data){
        instance = data;
    }

    public static GlobalRFCableConnectionData ofGlobal(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(GlobalRFCableConnectionData.factory(), GlobalRFCableConnectionData.DATA_NAME);
    }

}
