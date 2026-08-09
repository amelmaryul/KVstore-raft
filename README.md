# Distributed Raft Key Value Store

A distributed key value store written in Java implementing the Raft consensus algorithm for leader election and log replication. The project explores how distributed systems maintain consistency across multiple nodes while tolerating failures.
## Setup

### 1. Build the project

Create the Docker images for the cluster:

```bash
./kvstore.sh build
```

After the images have been built, you can start and stop the cluster at any time using:

```bash
./kvstore.sh start
./kvstore.sh stop
```

### 2. Connect to the cluster

The gateway listens for TCP connections and accepts client requests. You can connect using your own TCP client or use the client included with the project.

To use the provided client, first compile the project:

```bash
./kvstore.sh compile
```

Then launch the client:

```bash
./kvstore.sh client
```

### 3. Supported commands

The gateway accepts the following commands:

```text
get <key>
set <key> <value>
delete <key>
```
Keys or values longer then 1 must be wrapped in quotes:

```text
set "key longer than one word" "value longer than one word"
```


## Features

- Raft leader election
- Log replication
- Persistent key-value storage
- TCP gateway
- Docker Compose deployment
- Thread-safe request handling
- RESP based client communication 

## Known Limitations
- No log compaction
- Static cluster membership
- Gateway single point of failure
- RESP parsing is line based and not length based























