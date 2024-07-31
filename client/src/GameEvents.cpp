#include "../include/GameEvents.h"
#include <sstream>

GameEvents::GameEvents()
: gamesToUsers(map<string, vector<string>>()), eventsByTime(map<string, map<int, string>>()) , generalEvents(map<string, map<string, string>>()), teamaEvents(map<string, map<string, string>>()), teambEvents(map<string, map<string, string>>())
{
}


void GameEvents::addUserToGame( string gameName, string username) {
    if (gamesToUsers.find(gameName) == gamesToUsers.end()) { // Check if game exists.
        gamesToUsers.insert(std::make_pair(gameName, vector<string>()));
    }
    if (gamesToUsers.find(gameName) == gamesToUsers.end()) { // Check if game exists.
        gamesToUsers.insert(std::make_pair(gameName, vector<string>())); 
    }

    if (std::find(gamesToUsers.at(gameName).begin(), gamesToUsers.at(gameName).end(), username) == gamesToUsers.at(gameName).end() ){ // check if user exist.
        gamesToUsers.at(gameName).push_back(username); // Add user to gameReport.
        generalEvents[gameName + "_" + username]["   "] = "  ";
        teamaEvents[gameName + "_" + username]["   "] = "  ";
        teambEvents[gameName + "_" + username]["   "] = "   ";
    }
    
}

void GameEvents::handleUpdates(vector<string> msgLines, string gameName, string username) {
    string name = gameName + "_" + username;
    string eventName;
    string time;
    for (int i = 0; i < msgLines.size(); i++) {
        if (msgLines[i] != "") {
            vector<string> currLine = split(msgLines[i], ':');
            if (currLine[0] == "event name") {
                eventName = currLine[1];
            }
            if (currLine[0] == "time") {
                time = currLine[1];
            }
            if (currLine[0] == "general game updates") { //handle general game updates
                i++;
                if (msgLines[i] != "") {
                    currLine = split(msgLines[i], ':');
                    while (currLine[0] != "team a updates") {
                        if (generalEvents.find(name) == generalEvents.end()) { // Check if game_user exists.
                            generalEvents[name].insert(std::make_pair(currLine[0], currLine[1]));
                        } else {
                            if (generalEvents[name].find(currLine[0]) == generalEvents[name].end()) { // Check if game exists.
                                generalEvents[name].insert(std::make_pair(currLine[0], currLine[1]));
                            } else {
                                generalEvents[name][currLine[0]] = currLine[1];
                            }
                        }
                        i++;
                        currLine = split(msgLines[i], ':');
                    }
                }
            }

            if (currLine[0] == "team a updates") { //handle final team-a updates
                i++;
                if (msgLines[i] != "") {
                    currLine = split(msgLines[i], ':');
                    while (currLine[0] != "team b updates") {
                        if (teamaEvents.find(name) == teamaEvents.end()) { // Check if game_user exists.
                            teamaEvents[name].insert(std::make_pair(currLine[0], currLine[1]));
                        } else {
                            if (teamaEvents[name].find(currLine[0]) == teamaEvents[name].end()) { // Check if game exists.
                                teamaEvents[name].insert(std::make_pair(currLine[0], currLine[1]));
                            } else {
                                teamaEvents[name][currLine[0]] = currLine[1];
                            }
                        }
                        i++;
                        currLine = split(msgLines[i], ':');
                    }
                }    
            }

            if (currLine[0] == "team b updates") { //handle final team-b updates
                i++;
                if (msgLines[i] != "") {
                    currLine = split(msgLines[i], ':');
                    while (currLine[0] != "description") {
                        if (teambEvents.find(name) == teambEvents.end()) { // Check if game_user exists.
                            teambEvents[name].insert(std::make_pair(currLine[0], currLine[1]));
                        } else {
                            if (teambEvents[name].find(currLine[0]) == teambEvents[name].end()) { // Check if game exists.
                                teambEvents[name].insert(std::make_pair(currLine[0], currLine[1]));
                            } else {
                                teambEvents[name][currLine[0]] = currLine[1];
                            }
                        }
                        i++;
                        currLine = split(msgLines[i], ':');
                    }
                }
            }

            if (currLine[0] == "description") { //handle description
                i++;
                while (i < msgLines.size()) {
                    eventName = eventName + "\n" + msgLines[i] + "\n";
                    i++;
                }
                stringstream tempTime(time); //convert string to int
                int intTime = 0;
                tempTime >> intTime;
                eventsByTime[name][intTime]= eventName; //Add event name - and description to map
            }
        }
    }
}


vector<string> GameEvents::split(string& str, char delimiter) {
    vector<string> substrings;
    stringstream ss(str);
    string substring;
    while(std::getline(ss, substring, delimiter)) {
        substrings.push_back(substring);
    }
    return substrings;
}
