package com.photonapi;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.eclipse.jetty.websocket.jsr356.JettyClientContainerProvider;

import javax.websocket.DeploymentException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClientApi {

    private BasicCookieStore cookieStore = new BasicCookieStore();
    private CloseableHttpClient client;

    private String host;
    private final String HTTP = "http://";
    private final String WS = "ws://";
    private final String ID = "{id}";

    private EndPoints endPoints;

    ClientApi(String host) {
        client = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
        this.host = host;
        endPoints = new EndPoints();
    }

    public CloseableHttpResponse login(String username, String password) throws IOException {
        HttpPost authPost = new HttpPost(endPoints.login);
        List<NameValuePair> usernameAndPassword = new ArrayList<>();
        usernameAndPassword.add(new BasicNameValuePair("username", username));
        usernameAndPassword.add(new BasicNameValuePair("password", password));
        authPost.setEntity(new UrlEncodedFormEntity(usernameAndPassword));
        return client.execute(authPost);
    }

    public CloseableHttpResponse sendDocument(String filePath) throws IOException, URISyntaxException {
        InputStream in = null;
        File file = null;
        if (filePath == null) {
            System.out.println(getClass().getClassLoader().getResource("testImg.jpg").toURI().toString());
             in = getClass().getClassLoader().getResource("testImg.jpg").openStream();
        } else {
            file = new File(filePath);
            in = FileUtils.openInputStream(file);
        }
        HttpEntity imgEntity = MultipartEntityBuilder.create()
                .addBinaryBody("file", in, ContentType.create("image/jpeg"),
                        filePath == null ? "testImg.jpg" : file.getName())
                .build();

        HttpPost imgPost = new HttpPost(endPoints.document);
        imgPost.setEntity(imgEntity);
        return client.execute(imgPost);
    }

    public boolean startProcess(String docId)
            throws URISyntaxException, IOException, DeploymentException, InterruptedException {
        WebsocketClientEndpoint clientEndpoint = new WebsocketClientEndpoint(endPoints.wsDocument, getCookies());
        System.out.println("Start process");
        clientEndpoint.sendMessage(docId);
        clientEndpoint.latch.await();
        try {
            JettyClientContainerProvider.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public CloseableHttpResponse getMeta(String docId) throws IOException {
        HttpGet httpGetMeta = new HttpGet(endPoints.meta.replace(ID, docId));
        return client.execute(httpGetMeta);
    }

    public CloseableHttpResponse getDCT(String docId) throws IOException {
        HttpGet httpGetDct = new HttpGet(endPoints.dct.replace(ID, docId));
        return client.execute(httpGetDct);
    }

    public CloseableHttpResponse getELA(String docId) throws IOException {
        HttpGet httpGetEla = new HttpGet(endPoints.ela.replace(ID, docId));
        return client.execute(httpGetEla);
    }

    public CloseableHttpResponse getNoiseMap(String docId) throws IOException {
        HttpGet httpGetNoiseMap = new HttpGet(endPoints.noiseMap.replace(ID, docId));
        return client.execute(httpGetNoiseMap);
    }

    public CloseableHttpResponse getClones(String docId) throws IOException {
        HttpGet httpGetClones = new HttpGet(endPoints.clones.replace(ID, docId));
        return client.execute(httpGetClones);
    }

    public CloseableHttpResponse getAutoVerdict(String docId) throws IOException {
        HttpGet httpGetAutoVerdict = new HttpGet(endPoints.autoVerdict.replace(ID, docId));
        return client.execute(httpGetAutoVerdict);
    }

    public CloseableHttpResponse getPdfReport(String docId) throws IOException {
        HttpGet httpGetPdfReport = new HttpGet(endPoints.pdfReport.replace(ID, docId));
        return client.execute(httpGetPdfReport);
    }

    private Map<String, String> getCookies() {
        return cookieStore.getCookies().stream()
                .collect(Collectors.toMap(Cookie::getName, Cookie::getValue));
    }

    private class EndPoints {

        String login = HTTP + host + "/login";
        String document = HTTP + host + "/api/rest/document";
        String meta = HTTP + host + "/api/rest/document/{id}/meta";
        String dct = HTTP + host + "/api/rest/document?id={id}&type=dct";
        String ela = HTTP + host + "/api/rest/document?id={id}&type=ela";
        String noiseMap = HTTP + host + "/api/rest/document?id={id}&type=noiseMap";
        String clones = HTTP + host + "/api/rest/document?id={id}&type=clones";
        String autoVerdict = HTTP + host + "/api/rest/document/{id}/autoverdict";
        String pdfReport = HTTP + host + "/api/rest/report/{id}?localization=en";
        String wsDocument = WS + host + "/document";
        String wsEchTest = WS + host + "/echoTest";
    }
}
