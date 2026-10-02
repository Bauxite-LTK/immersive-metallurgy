package net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.global;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

public class GlobalCableConnectionData extends SavedData {

    public static GlobalCableConnectionData instance;
    public static String DATA_NAME = "im_global_cable_connection_data";

    public int testInt = 0;

    public static SavedData.Factory<GlobalCableConnectionData> factory() {
        return new SavedData.Factory<>(
                GlobalCableConnectionData::new,
                GlobalCableConnectionData::load,
                DataFixTypes.LEVEL);
    }

    public GlobalCableConnectionData(){

    }

    @Override
    public @NotNull CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("test_int", testInt);
        return tag;
    }

    public static GlobalCableConnectionData load(CompoundTag tag, HolderLookup.Provider provider){
        var instance = new GlobalCableConnectionData();
        instance.testInt = tag.getInt("test_int");
        return instance;
    }

    public static void addTestInt(int a){
        instance.testInt += a;
        makeDirty();
    }

    public static void makeDirty(){
        instance.setDirty();
    }

    public static void setInstance(GlobalCableConnectionData data){
        instance = data;
    }


    public static GlobalCableConnectionData ofGlobal(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(GlobalCableConnectionData.factory(), GlobalCableConnectionData.DATA_NAME);
    }

}
