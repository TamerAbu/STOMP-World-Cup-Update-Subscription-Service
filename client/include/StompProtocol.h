#pragma once
#include <vector>
#include <string>
#include "../include/ConnectionHandler.h"
#include "../include/GameEvents.h"
using namespace std;

// TODO: implement the STOMP protocol
class StompProtocol
{
private:
    

public:
    StompProtocol();
    void socketListener();
    bool sendFrame(string &frame);
    vector<string> getWordsBySpace(string &s);
    vector<string> split(string &str, char delimiter);
    void convertToConnect(vector<string> &userCommand, string &frame);
    void convertToSubscribe(vector<string> &userCommand, string &frame);
    void convertToUnsubscribe(vector<string> &userCommand, string &frame);
    void convertToSend(vector<string> &commandBySpaces, string &frameToSend);
    void convertToDisconnect(vector<string> &commandBySpaces, string &frameToSend);
    void summaryPrint(vector<string> &commandBySpaces, string &frameToSend);
    string convertToSendHelper(map<string, string> updates);
    void parseFrame(string &s);
    void parseConnected();
    void parseMessage(vector<string> &connectedFrame);
    void parseReceipt(vector<string> &connectedFrame);
    void parseError(vector<string> &connectedFrame);

    int topicId;
    int receiptId;
    ConnectionHandler *connectionHandler;
    bool _isConnected;
    map<string, int> topicToId;
    string username;
    GameEvents gameReports;
    // GameEvents *gameReports;


    //void StompProtocol::convertToSummey(vector<string> &commandBySpaces, string &frameToSend);
};
