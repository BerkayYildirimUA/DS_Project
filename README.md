# 6-DS project

This projects will create a ring topology with clients. A Naming server that will manage the resources and namings of the nodes files in the ring topology

# Naming Server part

- Robbe:
  - [x] Algorithm for file names convertion to hash value
  - [x] Save to JSON to a file

- Tom:
  - [x] API

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

## Discovery and Bootstrap (Robbe)
  ### Naming Server (Robbe)
  - [x] needs to have a method to recieve a multicast 
    - [x] When receiving a multicast, add the node (if not exist) to the database
  ### Node (Robbe)
  - [x] needs to be able to transmit a multicast to all the nodes on the network on startup.
  - [x] needs to be able to recieve a multicast.
    - [x] When receiving a multicast, update local database on previous and next node in the ring.
    - [x] Sent the changes to the new node (it's own ID so the new node can configure this as next/prev node)
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
    1. [x] Calc hash of receiving name
    2. [x] Set the prev and next node based on the receiving node.
    3. [x] Response to the multicast node.
  6. [x] Node that casts the multicast, recieves a message from the naming server containing the amount of nodes.
    1. [x] Wait an x amount of time before closing the readPort (and some retries)
    2. [x] Check the amount of available nodes and set the prev and next node accordingly


## Shutdown (Berkay)
  ### Naming Server
  - [] needs a method (or API call) that removes a node that then shuts itself down.
    - This removes the Node ID from the NameServer database
  ### Node
  - [] needs to be able to send out the deletion of its own ID in the Naming Server at shutdown.
  - [] needs to be able to send out the renewal of the neighbour nodes (previous and next node) configurations so the ring doesn't break.
    - The node that shutdowns, has the data of the direct neighbour nodes so the communication is direct.
  - [] needs to be able to recieve a shutdown state from a neighbour and reassign its previous and next node accordingly
    - The previous or next node object, can be send through the shutdown state message so the neighbour node doesn't need to recalculate stuff.


## Failure (Tom)
### Naming Server
  - [] Needs to be able to receive a request for the closest ID's of nodes based on the received node id.
    - This means, send out 2 node objects to the requester


### Node
  - needs to have a failback method at every exception to transmit the ID's to the next and previous.
    - The node is suddenly gone so the neighbour nodes must detect these with a ping or alive packet.

## DsProjectApplication (Ahmad)
- [x] Integrating discovery in the DsProjectApplication
## Unit test (Ahmad)

### Client
  - [x] Discovery
  - [x] Bootstrap
  - [ ] Failure
  - [ ] Shutdown 
### Naming server
  - [ ] Discovery
  - [ ] Bootstrap
  - [ ] Failure
  - [ ] Shutdown 

# Replication part
We have files with a name. This name can be hashed by our simple hash algorithm. Now we can compare these hashes with the known node's ID's that are situated in the ring topology.

The goal of this part is to ensure that all files, with a specific hash range, are located at the same node. Now we know through the naming server, where a file might be located.

To ensure easy coding, we'll create a file transfer class that can be used by the nodes to transfer a file over a TCP socket from node to node.

This will be done in 3 phases.

## starting (Robbe)
All files that are stored on each node should be replicated to corresponding nodes in the ring topology. This way, a new node to which the file is replicated becomes the owner of the file.

After bootstrap and discovery, the new node has to verify its local files (folder on
the hard drive). The node will send over each file name to the naming server and the naming server will send back a destination node if the file has to be replicated. The replicated node is the first smaller hash ID node the n the file hash.

So there are 3 nodes with the fommowing hash ID: 1 5 7. We have a file on node 1 with hash equal to 6. Then the new replication node will be node 5 because this is the 1 lesser then node based on the hash ID of the file.

The naming server will respond to the original node where to transfer through. If a replication node receives the file, it adds a log to the file logging.

Each file will have a full log available to track its replications and owners of the file.

## Update (Ahmad)
If new files are added locally to certain node, or deleted from a node, this state has to be synchronized in the whole system. If a new file is added, then it has to be replicated. Otherwise, if deleted, it has to be deleted from the replicated files of the file owner as well.

When we add a local file, this should be replicated immediately. We can startup a thread to check if the file three has changed, in a given interval (eg 2 seconds interval).

The replication can be used from the starting phase where we send the file name to the namingserver API and then determine where to transfer it to over a TCP socket.

## shutdown
If needed, all files locally stored on a node that is about to shutdown should be transferred to other nodes. Otherwise, they can be deleted and this change needs to be synced as described above in the Update.

Shutdown has a couple of steps:

### shutdown of replication node
When the node is terminated, the files that are replicated on this node, needs to be replicated to the previous node (because this will be the smallest hash ID in line now).

If the previous node has a locally stored file of the receiving files, it will send it to his previous node. (remember -> node.id < file.id).

When transfering the files, we must include the logging file as well.

### shutdown of owner node

I'am confused by the given specifications, so this section has to be revised.

## Group division

### Robbe

- [x] Create log class
- [x] Ensure that when a new file arrives, the log can be created of that file.
- [x] Create the File Transfer class.
- [x] Ensure that when transfering a file, the log of that file is automatically included.
- [x] Create File Controlling class for easy interaction with the files.


### Berkay

- [] shutdown

### Tom

- [] Startup
  - [] Extra state toevoegen
  - [] Bekijkt bestand (in specifieke directory)
  - [] Verwerkt bestanden en voegt toe aan database
  - [] Stuurt data over bestanden naar NS
  - [] NS zoekt naar de eerste id die boven de file id staat
    - [] Als dit de originele verzend node is dan sturen naar prev node
  - [] Indien kleiner, stuur naar NS voor correcte node IP
  - [] Stuur naar juiste node via FileTranscieverService
  - [] Aan ontvangende node bestand opslaan

### Ahmad

- [] Update state File
