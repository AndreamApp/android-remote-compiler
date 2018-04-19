package com.andreamapp.richeditor.list;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Andream on 2016/3/13.
 * Website: http://andream.com.cn
 * Email: me@andream.com.cn
 */
public class OrderedListHead extends ListHead {
    @Override
    public int getHeadType() {
        return TYPE_ORDERED;
    }

    @Override
    public String getHeadRegex() {
        return "^\\s*[0-9]+\\.";
    }

    @Override
    public String getStandardHead() {
        return "1.";
    }

    @Override
    public String nextHead() {
        String head = getHead();
        String nHead = null;
        if (head != null && head.length() != 0) {
            Pattern p = Pattern.compile("[0-9]+");
            Matcher m = p.matcher(head);
            if (m.find()) {
                String numStr = m.group();
                nHead = head.replace(numStr, String.valueOf(Integer.parseInt(numStr) + 1));
            }
        }
        return nHead;
    }
}
