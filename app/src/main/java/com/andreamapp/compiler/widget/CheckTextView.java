package com.andreamapp.compiler.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AndroidRuntimeException;
import android.util.AttributeSet;
import android.view.View;

import com.andreamapp.compiler.R;


public class CheckTextView extends android.support.v7.widget.AppCompatTextView {

    private boolean checked;

    private OnCheckedChangeListener onCheckedChangeListener;

    public CheckTextView(Context context) {
        super(context);
        init();
    }

    public CheckTextView(Context context, AttributeSet set) {
        super(context, set);
        init();
    }

    public CheckTextView(Context context, AttributeSet set, int style) {
        super(context, set, style);
        init();
    }

    void init(){
        setBackgroundResource(R.drawable.action_selector);
        setTypeface(Typeface.MONOSPACE, Typeface.BOLD);

        super.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View p1) {
                toggleChecked();
                if (onCheckedChangeListener != null)
                    onCheckedChangeListener.onCheckedChange(CheckTextView.this, isChecked());
            }
        });
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

    public OnCheckedChangeListener getOnCheckedChangeListener() {
        return onCheckedChangeListener;
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        throw new AndroidRuntimeException("this method has ben desprated,use setOnCheckedChangeListener() instead");
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        setSelected(checked);
    }

    public boolean isChecked() {
        return checked;
    }

    public void toggleChecked() {
        setChecked(!isChecked());
    }

    public interface OnCheckedChangeListener {
        void onCheckedChange(CheckTextView view, boolean isChecked);
    }
}
