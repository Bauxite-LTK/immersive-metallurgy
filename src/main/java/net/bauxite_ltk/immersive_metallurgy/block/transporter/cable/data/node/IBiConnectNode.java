package net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.node;

import net.bauxite_ltk.immersive_metallurgy.block.transporter.cable.data.connection.INodeConnection;

@Deprecated
public interface IBiConnectNode extends INode{
    //looks pretty strange, but it's Self Wrapping

    default boolean tryConnectTo(IBiConnectNode otherNode, INodeConnection.INodeConnectionBuilder<IBiConnectNode> cBuilder, long gameTime){
//        final IBiConnectNode thisNode = this;
//        // check if is notified by other node
//        BuildingSemaphore othersNotif = BuildingSemaphore.of(otherNode, cBuilder);
//        if(thisNode.isNotified(othersNotif)) return false;
//
//        // notify other node
//        BuildingSemaphore notif = BuildingSemaphore.of(thisNode, cBuilder);
//        otherNode.P(notif);
//
//        // test if the connection can be built
//        if(!cBuilder.canBuildConnection(thisNode, otherNode)){
//            otherNode.V(notif);
//            return false;
//        }
//        // create connection instance
//        INodeConnection<IBiConnectNode> conn = cBuilder.build(thisNode, otherNode, gameTime);
//
//        // test if each node confirm this connection
//        if(this.canConfirmConnection(conn) && otherNode.canConfirmConnection(conn)){
//            thisNode.directConnect(conn);
//            otherNode.directConnect(conn);
//            otherNode.V(notif);
//            return true;
//        }
//        else{
//            otherNode.V(notif);
//            return false;
//        }
        return true;
    }

    default boolean disconnectTo(IBiConnectNode otherNode, INodeConnection<IBiConnectNode> connection){

//        final IBiConnectNode thisNode = this;
//
//        INodeConnection.INodeConnectionBuilder<IBiConnectNode> cBuilder = connection.getBuilder();
//
//        // check if is notified by other node
//        BuildingSemaphore othersNotif = BuildingSemaphore.of(otherNode, cBuilder);
//        if(thisNode.isNotified(othersNotif))
//            return false;
//
//        // notify other node
//        BuildingSemaphore notif = BuildingSemaphore.of(thisNode, cBuilder);
//        otherNode.P(notif);
//
//        thisNode.directDisconnect(connection);
//        otherNode.directDisconnect(connection);
        return true;
    }

    boolean testConnection(INodeConnection<? extends IBiConnectNode> connection);


}
