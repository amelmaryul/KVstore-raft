#!/bin/bash


set -e

show_help() {
    echo "Usage: $0 [command]"
    echo ""
    echo "Commands:"
    echo "  compile   Building java source code into target directory"
    echo "  build     Compiling and building docker images"
    echo "  start     Starting docker cluster"
    echo "  stop      Pausing docker cluster"
    echo "  logs      Showing live cluster logs (Press Ctrl+C to exit)"
    echo "  clean     Wiping cluster, deleting containers, and removing networks"
    echo "  client    Starting kvstore client on port 5050"
    echo "  help      Show this help menu"
}

case $1 in
    compile)
    echo "Building java source code"
    javac -d target $(find src -name "*.java")
    echo "build successful"
    ;;

    build)
    echo "Compiling and building docker images"
    docker compose up --build -d
    ;;

    start)
    echo "Starting docker cluster"
    docker compose start
    ;;

    stop)
    echo "Pausing docker cluster"
    docker compose stop
    ;;

    logs)
    echo "Showing live cluster logs (Press Ctrl+C to exit)..."
    docker compose logs -f
    ;;

    clean)
    echo "Wiping cluster, deleting containers, and removing networks..."
    docker compose down -v
    ;;

    client)
    echo "Starting kvstore client"
    java -cp target client.Client 5050
    ;;

    help|-h|--help)
    show_help
    ;;

    *)
    echo "Error unknown command: '$1'"
    echo ""
    show_help
    exit 1
    ;;

esac