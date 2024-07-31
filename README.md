﻿# STOMP World Cup Update Subscription Service
World Cup Update Subscription Service
This project is a STOMP (Simple-Text-Oriented-Messaging-Protocol) based server-client system developed as part of the SPL (Systems Programming Language) course. It aims to facilitate a community-driven World Cup update subscription service, allowing users to subscribe to game channels, and report and receive game updates.

Features
Message Exchange: Enables clients to send and receive messages via a central server.
Concurrent Connections: Supports multiple clients connecting to the server simultaneously.
STOMP Protocol Commands: Implements essential STOMP commands including CONNECT, SEND, SUBSCRIBE, UNSUBSCRIBE, and DISCONNECT.
Requirements
C++ Compiler: Supporting C++11.
Boost Library: Required for networking and threading functionalities.
Java 8 or Higher: For server implementation.
Getting Started
Clone the Repository
sh
Copy code
git clone <repository_url>
Server Setup
Navigate to the server directory:
sh
Copy code
cd server
Build the server using Maven:
sh
Copy code
mvn compile
Run the server using one of the design patterns:
Thread-Per-Client (TPC):
sh
Copy code
mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.stomp.StompServer" -Dexec.args="<port> tpc"
Reactor:
sh
Copy code
mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.stomp.StompServer" -Dexec.args="<port> reactor"
Client Setup
Navigate to the client directory:
sh
Copy code
cd client
Build the client using Makefile:
sh
Copy code
make
Run the client:
sh
Copy code
./bin/StompWCIClient
Client Commands
Login: Connect to the server.
sh
Copy code
login {host:port} {username} {password}
Subscribe: Join a game channel.
sh
Copy code
join {game_name}
Unsubscribe: Exit a game channel.
sh
Copy code
exit {game_name}
Send Report: Report game events from a file.
sh
Copy code
report {file}
Summarize: Generate a summary of game events for a specific user.
sh
Copy code
summary {game_name} {user} {file}
Logout: Disconnect from the server.
sh
Copy code
logout
Example Usage
Login:
sh
Copy code
login 127.0.0.1:7777 user pass
Join a Game Channel:
sh
Copy code
join Germany_Japan
Report Game Events:
sh
Copy code
report ./data/events1.json
Summarize Game Events:
sh
Copy code
summary Germany_Japan user ./data/test
Exit Game Channel:
sh
Copy code
exit Germany_Japan
Logout:
sh
Copy code
logout
