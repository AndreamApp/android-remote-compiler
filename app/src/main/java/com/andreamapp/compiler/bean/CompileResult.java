package com.andreamapp.compiler.bean;

/**
 * Created by Andream on 2017/2/5.
 * Website: http://andream.com.cn
 * Email: me@andream.com.cn
 */

public class CompileResult {
    private String output, error;

    public CompileResult(String output, String error) {
        this.output = output;
        this.error = error;
    }

    public String getOutput() {
        return output;
    }

    public String getError() {
        return error;
    }
}
