package com.joywe.locationlibrary;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.joywe.locationlibrary.ip.IP;
import com.joywe.locationlibrary.ip.SoHuIP;
import com.joywe.locationlibrary.ip.TaoBaoIP;
import com.joywe.locationlibrary.ip.XinLangIP;
import com.smart.common.data.DataFormat;
import com.smart.common.http.HttpManger;
import com.smart.common.util.DebugUtil;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IpToCityManger {
    private static final String TAG = "IpToCityManger";
    private static final String XIN_LANG_IP_URL = "http://int.dpool.sina.com.cn/iplookup/iplookup.php?format=json";
    private static final String TAO_BAO_IP_URL = "http://ip.taobao.com/service/getIpInfo2.php?ip=myip";
    private static final String SO_HU_IP_URL = "http://pv.sohu.com/cityjson?ie=utf-8";
    private static final String IP_URL = "http://ip.chinaz.com/getip.aspx";
    private static final String PCON_LINE_IP_URL = "http://whois.pconline.com.cn/ipJson.jsp";
    private static final String IP_126_URL = "http://ip.ws.126.net/ipquery";

    private static class Holder {
        static final IpToCityManger INSTANCE = new IpToCityManger();
    }

    public static IpToCityManger getInstance() {
        return Holder.INSTANCE;
    }

    public static String getIp126Url() throws Exception {
        return HttpManger.getInstance().getSyncString(IP_126_URL);
    }

    public static String getPconLineIpUrl() throws Exception {
        return HttpManger.getInstance().getSyncString(PCON_LINE_IP_URL);
    }

    /**
     * 获取城市名
     *
     * @return 成功返回城市名 失败返回null
     * @throws IOException IOException
     */
    public String getCityName() throws Exception {
        String name = getCityNameBySoHuUrl();
        if (name == null || TextUtils.isEmpty(name)) {
            name = getCityNameByTaoBaoUrl();
        }
        if (name == null || TextUtils.isEmpty(name)) {
            name = getCityNameByIpUrl();
        }
        if (name == null || TextUtils.isEmpty(name)) {
            name = getCityNameByXinLangUrl();
        }
        return name;
    }

    /**
     * 通过新浪接口获取城市名
     *
     * @return 成功返回城市名 失败返回null
     * @throws IOException IOException
     */
    public String getCityNameByXinLangUrl() throws IOException {
        try {
            String response = HttpManger.getInstance().getSyncString(XIN_LANG_IP_URL);
            if (response != null) {
                XinLangIP xinLangIP = new Gson().fromJson(response, XinLangIP.class);
                if (xinLangIP != null && xinLangIP.getCity() != null) {
                    return xinLangIP.getCity();
                }
            }
            return response;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 通过taobao接口获取城市名
     *
     * @return 成功返回城市名 失败返回null
     */
    public String getCityNameByTaoBaoUrl() {
        try {

            String response = HttpManger.getInstance().getSyncString(TAO_BAO_IP_URL);
            if (response != null) {
                Log.d(TAG, "getCityNameByTaoBaoUrl: " + response);
                TaoBaoIP taoBaoIP = new Gson().fromJson(response, TaoBaoIP.class);
                if (taoBaoIP != null && taoBaoIP.getCode() == 0) {
                    TaoBaoIP.DataBean data = taoBaoIP.getData();
                    String city = data.getCity();
                    String area = data.getArea();
                    String region = data.getRegion();
                    if (!TextUtils.isEmpty(city)) {
                        return city;
                    }
                    if (!TextUtils.isEmpty(region)) {
                        return region;
                    }
                    if (!TextUtils.isEmpty(area)) {
                        return area;
                    }
                }
            }
            return response;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 通过搜狐接口获取城市名
     *
     * @return 成功返回城市名 失败返回null
     */
    public String getCityNameBySoHuUrl() {
        try {
            String response = HttpManger.getInstance().getSyncString(SO_HU_IP_URL);
            if (response != null) {
                DebugUtil.d(response);
                String substring = response.substring(response.indexOf("{"), response.indexOf("}") + 1);
                SoHuIP soHuIP = new Gson().fromJson(substring, SoHuIP.class);
                if (soHuIP != null && soHuIP.getCname() != null) {
                    return getCityName(soHuIP.getCname());
                }
            }
            return response;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 通过IP接口获取城市名
     *
     * @return 成功返回城市名 失败返回null
     */
    public String getCityNameByIpUrl() {
        try {
            String response = HttpManger.getInstance().getSyncString(IP_URL);
            DebugUtil.d(response);
            if (response != null) {
                IP ip = new Gson().fromJson(response, IP.class);
                if (ip != null && ip.getAddress() != null) {
                    return getCityName(ip.getAddress());
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 截取城市名字
     *
     * @param address 需要被截取的str
     * @return 截取后的城市名字
     */
    public static String getCityName(String address) {
        String cityName = getCityNameByAddress(address);
        DebugUtil.d("cityName = " + cityName);
        if (DataFormat.isEmpty(cityName)) {
            cityName = getCityNameStr(address);
        }
        return cityName;
    }

    /**
     * 截取城市名字
     *
     * @param address 需要被截取的str
     * @return 截取后的城市名字
     */
    public static String getCityNameStr(String address) {
        String[] split = address.split(" ");
        String[] strings = split[0].split("省");
        if (strings.length >= 2) {
            String[] strings1 = strings[1].split("市");
            if (strings1.length >= 2) {
                String[] strings2 = strings1[1].split("区");
                if (strings2.length >= 2) {
                    String[] strings3 = strings2[1].split("县");
                    if (strings3.length >= 2) {
                        return strings3[1];
                    } else {
                        return strings3[0];
                    }
                } else {
                    return strings2[0];
                }
            } else {
                return strings1[0];
            }
        } else {
            return strings[0];
        }
    }

    /**
     * 截取城市名字
     *
     * @param address 需要被截取的str
     * @return 截取后的城市名字
     */
    public static String getCityNameByAddress(String address) {
        String regex = "((?<province>[^省]+省|.+自治区)|上海|北京|天津|重庆)(?<city>[^市]+市|.+自治州)(?<county>[^县]+县|.+区|.+镇|.+局)";
        Matcher m = Pattern.compile(regex).matcher(address);
        String province = null, city = null, county = null;
        while (m.find()) {
            province = m.group(2);
            city = m.group(3);
            county = m.group(4);
            if (county != null) {
                String[] split = county.split(" ");
                county = split[0];
            }
        }
        if (county != null) {
            return county;
        }
        if (city != null) {
            return city;
        }
        if (province != null) {
            return province;
        }
        return null;
    }

}