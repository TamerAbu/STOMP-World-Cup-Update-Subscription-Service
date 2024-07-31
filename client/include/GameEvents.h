#include <map>
#include <vector>
#include <string>
#include <iostream>
#include <algorithm>

using namespace std;

class GameEvents {

    public:
        GameEvents();
        void addUserToGame(string gameName, string username);
        void handleUpdates(vector<string> msgLines, string gameName, string username);
        void addEvent(string &msg);
        vector<string> split(string& str, char delimiter);
        map<string, vector<string>> gamesToUsers;

        map<string, map<string, string>> generalEvents;
        map<string, map<string, string>> teamaEvents;
        map<string, map<string, string>> teambEvents;
        map<string, map<int, string>> eventsByTime;



    void handleUpdates(string &msg, string gameName ,string &user);


};