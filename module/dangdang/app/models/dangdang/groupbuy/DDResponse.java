package models.dangdang.groupbuy;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import play.libs.XPath;

import java.util.List;

/**
 * @author likang
 *         Date: 13-1-22
 */
public class DDResponse {
    public String ver;
    public String spid;
    public String errorCode;
    public String desc;
    public Node data;

    public static DDResponse parseResponse(Document document) {
        DDResponse response = new DDResponse();
        response.ver = XPath.selectText("/resultObject/ver", document).trim();
        response.spid = XPath.selectText("/resultObject/spid", document).trim();
        response.errorCode = XPath.selectText("/resultObject/error_code", document).trim();
        response.desc = XPath.selectText("/resultObject/desc", document).trim();
        response.data = XPath.selectNode("/resultObject/data", document);
        return response;
    }

    public boolean isOk() {
        return errorCode != null && "0".equals(errorCode);
    }

    public Node selectNode(String path) {
        return XPath.selectNode(path, data);
    }

    public List<Node> selectNodes(String path) {
        return XPath.selectNodes(path, data);
    }

    public String selectText(String path) {
        return XPath.selectText(path, data);
    }

    public String selectTextTrim(String path) {
        return XPath.selectText(path, data).trim();
    }

    @Override
    public String toString() {
        return "errorCode: " + errorCode + "\ndesc: " + desc
                + "\ndata:\n" + ((data == null) ? "null" : data.toString());
    }
}
