#include "../include/StompProtocol.h"
#include "../include/event.h"
#include "../src/event.cpp"
#include <sstream>
#include <string>
#include <vector>

#include <iostream>
#include <fstream>

using namespace std;

StompProtocol::StompProtocol()
: topicId(0), receiptId(0), connectionHandler(nullptr), _isConnected(0),
topicToId(map<string, int>()), gameReports(GameEvents()) {}
// topicToId(map<string, int>()), gameReports(new GameEvents()) {}

void StompProtocol::socketListener() {
    while (1) {
        if (_isConnected) {
            // We can use one of three options to read data from the server:
            // 1. Read a fixed number of characters
            // 2. Read a line (up to the newline character using the getline() buffered reader
            // 3. Read up to the null(= \0) character
            std::string answer;

            if (!connectionHandler->getLine(answer)) {
                std::cout << "Disconnected. Exiting...\n" << std::endl;
            }

            parseFrame(answer);

        }
    }
}

bool StompProtocol::sendFrame(string &frame) {

    vector<string> commandBySpaces = getWordsBySpace(frame); // getWordsBySpace(line);
    string frameCommand = commandBySpaces[0];
    string reply;
    string frameToSend = "";

    if (frameCommand == "login" && !_isConnected) {
        convertToConnect(commandBySpaces, frameToSend);

    } else if (frameCommand == "join" && _isConnected) {
        cout <<"debug:" << frameCommand << endl;
        convertToSubscribe(commandBySpaces, frameToSend);

    } else if (frameCommand == "exit" && _isConnected) {
        convertToUnsubscribe(commandBySpaces, frameToSend);
        
    } else if (frameCommand == "report" && _isConnected) {
        convertToSend(commandBySpaces, frameToSend);

    } else if (frameCommand == "summary" && _isConnected) {
        summaryPrint(commandBySpaces, frameToSend);
    } else if (frameCommand == "logout" && _isConnected) {
        convertToDisconnect(commandBySpaces, frameToSend);
    } else if (frameCommand == "login" && _isConnected) {
        cout << "The client is already logged in, log out before trying again";
        cout << "login first!";
    }

    if (frameToSend.compare("")) {
        if (!connectionHandler->sendFrameAscii(frameToSend, '\0')) {
            cout << "Could not connect to server";
            return false;
        }
    }

    return true;
}

void StompProtocol::convertToConnect(vector<string> &commandBySpaces, string &frameToSend) {
        string host = split(commandBySpaces[1],':')[0];
        short port = stoi(split(commandBySpaces[1],':')[1]);

        connectionHandler = new ConnectionHandler(host, port);
        connectionHandler->connect();
        _isConnected = true;

        frameToSend = "CONNECT\n";
        frameToSend.append("accept-version:1.2\nhost:stomp.cs.bgu.ac.il\n");
        frameToSend.append("login:" + commandBySpaces[2] + "\npasscode:" + commandBySpaces[3] + "\n\n");
        username = commandBySpaces[2];
}

void StompProtocol::convertToSubscribe(vector<string> &commandBySpaces, string &frameToSend) {
    string topic = commandBySpaces[1];
    if (topicToId.find(topic) == topicToId.end()) {
        frameToSend = "SUBSCRIBE\ndestination:" + commandBySpaces[1] + "\nid:" + to_string(topicId) +
        "\nreceipt:" + to_string(receiptId) + "\n\n";
        topicToId.insert(std::make_pair(topic, topicId));
        receiptId++;
        topicId++;
    }
}

void StompProtocol::convertToUnsubscribe(vector<string> &commandBySpaces, string &frameToSend) {
    string topic = commandBySpaces[1];
    if (topicToId.find(topic) != topicToId.end()) {
        frameToSend = "UNSUBSCRIBE\nid:" + to_string(topicToId.at(topic)) +
        "\nreceipt:" + to_string(receiptId) + "\n\n";
        topicToId.erase(topic);
        receiptId++;
    }
}

void StompProtocol::convertToSend(vector<string> &commandBySpaces, string &frameToSend) {
        string jsonFilePath = commandBySpaces[1];
        names_and_events data = parseEventsFile(jsonFilePath);

        for (Event &event : data.events) {  // send event
            frameToSend = "SEND\ndestination:" + data.team_a_name + "_" + data.team_b_name
            + "\n\nuser:" + username + "\nteam a:" + data.team_a_name  
            + "\nteam b:" + data.team_b_name + "\nevent name:" + event.get_name()
            + "\ntime:" + to_string(event.get_time()) 
            + "\ngeneral game updates:\n" + convertToSendHelper(event.get_game_updates())  
            + "team a updates:\n" + convertToSendHelper(event.get_team_a_updates())
            + "team b updates:\n" + convertToSendHelper(event.get_team_b_updates())
            + "description:\n" + event.get_discription();
            connectionHandler->sendLine(frameToSend);
        }

}

