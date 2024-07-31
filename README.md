STOMP Based Server Client World Cup Update Subscription Service
World Cup Update Subscription Service
This project is a STOMP (Streaming Text Oriented Messaging Protocol) based server-client system developed as part of the SPL (Systems Programming Language) course. It aims to facilitate a community-driven World Cup update subscription service, allowing users to subscribe to game channels, and report and receive game updates.

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

Server Setup
Navigate to the server directory:
cd server
Build the server using Maven:

mvn compile
Run the server using one of the design patterns:
Thread-Per-Client (TPC):

mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.stomp.StompServer" -Dexec.args="<port> tpc"
Reactor:

mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.stomp.StompServer" -Dexec.args="<port> reactor"
Client Setup
Navigate to the client directory:

cd client
Build the client using Makefile:

make
Run the client:

./bin/StompWCIClient
Client Commands
Login: Connect to the server.

login {host:port} {username} {password}
Subscribe: Join a game channel.

join {game_name}
Unsubscribe: Exit a game channel.

exit {game_name}
Send Report: Report game events from a file.

report {file}
Summarize: Generate a summary of game events for a specific user.

summary {game_name} {user} {file}
Logout: Disconnect from the server.

logout

Example Usage

Login:
login 127.0.0.1:7777 user pass

Join a Game Channel:
join Germany_Japan

Report Game Events:
report ./data/events1.json

Summarize Game Events:
summary Germany_Japan user ./data/test

Exit Game Channel:
exit Germany_Japan

Logout:
logout
