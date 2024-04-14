package nintendods.ds_project.service;

import nintendods.ds_project.model.ClientNode;
import nintendods.ds_project.model.message.MNObject;
import nintendods.ds_project.model.message.UNAMNObject;
import nintendods.ds_project.model.message.eMessageTypes;

public class ListenerService {

    private static MulticastService multicastService = null;

    public ListenerService(String multicastAddress, int multicastPort, int multicastBufferCapacity) {
        if (multicastService == null)
            multicastService = new MulticastService(multicastAddress, multicastPort, multicastBufferCapacity);
    }

    public void listenAndUpdate(ClientNode node) throws Exception {
        // Checks if a multicast has arrived;
        MNObject message = null;
        try {
            message = multicastService.getMessage();
        } catch (NullPointerException ignored) {
            return;
        }

        if (message != null) {
            // Message arrived
            // compose incomming node
            boolean send = false;
            ClientNode incommingNode = new ClientNode(message);

            // Check the position of own node and incomming node and place it in the ring

            if (node.getId() < incommingNode.getId() && (incommingNode.getId() <= node.getNextNodeId()
                    || node.getNextNodeId() == node.getId())) {
                // new node is the new next node for current node
                node.setNextNodeId(incommingNode.getId());
                // Check if first node of network?
                if (node.getId() == node.getPrevNodeId())
                    node.setPrevNodeId(incommingNode.getId());
                System.out.println("\r\n current node is below the incomming node\r\n");
                send = true;
            }

            if (node.getId() > incommingNode.getId() && (incommingNode.getId() >= node.getPrevNodeId()
                    || node.getPrevNodeId() == node.getId())) {
                // new node is the new prev node for current node
                node.setPrevNodeId(incommingNode.getId());
                // Check if first node of network?
                if (node.getId() == node.getNextNodeId())
                    node.setNextNodeId(incommingNode.getId());

                System.out.println("\r\nnode is above the next node \r\n");
                send = true;
            }

            // Closing the ring checks

            // The incomming node is a new end node.
            if (!send && node.getPrevNodeId() >= node.getNextNodeId()) {
                if (node.getPrevNodeId() <= incommingNode.getId()
                        && node.getNextNodeId() <= incommingNode.getId()) {
                    if (node.getId() > node.getNextNodeId())    //If the current node is the original end node
                        node.setNextNodeId(incommingNode.getId());
                    else
                        node.setPrevNodeId(incommingNode.getId());
                    send = true;
                    System.out.println("\r\n new end node!\r\n");
                }

                // The incomming node is a new start node.
                if (node.getPrevNodeId() >= incommingNode.getId()
                        && node.getNextNodeId() >= incommingNode.getId()) {
                    if (node.getId() > node.getNextNodeId()) //If the current node is the original end node
                        node.setNextNodeId(incommingNode.getId());
                    else
                        node.setPrevNodeId(incommingNode.getId());
                    send = true;
                    System.out.println("\r\n new start node!\r\n");
                }
            }

            if (send) {
                // Compose message and send out
                UNAMNObject reply = new UNAMNObject(eMessageTypes.UnicastNodeToNode, node.getId(),
                        node.getPrevNodeId(), node.getNextNodeId());

                multicastService.sendReply(reply, incommingNode);

                System.out.println(" \r\nThe node has been updated!");
                System.out.println(node + "\r\n");
            } else
                System.out.println("Node doesn't need to be updated.");
        }
    }
}
