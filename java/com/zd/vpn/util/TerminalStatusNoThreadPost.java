package com.zd.vpn.util;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


/**
 * 状态校验接口
 * <p/>
 * Created by yf on 2014-12-26.
 */
public class TerminalStatusNoThreadPost {
    private String ip;
    private String port;
    private Context context;

    public TerminalStatusNoThreadPost(Context context, String ip, String port) {
        this.context = context;
        this.ip = ip;
        this.port = port;
    }

    public String postData() {
        String sn = SecuTFHelper.find_serial(context);
        String[][] params = new String[][]{
                {"serial", sn}
        };
        String pathUrl = "http://" + ip + ":" + port + "/DoTerminalStatus";
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams().setConnectionTimeout(1 * 1000 * 60);
        client.getHttpConnectionManager().getParams().setSoTimeout(1 * 1000 * 60);
        PostMethod post = new PostMethod(pathUrl);
        post.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 1 * 1000 * 60);
        post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        for (String[] param : params) {
            post.addParameter(param[0], param[1]);
        }
        int statusCode = 0;
        try {
            statusCode = client.executeMethod(post);        } catch (IOException e) {
            e.printStackTrace();
        }
        if (statusCode == 200) {
            //取出回应字串
            String data = null;
            try {
                data = post.getResponseBodyAsString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            boolean flag = false;
//                        String msg = "";
            String code = "";
            try {
                JSONObject result = new JSONObject(data);//转换为JSONObject
                flag = result.getBoolean("success");
//                            msg = result.getString("msg");
                code = result.getString("code");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (flag) {
                return code;
            } else {
                if (!"".equals(code) && code != null) {
                    return code;
                } else {
                   return ReturnCode.RETURN_CLIENT_STATUS_ERROR;
                }
            }
        } else {
            Log.v("vpn", "Error Response" + statusCode);
            return ReturnCode.RETURN_CLIENT_STATUS_ERROR;
        }
    }
}
