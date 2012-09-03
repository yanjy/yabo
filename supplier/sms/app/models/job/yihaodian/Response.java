package models.job.yihaodian;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * @author likang
 *         Date: 12-8-30
 */
public class Response<V> {
    private List<V> vs;
    private int totalCount;

    private int errorCount;
    private List<ErrorInfo> errors;

    public Response(){
        totalCount = 0;
        errorCount = 0;
        errors = new ArrayList<>();
        vs = new ArrayList<>();
    }

    /**
     * 解析一号店返回的XML
     * @param xml   一号店的返回内容
     * @throws DocumentException
     */
    public void parseXml(String xml, String contentNodeName, boolean isList, Parser<V> parser) throws DocumentException {
        Document document = null;
        document = DocumentHelper.parseText(xml);
        Element root = document.getRootElement();

        //解析 errorCount
        errorCount = parseIntValue(root, "errorCount");
        //解析 totalCount
        totalCount = parseIntValue(root, "totalCount");
        //解析错误信息
        parseErrors(root);
        //解析内容
        parseContent(root, contentNodeName, isList, parser);
    }

    private void parseContent(Element root, String contentNodeName, boolean isList, Parser<V> parser) {
        Element node = root.element(contentNodeName);
        if(node == null){
            return;
        }
        //解析对象
        if(isList) {
            for(Object o : node.elements()){
                parseOneContent((Element)o, parser);
            }
        }else {
            parseOneContent(node, parser);
        }
    }

    private void parseOneContent(Element e, Parser<V> parser){
        V v = parser.parse(e);
        if(v != null){
            vs.add(v);
        }
    }

    private void parseErrors(Element root) {
        Element errorInfoElement = root.element("errInfoList");
        for (Object o : errorInfoElement.elements()) {
            Element e = (Element) o;
            ErrorInfo errorInfo = new ErrorInfo();
            errorInfo.errorCode = e.elementText("errorCode");
            errorInfo.errorDes = e.elementText("errorDes");
            errorInfo.pkInfo = e.elementText("pkInfo");
            errors.add(errorInfo);
        }
    }

    private int parseIntValue(Element element, String nodeName){
        String nodeValue = element.elementText(nodeName);
        if(nodeValue == null) {
            return 0;
        }else {
            return Integer.parseInt(nodeValue);
        }
    }

    public List<V> getVs() {
        return vs;
    }

    public void setVs(List<V> vs) {
        this.vs = vs;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public List<ErrorInfo> getErrors() {
        return errors;
    }

    public void setErrors(List<ErrorInfo> errors) {
        this.errors = errors;
    }


}
