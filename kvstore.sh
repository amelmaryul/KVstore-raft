#!/bin/bash



case $1 in
    build)
    echo "building.."
    javac -d target src/*.java
    echo "build successful"
    ;;

    start)
    docker compose -f target/docker-compose.yml up --build
    ;;

    client)
    java -cp target Client 5050
    ;;

    *)
    echo "Error unknown command: '$1'"
    ;;

esac