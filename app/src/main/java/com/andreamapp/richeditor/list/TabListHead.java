package com.andreamapp.richeditor.list;

/**
 * Created by Andream on 2016/3/13.
 * Website: http://andream.com.cn
 * Email: me@andream.com.cn
 */
public class TabListHead extends ListHead{
    @Override
    public int getHeadType() {
        return TYPE_TAB;
    }

    @Override
    public String getHeadRegex() {
        return "^\\s+";
    }

    @Override
    public String getStandardHead() {
        return "    ";
    }
}
