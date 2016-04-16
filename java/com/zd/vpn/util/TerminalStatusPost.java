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
public class TerminalStatusPost {
    private Handler handler;
    private String ip;
    private String port;
    private String sn;
    private Context context;

    public TerminalStatusPost(Context context, String ip, String port, String sn) {
        handler = new Handler(context.getMainLooper());
        this.context = context;
        this.ip = ip;
        this.port = port;
        this.sn = sn;
    }

    public interface OnThreeYardsPostListener {
        public void onThreeYardsPostOk(String msg);

        public void onThreeYardsPostErr(String msg);
    }

    public void postData( final OnThreeYardsPostListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
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
                        statusCode = client.executeMethod(post);
                    } catch (IOException e) {
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
//                        Log.v("vpn", data);
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
                            final String finalCode = code;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onThreeYardsPostOk(finalCode);
                                }
                            });
                        } else {
                            if (!"".equals(code) || code != null) {
                                final String finalData = code;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        listener.onThreeYardsPostErr(finalData);
                                    }
                                });
                            } else {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        String code = ReturnCode.RETURN_CLIENT_STATUS_ERROR;
                                        listener.onThreeYardsPostErr(code);
                                    }
                                });
                            }
                        }
                    } else {
                        Log.v("vpn", "Error Response" + statusCode);
//                        final int finalStatusCode = statusCode;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                String code = ReturnCode.RETURN_CLIENT_STATUS_ERROR;
                                listener.onThreeYardsPostErr(String.valueOf(code));
                            }
                        });
                    }
                }
        }).start();
    }

    /*public void postData(String imei,String sim,String sn,final OnThreeYardsPostListener listener){
        HttpPost httpRequest = new HttpPost("http://"+ip+":"+port+"/DoTerminalThreeYards");
        List<NameValuePair> params=new ArrayList<NameValuePair>();
//        params.add(new BasicNameValuePair("name","this is post"));
        try{
            params.add(new BasicNameValuePair("serial",sn));
            params.add(new BasicNameValuePair("terminalId",imei));
            params.add(new BasicNameValuePair("sim",sim));
            //发出HTTP request
            httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            //取得HTTP response
            HttpResponse httpResponse=new DefaultHttpClient().execute(httpRequest);
            //若状态码为200 ok
            if(httpResponse.getStatusLine().getStatusCode()==200){
                //取出回应字串
                String strResult= EntityUtils.toString(httpResponse.getEntity());
                Log.v("vpn", strResult);
               JSONObject jsonObj = net.sf.json.JSONObject.fromString(strResult);
                boolean success = jsonObj.getBoolean("success");
                if(success) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onThreeYardsPostOk();
                        }
                    });
                }else{
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onThreeYardsPostErr();
                        }
                    });
                }
            }else{
                Log.v("vpn", "Error Response"+httpResponse.getStatusLine().toString());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onThreeYardsPostErr();
                    }
                });
            }
        }catch(ClientProtocolException e){
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
}
