package net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.shapes;

import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.CableConnectionKey;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public class ICableShapes {

    public static List<AABB> cableAABBs(CableConnectionKey key){
        List<AABB> aabbList = new ArrayList<>();
        for(Direction att : Direction.values()){
            var nodeConnects = key.allConnections().get(att);
            var nodeObstacles = key.allObstacles().get(att);

            if(key.nodes().contains(att)){
                if(nodeConnects == null || (nodeConnects.size()<=1 && !nodeConnects.contains(att))){
                    aabbList.add(terminalAABB(att));
                }
                else aabbList.add(centerAABB(att));
            }

            if(nodeConnects != null){
                for(Direction con : key.allConnections().get(att)){
                    if(con.equals(att)) aabbList.add(terminalAABB(att));
                    else{
                        AABB obsAABB = connectionAABB(att, con);
                        if(obsAABB!=null)
                            aabbList.add(connectionAABB(att, con));
                    }
                }
            }

            if(nodeObstacles != null){
                for(var obs : nodeObstacles){
                    if(obs.equals(att)) aabbList.add(terminalObstacleAABB(att));
                    else{
                        AABB obsAABB = obstacleAABB(att, obs);
                        if(obsAABB!=null)
                            aabbList.add(obstacleAABB(att, obs));
                    }
                }
            }
        }
        return aabbList;
    }


    public static class Virtual{
        protected static List<AABB> allTerminals(){
            List<AABB> aabbList = new ArrayList<>();
            for(int i = 0; i < 6; i ++){
                Direction attachmentDir = Direction.from3DDataValue(i);
                aabbList.add(terminalAABB(attachmentDir));

            }
            return aabbList;
        }

        protected static List<AABB> extraTerminal(CableConnectionKey key, Direction att){
            var result = cableAABBs(key);
            //result.add(terminalAABB(att));
            result.add(getFaceSelectionPlate(att));
            return result;
        }


        protected static List<AABB> allConfigurableConnectionsAABBs(CableConnectionKey key, boolean connectMode){
            List<AABB> aabbList = new ArrayList<>();
            for(Direction att : key.nodes()){
                aabbList.add(centerAABB(att));
                for(Direction con : Direction.values()){
                    if(con.equals(att.getOpposite())) continue;
                    boolean hasConnection = key.allConnections().get(att).contains(con);
                    boolean hasObstacle = key.allObstacles().get(att).contains(con);
                    if(hasConnection || (!hasObstacle && connectMode)){
                        if(att.equals(con)) aabbList.add(terminalAABB(att));
                        else aabbList.add(connectionAABB(att, con));
                    }
                    else if(hasObstacle || (!hasConnection && !connectMode)){
                        if(att.equals(con)) aabbList.add(terminalObstacleAABB(att));
                        else aabbList.add(obstacleAABB(att, con));
                    }
                }
            }
            return aabbList;
        }

        protected static List<AABB> oneConfigurableConnectionsAABBs(CableConnectionKey key, Direction att, boolean connectMode){
            List<AABB> aabbList = new ArrayList<>();
            aabbList.add(centerAABB(att));
            for(Direction con : Direction.values()){
                if(con.equals(att.getOpposite())) continue;
                boolean hasConnection = key.allConnections().get(att) !=null && key.allConnections().get(att).contains(con);
                boolean hasObstacle = key.allObstacles().get(att) != null && key.allObstacles().get(att).contains(con);
                if(hasConnection || (!hasObstacle && connectMode)){
                    if(att.equals(con)) aabbList.add(terminalAABB(att));
                    else aabbList.add(connectionAABB(att, con));
                }
                else if(hasObstacle || (!hasConnection && !connectMode)){
                    if(att.equals(con)) aabbList.add(terminalObstacleAABB(att));
                    else aabbList.add(obstacleAABB(att, con));
                }
            }
            return aabbList;
        }

        public static AABB getFaceSelectionBounding(Direction face){
            return switch (face){
                case DOWN -> new AABB(3d/16,0,3d/16,13d/16,3d/16,13d/16);
                case UP -> new AABB(3d/16,13d/16,3d/16,13d/16,1,13d/16);
                case NORTH -> new AABB(3d/16,3d/16,0,13d/16,13d/16,3d/16);
                case SOUTH -> new AABB(3d/16,3d/16,13d/16,13d/16,13d/16,1);
                case WEST -> new AABB(0,3d/16,3d/16,3d/16,13d/16,13d/16);
                case EAST -> new AABB(13d/16,3d/16,3d/16,1,13d/16,13d/16);
            };
        }

        public static AABB getFaceSelectionPlate(Direction face){
            return switch (face){
                case DOWN -> new AABB(3d/16,0,3d/16,13d/16,2d/16,13d/16);
                case UP -> new AABB(3d/16,14d/16,3d/16,13d/16,1,13d/16);
                case NORTH -> new AABB(3d/16,3d/16,0,13d/16,13d/16,2d/16);
                case SOUTH -> new AABB(3d/16,3d/16,14d/16,13d/16,13d/16,1);
                case WEST -> new AABB(0,3d/16,3d/16,2d/16,13d/16,13d/16);
                case EAST -> new AABB(14d/16,3d/16,3d/16,1,13d/16,13d/16);
            };
        }
    }


    protected static AABB connectionAABB(Direction attachmentDir, Direction connectionDir){
        return switch (attachmentDir){
            case DOWN -> downOrUpConnectionAABB(connectionDir,true);
            case UP -> downOrUpConnectionAABB(connectionDir,false);
            case NORTH -> northOrSouthConnectionAABB(connectionDir,true);
            case SOUTH -> northOrSouthConnectionAABB(connectionDir,false);
            case WEST -> westOrEastConnectionAABB(connectionDir,true);
            case EAST -> westOrEastConnectionAABB(connectionDir,false);
        };
    }

    protected static AABB obstacleAABB(Direction attachmentDir, Direction connectionDir){
        return switch (attachmentDir){
            case DOWN -> downOrUpObstacleAABB(connectionDir,true);
            case UP -> downOrUpObstacleAABB(connectionDir,false);
            case NORTH -> northOrSouthObstacleAABB(connectionDir,true);
            case SOUTH -> northOrSouthObstacleAABB(connectionDir,false);
            case WEST -> westOrEastObstacleAABB(connectionDir,true);
            case EAST -> westOrEastObstacleAABB(connectionDir,false);
        };
    }

    protected static AABB centerAABB(Direction attachmentDir){
        return switch (attachmentDir){
            case DOWN -> new AABB(6d/16, 0, 6d/16, 10d/16, 2d/16, 10d/16);
            case UP -> new AABB(6d/16, 14d/16, 6d/16, 10d/16, 1, 10d/16);
            case NORTH -> new AABB(6d/16, 6d/16, 0, 10d/16, 10d/16, 2d/16);
            case SOUTH -> new AABB(6d/16, 6d/16, 14d/16, 10d/16, 10d/16, 1);
            case WEST -> new AABB(0, 6d/16, 6d/16, 2d/16, 10d/16, 10d/16);
            case EAST -> new AABB(14d/16, 6d/16, 6d/16, 1, 10d/16, 10d/16);
        };
    }

    protected static AABB terminalAABB(Direction attachmentDir){
        return switch (attachmentDir){
            case DOWN -> new AABB(5d/16, 0, 5d/16, 11d/16, 3d/16, 11d/16);
            case UP -> new AABB(5d/16, 13d/16, 5d/16, 11d/16, 1, 11d/16);
            case NORTH -> new AABB(5d/16, 5d/16, 0, 11d/16, 11d/16, 3d/16);
            case SOUTH -> new AABB(5d/16, 5d/16, 13d/16, 11d/16, 11d/16, 1);
            case WEST -> new AABB(0, 5d/16, 5d/16, 3d/16, 11d/16, 11d/16);
            case EAST -> new AABB(13d/16, 5d/16, 5d/16, 1, 11d/16, 11d/16);
        };
    }

    protected static AABB terminalObstacleAABB(Direction attachmentDir){
        return switch (attachmentDir){
            case DOWN -> new AABB(3d/16, 0, 3d/16, 13d/16, 1d/16, 13d/16);
            case UP -> new AABB(3d/16, 15d/16, 3d/16, 13d/16, 1, 13d/16);
            case NORTH -> new AABB(3d/16, 3d/16, 0, 13d/16, 13d/16, 1d/16);
            case SOUTH -> new AABB(3d/16, 3d/16, 15d/16, 13d/16, 13d/16, 1);
            case WEST -> new AABB(0, 3d/16, 3d/16, 1d/16, 13d/16, 13d/16);
            case EAST -> new AABB(15d/16, 3d/16, 3d/16, 1, 13d/16, 13d/16);
        };
    }

    private static AABB downOrUpConnectionAABB(Direction connectionDir, boolean isDown){
        double y1 = isDown? 0:14d/16;
        double y2 = isDown? 2d/16:1;
        return switch (connectionDir){
            case NORTH -> new AABB(6d/16,y1,0,10d/16,y2,6d/16);
            case SOUTH -> new AABB(6d/16,y1,10d/16,10d/16,y2,1);
            case WEST -> new AABB(0,y1,6d/16,6d/16,y2,10d/16);
            case EAST -> new AABB(10d/16,y1,6d/16,1,y2,10d/16);
            default -> null;
        };
    }

    private static AABB northOrSouthConnectionAABB(Direction connectionDir, boolean isNorth){
        double z1 = isNorth? 0:14d/16;
        double z2 = isNorth? 2d/16:1;
        return switch (connectionDir){
            case DOWN -> new AABB(6d/16,0,z1,10d/16,6d/16,z2);
            case UP -> new AABB(6d/16,10d/16,z1,10d/16,1,z2);
            case WEST -> new AABB(0,6d/16,z1,6d/16,10d/16,z2);
            case EAST -> new AABB(10d/16,6d/16,z1,1,10d/16,z2);
            default -> null;
        };
    }

    private static AABB westOrEastConnectionAABB(Direction connectionDir, boolean isWest){
        double x1 = isWest? 0:14d/16;
        double x2 = isWest? 2d/16:1;
        return switch (connectionDir){
            case NORTH -> new AABB(x1,6d/16,0,x2,10d/16,6d/16);
            case SOUTH -> new AABB(x1,6d/16,10d/16,x2,10d/16,1);
            case DOWN -> new AABB(x1,0,6d/16,x2,6d/16,10d/16);
            case UP -> new AABB(x1,10d/16,6d/16,x2,1,10d/16);
            default -> null;
        };
    }


    private static AABB downOrUpObstacleAABB(Direction connectionDir, boolean isDown){
        double y1 = isDown? 0:15d/16;
        double y2 = isDown? 1d/16:1;
        return switch (connectionDir){
            case NORTH -> new AABB(7d/16,y1,0,9d/16,y2,6d/16);
            case SOUTH -> new AABB(7d/16,y1,10d/16,9d/16,y2,1);
            case WEST -> new AABB(0,y1,7d/16,6d/16,y2,9d/16);
            case EAST -> new AABB(10d/16,y1,7d/16,1,y2,9d/16);
            default -> null;
        };
    }

    private static AABB northOrSouthObstacleAABB(Direction connectionDir, boolean isNorth){
        double z1 = isNorth? 0 : 15d/16;
        double z2 = isNorth? 1d/16 : 1;
        return switch (connectionDir){
            case DOWN -> new AABB(7d/16,0,z1,9d/16,6d/16,z2);
            case UP -> new AABB(7d/16,10d/16,z1,9d/16,1,z2);
            case WEST -> new AABB(0,7d/16,z1,6d/16,9d/16,z2);
            case EAST -> new AABB(10d/16,7d/16,z1,1,9d/16,z2);
            default -> null;
        };
    }

    private static AABB westOrEastObstacleAABB(Direction connectionDir, boolean isWest){
        double x1 = isWest? 0 : 15d/16;
        double x2 = isWest? 1d/16 : 1;
        return switch (connectionDir){
            case NORTH -> new AABB(x1,7d/16,0,x2,9d/16,6d/16);
            case SOUTH -> new AABB(x1,7d/16,10d/16,x2,9d/16,1);
            case DOWN -> new AABB(x1,0,7d/16,x2,6d/16,9d/16);
            case UP -> new AABB(x1,10d/16,7d/16,x2,1,9d/16);
            default -> null;
        };
    }
}
