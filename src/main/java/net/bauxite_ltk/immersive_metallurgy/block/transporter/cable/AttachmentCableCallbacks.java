package net.bauxite_ltk.immersive_metallurgy.block.transporter.cable;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.client.ieobj.BlockCallback;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.mojang.math.Transformation;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.CableConnectionKey;
import net.bauxite_ltk.immersive_metallurgy.util.IMUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.*;

public class AttachmentCableCallbacks implements BlockCallback<CableConnectionKey> {

    public static final AttachmentCableCallbacks INSTANCE = new AttachmentCableCallbacks();
    public static final ResourceLocation CALLBACK_RL = IMUtils.modRL("attachment_cable");

    private static final CableConnectionKey INVALID = CableConnectionKey.defaultKey();

    @Override
    public CableConnectionKey extractKey(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, BlockEntity blockEntity)
    {
        //ImmersiveMetallurgy.LOGGER.info("execute extractKey");
        if(!(blockEntity instanceof ICableBEImplements cable))
            return getDefaultKey();
        return cable.getConnectionKey();
    }

    @Override
    public CableConnectionKey getDefaultKey()
    {
        return INVALID;
    }

    @Override
    public IEProperties.IEObjState getIEOBJState(CableConnectionKey key)
    {
        //ImmersiveMetallurgy.LOGGER.info("execute getIEOBJState");
        List<String> parts = new ArrayList<>();
        Matrix4 rotationMatrix = new Matrix4();
        rotationMatrix.translate(0.5, 0.5, 0.5);

        for(Direction att : Direction.values()){
            boolean isExist = key.nodes().contains(att);
            var nodeConnections = key.allConnections().get(att);
            var nodeObstacles = key.allObstacles().get(att);
            if(isExist){
                if(nodeConnections == null || (nodeConnections.size()<=1 && !nodeConnections.contains(att))){
                    parts.add(String.format("terminal_%s", att.getName()));
                }
                else parts.add("center_" + att.getName());
            }

            if(nodeConnections != null){
                for(Direction con : nodeConnections){
                    if(nodeConnections.contains(con)) {
                        if(att.equals(con))
                            parts.add(String.format("terminal_%s", att.getName()));
                        else
                            parts.add(String.format("connection_%s_%s", att.getName(), con.getName()));
                    }
                }
            }
            if(nodeObstacles!=null){
                for(Direction con : nodeObstacles){
                    if(nodeObstacles.contains(con)) {
                        if(att.equals(con))
                            parts.add(String.format("obs_terminal_%s", att.getName()));
                        else
                            parts.add(String.format("obstacle_%s_%s", att.getName(), con.getName()));

                    }
                }
            }
        }

        rotationMatrix.translate(-0.5, -0.5, -0.5);

        return new IEProperties.IEObjState(IEProperties.VisibilityList.show(parts), new Transformation(rotationMatrix.toMatrix4f()));
    }


}
