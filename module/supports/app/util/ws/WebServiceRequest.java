package util.ws;

import com.google.gson.JsonElement;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import play.libs.WS;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 重构WebServiceClient用法.
 *
 * WebServiceClientResquest.with(callType).url(url).params(map).postXml();
 * User: tanglq
 * Date: 13-1-23
 * Time: 下午6:41
 */
public class WebServiceRequest {
    public String callType;
    public String callMethod;
    public String encoding;
    public String url;
    public String requestBody;
    public Map<String, Object> params;
    public Map<String, String> headers;
    public String keyword1;
    public String keyword2;
    public String keyword3;

    public List<File> uploadFiles;

    public WebServiceCallback callback;

    private WebServiceRequest() {

    }

    public static WebServiceRequest url(String url) {
        WebServiceRequest response = new WebServiceRequest();
        response.callType = null;
        response.url = url;
        return response;
    }

    public WebServiceRequest type(String callType) {
        this.callType = callType;
        return this;
    }

    public WebServiceRequest requestBody(String value) {
        this.requestBody = value;
        return this;
    }

    public WebServiceRequest params(Map<String, Object> map) {
        this.params = map;
        return this;
    }

    public WebServiceRequest addHeader(String key, String value) {
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put(key, value);
        return this;
    }

    public WebServiceRequest encoding(String value) {
        this.encoding = value;
        return this;
    }

    public WebServiceRequest uploadFiles(List<File> value) {
        this.uploadFiles = value;
        return this;
    }

    public WebServiceRequest addFile(File value) {
        if (this.uploadFiles == null) {
            this.uploadFiles = new ArrayList<>();
        }
        this.uploadFiles.add(value);
        return this;
    }

    public WebServiceRequest addKeyword(Object value) {
        if (value == null) return this;
        if (StringUtils.isBlank(this.keyword1)) {
            this.keyword1 = value.toString();
        } else if (StringUtils.isBlank(this.keyword2)) {
            this.keyword2 = value.toString();
        } else {
            this.keyword3 = value.toString();
        }
        return this;
    }

    public WebServiceRequest callback(WebServiceCallback _callback) {
        this.callback = _callback;
        return this;
    }

    public String getString() {
        WS.HttpResponse response = processHttpResponse(WebServiceClient.HttpMethod.GET);
        if (hasEncoding()) {
            return response.getString(this.encoding);
        }
        return response.getString();
    }

    public String postString() {
        WS.HttpResponse response = processHttpResponse(WebServiceClient.HttpMethod.POST);
        if (hasEncoding()) {
            return response.getString(this.encoding);
        }
        return response.getString();
    }
    public String putString() {
        WS.HttpResponse response = processHttpResponse(WebServiceClient.HttpMethod.PUT);
        if (hasEncoding()) {
            return response.getString(this.encoding);
        }
        return response.getString();
    }
    public String deleteString() {
        WS.HttpResponse response = processHttpResponse(WebServiceClient.HttpMethod.DELETE);
        if (hasEncoding()) {
            return response.getString(this.encoding);
        }
        return response.getString();
    }

    public Document getXml() {
        WS.HttpResponse response = processHttpResponse(WebServiceClient.HttpMethod.GET);
        if (hasEncoding()) {
            return response.getXml(this.encoding);
        }
        return response.getXml();
    }

    public Document postXml() {
        WS.HttpResponse response = processHttpResponse(WebServiceClient.HttpMethod.POST);
        if (hasEncoding()) {
            return response.getXml(this.encoding);
        }
        return response.getXml();
    }

    private boolean hasEncoding() {
        return StringUtils.isNotBlank(this.encoding);
    }

    public JsonElement getJson() {
        return processHttpResponse(WebServiceClient.HttpMethod.GET).getJson();
    }

    public JsonElement postJson() {
        return processHttpResponse(WebServiceClient.HttpMethod.POST).getJson();
    }
    public JsonElement putJson() {
        return processHttpResponse(WebServiceClient.HttpMethod.PUT).getJson();
    }
    public JsonElement deleteJson() {
        return processHttpResponse(WebServiceClient.HttpMethod.DELETE).getJson();
    }

    private WS.HttpResponse processHttpResponse(WebServiceClient.HttpMethod httpMethod) {
        WebServiceClient client = WebServiceClientFactory.getClientHelper();
        if (hasEncoding()) {
            client = WebServiceClientFactory.getClientHelper(this.encoding);
        }
        return client.processHttpResponse(this, httpMethod);
    }

}
