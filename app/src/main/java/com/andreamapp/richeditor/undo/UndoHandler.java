package com.andreamapp.richeditor.undo;

import android.util.Log;

import java.util.LinkedList;

/**
 * Created by Andream on 2016/3/8.
 * Website: http://andream.com.cn
 * Email: me@andream.com.cn
 */
public class UndoHandler implements Undoable {
    private static final int DEFAULT_MAX_LENGTH = 100;
    private static final String TAG = "UndoHandler";
    private static final boolean DEBUG = true;

    private LinkedList<Undoable> mUndoableList;
    private int mMaxLength;
    private int mCurrentIndex;


    public UndoHandler() {
        this(DEFAULT_MAX_LENGTH);
    }

    public UndoHandler(int maxLength) {
        mMaxLength = maxLength;
        mCurrentIndex = 0;
        mUndoableList = new LinkedList<Undoable>();
    }

    public void addUndoable(Undoable u) {
        int index = mCurrentIndex;
        if (isFull()) {
            removeAndRecycleIfPossible(0);
            index--;
        }
        else if (size() > index) {
            final int S = size();
            for (int i = index; i < S; i++) {
               removeAndRecycleIfPossible(index);
            }
        }

        boolean hasMerged = false;
        if (u instanceof Mergeable && size() > 0) {
            int lIndex = size() - 1;
            Undoable last = mUndoableList.get(lIndex);
            if (last instanceof Mergeable) {
                Mergeable merged = ((Mergeable) u).mergeTo((Mergeable) last);
                if (merged != null) {
                    if (merged != last) {
                        removeAndRecycleIfPossible(lIndex);
                        mUndoableList.add((Undoable) merged);
                    }
                    hasMerged = true;
                }
            }
        }
        if (!hasMerged) {
            mUndoableList.add(u);
            index++;
        }

        if (DEBUG) Log.i(TAG,"addUndoable");
        setCurrentIndex(index);
    }

    public boolean isFull() {
        return size() == mMaxLength;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean canUndo() {
        return mCurrentIndex != 0;
    }

    public boolean canRedo() {
        return mCurrentIndex != size();
    }

    @Override
    public void undo() {
        synchronized (this) {
            if (canUndo()) {
                int index = mCurrentIndex - 1;
                mUndoableList.get(index).undo();
                setCurrentIndex(index);
            }
        }
    }

    @Override
    public void redo() {
        synchronized (this) {
            if (canRedo()) {
                int index = mCurrentIndex;
                mUndoableList.get(index).redo();
                setCurrentIndex(index + 1);
            }
        }
    }

    public int size() {
        return mUndoableList.size();
    }

    public int getMaxLength() {
        return mMaxLength;
    }

    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        mCurrentIndex = currentIndex;
        //到队列最右端 currentIndex == size()
        //到队列最左端 currentIndex == 0
        if (DEBUG) Log.i(TAG,"index = "+currentIndex);
        if (mOnUndoIndexChangeListener != null) {
            mOnUndoIndexChangeListener.onUndoIndexChanged(currentIndex, size(), canUndo(), canRedo());
        }
    }

    public void clear() {
        mUndoableList.clear();
    }

    private void removeAndRecycleIfPossible(int index) {
        Object obj = mUndoableList.get(index);
        if (obj instanceof Recyclable) {
            ((Recyclable) obj).recycle();
        }
        mUndoableList.remove(index);
    }

    private OnUndoIndexChangeListener mOnUndoIndexChangeListener;

    public OnUndoIndexChangeListener getOnUndoIndexChangeListener() {
        return mOnUndoIndexChangeListener;
    }

    public void setOnUndoIndexChangeListener(OnUndoIndexChangeListener onUndoIndexChangeListener) {
        mOnUndoIndexChangeListener = onUndoIndexChangeListener;
    }

    public interface OnUndoIndexChangeListener {
        void onUndoIndexChanged(int index, int size, boolean canUndo, boolean canRedo);
    }
}
