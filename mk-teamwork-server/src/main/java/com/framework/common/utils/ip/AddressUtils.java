package com.framework.common.utils.ip;

import com.alibaba.fastjson.JSONObject;
import com.framework.common.utils.StringUtils;
import com.framework.common.utils.http.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * get address class
 * 
 */
public class AddressUtils
{
    private static final Logger log = LoggerFactory.getLogger(AddressUtils.class);

    public static final String IP_URL = "http://ip.taobao.com/service/getIpInfo.php";

    public static String getRealAddressByIP(String ip)
    {
        String address = "XX XX";
        // Intranet does not query
        if (IpUtils.internalIp(ip))
        {
            return "IntranetIP";
        }
        /*if (RuoYiConfig.isAddressEnabled())
        {
            String rspStr = HttpUtils.sendPost(IP_URL, "ip=" + ip);
            if (StringUtils.isEmpty(rspStr))
            {
                log.error("Get geographic location exception {}", ip);
                return address;
            }
            try {
                JSONObject obj = JSONObject.parseObject(rspStr);
                JSONObject data = obj.getObject("data", JSONObject.class);
                String region = data.getString("region");
                String city = data.getString("city");
                address = region + " " + city;
            }catch(NullPointerException e){
                e.printStackTrace();
            }
        }*/
        return address;
    }
}
