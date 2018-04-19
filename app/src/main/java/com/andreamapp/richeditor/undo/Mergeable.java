package com.andreamapp.richeditor.undo;

/**
 * Created by Andream on 2016/3/10.
 * Website: http://andream.com.cn
 * Email: me@andream.com.cn
 */
public interface Mergeable {
    boolean shouldMergeWith(Mergeable last);
    Mergeable mergeTo(Mergeable last);
}
