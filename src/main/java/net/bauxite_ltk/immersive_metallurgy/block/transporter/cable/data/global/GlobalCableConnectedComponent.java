package net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.global;

import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.blockface.BlockFace;

import java.util.ArrayList;
import java.util.List;

public class GlobalCableConnectedComponent {



    public record Vertex(BlockFace blockFace, List<Vertex> neighbors) {
        public Vertex(BlockFace blockFace){
            this(blockFace, new ArrayList<>(6));
        }

        public boolean hasNeighbor(Vertex other){
            return neighbors.contains(other);
        }
    }
}
