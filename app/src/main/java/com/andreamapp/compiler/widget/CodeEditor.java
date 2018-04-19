package com.andreamapp.compiler.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.widget.EditText;

import com.andreamapp.compiler.bean.Language;
import com.andreamapp.richeditor.list.ListHeadHelper;
import com.andreamapp.richeditor.undo.Mergeable;
import com.andreamapp.richeditor.undo.Recyclable;
import com.andreamapp.richeditor.undo.RecyclerPool;
import com.andreamapp.richeditor.undo.UndoHandler;
import com.andreamapp.richeditor.undo.Undoable;

import java.io.IOException;
import java.util.regex.Matcher;

/**
 * Created by Andream on 2017/2/2.
 * Website: http://andreamapp.com
 * Email: pqbiao@gmail.com
 * <p>
 * 显示行号
 * 首行缩进
 * 撤销重做
 * 设置语言
 * 语法高亮
 * 代码提示
 * 快捷输入
 */
//@SuppressLint("AppCompatCustomView")
public class CodeEditor extends EditText implements Undoable {
    Language mLanguage;
    boolean isDrawing;

    ListHeadHelper mListHeadHelper;
    UndoHandler mUndoHandler;
    ChangeListener mChangeListener;

    public CodeEditor(Context context) {
        super(context);
        init();
    }

