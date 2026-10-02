package net.bauxite_ltk.immersive_metallurgy.callback;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.client.ieobj.BlockCallback;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.mojang.math.Transformation;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.casting_channel.CastingChannelBlockEntity;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static net.minecraft.core.Direction.*;

public class CastingChannelCallbacks implements BlockCallback<CastingChannelCallbacks.Key> {

    public static final CastingChannelCallbacks INSTANCE = new CastingChannelCallbacks();

    private static final Key INVALID = new Key(
            Util.make(new EnumMap<>(Direction.class), m -> {
                for(Direction d : DirectionUtils.VALUES)
                    m.put(d, CastingChannelBlockEntity.ConnectionStyle.NO_CONNECTION);
            })
    );

    @Override
    public Key extractKey(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, BlockEntity blockEntity)
    {
        if(!(blockEntity instanceof CastingChannelBlockEntity castingChannelBE))
            return getDefaultKey();
        Map<Direction, CastingChannelBlockEntity.ConnectionStyle> connections = new EnumMap<>(Direction.class);
        for(Direction face : DirectionUtils.VALUES)
            connections.put(face, castingChannelBE.getConnectionStyle(face));
        return new Key(connections);
    }

    @Override
    public Key getDefaultKey()
    {
        return INVALID;
    }

    @Override
    public IEProperties.IEObjState getIEOBJState(Key key)
    {
        List<String> parts = new ArrayList<>();
        Matrix4 rotationMatrix = new Matrix4();
        rotationMatrix.translate(0.5, 0.5, 0.5);
//        if(key.cover()!=null)
//            parts.add("cover");
        int totalConnections = key.numActiveConnections();
        boolean straightZ = key.all(NORTH, SOUTH);
        boolean straightX = key.all(EAST, WEST);
        switch(totalConnections) {
            case 0://stub
                parts.add("center");
                break;
            case 1:
                //casting channel cannot connect down without horizontal allConnections
                //so we only have 2 cases: UP and Horizontal
                if (key.hasActiveConnection(UP)){
                    parts.add("up");
                    switch(key.connections.get(UP)){
                        //Default South;
                        case TO_UP_NORTH -> //z-
                            rotationMatrix.rotate(Math.PI, 0, 1, 0);
                        case TO_UP_EAST -> //x+
                            rotationMatrix.rotate(Math.PI/2, 0, 1, 0);
                        case TO_UP_WEST -> //x-
                            rotationMatrix.rotate(-Math.PI/2,0,1,0);
                    }
                }
                else{
                    parts.add("stopper");
                    if(key.hasActiveConnection(SOUTH))//z+
                        rotationMatrix.rotate(Math.PI, 0, 1, 0);
                    else if(key.hasActiveConnection(WEST))//x-
                        rotationMatrix.rotate(Math.PI/2, 0, 1, 0);
                    else if(key.hasActiveConnection(EAST))//x+
                        rotationMatrix.rotate(-Math.PI/2, 0, 1, 0);
                }
                break;

            case 2:
                //horizontal straight
                if(straightZ)
                {
                    parts.add("straight");
                }
                else if(straightX)
                {
                    parts.add("straight");
                    rotationMatrix.rotate(Math.PI/2,0,1,0);
                }
                // to up
                else if (key.hasActiveConnection(UP)){
                    parts.add("up");
                    switch(key.connections.get(UP)){
                        //Default SOUTH;
                        case TO_UP_NORTH -> //z-
                            rotationMatrix.rotate(Math.PI, 0, 1, 0);
                        case TO_UP_EAST -> //x+
                            rotationMatrix.rotate(Math.PI/2, 0, 1, 0);
                        case TO_UP_WEST -> //x-
                            rotationMatrix.rotate(-Math.PI/2,0,1,0);
                    }
                }
                // to down
                else if(key.hasActiveConnection(DOWN)){
                    parts.add("down");
                    //Default North
                    if(key.hasActiveConnection(SOUTH))
                        rotationMatrix.rotate(Math.PI, 0, 1, 0);
                    else if(key.hasActiveConnection(EAST))
                        rotationMatrix.rotate(-Math.PI/2,0,1,0);
                    else if(key.hasActiveConnection(WEST))
                        rotationMatrix.rotate(Math.PI/2, 0, 1, 0);
                }
                //horizontal curve
                else
                {
                    parts.add("curve");
                    //default: north to west
                    if(key.all(SOUTH, EAST))//z+ to x+
                        rotationMatrix.rotate(Math.PI, 0, 1, 0);
                    else if(key.all(SOUTH, WEST))//z+ to x-
                        rotationMatrix.rotate(Math.PI/2, 0, 1, 0);
                    else if(key.all(NORTH, EAST))//z- to x+
                        rotationMatrix.rotate(-Math.PI/2, 0, 1, 0);

                }
                break;
            case 3://tcurve

                parts.add("tcurve");
                if(straightX)
                {
                    // default curve to North
                    if(key.hasActiveConnection(SOUTH))//z+
                        rotationMatrix.rotate(Math.PI, 0, 1, 0);
                }
                else if(straightZ)
                {
                    if(key.hasActiveConnection(WEST))//x-
                        rotationMatrix.rotate(Math.PI/2, 0, 1, 0);
                    else if(key.hasActiveConnection(EAST))//x+
                        rotationMatrix.rotate(-Math.PI/2, 0, 1, 0);
                }
                break;
            case 4://cross
                parts.add("cross");
                break;
        }
        rotationMatrix.translate(-0.5, -0.5, -0.5);

        return new IEProperties.IEObjState(IEProperties.VisibilityList.show(parts), new Transformation(rotationMatrix.toMatrix4f()));
    }
    
    
    public record Key(
            Map<Direction, CastingChannelBlockEntity.ConnectionStyle> connections
    )
    {
        int numActiveConnections()
        {
            int count = 0;
            for(CastingChannelBlockEntity.ConnectionStyle c : connections.values())
                if(c!= CastingChannelBlockEntity.ConnectionStyle.NO_CONNECTION)
                    count++;
            return count;
        }

        public boolean hasActiveConnection(Direction side)
        {
            return connections.get(side)!= CastingChannelBlockEntity.ConnectionStyle.NO_CONNECTION;
        }

        public boolean any(Direction... sides)
        {
            for(Direction side : sides)
                if(hasActiveConnection(side))
                    return true;
            return false;
        }

        public boolean all(Direction... sides)
        {
            for(Direction side : sides)
                if(!hasActiveConnection(side))
                    return false;
            return true;
        }
    }
}
