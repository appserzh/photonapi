package com.photonapi;

import javax.websocket.ClientEndpoint;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@ClientEndpoint
public class WebsocketClientEndpoint extends Endpoint {

    public final CountDownLatch latch = new CountDownLatch(1);
    private Session session;

    WebsocketClientEndpoint(String uri, Map<String, String> cookies)
            throws IOException, DeploymentException, InterruptedException, URISyntaxException {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        if (cookies != null) {
            ClientEndpointConfig cec = ClientEndpointConfig.Builder.create()
                    .configurator(new ClientEndpointConfig.Configurator() {

                        @Override
                        public void beforeRequest(Map<String, List<String>> headers) {
                            super.beforeRequest(headers);
                            List<String> cookieList = headers.get("Cookie");
                            if (null == cookieList) {
                                cookieList = new ArrayList<>();
                            }
                            for (String s : cookies.keySet()) {
                                cookieList.add(s + "=" + cookies.get(s));
                            }
                            headers.put("Cookie", cookieList);
                        }
                    }).build();
            session = container.connectToServer(this, cec, new URI(uri));
        } else {
            session = container.connectToServer(this, new URI(uri));
        }
    }

    @OnOpen
    @Override
    public void onOpen(Session session, EndpointConfig config) {
        List<String> prMet = Arrays.asList("meta", "dct", "ela", "noiseMap", "clones");
        List<String> processedMet = new ArrayList<>();
        session.addMessageHandler(new MessageHandler.Whole<String>() {

            @Override
            public void onMessage(String message) {
                processedMet.add(message);
                System.out.println(message + " done");
                if (processedMet.containsAll(prMet)) {
                    try {
                        close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @OnClose
    @Override
    public void onClose(Session session, CloseReason reason) {
        this.session = null;
        latch.countDown();
    }

    public void sendMessage(String message) throws IOException {
        this.session.getAsyncRemote().sendText(message);
    }

    public void close() throws IOException {
        session.close();
    }

    @Override
    public void onError(Session session, Throwable thr) {
        thr.printStackTrace();
    }
}
