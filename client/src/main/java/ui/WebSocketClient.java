package ui;

import com.google.gson.Gson;
import org.glassfish.tyrus.client.ClientManager;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.net.URI;

@ClientEndpoint
public class WebSocketClient {
    private final Gson gson = new Gson();
    private ChessClient client;
    private Session session;

    public WebSocketClient(String serverUrl, ChessClient client) throws Exception {
        this.client = client;
        ClientManager clientManager = ClientManager.createClient();
        URI uri = new URI(serverUrl + "/ws");
        session = clientManager.connectToServer(this, uri);
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("Connected to WebSocket server");
    }

    @OnMessage
    public void onMessage(String message) {
        ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
        client.handleServerMessage(serverMessage);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("WebSocket connection closed: " + reason);
        this.session = null;
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("WebSocket error: " + throwable.getMessage());
    }

    public void send(UserGameCommand command) {
        if (session == null || !session.isOpen()) {
            System.out.println("WebSocket not connected");
            return;
        }
        try {
            String json = gson.toJson(command);
            session.getBasicRemote().sendText(json);
        } catch (Exception e) {
            System.out.println("Failed to send WebSocket message: " + e.getMessage());
        }
    }

    public void close() {
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (Exception e) {
                System.out.println("Failed to close WebSocket: " + e.getMessage());
            }
        }
    }
}