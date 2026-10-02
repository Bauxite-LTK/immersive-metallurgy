package net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data;

import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.node.CableNode;
import net.minecraft.core.Direction;

import java.util.*;

public record CableConnectionKey(
        EnumSet<Direction> nodes,
        EnumMap<Direction, EnumSet<Direction>> allConnections,
        EnumMap<Direction, EnumSet<Direction>> allObstacles
) {
    public static CableConnectionKey defaultKey(){
        return new CableConnectionKey(EnumSet.noneOf(Direction.class) , new EnumMap<>(Direction.class), new EnumMap<>(Direction.class));
    }

    public CableConnectionKey(Map<Direction, ? extends CableNode> nodeMap){
        this(EnumSet.noneOf(Direction.class) , new EnumMap<>(Direction.class), new EnumMap<>(Direction.class));
        for(Direction att : Direction.values()){
            CableNode node = nodeMap.get(att);
            if(node == null) continue;
            nodes.add(att);
            for(Direction con : node.getConnectionDirSet()){
                allConnections.computeIfAbsent(att, d -> EnumSet.noneOf(Direction.class)).add(con);
            }
            for(Direction obs : node.getForbiddenDirSet()){
                allObstacles.computeIfAbsent(att, d -> EnumSet.noneOf(Direction.class)).add(obs);
            }
        }
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof CableConnectionKey that)) return false;
        return Objects.equals(nodes, that.nodes) && Objects.equals(allObstacles, that.allObstacles) && Objects.equals(allConnections, that.allConnections);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodes, allConnections, allObstacles);
    }
}
