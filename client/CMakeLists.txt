cmake_minimum_required(VERSION 3.24)
project(client)

set(CMAKE_CXX_STANDARD 14)

include_directories(include)

add_executable(client
        include/ConnectionHandler.h
        include/event.h
        include/GameEvents.h
        include/json.hpp
        include/StompProtocol.h
        src/ConnectionHandler.cpp
        src/echoClient.cpp
        src/event.cpp
        src/GameEvents.cpp
        src/StompClient.cpp
        src/StompProtocol.cpp)
