# 6-DS project

This projects will create a ring topology with clients. A Naming remoteNode that will manage the resources and namings of the nodes files in the ring topology

# Naming Server part

- Robbe:
  - [x] Algorithm for file ngit ames convertion to hash value
  - [x] Save to JSON to a file

- Tom:
  - [x] API

- Berkay
  - Name remoteNode:
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
  4. Naming remoteNode steps when recieving multicast from node
    1. [x] Calc hash of node (create node object)
    2. [x] count the known hosts in the network
    3. [x] Check if node exists (can be done in database?) and add to database
    4. [x] Send the amount of nodes available to the multicaster
  5. Nodes steps when receiving multicasts 
    1. [x] Calc hash of receiving name
    2. [x] Set the prev and next node based on the receiving node.
    3. [x] Response to the multicast node.
  6. [x] Node that casts the multicast, recieves a message from the naming remoteNode containing the amount of nodes.
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
  - [x] Needs to be able to receive a request for the closest ID's of nodes based on the received node id.
    - This means, send out 2 node objects to the requester


### Node
  - [] Needs to have a failback method at every exception to transmit the ID's to the next and previous.
    - The node is suddenly gone so the neighbour nodes must detect these with a ping or alive packet.

## DsProjectApplication (Ahmad)
- [x] Integrating discovery in the DsProjectApplication
## Unit test (Ahmad)

### Client
  - [x] Discovery
  - [x] Bootstrap
  - [ ] Failure
  - [ ] Shutdown 
### Naming remoteNode
  - [ ] Discovery
  - [ ] Bootstrap
  - [ ] Failure
  - [ ] Shutdown 

# Replication part
We have files with a name. This name can be hashed by our simple hash algorithm. Now we can compare these hashes with the known node's ID's that are situated in the ring topology.

The goal of this part is to ensure that all files, with a specific hash range, are located at the same node. Now we know through the naming remoteNode, where a file might be located.

To ensure easy coding, we'll create a file transfer class that can be used by the nodes to transfer a file over a TCP socket from node to node.

This will be done in 3 phases.

## starting (Robbe & Tom)
All files that are stored on each node should be replicated to corresponding nodes in the ring topology. This way, a new node to which the file is replicated becomes the owner of the file.

After bootstrap and discovery, the new node has to verify its local files (folder on
the hard drive). The node will send over each file name to the naming remoteNode and the naming remoteNode will send back a destination node if the file has to be replicated. The replicated node is the first smaller hash ID node the n the file hash.

So there are 3 nodes with the fommowing hash ID: 1 5 7. We have a file on node 1 with hash equal to 6. Then the new replication node will be node 5 because this is the 1 lesser then node based on the hash ID of the file.

The naming remoteNode will respond to the original node where to transfer through. If a replication node receives the file, it adds a log to the file logging.

Each file will have a full log available to track its replications and owners of the file.

## Update (Ahmad)
If new files are added locally to certain node, or deleted from a node, this state has to be synchronized in the whole system. If a new file is added, then it has to be replicated. Otherwise, if deleted, it has to be deleted from the replicated files of the file owner as well.

When we add a local file, this should be replicated immediately. We can startup a thread to check if the file three has changed, in a given interval (eg 2 seconds interval).

The replication can be used from the starting phase where we send the file name to the namingserver API and then determine where to transfer it to over a TCP socket.

## Shutdown
When the node is terminated, all files on this node need to be sent to the previous node.

The previous node will check and handle the files based on the following conditions:

If the file is a backup file and the node does not have the original, the file will be saved.  
If the file is a backup file and the node DOES have the original, the file will be sent to the next previous node.  
If the file is an original file, the node will contact the node with the backup file and update the download location in the logs.  

## Group division

### Robbe

- [x] Create log class
- [x] Ensure that when a new file arrives, the log can be created of that file.
- [x] Create the File Transfer class.
- [x] Ensure that when transfering a file, the log of that file is automatically included.
- [x] Create File Controlling class for easy interaction with the files.
- [x] Create File based on a condition interface (idea by Berkay).

### Tom

- [x] Look for and load in all files in a specific directory (/assets) on startup.
- [x] Place files in local database.
- [x] Add a GET-request to the NameServer to ask for the right node to which the files should be replicated to.
  - [x] In case this node is the same as the one who performed the request, send the file to the previous node.
- [x]  Send files to the right node.
  - Done using Robbe's FileTransceiver class.
