# Tasks

- Robbe:
  - [x] Algorithm for file names convertion to hash value
  - [x] Save to JSON

- Tom:
  - [x] API

- Berkay
  - Name server:
    - [] Map(int, ip)
    - [] get ip from filename
    - [] Add/Remove nodes from map
    - [] Algorithm to look up NodeID for gotten hashID (Xor, extends/implemts)

- Ahmad
  - Node (client node)
    - [x] id, ip
    - [x] local list of files & replica/remote of files
    - [] create/delete file --> api call for update
    - [] get file api call
    - [] get request(name) --> file
    - [] Possibility: JPArepo instead of list
- Base node classes


# Discovery services

## Discovery and Bootstrap
  ### Naming Server
    needs to have a method to recieve a multicast on the Naming server and perform some tasks on it.
  ### Node
    needs to transmit a multicast to all the nodes on the network.

## Shutdown

  ### Naming Server
    needs a method that removes the node that shutdowns
  ### Node
    needs to have a failback method to transmit the ID's to the next and previous
    needs to have method to recieve these
## Failure