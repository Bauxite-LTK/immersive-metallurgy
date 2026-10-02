package net.bauxite_ltk.immersive_metallurgy.event;

import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import it.unimi.dsi.fastutil.longs.*;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.ElectricCableBlockEntity;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.ICableBEImplements;
import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.RFCableBlockEntity;
import net.bauxite_ltk.immersive_metallurgy.util.IMUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Map;


public class IMListeners {
    @SubscribeEvent
    public void forElectricCable(BlockEvent.NeighborNotifyEvent event){
        Level eventLevel = (Level)event.getLevel();
        if(eventLevel.isClientSide) return;
        BlockPos updatePos = event.getPos();
        //IMUtils.LOGGER.info("[forElectricCable] updatePos:{}", updatePos);
        for(int i = -1; i <= 1; i++){
            for(int j = -1; j <= 1; j++){
                for(int k = -1; k <= 1; k++){
                    BlockPos targetPos = updatePos.offset(i,j,k);
                    if(targetPos == updatePos) continue;
                    if(SafeChunkUtils.getSafeBE(eventLevel, targetPos) instanceof ElectricCableBlockEntity targetElectricCable){
                        if(targetElectricCable.updateBackCornerConnection(updatePos)) {
                            targetElectricCable.updateAllRootNode();
                            //targetElectricCable.invalidateCapabilities();
                            Level world = targetElectricCable.getLevelNonnull();
                            world.sendBlockUpdated(targetPos, targetElectricCable.getBlockState(), targetElectricCable.getBlockState(), 3);
                        }
                        if(SafeChunkUtils.getSafeBE(eventLevel, updatePos) instanceof ElectricCableBlockEntity thisElectricCable){
                            if(thisElectricCable.updateBackCornerConnection(targetPos)) {
                                thisElectricCable.updateAllRootNode();
                                //thisElectricCable.invalidateCapabilities();
                                Level world = thisElectricCable.getLevelNonnull();
                                world.sendBlockUpdated(updatePos, thisElectricCable.getBlockState(), thisElectricCable.getBlockState(), 3);
                            }
                        }
                    }

                }
            }
        }
    }

    // Map<Long, Long>: Origin -> Notified
    private static final Map<ResourceKey<Level>, Long2LongMap> PENDING_STRAIGHT = new HashMap<>();
    private static final Map<ResourceKey<Level>, Long2LongMap> PENDING_BACK_CORNER = new HashMap<>();
    private static final Direction[] DIRECTIONS = Direction.values();

    @SubscribeEvent
    public void onNeighborNotified(BlockEvent.NeighborNotifyEvent event){
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        BlockPos ori = event.getPos();
        for(Direction step1 : DIRECTIONS){
            BlockPos straightPos = ori.relative(step1);
            if(!level.isLoaded(straightPos)) continue;
            PENDING_STRAIGHT.computeIfAbsent(level.dimension(), rk -> new Long2LongOpenHashMap())
                    .put(straightPos.asLong(), ori.asLong());

        }
        for(int i = 0; i < 3; i++){
            for(int j = i+1; j < 3; j++){
                for(int k = 0; k < 4; k++){
                    int[] xyz = new int[]{0,0,0};
                    xyz[i] = (k & 1) == 0 ? 1:-1;
                    xyz[j] = (k & 2) == 0 ? 1:-1;
                    BlockPos backCornerPos = ori.offset(xyz[0], xyz[1], xyz[2]);
                    if(!level.isLoaded(backCornerPos)) continue;
                    PENDING_BACK_CORNER.computeIfAbsent(level.dimension(), rk -> new Long2LongOpenHashMap())
                            .put(backCornerPos.asLong(), ori.asLong());
                }
            }
        }
    }

