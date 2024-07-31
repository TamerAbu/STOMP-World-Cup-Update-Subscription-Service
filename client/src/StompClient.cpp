#include <stdlib.h>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>
#include <map>
#include "../include/ConnectionHandler.h"
#include "../include/event.h"
#include "../include/StompProtocol.h"
#include <thread>

using namespace std;
/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/
int main (int argc, char *argv[]) {
    // parse argumnents

    // if (argc < 3) {
    //     std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
    //     return -1;
    // }

    // std::string host = argv[1];
    // short port = atoi(argv[2]);
    ConnectionHandler *connectionHandler = nullptr;
    StompProtocol protocol = StompProtocol();
    bool _isConnected = false;
    // Initiate a GameEvents instance;
	std::thread socketListenerThread(&StompProtocol::socketListener, &protocol);
	//From here we will see the rest of the ehco client implementation:
    while (1) {
        // We want to create only 1 CH per client, iff client isn't logged in.
        
        const short bufsize = 1024;
        char buf[bufsize];

        // keyboard input
        std::cin.getline(buf, bufsize);
        std::string line(buf);
        // process input
        vector<string> commandBySpaces = protocol.getWordsBySpace(line); // getWordsBySpace(line);
        string frameCommand = commandBySpaces[0];

        if (!protocol.sendFrame(line)) {
            std::cout << "Disconnected. Exiting...\n" << std::endl;
        }

        int len=line.length();
        std::cout << "Sent " << len+1 << " bytes to server" << std::endl;
    
    }
    //socketListenerThread.join();
    return 0;
}






