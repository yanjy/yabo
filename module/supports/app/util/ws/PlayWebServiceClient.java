package util.ws;

import java.util.Map;

import models.journal.WebServiceCallLog;
import play.libs.WS;
import play.libs.WS.HttpResponse;

public class PlayWebServiceClient extends WebServiceClient {

    private static PlayWebServiceClient _instance;
    
    public static PlayWebServiceClient getInstance() {
        if (_instance == null) {
            _instance = new PlayWebServiceClient();
        }
        return _instance;
    }
    
    private PlayWebServiceClient() {}
    
    @Override
    public HttpResponse doGet(WebServiceCallLog log, WebServiceCallback callback) {
        play.libs.WS.HttpResponse response = WS.url(log.url).get();
        log.statusCode = response.getStatus();
        log.responseText = response.getString();
        if (callback != null) {
            callback.process(response.getStatus(), response.getString());
        }
        return response;
    }

    public void doPost(WebServiceCallLog log, WebServiceCallback callback) {
        
        WS.url("").post();
    }

    @Override
    protected HttpResponse doPost(WebServiceCallLog log,
                    Map<String, Object> params, WebServiceCallback callback) {
        play.libs.WS.HttpResponse response = WS.url(log.url).params(params).post();
        log.statusCode = response.getStatus();
        log.responseText = response.getString();
        callback.process(response.getStatus(), response.getString());
        return response;
    }
}