    private static final int MAX_PER_TICK = 64;

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event){
        if(!PENDING_STRAIGHT.isEmpty()){
            MinecraftServer server = event.getServer();
            var byDim = PENDING_STRAIGHT.entrySet().iterator();



            while (byDim.hasNext()) {
                var dimEntry = byDim.next();

                ServerLevel level = server.getLevel(dimEntry.getKey());
                if (level == null) { byDim.remove(); continue; }

                Long2LongMap pending = dimEntry.getValue();

                var it = pending.long2LongEntrySet().iterator();

                int budget = MAX_PER_TICK;
                while(2*budget < pending.size()) budget*=2;

                while (it.hasNext() && budget-- > 0) {
                    var entry = it.next();
                    BlockPos toNotify = BlockPos.of(entry.getLongKey());
                    BlockPos origin = BlockPos.of(entry.getLongValue());
                    it.remove();
                    notifyCableStraight(level, toNotify, origin);
                }

                if (pending.isEmpty())
                    byDim.remove();
            }
        }
        if(!PENDING_BACK_CORNER.isEmpty()){
            MinecraftServer server = event.getServer();
            var byDim = PENDING_BACK_CORNER.entrySet().iterator();
            while (byDim.hasNext()) {
                var dimEntry = byDim.next();
                ServerLevel level = server.getLevel(dimEntry.getKey());
                if (level == null) { byDim.remove(); continue; }

                Long2LongMap pending = dimEntry.getValue();

                var it = pending.long2LongEntrySet().iterator();

                int budget = MAX_PER_TICK;
                while(2*budget < pending.size()) budget*=2;

                while (it.hasNext() && budget-- > 0) {
                    var entry = it.next();
                    BlockPos toNotify = BlockPos.of(entry.getLongKey());
                    BlockPos origin = BlockPos.of(entry.getLongValue());
                    it.remove();
                    notifyCableBackCorner(level, toNotify, origin);
                }
                if (pending.isEmpty())
                    byDim.remove();
            }
        }
    }

    public void notifyCableStraight(ServerLevel level, BlockPos notifyPos, BlockPos originChangedPos){
        ChunkPos chunkPos = new ChunkPos(notifyPos);
        LevelChunk chunk = level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
        if(chunk == null) return;
        BlockEntity be = chunk.getBlockEntity(notifyPos);
        if(be instanceof ICableBEImplements cable)
            cable.notifiedStraight(originChangedPos);
    }

    public void notifyCableBackCorner(ServerLevel level, BlockPos notifyPos, BlockPos originChangedPos){
        ChunkPos chunkPos = new ChunkPos(notifyPos);
        LevelChunk chunk = level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
        if(chunk == null) return;
        BlockEntity be = chunk.getBlockEntity(notifyPos);
        if(be instanceof ICableBEImplements cable)
            cable.notifiedBackCorner(originChangedPos);
    }


    @SubscribeEvent
    public void forSpecialCraftingRecipes(PlayerEvent.ItemCraftedEvent event){

    }



//    @SubscribeEvent
//    public void forElectricCablePlacement(ClientTickEvent.Post tickEvent){
//        Minecraft mc = Minecraft.getInstance();
//        Player player = mc.player;
//        if(player!=null){
//            ItemStack mainHandItem = player.getMainHandItem();
//            ElectricCableSelectionRenderer.ClientData.setHeldItem(mainHandItem);
//            if (mainHandItem.is(Items.STICK)) { // 以木棍为例
//                // 获取玩家视线方向
//                HitResult hitResult = mc.hitResult;
//
//                if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
//                    BlockHitResult blockHit = (BlockHitResult) hitResult;
//                    BlockPos targetPos = blockHit.getBlockPos();
//                    BlockState targetState = mc.level.getBlockState(targetPos);
//
//                    // 检查是否为目标方块
//                    if (targetState.is(IMBlocks.ELECTRIC_CABLE_LV)) {
//                        ElectricCableSelectionRenderer.ClientData.setTargetedBlock(targetPos, blockHit.getDirection());
//                    } else {
//                        ElectricCableSelectionRenderer.ClientData.clearTargetedBlock();
//                    }
//                } else {
//                    ElectricCableSelectionRenderer.ClientData.clearTargetedBlock();
//                }
//            } else {
//                ElectricCableSelectionRenderer.ClientData.clearTargetedBlock();
//            }
//        }
//    }

}
