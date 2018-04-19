package com.andreamapp.richeditor.undo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andream on 2016/3/10.
 * Website: http://andream.com.cn
 * Email: me@andream.com.cn
 */
public abstract class RecyclerPool<T>{

    private List<T> sRecyclerPool = new ArrayList<T>();

    public final T obtain() {
        synchronized (sRecyclerPool) {
            T item = null;
            if (sRecyclerPool.isEmpty()) {
                item = newInstance();
            }
            else {
                item = sRecyclerPool.get(0);
                sRecyclerPool.remove(0);
            }
            return item;
        }
    }

    public final void recycle(T item) {
        sRecyclerPool.add(item);
    }

    public abstract T newInstance();
}
