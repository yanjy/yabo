package models.accounts.util;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * User: likang
 */
public class SerialNumberUtil {
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmssSSS");
    private static DecimalFormat decimalFormat = new DecimalFormat("00000");
    
    public static String generateSerialNumber(){
        return generateSerialNumber(new Date());
    }
    
    public static String generateSerialNumber(Date date){
        int random = new Random().nextInt(100000);
        return dateFormat.format(date) + decimalFormat.format(random);
    }
}
