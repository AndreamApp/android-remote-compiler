package com.andreamapp.compiler.bean;

/**
 * Created by Andream on 2017/2/9.
 * Website: http://andream.com.cn
 * Email: me@andream.com.cn
 */

public class UserProfile extends BaseBean{
    private String username, nickname, overallSolved, overallAttempted;

    public String getUsername() {
        return username;
    }

    public String getNickname() {
        return nickname;
    }

    public String getOverallSolved() {
        return overallSolved;
    }

    public String getOverallAttempted() {
        return overallAttempted;
    }
}
