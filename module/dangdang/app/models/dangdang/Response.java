package models.dangdang;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import play.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 当当发送过来的响应包.
 * <p/>
 * User: sujie
 * Date: 9/13/12
 * Time: 2:27 PM
 */
public class Response implements Serializable {

    public ErrorCode errorCode;
    public String desc;
    public String spid;
    public String ver;
    public Element data;

    private Map<String, Object> attributes = new HashMap<>();


    public Response() {

    }

    /**
     * <resultObject>
     * <status_code>-1</status_code>
     * <error_code>1002</error_code>
     * <desc>
     * <![CDATA[ 验证失败 ]]>
     * </desc>
     * <spid>1</spid>
     * <ver>1.0</ver>
     * </resultObject>
     *
     * @param responseBodyAsString xml字符串
     */
    public Response(InputStream responseBodyAsString) throws DocumentException, IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(responseBodyAsString));
        StringBuilder responseStr = new StringBuilder();
        try {
            String line;
            while ((line = br.readLine()) != null) {
                responseStr.append(line);
            }
            Logger.info("\nResponse====" + responseStr.toString());
            Document document = DocumentHelper.parseText(responseStr.toString());
            Element root = document.getRootElement();
            ver = root.elementText("ver");
            spid = root.elementText("spid");
            Logger.info("\nerror_code====" + root.elementTextTrim("error_code"));
            errorCode = ErrorCode.getErrorCode(Integer.parseInt(root.elementTextTrim("error_code")));
            desc = root.elementText("desc");
            data = root.element("data");
            br.close();
        } catch (IOException e) {
            Logger.info("xml error");
        }

    }

    /**
     * 当当接口调用是否成功.
     *
     * @return
     */
    public boolean success() {
        return errorCode!= null && errorCode.equals(ErrorCode.SUCCESS);
    }

    public void addAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }
}