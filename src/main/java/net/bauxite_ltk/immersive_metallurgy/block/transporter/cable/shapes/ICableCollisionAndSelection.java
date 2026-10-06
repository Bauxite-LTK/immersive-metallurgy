package net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.shapes;

import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.api.utils.shapes.CachedVoxelShapes;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.register.IEItems;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.CableConnectionKey;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ICableCollisionAndSelection extends IEBlockInterfaces.ICollisionBounds, IEBlockInterfaces.ISelectionBounds {


    CachedVoxelShapes<BoundingBoxKey> SHAPES = new CachedVoxelShapes<>(ICableCollisionAndSelection::getBoxes);


    @Override
    default @NotNull VoxelShape getCollisionShape(@NotNull CollisionContext ctx){
        return SHAPES.get(BoundingBoxKey.original(getConnectionKeyForBoundingBox()));
    }

    @Override
    default @NotNull VoxelShape getSelectionShape(@Nullable CollisionContext ctx){
        boolean wireCutter = ctx!=null&&ctx.isHoldingItem(IEItems.Tools.WIRECUTTER.get());
        boolean cable = ctx!=null&&ctx.isHoldingItem(getCableItemForBoundingBox());
        Direction toolViewDirection = null;
        Direction availableConnectionFace = null;

        if(!(ctx instanceof EntityCollisionContext ecc) || !(ecc.getEntity() instanceof Player player))
            return SHAPES.get(BoundingBoxKey.original(getConnectionKeyForBoundingBox()));
        HitResult hitResult = Minecraft.getInstance().hitResult;

        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK){
            Vec3 hitVec = hitResult.getLocation().subtract(Vec3.atLowerCornerOf(getWorldPositionForBoundingBox()));
            if(cable) {
                Direction chosenAtt = chooseTerminalBoundingDir(this, hitVec);
                if(chosenAtt != null)
                    return SHAPES.get(BoundingBoxKey.extraTerminalSelection(getConnectionKeyForBoundingBox(), chosenAtt));
            }
            else if(wireCutter) {
                Direction chosenAtt = chooseConfigFaceBoundingDir(this, hitVec);
                if(chosenAtt != null)
                    return SHAPES.get(BoundingBoxKey.oneFaceConfiguration(getConnectionKeyForBoundingBox(), chosenAtt));
            }
        }

        return SHAPES.get(BoundingBoxKey.original(getConnectionKeyForBoundingBox()));
    }

    static Direction chooseTerminalBoundingDir(ICableCollisionAndSelection cable, Vec3 hitVec){
        Direction chosenAtt = null;
        for(Direction att : Direction.values()){
            AABB box = ICableShapes.Virtual.getFaceSelectionBounding(att);
            if (box.inflate(.008).contains(hitVec) && cable.hasBlockForAttachment(att)) {
                chosenAtt = att;
                break;
            }
        }
        return chosenAtt;
    }

    static Direction chooseConfigFaceBoundingDir(ICableCollisionAndSelection cable, Vec3 hitVec){
        Direction chosenAtt = null;
        for (Direction att : Direction.values()) {
            List<AABB> attBoxes = SHAPES.get(BoundingBoxKey.oneFaceConfiguration(cable.getConnectionKeyForBoundingBox(),att)).toAabbs();
            for(AABB box : attBoxes) {
                boolean hasNode = cable.getConnectionKeyForBoundingBox().nodes().contains(att);
                if (box.inflate(.004).contains(hitVec) && hasNode) {
                    chosenAtt = att;
                    break;
                }
            }
            if(chosenAtt!=null) break;
        }
        return chosenAtt;
    }

    static Direction chooseConfigDir(ICableCollisionAndSelection cable, Direction faceToConfigure, Vec3 hitVec){
        List<AABB> attBoxes = ICableShapes.Virtual.oneConfigurableConnectionsAABBs(cable.getConnectionKeyForBoundingBox(), faceToConfigure, true);
        Direction directionToConfigure = null;
        for(AABB box : attBoxes) {
            if (box.inflate(.002).contains(hitVec)) {
                Vec3 terminalBoxCenter = new Vec3(
                        0.5 + 7.5f/16 * faceToConfigure.getStepX(),
                        0.5 + 7.5f/16 * faceToConfigure.getStepY(),
                        0.5 + 7.5f/16 * faceToConfigure.getStepZ());
                for (Direction d : DirectionUtils.VALUES) {
                    if(d == faceToConfigure.getOpposite()) continue;
                    Vec3 testVec;
                    if(d == faceToConfigure) testVec = terminalBoxCenter;
                    else testVec = terminalBoxCenter.add(7d/16*d.getStepX(),7d/16*d.getStepY(),7d/16*d.getStepZ());
                    if (box.inflate(0.002).contains(testVec)) {
                        directionToConfigure = d;
                        break;
                    }
                }
                break;
            }
        }
        return directionToConfigure;
    }

    static List<AABB> getBoxes(BoundingBoxKey key){
        if(key.keyConfig.type.equals(BoundingBoxKey.KeyConfig.KeyType.ORIGINAL)){
            return ICableShapes.cableAABBs(key.connectionKey);
        }
        if(key.keyConfig.type.equals(BoundingBoxKey.KeyConfig.KeyType.ALL_TERMINALS_BOUNDING)){
            return ICableShapes.Virtual.allTerminals();
        }
        if(key.keyConfig.type.equals(BoundingBoxKey.KeyConfig.KeyType.EXTRA_TERMINAL_SELECTION)){
            return ICableShapes.Virtual.extraTerminal(key.connectionKey, key.keyConfig.dir);
        }
        if(key.keyConfig.type.equals(BoundingBoxKey.KeyConfig.KeyType.ALL_CONFIGURABLE_CONNECTIONS_BOUNDING)){
            return ICableShapes.Virtual.allConfigurableConnectionsAABBs(key.connectionKey, true);
        }
        if(key.keyConfig.type.equals(BoundingBoxKey.KeyConfig.KeyType.ONE_FACE_CONFIGURABLE_CONNECTIONS_SELECTION)){
            return ICableShapes.Virtual.oneConfigurableConnectionsAABBs(key.connectionKey, key.keyConfig.dir, true);
        }
        return List.of();
    }

    CableConnectionKey getConnectionKeyForBoundingBox();

    Item getCableItemForBoundingBox();

    BlockPos getWorldPositionForBoundingBox();

    boolean hasBlockForAttachment(Direction att);


    record BoundingBoxKey(CableConnectionKey connectionKey, KeyConfig keyConfig) {
        public static BoundingBoxKey original(CableConnectionKey connectionKey){
            return new BoundingBoxKey(connectionKey, new KeyConfig(KeyConfig.KeyType.ORIGINAL, null));
        }

        public static BoundingBoxKey allTerminals(){
            return new BoundingBoxKey(null, new KeyConfig(KeyConfig.KeyType.ALL_TERMINALS_BOUNDING, null));
        }

        public static BoundingBoxKey extraTerminalSelection(CableConnectionKey connectionKey, Direction extraAtt){
            return new BoundingBoxKey(connectionKey, new KeyConfig(KeyConfig.KeyType.EXTRA_TERMINAL_SELECTION, extraAtt));
        }

        public static BoundingBoxKey allConfiguration(CableConnectionKey connectionKey){
            return new BoundingBoxKey(connectionKey, new KeyConfig(KeyConfig.KeyType.ALL_CONFIGURABLE_CONNECTIONS_BOUNDING, null));
        }

        public static BoundingBoxKey oneFaceConfiguration(CableConnectionKey connectionKey, Direction att){
            return new BoundingBoxKey(connectionKey,new KeyConfig(KeyConfig.KeyType.ONE_FACE_CONFIGURABLE_CONNECTIONS_SELECTION, att));
        }


        record KeyConfig(KeyType type, Direction dir) {
            enum KeyType {
                ORIGINAL,
                ALL_TERMINALS_BOUNDING,
                ALL_CONFIGURABLE_CONNECTIONS_BOUNDING,
                ONE_FACE_CONFIGURABLE_CONNECTIONS_SELECTION,
                EXTRA_TERMINAL_SELECTION,
            }
        }

    }


}
