package com.andreamapp.compiler.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import com.andreamapp.compiler.bean.CompileResult;

/**
 * Created by Andream on 2017/2/5.
 * Website: http://andream.com.cn
 * Email: me@andream.com.cn
 */

@SuppressLint("AppCompatCustomView")
public class ConsoleView extends TextView{
    private CompileResult mCompileResult;
    private int mOutputColor, mErrorColor;

    public ConsoleView(Context context) {
        super(context);
        init();
    }

    public ConsoleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ConsoleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    void init() {
        mOutputColor = 0xfff0f0f0;
        mErrorColor = 0xfff02f2f;
        setTextColor(mOutputColor);
        setTypeface(Typeface.MONOSPACE);
    }

    public CompileResult getCompileResult() {
        return mCompileResult;
    }

    public void setCompileResult(CompileResult compileResult) {
        mCompileResult = compileResult;
        String output = compileResult.getOutput() == null ? "" : compileResult.getOutput();
        String error = compileResult.getError() == null ? "" : compileResult.getError();
        String text = output + "\n" + error;
        SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        ssb.setSpan(new ForegroundColorSpan(mOutputColor), 0, output.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new ForegroundColorSpan(mErrorColor), output.length() + 1, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        setText(ssb);
    }
}
