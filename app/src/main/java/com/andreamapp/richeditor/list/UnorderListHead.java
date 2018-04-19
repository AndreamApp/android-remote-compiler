package com.andreamapp.richeditor.list;

/**
 * Created by Andream on 2016/3/13.
 * Website: http://andream.com.cn
 * Email: me@andream.com.cn
 */
public class UnorderListHead extends ListHead {
    @Override
    public int getHeadType() {
        return TYPE_UNORDER;
    }

    @Override
    public String getHeadRegex() {
        return "^\\s+- ";
    }

    @Override
    public String getStandardHead() {
        return " - ";
    }
}
