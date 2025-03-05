package dataaccess;

import model.UserData;
import model.AuthData;
import model.GameData;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;

public class MemoryDatabase {
    // For thread safety, use ConcurrentHashMap (or sync blocks).
    static final Map<String, UserData> USERS = new ConcurrentHashMap<>();
    static final Map<String, AuthData> AUTHS = new ConcurrentHashMap<>();
    static final Map<Integer, GameData> GAMES = new ConcurrentHashMap<>();

    // Clears everything
    public static void clearAll() {
        USERS.clear();
        AUTHS.clear();
        GAMES.clear();
    }
}
