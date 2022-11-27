package ru.kalinina.model;

import com.neovisionaries.ws.client.WebSocket;

public class SocketConnector {
    private Boolean isConnect;
    private WebSocket webSocket = null;

    public SocketConnector(Boolean isConnect) {
        this.isConnect = isConnect;
    }

    public Boolean getIsConnect() {
        return isConnect;
    }


    public void setIsConnect(Boolean isConnect){
        this.isConnect = isConnect;
    }

    public WebSocket getWebSocket() {
        return webSocket;
    }

    public void setWebSocket(WebSocket webSocket) {
        this.webSocket = webSocket;
    }
}
