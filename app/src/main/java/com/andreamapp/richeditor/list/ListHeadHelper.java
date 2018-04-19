package com.andreamapp.richeditor.list;

import android.text.Editable;

/**
 * Created by Andream on 2016/3/13.
 * Website: http://andream.com.cn
 * Email: me@andream.com.cn
 * <p/>
 * 回车 添加
 * 回车 删除
 * <p/>
 * 返回新行应该添加的字符串
 * 如果什么都不添加 返回“”
 * 如果应该删除上一行 返回null
 * String enterKey(String lastLine);
 * <p/>
 * <p/>
 * toggle 添加
 * toggle 删除
 * <p/>
 * 获取上一行 新head应该插入的位置
 * 如果已经有该head了，返回-1
 * headtype必须>0
 * int getHeadInsertion(String lastLine，int headType);
 * <p/>
 * 改变指定行指定head的状态
 * void toggle(Editable s,int lineStart,int lineEnd,int headType);
 */
public class ListHeadHelper {
    private ListHead[] mListHeads;
    private Editable text;
    private String line;
    private int lineStart, lineEnd;
    private int headType;
    private boolean working;

    public ListHeadHelper() {
        mListHeads = new ListHead[]{new TabListHead()}; // code editor, don't need ordered list and unordered list
    }

    public ListHead getMatchedListHead()
    {
        checkPrepared();
        for (int i = mListHeads.length - 1; i >= 0; i--) {
            ListHead lh = mListHeads[i];
            String head = lh.getHead();
            if (head != null) {
                return lh;
            }
        }
        return null;
    }

    protected ListHead getListHead(int headType) {
        ListHead lh = mListHeads[headType];
        return lh;
    }

    String enterKeyResponse() {
        ListHead listHead = getMatchedListHead();
        String head = listHead == null ? null : listHead.getHead();
        String result;
        if (head != null) {
            if (head.length() == line.length()) {
                //lastLine是一个空的表头，应该删除上一行
//                result = null;
                result = listHead.nextHead();
            }
            else {
                //lastLine含有某个表头，当前行应该自动补全该表头
                result = listHead.nextHead();
            }
        }
        else {
            //上一行没有表头，当前行什么都不添加
            result = "";
        }
        return result;
    }

    public ListHeadHelper enterKey() {
        checkPrepared();

        String newLine = enterKeyResponse();
        if (newLine == null) {
            text.delete(lineStart,lineEnd);
        }
        else if ("".equals(newLine)) {
        }
        else {
            text.insert(lineEnd + 1, newLine);
        }
        return this;
    }


    //    获取上一行 新head应该插入的位置
//    如果已经有该head了，返回-1
//    headtype必须>0
    int getHeadInsertion() {
        ListHead listHead = getListHead(headType);
        String head = listHead.getHead();
        int result;
        if (head != null) {
            //已经有该head了，返回-1
            result = -1;
        }
        else {
            //没有该head，计算缩进量
            ListHead tabHead = getListHead(ListHead.TYPE_TAB);
            String tab = tabHead.getHead();
            if (tab != null) {
                //前面有缩进，应该在缩进右边缘插入
                result = tab.length();
            }
            else {
                //没有缩进，从最左端插入
                result = 0;
            }
        }
        return result;
    }

    //    改变指定行指定head的状态
    public ListHeadHelper toggle() {
        checkPrepared();

        ListHead listHead = getListHead(headType);
        int insertion = getHeadInsertion();
        if (insertion == -1) {
            //删除该head
            int start = lineStart + line.indexOf(listHead.getStandardHead());
            int end = start + listHead.getStandardHead().length();
            text.delete(start, end);
        }
        else {
            text.insert(lineStart + insertion,listHead.getStandardHead());
        }

        return this;
    }


    public ListHeadHelper prepare(Editable text, int lineStart, int lineEnd, int headType) {
        if (!working) {
            this.text = text;
            this.line = text.subSequence(lineStart, lineEnd).toString();
            this.lineStart = lineStart;
            this.lineEnd = lineEnd;
            this.headType = headType;
            this.working = true;

            for (ListHead listHead : mListHeads) {
                listHead.setLine(line);
            }
            return this;
        }
        else {
            throw new IllegalStateException("you should call ok() before next prepare()");
        }
    }

    public void ok() {
        working = false;
    }

    protected void checkPrepared() {
        if (!working) {
            throw new IllegalStateException("hasnot prepared yet!");
        }
    }

}
