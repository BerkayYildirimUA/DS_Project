# 6-DS project

This projects will create a ring topology with clients. A Naming server that will manage the resources and namings of the nodes files in the ring topology

# Tasks

- Robbe:
  - [x] Algorithm for file names convertion to hash value
  - [x] Save to JSON to a file

- Tom:
  - [] API

- Berkay
  - Name server:
    - [x] Map(int, ip)
    - [x] get ip from filename
    - [x] Add/Remove nodes from map
    - [x] Algorithm to look up NodeID for gotten hashID (Xor, extends/implemts)

- Ahmad
  - Node (client node)
    - [] id, ip
    - [] local list of files & replica/remote of files
    - [] create/delete file --> api call for update
    - [] get file api call
    - [] get request(name) --> file
    - [] Possibility: JPArepo instead of list
- Base node classes


# Discovery services part

The nodes are organized in ring topology. If a node joins or leaves, it needs to enter/ leave this topology, dynamically.
On failure of a node, the network must be self healing.

## Discovery and Bootstrap 
  ### Naming Server (Robbe)
  - [x] needs to have a method to recieve a multicast 
    - [x] When receiving a multicast, add the node (if not exist) to the database
  ### Node (Robbe)
  - [x] needs to be able to transmit a multicast to all the nodes on the network on startup.
  - [] needs to be able to recieve a multicast.
    - [] When receiving a multicast, update local database on previous and next node in the ring.
    - [] Sent the changes to the new node (it's own ID so the new node can configure this as next/prev node)
  ### Steps done (see ppt)
  1. [x] Method for multicast transmission developed 
  2. [x] Develop method to calc hash
  3. [x] node will send during bootstrap a multicast with its name and IP (and port for reply)
  4. Naming server steps when recieving multicast from node
    1. [x] Calc hash of node (create node object)
    2. [x] count the known hosts in the network
    3. [x] Check if node exists (can be done in database?) and add to database
    4. [x] Send the amount of nodes available to the multicaster
  5. Nodes steps when receiving multicasts 
    1. [] Calc hash of receiving name
    2. [] Set the prev and next node based on the receiving node.
    3. [] Response to the multicast node where we only fill in the next and current or prev and current set for this node.
      This collides with the naming server task? Maybe the sendout from node to node is not nesecairy because the naming server does this.
  6. [] Node that casts the multicast, recieves a message from the naming server containing the amount of nodes.
    1. [x] Wait an x amount of time before closing the readPort
    2. [] Check the amount of available nodes and set the prev and next node accordingly
## Shutdown
  ### Naming Server
  - [] needs a method (or API call) that removes the node that shuts itself down.
    - This removes the Node ID from the NameServer database
  ### Node
  - [] needs to be able to send out the deletion of its own ID in the Naming Server at shutdown.
  - [] needs to be able to send out the renewal of the neighbour nodes (previous and next node) configurations so the ring doesn't break.
    - The node that shutdowns, has the data of the direct neighbour nodes so the communication is direct.
  - [] needs to be able to recieve a shutdown state from a neighbour and reassign its previous and next node accordingly
    - The previous or next node object, can be send through the shutdown state message so the neighbour node doesn't need to recalculate stuff.


## Failure
### Naming Server
  - Needs to be able to receive a request for the closest ID's of nodes based on the received node id.
    - This means, send out 2 node objects to the requester

### Node
  - needs to have a failback method at every exception to transmit the ID's to the next and previous.
    - The node is suddenly gone so the neighbour nodes must detect these with a ping or alive packet.
