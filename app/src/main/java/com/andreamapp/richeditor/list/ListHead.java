package com.andreamapp.richeditor.list;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Andream on 2016/3/13.
 * Website: http://andream.com.cn
 * Email: me@andream.com.cn
 */
public abstract class ListHead {
    public static final int TYPE_TAB = 0;
    public static final int TYPE_UNORDER = 1;
    public static final int TYPE_ORDERED = 2;

    private String line;
    private String head;

    public String getLine() {
        return line;
    }

    /*
    * SetLine 会重置head
    * */
    public void setLine(String line) {
        this.line = line;
        setHead(null);
    }

    public String getHead() {
        String mHead = head;
        if (head == null) {
            Pattern p = Pattern.compile(getHeadRegex());
            Matcher m = p.matcher(getLine());
            if (m.find()) {
                mHead = m.group();
            }
            setHead(mHead);
        }
        return mHead;
    }

    protected void setHead(String head) {
        this.head = head;
    }

    public abstract int getHeadType();

    public abstract String getHeadRegex();

    public abstract String getStandardHead();

    /*
    * 默认返回和当前行一样的head
    * 如果需要，自类可以复写
    * */
    public String nextHead() {
        return getHead();
    }
}
