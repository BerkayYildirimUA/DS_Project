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
  - [] needs to have a method to recieve a broadcast
    - [] When receiving a broadcast, add the node (if not exist) to the database
  ### Node (Robbe)
  - [] needs to be able to transmit a broadcast to all the nodes on the network on startup.
  - [] needs to be able to recieve a broadcast.
    - [] When receiving a broadcast, update local database on previous and next node in the ring.
    - [] Sent the changes to the new node (it's own ID so the new node can configure this as next/prev node)

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
