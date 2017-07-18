package com.photonapi;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;

import javax.websocket.DeploymentException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public final class PhotonApi {

    private PhotonApi() {}

    public static void main(String[] args)
            throws URISyntaxException, IOException, DeploymentException, InterruptedException, ParseException {

        Cli cli = new Cli(args);

        ClientApi clientApi = new ClientApi(cli.getHost());

        CloseableHttpResponse authResponse = clientApi.login(cli.getLogin(), cli.getPassword());
        System.out.println("login: " + authResponse.getStatusLine());

        CloseableHttpResponse docSendResponse = clientApi.sendDocument(cli.getFile());
        System.out.println("Send doc: " + docSendResponse.getStatusLine());

        String docId = IOUtils.toString(docSendResponse.getEntity().getContent(), "UTF-8");
        System.out.println("doc Id: " + docId);

        clientApi.startProcess(docId);

        CloseableHttpResponse metaResponse = clientApi.getMeta(docId);
        System.out.println("Meta info: \n" + IOUtils.toString(metaResponse.getEntity().getContent(), "UTF-8"));

        CloseableHttpResponse dctResponse = clientApi.getDCT(docId);
        System.out.println("DCT: " + dctResponse.getStatusLine());
        toFile(dctResponse, "dct.jpg");

        CloseableHttpResponse elaResponse = clientApi.getELA(docId);
        System.out.println("ELA: " + elaResponse.getStatusLine());
        toFile(elaResponse, "ela.jpg");

        CloseableHttpResponse noiseMapResponse = clientApi.getNoiseMap(docId);
        System.out.println("Noise Map: " + noiseMapResponse.getStatusLine());
        toFile(noiseMapResponse, "noiseMap.jpg");

        CloseableHttpResponse clonesResponse = clientApi.getClones(docId);
        System.out.println("Clones: " + clonesResponse.getStatusLine());
        toFile(clonesResponse, "clones.jpg");

        CloseableHttpResponse autoVerdictResponse = clientApi.getAutoVerdict(docId);
        System.out.println("Auto verdict: " + IOUtils.toString(autoVerdictResponse.getEntity().getContent(), "UTF-8"));

        CloseableHttpResponse pdfResponse = clientApi.getPdfReport(docId);
        System.out.println("Pdf report: " + pdfResponse.getStatusLine());
        toFile(pdfResponse, pdfResponse
                .getLastHeader("Content-Disposition")
                .getValue()
                .replace("attachment; filename=", "")
                .replace("\"", ""));
    }

    private static void toFile(CloseableHttpResponse is, String fileName) throws IOException {
        FileUtils.writeByteArrayToFile(new File("result", fileName),
                IOUtils.toByteArray(is.getEntity().getContent()));
    }
}
