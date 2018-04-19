package com.andreamapp.compiler.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.andreamapp.compiler.bean.BaseBean;
import com.andreamapp.compiler.bean.ProblemDescription;
import com.andreamapp.compiler.bean.Solution;
import com.andreamapp.compiler.bean.SolutionStatus;
import com.andreamapp.compiler.bean.UserProfile;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Andream on 2017/2/9.
 * Website: http://andream.com.cn
 * Email: me@andream.com.cn
 */

public class API {

    public static final String VJ_HOST = "https://cn.vjudge.net";

    private static OkHttpClient withCookie(Context context){
        CookieJar cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context));
        OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();
        return client;
    }

    private static OkHttpClient withSaveOnlyCookie(Context context){
        CookieJar cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context)){
            @Override
            public synchronized List<Cookie> loadForRequest(HttpUrl url) {
                return new ArrayList<Cookie>();
            }
        };
        OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();
        return client;
    }

    public static ProblemDescription getProblemDescription(String problemId) throws IOException {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("id", problemId)
                .build();
        Request request = new Request.Builder()
                .url("http://andreamapp.com/compiler/problem.php")
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        if (response.code() == 200) {
            return new Gson().fromJson(response.body().string(), ProblemDescription.class);
        }
        else {
            throw new IOException("Response with code " + response.code());
        }
    }

    public static String getProblemDescriptionHtml(String problemId) throws IOException {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("id", problemId)
                .build();
        Request request = new Request.Builder()
                .url("http://andreamapp.com/compiler/problem.php?id="+problemId)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        if (response.code() == 200) {
            return response.body().string();
        }
        else {
            throw new IOException("Response with code " + response.code());
        }
    }

    public static UserProfile getUserProfile(String username) throws IOException {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("username", username)
                .build();
        Request request = new Request.Builder()
                .url("http://andreamapp.com/compiler/profile.php")
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        if (response.code() == 200) {
            return new Gson().fromJson(response.body().string(), UserProfile.class);
        }
        else {
            throw new IOException("Response with code " + response.code());
        }
    }

    public static String[] getOJList() throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://andreamapp.com/compiler/ojlist.php")
                .build();
        Response response = client.newCall(request).execute();
        if (response.code() == 200) {
            return new Gson().fromJson(response.body().string(), String[].class);
        }
        else {
            throw new IOException("Response with code " + response.code());
        }
    }

    public static SolutionStatus getSolutionStatus(String solutionId, String cookie) throws IOException {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("id", solutionId)
                .add("cookie", cookie)
                .build();
        Request request = new Request.Builder()
                .url("http://andreamapp.com/compiler/solution.php")
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        if (response.code() == 200) {
            String s = response.body().string();
            Log.i("API", s);
            return new Gson().fromJson(s, SolutionStatus.class);
        }
        else {
            throw new IOException("Response with code " + response.code());
        }
    }

    public static Solution submit(Context context, String languageCode, String source, String share, String oj, String probNum, String cookie) throws IOException {
        OkHttpClient client = withCookie(context);
        RequestBody body = new FormBody.Builder()
                .add("languageCode", languageCode)
                .add("source", source)
                .add("share", share)
                .add("oj", oj)
                .add("probNum", probNum)
                .add("cookie", cookie)
                .build();
        Request request = new Request.Builder()
                .url("http://andreamapp.com/compiler/submit.php")
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        if (response.code() == 200) {
            return new Gson().fromJson(response.body().string(), Solution.class);
        }
        else {
            throw new IOException("Response with code " + response.code());
        }
    }

    public static boolean checkLoginStatus(Context context) throws IOException {
        OkHttpClient client = withCookie(context);
        RequestBody body = new FormBody.Builder()
                .build();
        Request request = new Request.Builder()
                .url(VJ_HOST+"/user/checkLogInStatus")
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        if (response.code() == 200) {
            return Boolean.valueOf(response.body().string());
        }
        else {
            throw new IOException("Response with code " + response.code());
        }
    }

    public static Boolean login(Context context, String username, String password) throws IOException {
        OkHttpClient client = withSaveOnlyCookie(context);
        client.newCall(
                new Request.Builder()
                        .url(VJ_HOST)
                        .get()
                        .build()
        ).execute(); // visit VJ_HOST to fetch SESSION cookie
        RequestBody body = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build();
        Request request = new Request.Builder()
                .url(VJ_HOST+"/user/login")
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        if (response.code() == 200) {
            return "success".equalsIgnoreCase(response.body().string());
        }
        else {
            throw new IOException("Response with code " + response.code());
        }
    }


    /**
     * Encodes the passed String as UTF-8 using an algorithm that's compatible
     * with JavaScript's <code>encodeURIComponent</code> function. Returns
     * <code>null</code> if the String is <code>null</code>.
     *
     * @param s The String to be encoded
     * @return the encoded String
     */
    public static String encodeURIComponent(String s)
    {
        String result = null;

        try
        {
            result = URLEncoder.encode(s, "UTF-8")
                    .replaceAll("\\+", "%20")
                    .replaceAll("\\%21", "!")
                    .replaceAll("\\%27", "'")
                    .replaceAll("\\%28", "(")
                    .replaceAll("\\%29", ")")
                    .replaceAll("\\%7E", "~");
        }

        // This exception should never occur.
        catch (UnsupportedEncodingException e)
        {
            result = s;
        }

        return result;
    }


    public static abstract class Callback<T extends BaseBean> extends AsyncTask<String, Void, T>{

        @Override
        protected T doInBackground(String... params) {
            try {
                return onRequest(params);
            }catch (IOException e){
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(T t) {
            if(t == null){
                onFailure("Network failed");
            }else if(!t.isStatus()){
                onFailure(t.getError());
            }else{
                onSuccess(t);
            }
        }

        public abstract T onRequest(String... params) throws  IOException;
        public abstract void onSuccess(T t);
        public abstract void onFailure(String error);
    }
}
