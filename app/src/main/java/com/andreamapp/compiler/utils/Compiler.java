package com.andreamapp.compiler.utils;

import android.os.AsyncTask;

import com.andreamapp.compiler.bean.CompileResult;
import com.andreamapp.compiler.bean.Language;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Andream on 2017/2/5.
 * Website: http://andream.com.cn
 * Email: me@andream.com.cn
 */

public class Compiler {
    public static final String API_HOST = "http://compiler.run";
    public static final String API_URL = API_HOST + "/api/run";
    public static final MediaType FORMED_URLENCODED = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
    private static final String FCMTOKEN = "fhJ9AwoUuF0:APA91bHEeyCjj_6_B54y70BceEj-T90f_J-z5XE02G8ugkciBj3P-Fq2AiGB2VEZklKe31-ql4szzgDQh_a_ijk0EsdjI_9RpxmlrXDyv_a2aJdj64TSvcii7-A_X7zvOoX31skyW2V6";


    public static CompileResult compile(String language, String code, String input) throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("language", language)
                .add("code", code)
                .add("input", input)
                .add("language_v", "default")
                .add("client", "android_compiler")
                .add("fcmtoken", FCMTOKEN)
                .build();
        Request request = new Request.Builder()
                .url(API_URL)
                //if I add this header, I won't accept Content-Encoding header in its response! Btw...I don't know why...
//                .addHeader("Accept-Encoding", "gzip")
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        if (response.code() == 200) {
            String result = response.body().string();
            JSONObject reJson = new JSONObject(result);
            CompileResult compileResult = new CompileResult(reJson.getString("output"), reJson.getString("error"));
            return compileResult;
        }
        else {
            throw new IOException("Response with code " + response.code());
        }
    }

    public static CompileResult compile(Language language, String code, String input) throws IOException, JSONException {
        return compile(language.getCode(), code, input);
    }

    public static final int ERROR_NETWORK = 100;
    public static final int ERROR_PARSE = 101;


    public static class Task extends AsyncTask<String, Void, CompileResult> {

        private OnCompileListener mOnCompileListener;
        private int errorCode;

        public Task(OnCompileListener onCompileListener) {
            mOnCompileListener = onCompileListener;
        }

        @Override
        protected void onPreExecute() {
            if (mOnCompileListener != null) {
                mOnCompileListener.onPrepare();
            }
        }

        @Override
        protected CompileResult doInBackground(String... params) {
            CompileResult result = null;
            try {
                result = compile(params[0], params[1], params[2]);
                errorCode = 0;
            }
            catch (IOException e) {
                e.printStackTrace();
                errorCode = ERROR_NETWORK;
            }
            catch (JSONException e) {
                e.printStackTrace();
                errorCode = ERROR_PARSE;
            }
            return result;
        }

        @Override
        protected void onPostExecute(CompileResult result) {
            if (result != null) {
                mOnCompileListener.onSuccess(result);
            }
            else {
                mOnCompileListener.onFailure(errorCode);
            }
        }
    }

    public interface OnCompileListener {

        void onPrepare();

        void onSuccess(CompileResult result);

        void onFailure(int code);
    }
}