void StompProtocol::summaryPrint(vector<string> &commandBySpaces, string &frameToSend) {
//string &game_name, string &user, string file
    vector<string> names = split(commandBySpaces[1],'_');

    string gameUserName = commandBySpaces[1] + "_" + commandBySpaces[2];
    string finalReport = "\nReport by:" + commandBySpaces[2] + "\n" + names[0] + " VS " + names[1] +"\nGame stats:\nGeneral stats";
    for(auto const& event : gameReports.generalEvents[gameUserName]){
        finalReport = finalReport + event.first + ":" + event.second + "\n";
    }
    finalReport = finalReport + "Team A stats";
    for(auto const& event : gameReports.teamaEvents[gameUserName]){
        finalReport = finalReport + event.first + ":" + event.second + "\n";
    }
    finalReport = finalReport + "Team B stats";
    for(auto const& event : gameReports.teambEvents[gameUserName]){
        finalReport = finalReport + event.first + ":" + event.second + "\n";
    }
    finalReport = finalReport + "Game event reports:\n";
    for(auto const& event : gameReports.eventsByTime[gameUserName]){
        stringstream ss;
        ss << event.first;
        string str = ss.str();
        finalReport = finalReport + "At time: "+ str + "\nEvent:" + event.second + "\n";
    }

    /* try to open file to read */
    ofstream oldFile;
    oldFile.open("finalReportFor" + names[0] + "_" + names[1] + ".txt",std::ios_base::app);
    if(oldFile) {
        cout<<"file exists\n";
        oldFile << finalReport + "\n\n\n\n\n\n";
    } else {
        ofstream file("finalReportFor" + names[0] + "_" + names[1] + ".txt");
        file << finalReport;
        file.close();
        cout << finalReport;
    }

}

void StompProtocol::convertToDisconnect(vector<string> &commandBySpaces, string &frameToSend) {
    frameToSend = "DISCONNECT\nreceipt:-1";
}

string StompProtocol::convertToSendHelper(map<string, string> updates) {
    string formattedUpdates = "";

    for (std::map<string, string>::iterator it = updates.begin(); it != updates.end(); it++) {
        formattedUpdates.append("   " + it->first + ":" + it->second + "\n");
    }

    return formattedUpdates;
}

void StompProtocol::parseFrame(string &frame) {
    vector<string> frameLines = split(frame, '\n');
    string frameCommand = frameLines[0];
    if (!frameCommand.compare("CONNECTED")) { // compare returns 0 if identical.
        parseConnected();
    } else if (!frameCommand.compare("MESSAGE")) {
        parseMessage(frameLines);
    } else if (!frameCommand.compare("RECEIPT")) {
        parseReceipt(frameLines);
    } else {
        // Assuming server works correctly.
        // Only error is left.
        parseError(frameLines);
    }
}

void StompProtocol::parseConnected() {
    cout << "Login successful";
}

void StompProtocol::parseMessage(vector<string> &messageFrame) {
    // Should put message in correct data structure, according to ID.
    string user;
    string gameName;
    for (int i = 0; i < messageFrame.size(); i++) {
        if (messageFrame[i] != "") {
            vector<string> currLine = split(messageFrame[i], ':');

            if (currLine[0] == "destination"){
                gameName = currLine[1];
            }

            if (currLine[0] == "user"){
                user = currLine[1];
                gameReports.addUserToGame( gameName, user);
                break;
            }
        }
    }
    gameReports.handleUpdates(messageFrame, gameName, user);
    std::cout << "Disconnected. Exiting...\n" << std::endl;
}

void StompProtocol::parseReceipt(vector<string> &receiptFrame) {
    vector<string> receiptHeader = split(receiptFrame[1], ':');
    if (!receiptHeader[1].compare("-1")) {
        _isConnected = false;
        connectionHandler->close(); // Closes connection.
        connectionHandler->~ConnectionHandler();
    }
}

void StompProtocol::parseError(vector<string> &errorFrame) {
    _isConnected = false;
    connectionHandler->close(); // Closes connection.
}

vector<string> StompProtocol::getWordsBySpace(string &s) {
    vector<string> commandBySpaces;
    stringstream ss(s);
    string word;
    while(getline(ss, word, ' ')) {
        commandBySpaces.push_back(word);
    }
    return commandBySpaces;
}

vector<string> StompProtocol::split(/*const*/ string& str, char delimiter) {
    vector<string> substrings;
    stringstream ss(str);
    string substring;
    while(std::getline(ss, substring, delimiter)) {
        substrings.push_back(substring);
    }
    return substrings;
}

