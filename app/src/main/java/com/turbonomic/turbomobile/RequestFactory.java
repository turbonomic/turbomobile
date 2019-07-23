package com.turbonomic.turbomobile;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class RequestFactory {
    private RequestFactory(){}

    public static Request getInstance(String ip, String api,String body, String method) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), body);
        Request request = new Request.Builder()
                .url("https://"+ip+"/api/"+api)
                .method(method,requestBody).build();
        return request;
    }

    public static Request getInstance(String ip, String api,String body, String method,String cookie) {
        RequestBody requestBody = (method.equals("GET")) ? null :RequestBody.create(MediaType.parse("application/json"), body);
        Request request = new Request.Builder()
                .url("https://"+ip+"/api/"+api)
                .addHeader("Cookie",cookie)
                .method(method,requestBody).build();
        return request;
    }

}
