#!/bin/bash

get_container () {
    temp=$(echo -n "$old_leader" | tail -c 1 )
    case $temp in
        5)
        container="kvstore-raft-node5-1"
        ;;
        4)
        container="kvstore-raft-node4-1"
        ;;
        3)
        container="kvstore-raft-node3-1"
        ;;
        2)
        container="kvstore-raft-node2-1"
        ;;
        1)
        container="kvstore-raft-node1-1"
        ;;

        *)
        echo "invalid container"
    esac
}

start_time () {
    start_t=$(date +"%s%3N")

}

end_time () {
    end_t=$(date +"%s%3N")
}

write_key () {
        ./kvstore.sh client << EOF
set $i $i
EOF
    
}


get_key () {
        ./kvstore.sh client << EOF
get $key
EOF
}

t=0

case $1 in
    election)
    > ./bench/results/election.csv
    # get the leaders container id and stop that container
    # keep on looping over the gateway until a new leader is elected
    # once leader is elected stop time and restart the killed node
    s=$(date +"%s")
    for i in {1..20}; do
        old_leader=$(./kvstore.sh client <<< "Leader" | grep 'Server says' | tail -n 1 | awk '{print $3}')
        get_container
        start_time
        docker kill "$container" > /dev/null
        echo -n "Value of old leader: "
        echo "$old_leader"
        new_leader=$(./kvstore.sh client <<< "Leader" | grep 'Server says' | tail -n 1 | awk '{print $3}')
        echo -n "Value of new leader: "
        echo "$new_leader"
        while [[ "$new_leader" == "$old_leader" ]]; do
            new_leader=$(./kvstore.sh client <<< "Leader" | grep 'Server says' | tail -n 1 | awk '{print $3}')
            echo -n "Value of new leader: "
            echo "$new_leader"
        done
        end_time
        echo $(("$end_t" - "$start_t")) >> ./bench/results/election.csv 
        docker start "$container" > /dev/null
        sleep 5
    done
    total_time=$(date +"%s")
    echo -n "Time taken in seconds: "
    echo $(("$total_time" - "$s"))
    ;;


    correctness)
    # note you may have to delete the current logs first. 

    # check correctness under failure.
    # take turns killing a random node and killing the leader node 
    # try appending some key to the log. only keep track of keys that have been acknowleged from the cluster
    # after seting all keys cross check if they keys were appended by attempting to get all keys. 
    # should always work 100% of the time bcs of raft invariants. 
    count=0
    acks=()
    for i in {1..20}; do 
        if (( i % 2 == 0)); then
            old_leader=$(./kvstore.sh client <<< "Leader" | grep 'Server says' | tail -n 1 | awk '{print $3}')
        else 
            old_leader=$(( RANDOM % 5 + 1 ))
        fi
        get_container
        docker kill "$container" > /dev/null
        sleep 3
        ack=$(write_key | grep "Server says:" | tail -1)
        if [[ $ack == "Server says: Added Set command" ]]; then
            acks+=("$i")
        fi
        sleep 1
        docker start "$container" > /dev/null
    done
    for i in "${!acks[@]}"; do
        key="${acks[i]}"
        getk=$(get_key | grep "Server says:" | tail -1)
        if [[ $getk == "Server says: $key" ]]; then
            ((count++))
        fi

    done
    echo -n correctness under failure: 
    echo -n  $(( count * 100 / "${#acks[@]}" ))
    echo "%"
    ;;

esac