- [x]  Listen for any incoming files and save incoming files.
  - Done using Robbe's FileTransceiver class.

### Ahmad

- [] Update state File

### Berkay
- [x] make util class so easly use REST api of client/nameserver
- [x] shutdown
  - [x] when terminated it should move it's replicated files to the prev node
  - [x] if a node receives a repliceted file it should check if they have the Originals
    - [x] if they do: send the file to the it's prev node
    - [x] if they don't: save file
  - [x] if a node with an Original file gets shut down:
      - [x] send file to prev node 
      - [x] prev ndoe will rell the backup file that it is the new download location
  - [x] tests


# sync and failure agents

We have to create 2 types of agents. A synchronize agent and a failure agent. This to ensure that we have a fully synchronized distributed file access (by the sync agent) and a failure event where the failure agent will come in to make sure no files are lost during the steps of the shutdown.

## Sync (Robbe)
The sync or synchronize agent will hold all the files that are currently in the topology. We'll transfer this agent list through a REST call from the next node of the current node.

The sync agent is present on each node. This agent will be called in an interval of X seconds. When called, the sync agent begins his run method. This include checking his own files on changes, the next node’s sync agent files on changes, checking if there’s a request on a file lock and lastly check if there’s a request on an unlocking of a file.

The sync or synchronize agent will have a data structure that holds the filename and the lock of that file. When running, we'll check the files of that node with the database and update accordingly if any changes have occurred. This are only adding changes and no delete changes.

Each node will have 2 queues where it can request a file lock or unlock (only after a lock of course). These queues will be checked by the sync agent when activated by the interval. If something is in the lock request queue, the agent can check this if the file is somewhere else already locked or not. if not, the file gets locked by this node and the sync agent updates its database. When a lock is not required anymore, the node can request an unlock and place this in the queue. The sync agent again will check if the file was locked and then unlock this.

A client can check his lock request in a read only queue where all the accepted lock request are listed. When a client asks for an unlock, the queue data structure, will check if there were any accepted locks on this file. This to prevent unneeded items in the queue.

## Failure

- [] shutdown
  - [] when terminated it should move it's replicated files to the prev node
  - [] if a node receives a repliceted file it should check if they have the Originals
    - [] if they do: send the file to the it's prev node
    - [] if they don't: save file
  - [] if a node with an Original file gets shut down:
      - [] tell the node with the replicated file that it's the only one now --> becomes new local file, needs a new replication?
      - []  
      
      
# Agents

We have to create 2 type of agents. A synchronize agent and a failure agent. This to ensure that we have a fully synchronized distributed file access (by the sync agent) and a failure event where the failure agent will come in to make shure no files are lost during the steps of the shutdown.

## sync agent (Robbe)

The sync or synchronize agent will hold all the files that are currently in the topology. We'll transfer this agent through a REST call on the next node of the current node. We can say that the synchronization agent will be tossed in the ring and will run on each node to check the files on the node and update accordingly.

This means that only 1 agent can be created inside the ring topology because it will be tossed arround like a hot potato. That's why we only activate the sync agent when there are more than 1 nodes in the network. This will be done by the node that joins the network and sees that he is the second node inside the network.

The sync or synchronize agent will have a datastructure that holds the filename and the lock of that file. When visiting a node, we'll check the files of that node with the database and update accordingly if any changes have occured. This are only add changes and no delete changes.

Each node will have 2 queues where it can request a file lock or unlock (only after a lock ofcource). These queues will be checked by the sync agent when it is at the node and after the file synchronization. If something is in the lock request queue, the agent can check this if the file is somewhere else already locked or not. if not, the file gets locked by this node and the sync agent updates its database. When a lock is not required anymore, the node can request an unlock and place this in the queue. The sync agent again will check if the file was locked and then unlock this.

After these tasks, the sync agent will be transmitted to the next node over a REST call where the same steps happens again.

For now we leave security out of the picture because we do not have the time to implement this. So for now we assume that we only request an unlock if we have locked the file in the first place (this could be a security leak).



## failure agent

...
=======
...

## Group division

### Robbe
- [x] sync agent
  - [x] check current node files
  - [x] check next node files
  - [x] check lock requests and update local db
  - [x] when lock approved, add to accepted queue
  - [x] check unlock requests and update local db
- [x] handle sync agent db GET from REST call
- [x] provide data structure for client to request locks, unlocks check accepted locks.


### Tom
/

### Ahmad
/

### Berkay
/