    public CodeEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CodeEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    void init() {
        mListHeadHelper = new ListHeadHelper();
        mUndoHandler = new UndoHandler();
        mChangeListener = new ChangeListener();
        addTextChangedListener(mChangeListener);

        setTextColor(0xfff0f0f0);
        setTextSize(14);
        setTypeface(Typeface.MONOSPACE);
        try {
            Language.init(getContext());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        mLanguage = Language.getLanguages()[0];

        initTemplate();
    }

    void initTemplate(){
        setText(mLanguage.getTemplate());
        setSelection(mLanguage.getTemplateCursorOffset());
    }

    void highlight() {
        highlight(getText().toString(), 0);
    }

    /**
     * src是Editor内容的一个子串，相对Editor内容的开头偏移量为offset
     * 调用这个函数将会对src的内容重新绘制
     * 遍历mLanguage.getSyntaxRegex
     * 匹配字符串src
     * 对匹配到的串在Editor内的映射着色为mLanguage.getSyntaxColors[i]
     */
    void highlight(CharSequence src, int offset) {
        isDrawing = true;
        Editable ss = getText();
        clearSpans(ss, offset, offset + src.length()); // clear broken span
        for (int i = 0; i < mLanguage.getSyntaxRegex().length; i++) {
            Matcher matcher = mLanguage.getSyntaxRegex()[i].matcher(src);
            while (matcher.find()) {
                clearSpans(ss, offset + matcher.start(), offset + matcher.end()); // clear low priority span
                ss.setSpan(new ForegroundColorSpan(mLanguage.getSyntaxColors()[i]), offset + matcher.start(), offset + matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        isDrawing = false;
    }

    private void clearSpans(Editable ss, int start, int end) {
        ForegroundColorSpan[] spans = ss.getSpans(start, end, ForegroundColorSpan.class);
        for (ForegroundColorSpan span : spans) {
//            Log.i("LineBounds", "find a span between " + start +" and " + end);
            ss.removeSpan(span);
        }
    }


    private int[] getLineBounds(int selStart, int selEnd) {
        String str = getText().toString();
        int start = str.lastIndexOf('\n', selStart - 1) + 1;
        int end = str.indexOf('\n', selEnd);
        start = start < 0 ? 0 : start;
        end = end <= 0 || end > str.length() ? str.length() : end;
//        Log.i("LineBounds", str + "\n from (" + selStart + "," + selEnd + ") to (" + start + "," + end + ")\n");
        return new int[]{start, end};
    }

    private float getLeftOffset() {
        return String.valueOf(getLineCount()).length() * getTextSize() + 3 * lineNumberPadding;
    }

    private int lineNumberPadding = 10;
    Rect bounds = new Rect();

    @Override
    protected void onDraw(Canvas canvas) {
        int lineCount = getLineCount();
        float offset = getLeftOffset();
//        canvas.translate(offset, 0);

        super.onDraw(canvas);

        getPaint().setAlpha(100);

        canvas.drawLine(offset - lineNumberPadding, 0, offset - lineNumberPadding, canvas.getHeight(), getPaint());

        for (int i = 0; i < lineCount; i++) {
            getLineBounds(i, bounds);
            canvas.drawText(String.valueOf(i + 1), lineNumberPadding, bounds.top + getTextSize(), getPaint());
        }

        getPaint().setAlpha(255);
    }

    /**
     * Undo & Redo
     */
    @Override
    public void undo() {
        mUndoHandler.undo();
    }

    @Override
    public void redo() {
        mUndoHandler.redo();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        String s = text.toString().replaceAll("\t", "  ");
        super.setText(s, type);
    }

    /**
     * Native ChangeListener
     * For list
     */
    private class ChangeListener implements TextWatcher {

        CharSequence beforeText, afterText;
        int startIndex;
        int left, offset;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //Log.i(TAG, "be at " + start + " : " + s.subSequence(start, start + count) + " -> " + after);
            beforeText = s.subSequence(start, start + count);
            startIndex = start;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //Log.i(TAG, "on at " + start + " : " + before + " -> " + s.subSequence(start, start + count));
            afterText = s.subSequence(start, start + count);
            left = start;
            offset = count;
        }

        @Override
        public void afterTextChanged(Editable s) {
            //Log.i(TAG, "afterTextChanged\n-------------------------");

            if (isSystemChangingText) { // undo & redo will call this method, we should ignore it.
                isSystemChangingText = false;
            }
            else if (beforeText.length() != 0 || afterText.length() != 0) {
                mUndoHandler.addUndoable(obtainTextUndo(beforeText, afterText, startIndex));

                if (afterText.toString().equals("\n")) {
                    onEnterKey(s);
                }
            }

            // anyway redraw highlights in changed line
            setPadding((int) getLeftOffset(), lineNumberPadding, lineNumberPadding, lineNumberPadding);
            if (!isDrawing) {
                int[] bounds = getLineBounds(left, offset + left);
                highlight(s.subSequence(bounds[0], bounds[1]), bounds[0]);
            }
        }

        private void onEnterKey(Editable s) {
            int[] lineBounds = getLineBounds(getSelectionStart() - 1, getSelectionEnd() - 1);
            mListHeadHelper
                    .prepare(s, lineBounds[0], lineBounds[1], 0)
                    .enterKey()
                    .ok();
        }
    }


    /**
     * Undo & Redo
     * Utils and RecyclerPool
     */
    private boolean isSystemChangingText;

    private RecyclerPool<TextUndo> sTextUndoRecyclerPool = new RecyclerPool<TextUndo>() {
        @Override
        public TextUndo newInstance() {
            return new TextUndo();
        }
    };

    protected TextUndo obtainTextUndo(CharSequence beforeText, CharSequence afterText, int startIndex) {
        return sTextUndoRecyclerPool.obtain().setup(beforeText, afterText, startIndex);
    }

    private class TextUndo implements Undoable, Mergeable, Recyclable {
        CharSequence beforeText, afterText;
        int startIndex;
        boolean hasEverUndoed;

        public TextUndo() {
        }

        public TextUndo setup(CharSequence beforeText, CharSequence afterText, int startIndex) {
            this.beforeText = beforeText;
            this.afterText = afterText;
            this.startIndex = startIndex;
            hasEverUndoed = false;
            return this;
        }

        @Override
        public void undo() {
            isSystemChangingText = true;
            getText().replace(startIndex, startIndex + afterText.length(), beforeText);
            hasEverUndoed = true;
        }

        @Override
        public void redo() {
            isSystemChangingText = true;
            getText().replace(startIndex, startIndex + beforeText.length(), afterText);
        }

        /**
         * 满足下列条件的TextUndo会被合并
         * 1.连续的insert(不包含\t \n 空格)
         * 2.连续的delete
         * 3.连续的空格 \t
         */
        @Override
        public boolean shouldMergeWith(Mergeable l) {
            boolean should = false;
            if (!hasEverUndoed && l instanceof TextUndo) {
                TextUndo last = (TextUndo) l;

                //连续的insert
                if (this.beforeText.length() == 0
                        && last.beforeText.length() == 0
                        && this.startIndex == last.startIndex + last.afterText.length()) {
                    should = true;

                    //用户输入空格 / 粘贴含有空格的字符串
                    //不应该合并
                    //除非前面的字符当前输入的字符一样（连续输入空格）
                    String newStr = this.afterText.toString();
                    String oldStr = last.afterText.toString();
                    if (newStr.contains(" ") || newStr.contains("\t") || newStr.contains("\n")) {
                        should = false;
                        if (newStr.length() == 1 && oldStr.endsWith(newStr)) {
                            should = true;
                        }
                    }
                    //前面有很长的空字符串，也不合并
                    else if (oldStr.replace(" ", "").replace("\t", "").replace("\n", "").isEmpty()) {
                        if (oldStr.length() >= 3) {
                            should = false;
                        }
                    }
                }

                //连续的delete
                else if (this.afterText.length() == 0
                        && last.afterText.length() == 0
                        && last.startIndex == this.startIndex + this.beforeText.length()) {
                    should = true;
                }
            }
            return should;
        }

        @Override
        public Mergeable mergeTo(Mergeable l) {
            TextUndo merged = null;
            if (shouldMergeWith(l)) {
                TextUndo last = (TextUndo) l;

                //将this合并到last中
                if (this.beforeText.length() == 0) {
                    if (!(last.afterText instanceof Editable)) {
                        last.afterText = new SpannableStringBuilder(last.afterText);
                    }
                    ((Editable) last.afterText).append(this.afterText);
                }
                else if (this.afterText.length() == 0) {
                    if (!(last.beforeText instanceof Editable)) {
                        last.beforeText = new SpannableStringBuilder(last.beforeText);
                    }
                    ((Editable) last.beforeText).insert(0, this.beforeText);
                    last.startIndex = this.startIndex;
                }

                merged = last;
                recycle();
            }
            return merged;
        }

        //当此对象被其他对象合并时，应该调用recycle
        @Override
        public void recycle() {
            sTextUndoRecyclerPool.recycle(this);
        }
    }
}
