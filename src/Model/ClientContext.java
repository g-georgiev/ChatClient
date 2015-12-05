package Model;

import ChatView.Controller;

import java.util.HashMap;

/**
 * Created by Georgi on 15.11.2015 г..
 */
public class ClientContext {
    private static HashMap<Integer, String> onlineClients = new HashMap<>();
    private static HashMap<String, Controller> activeChats = new HashMap<>();
    private static Integer currentId;

    public static synchronized String getClientName(Integer clientID) {
        return onlineClients.get(clientID);
    }

    public static synchronized void setClientName(Integer clientID, String name) {
        onlineClients.put(clientID, name);
    }

    public static String[] getAllClientNames() {
        return onlineClients.values().toArray(new String[onlineClients.size()]);
    }

    public static Integer[] getAllClientIds() {
        return onlineClients.keySet().toArray(new Integer[onlineClients.size()]);
    }

    public static boolean hasClient(Integer clientID) {
        return onlineClients.containsKey(clientID);
    }

    public static void removeClient(String clientId) {
        onlineClients.remove(Integer.parseInt(clientId));
    }

    public static Integer getCurrentId() {
        return currentId;
    }

    public static void setCurrentId(Integer _currentId) {
        currentId = _currentId;
    }

    public static Controller getActiveChatController(String chatID) {
        return activeChats.get(chatID);
    }

    public static synchronized void setActiveChatController(String chatID, Controller controller) {
        activeChats.put(chatID, controller);
    }

    public static void removeController(String chatID) {
        activeChats.remove(chatID);
    }
}
