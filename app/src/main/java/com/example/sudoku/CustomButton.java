package com.example.sudoku;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

public class CustomButton extends FrameLayout {
    int row;
    int col;
    int value;
    TextView textView;
    int k = 0;
    TextView[] memos = new TextView[9];
    boolean[][] isChecked = new boolean[3][3];

    public CustomButton(Context context) {
        super(context);
    }

    public CustomButton(Context context, int row, int col) {
        super(context);
        this.row = row;
        this.col = col;
        textView = new TextView(context);
        addView(textView);
        setClickable(true);
        setBackgroundResource(R.drawable.button_selector);

        LayoutInflater layoutInflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TableLayout memo = (TableLayout) layoutInflater.inflate(R.layout.layout_memo, null);
        addView(memo);

        for(int i = 0; i < 3; i++) {
            TableRow tableRow = (TableRow) memo.getChildAt(i);
            for(int j = 0; j < 3; j++, k++) {
                memos[k] = (TextView) tableRow.getChildAt(j);
            }
        }
    }

    public void set(int a) {
        if (a == 0) {
            value = 0;
            textView.setText(null);
        } else {
            value = a;
            textView.setText(String.valueOf(value));
        }
    }
}
