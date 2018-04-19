package com.andreamapp.richeditor.undo;

/**
 * Created by Andream on 2016/3/8.
 * Website: http://andream.com.cn
 * Email: me@andream.com.cn
 */
public interface Undoable {
    void undo();

    void redo();
}
