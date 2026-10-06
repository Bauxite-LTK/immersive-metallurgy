package net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.connection.controller;

import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.blockface.BlockFace;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

import java.util.EnumMap;
import java.util.Map;

public class SidePermissionController {
    final Map<Direction,Boolean> sidePermissionList = new EnumMap<>(Direction.class);

    private SidePermissionController(byte dataByte){
        for(Direction d : Direction.values()){
            boolean value = ((dataByte >> d.get3DDataValue()) & 1)!=0;
            sidePermissionList.put(d, value);
        }
    };

    public SidePermissionController(BlockFace blockFace){
        for(Direction d : Direction.values()){
            if(d.equals(blockFace.attach().getOpposite()))
                sidePermissionList.put(d, false);
            else sidePermissionList.put(d, true);
        }
    }

    public boolean isPermitted(Direction direction){
        return sidePermissionList.get(direction);
    }

    public void changePermission(Direction direction, boolean permit){
        sidePermissionList.replace(direction, permit);
    }

    public CompoundTag toNBT(){
        CompoundTag tag = new CompoundTag();
        byte a = 0;
        for(Direction d : Direction.values()){
            if(isPermitted(d))
                a += (byte) (1 << d.get3DDataValue());
        }
        tag.putByte("data_byte", a);
        return tag;
    }

    public void loadFromNBT(CompoundTag tag){
        byte dataByte = tag.getByte("data_byte");
        for(Direction d : Direction.values()){
            boolean value = ((dataByte >> d.get3DDataValue()) & 1)!=0;
            sidePermissionList.replace(d, value);
        }
    }

    public static SidePermissionController createFromNBT(CompoundTag tag){
        return new SidePermissionController(tag.getByte("data_byte"));
    }
}