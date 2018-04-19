package com.andreamapp.compiler.bean;

/**
 * Created by Andream on 2017/2/9.
 * Website: http://andream.com.cn
 * Email: me@andream.com.cn
 */

public class ProblemDescription extends BaseBean{
    private String title, timeLimit, memoryLimit, descriptionUrl;

    public String getTitle() {
        return title;
    }

    public String getTimeLimit() {
        return timeLimit;
    }

    public String getMemoryLimit() {
        return memoryLimit;
    }

    public String getDescriptionUrl() {
        return descriptionUrl;
    }
}
