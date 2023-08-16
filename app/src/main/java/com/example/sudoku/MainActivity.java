package com.example.sudoku;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {
    CustomButton clickedCustomButton = null;
    CustomButton[][] buttons = new CustomButton[9][9];
    TableLayout table;
    BoardGenerator board = new BoardGenerator();
    int originalValue = 0;
    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        table = (TableLayout) findViewById(R.id.tableLayout);

        for (int i = 0; i < 9; i++) {
            TableRow tableRow = new TableRow(this);
            table.addView(tableRow);
            for (int j = 0; j < 9; j++) {

                TableRow.LayoutParams layoutParams =
                        new TableRow.LayoutParams(
                                TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT,
                                1.0f
                        );


                if (i % 3 == 0 && i != 0) {
                    if (j % 3 == 0 && j != 0) {
                        layoutParams.setMargins(20, 30, 6, 10);

                    } else {
                        layoutParams.setMargins(6, 30, 6, 10);
                    }
                } else {
                    if (j % 3 == 0 && j != 0) {
                        layoutParams.setMargins(20, 10, 6, 10);
                    } else {
                        layoutParams.setMargins(6, 10, 6, 10);
                    }
                }

                buttons[i][j] = new CustomButton(this, i, j);
                buttons[i][j].setLayoutParams(layoutParams);
                tableRow.addView(buttons[i][j]);

                buttons[i][j].textView.setTextSize(20);
                buttons[i][j].textView.setGravity(Gravity.CENTER);
                buttons[i][j].textView.setPadding(0,30,0,30);

                // 숫자 생성 확률
                int probability = (int) (Math.random() * 10);
                int number = board.get(i, j);
                if (probability > 3) {
                    buttons[i][j].set(number);
                    buttons[i][j].setEnabled(false);    // 원래 값(보이는 값)은 못 바뀌도록 버튼 비활성화
                }

                // NumberPad onClick으로 VISIBLE 설정
                TableLayout tableLayout = (TableLayout) findViewById(R.id.tableLayout1);
                buttons[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        clickedCustomButton = (CustomButton) view;
                        originalValue = clickedCustomButton.value;
                        tableLayout.setVisibility(View.VISIBLE);
                    }
                });

                // onLongClick으로 dialog.show() 설정
                buttons[i][j].setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        clickedCustomButton = (CustomButton) view;
                        tableLayout.setVisibility(View.INVISIBLE);
                        showDialogMemo();
                        return true;
                    }
                });

                // RESET 버튼 설정 => Reset 시, New Game!!
                Button button = (Button) findViewById(R.id.reset);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        board = new BoardGenerator();
                        for (int i = 0; i < 9; i++) {
                            for (int j = 0; j < 9; j++) {
                                buttons[i][j].setEnabled(true); // Reset시, 버튼 활성화
                                buttons[i][j].setBackgroundColor(Color.WHITE);
                                deleteMemoReset(i,j);
                                buttons[i][j].set(0);
                                int probability = (int) (Math.random() * 10);
                                int number = board.get(i, j);
                                if (probability > 3) {
                                    buttons[i][j].set(number);
                                    buttons[i][j].setEnabled(false);    // 원래 값(보이는 값)은 못 바뀌도록 버튼 비활성화
                                }
                            }
                        }
                    }
                });
            }
        }
    }
    
    // Reset시, 메모도 다 지움
    public void deleteMemoReset(int row, int col) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int order = (i*3)+j;
                if(buttons[row][col].isChecked[i][j]) {
                    buttons[row][col].isChecked[i][j] = false;
                    buttons[row][col].memos[order].setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    // 완료 확인 => 모든 값이 맞으면 토스트 메시지&버튼 비활성화&버튼색 노랑으로 변경
    public void complete() {
        int cnt = 0;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (buttons[i][j].value == board.get(i,j)) {
                    cnt++;
                }
                if (cnt == 81) {
                    Toast.makeText(this,"Perfect!",Toast.LENGTH_SHORT).show();
                    for (int k = 0; k < 9; k++) {
                        for (int l = 0; l < 9; l++) {
                            buttons[k][l].setEnabled(false);
                            buttons[k][l].setBackgroundColor(Color.YELLOW);
                        }
                    }
                }
            }
        }
    }

    // onLongClick 막기 => 값이 있다면 onLongClick(메모) 막음
    public void block() {
        clickedCustomButton.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
                return true;
            }
        });
    }

    // onLongClick 가능 => 값이 없다면 다시 onLongClick(메모) 할 수 있음
    public void able() {
        TableLayout tableLayout = (TableLayout) findViewById(R.id.tableLayout1);
        clickedCustomButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                clickedCustomButton = (CustomButton) view;
                tableLayout.setVisibility(View.INVISIBLE);
                showDialogMemo();
                return true;
            }
        });
    }

    // Conflict
    public void setConflict() {
        int row = clickedCustomButton.row;
        int col = clickedCustomButton.col;
        int value = clickedCustomButton.value;

        // Horizontal
        for (int i = 0; i < 9; i++) {
            if(buttons[row][i].value == value && value != 0 && i != col) {
                buttons[row][i].setBackgroundColor(Color.parseColor("#ff0000"));
                clickedCustomButton.setBackgroundColor(Color.parseColor("#ff0000"));
            }
        }

        // vertical
        for (int i = 0; i < 9; i++) {
            if(buttons[i][col].value == value && value != 0 && i != row) {
                buttons[i][col].setBackgroundColor(Color.parseColor("#ff0000"));
                clickedCustomButton.setBackgroundColor(Color.parseColor("#ff0000"));
            }
        }

        // 3x3 square
        int squareRow = row/3;
        int squareCol = col/3;
        for (int i = squareRow*3; i < squareRow*3+3; i++) {
            for (int j = squareCol*3; j < squareCol*3+3; j++) {
                if(buttons[i][j].value == value && value != 0 && (i != row && j != col)) {
                    buttons[i][j].setBackgroundColor(Color.parseColor("#ff0000"));
                    clickedCustomButton.setBackgroundColor(Color.parseColor("#ff0000"));
                }
            }
        }

    }

    // Horizontal 중복 확인하기
    public boolean checkHorizontal(int row) {
        int col = clickedCustomButton.col;
        int value = originalValue;
        int cnt = 0;
        for (int i = 0; i < 9; i++) {
            if(buttons[row][i].value == value && value != 0 && i != col) {
                cnt++;
            }
        }
        if(cnt == 0) return true;
        else return false;
    }

    // Vertical 중복 확인하기
    public boolean checkVertical(int col) {
        int row = clickedCustomButton.row;
        int value = originalValue;
        int cnt = 0;
        for (int i = 0; i < 9; i++) {
            if(buttons[i][col].value == value && value != 0 && i != row) {
                cnt++;
            }
        }
        if(cnt == 0) return true;
        else return false;
    }

    // 3x3 square 중복 확인하기
    public boolean checkSquare(int row, int col) {
        int squareRow = row/3;
        int squareCol = col/3;
        int value = originalValue;
        int cnt = 0;
        for (int i = squareRow*3; i < squareRow*3+3; i++) {
            for (int j = squareCol*3; j < squareCol*3+3; j++) {
                if(buttons[i][j].value == value && value != 0 && (i != row && j != col)) {
                    cnt++;
                }
            }
        }
        if(cnt == 0) return true;
        else return false;
    }

    // 3x3 square에서 Horizontal 중복 확인하기
    public boolean checkSquareHorizontal(int row, int col) {
        int value = originalValue;
        int cnt = 0;
        for (int i = 0; i < 9; i++) {
            if(buttons[row][i].value == value && value != 0 && i != col) {
                cnt++;
            }
        }
        if(cnt == 0) return true;
        else return false;
    }

    // 3x3 square에서 Vertical 중복 확인하기
    public boolean checkSquareVertical(int row, int col) {
        int value = originalValue;
        int cnt = 0;
        for (int i = 0; i < 9; i++) {
            if(buttons[i][col].value == value && value != 0 && i != row) {
                cnt++;
            }
        }
        if(cnt == 0) return true;
        else return false;
    }

    // No conflict
    public void unsetConflict() {
        int row = clickedCustomButton.row;
        int col = clickedCustomButton.col;
        int value = originalValue;
        int squareRow = row/3;
        int squareCol = col/3;

        // Horizontal
        for (int i = 0; i < 9; i++) {
            if (buttons[row][i].value == value) {
                if (checkVertical(i) && checkSquare(row, i)) {
                    buttons[row][i].setBackgroundColor(Color.parseColor("#ffffff"));

                }
            }
        }

        // Vertical
        for (int i = 0; i < 9; i++) {
            if (buttons[i][col].value == value) {
                if (checkHorizontal(i) && checkSquare(i, col)) {
                    buttons[i][col].setBackgroundColor(Color.parseColor("#ffffff"));
                }
            }
        }

        // 3x3 square
        for (int i = squareRow*3; i < squareRow*3+3; i++) {
            for (int j = squareCol*3; j < squareCol*3+3; j++) {
                if(buttons[i][j].value == value) {
                    if (checkSquareHorizontal(i,j) && checkSquareVertical(i,j)) {
                        buttons[i][j].setBackgroundColor(Color.parseColor("#ffffff"));
                    }
                }
            }
        }

        if (clickedCustomButton.value != value)clickedCustomButton.setBackgroundColor(Color.parseColor("#ffffff"));

        setConflict();  // 숫자 바꾸면서 충돌 변화 적용
    }



    // NumberPad 1 클릭
    public void onClickNum1 (View v){
        TableLayout tablelayout1 = (TableLayout) findViewById(R.id.tableLayout1);
        clickedCustomButton.set(1);
        setConflict();
        unsetConflict();
        deleteMemo();
        tablelayout1.setVisibility(View.INVISIBLE);
        complete();
    }

    // NumberPad 2 클릭
    public void onClickNum2 (View v){
        TableLayout tablelayout1 = (TableLayout) findViewById(R.id.tableLayout1);
        clickedCustomButton.set(2);
        setConflict();
        unsetConflict();
        deleteMemo();
        tablelayout1.setVisibility(View.INVISIBLE);
        complete();
    }
    // NumberPad 3 클릭
    public void onClickNum3 (View v){
        TableLayout tablelayout1 = (TableLayout) findViewById(R.id.tableLayout1);
        clickedCustomButton.set(3);
        setConflict();
        unsetConflict();
        deleteMemo();
        tablelayout1.setVisibility(View.INVISIBLE);
        complete();
    }
    // NumberPad 4 클릭
    public void onClickNum4 (View v){
        TableLayout tablelayout1 = (TableLayout) findViewById(R.id.tableLayout1);
        clickedCustomButton.set(4);
        setConflict();
        unsetConflict();
        deleteMemo();
        tablelayout1.setVisibility(View.INVISIBLE);
        complete();
    }
    // NumberPad 5 클릭
    public void onClickNum5 (View v){
        TableLayout tablelayout1 = (TableLayout) findViewById(R.id.tableLayout1);
        clickedCustomButton.set(5);
        setConflict();
        unsetConflict();
        deleteMemo();
        tablelayout1.setVisibility(View.INVISIBLE);
        complete();
    }
    // NumberPad 6 클릭
    public void onClickNum6 (View v){
        TableLayout tablelayout1 = (TableLayout) findViewById(R.id.tableLayout1);
        clickedCustomButton.set(6);
        setConflict();
        unsetConflict();
        deleteMemo();
        tablelayout1.setVisibility(View.INVISIBLE);
        complete();
    }
    // NumberPad 7 클릭
    public void onClickNum7 (View v){
        TableLayout tablelayout1 = (TableLayout) findViewById(R.id.tableLayout1);
        clickedCustomButton.set(7);
        setConflict();
        unsetConflict();
        deleteMemo();
        tablelayout1.setVisibility(View.INVISIBLE);
        complete();
    }
    // NumberPad 8 클릭
    public void onClickNum8 (View v){
        TableLayout tablelayout1 = (TableLayout) findViewById(R.id.tableLayout1);
        clickedCustomButton.set(8);
        setConflict();
        unsetConflict();
        deleteMemo();
        tablelayout1.setVisibility(View.INVISIBLE);
        complete();
    }
    // NumberPad 9 클릭
    public void onClickNum9 (View v){
        TableLayout tablelayout1 = (TableLayout) findViewById(R.id.tableLayout1);
        clickedCustomButton.set(9);
        setConflict();
        unsetConflict();
        deleteMemo();
        tablelayout1.setVisibility(View.INVISIBLE);
        complete();
    }
    // NumberPad Cancel 클릭
    public void onClickCancel (View v){
        TableLayout tablelayout1 = (TableLayout) findViewById(R.id.tableLayout1);
        tablelayout1.setVisibility(View.INVISIBLE);
    }
    // NumberPad Delete 클릭
    public void onClickDelete (View v){
        TableLayout tablelayout1 = (TableLayout) findViewById(R.id.tableLayout1);
        clickedCustomButton.set(0);
        able();
        unsetConflict();
        tablelayout1.setVisibility(View.INVISIBLE);
    }

    public void deleteMemo() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int order = (i*3)+j;
                if(clickedCustomButton.isChecked[i][j]) {
                    clickedCustomButton.isChecked[i][j] = false;
                    clickedCustomButton.memos[order].setVisibility(View.INVISIBLE);
                }
                if (clickedCustomButton.value != 0) {
                    block();
                }
            }
        }
    }

    // DialogMemo
    public void showDialogMemo() {
        View dialogView = (View) View.inflate(this,R.layout.dialog_memo,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Memo")
                .setView(dialogView);

        // Ok 선택
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                for (int j = 0; j < 3; j++) {
                    for (int k = 0; k < 3; k++) {
                        int order = (j*3)+k;
                        if(clickedCustomButton.isChecked[j][k]) {
                            clickedCustomButton.memos[order].setVisibility(View.VISIBLE);
                        }
                        else {
                            clickedCustomButton.memos[order].setVisibility(View.INVISIBLE);
                        }
                    }
                }
            }
        });

        // Delete 선택
        builder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                for (int j = 0; j < 3; j++) {
                    for (int k = 0; k < 3; k++) {
                        int order = (j*3)+k;
                        if(clickedCustomButton.isChecked[j][k]) {
                            clickedCustomButton.isChecked[j][k] = false;
                            clickedCustomButton.memos[order].setVisibility(View.INVISIBLE);
                        }
                    }
                }
            }
        });

        // Cancel 선택
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                for (int j = 0; j < 3; j++) {
                    for (int k = 0; k < 3; k++) {
                        int order = (j*3)+k;
                        if(clickedCustomButton.memos[order].getVisibility() == View.INVISIBLE && clickedCustomButton.isChecked[j][k]) {
                            clickedCustomButton.isChecked[j][k] = false;
                        }
                    }
                }
            }
        });

        dialog = builder.create();
        dialog.show();

        // toggleButton1 설정
        ToggleButton toggleButton1 = dialog.findViewById(R.id.button1);
        toggleButton1.setChecked(clickedCustomButton.isChecked[0][0]);
        toggleButton1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (clickedCustomButton.isChecked[0][0]) {
                    clickedCustomButton.isChecked[0][0] = false;
                }
                else {
                    clickedCustomButton.isChecked[0][0] = true;
                }
            }
        });

        // toggleButton2 설정
        ToggleButton toggleButton2 = dialog.findViewById(R.id.button2);
        toggleButton2.setChecked(clickedCustomButton.isChecked[0][1]);
        toggleButton2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (clickedCustomButton.isChecked[0][1]) {
                    clickedCustomButton.isChecked[0][1] = false;
                }
                else {
                    clickedCustomButton.isChecked[0][1] = true;
                }
            }
        });

        // toggleButton3 설정
        ToggleButton toggleButton3 = dialog.findViewById(R.id.button3);
        toggleButton3.setChecked(clickedCustomButton.isChecked[0][2]);
        toggleButton3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (clickedCustomButton.isChecked[0][2]) {
                    clickedCustomButton.isChecked[0][2] = false;
                }
                else {
                    clickedCustomButton.isChecked[0][2] = true;
                }
            }
        });

        // toggleButton4 설정
        ToggleButton toggleButton4 = dialog.findViewById(R.id.button4);
        toggleButton4.setChecked(clickedCustomButton.isChecked[1][0]);
        toggleButton4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (clickedCustomButton.isChecked[1][0]) {
                    clickedCustomButton.isChecked[1][0] = false;
                }
                else {
                    clickedCustomButton.isChecked[1][0] = true;
                }
            }
        });

        // toggleButton5 설정
        ToggleButton toggleButton5 = dialog.findViewById(R.id.button5);
        toggleButton5.setChecked(clickedCustomButton.isChecked[1][1]);
        toggleButton5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (clickedCustomButton.isChecked[1][1]) {
                    clickedCustomButton.isChecked[1][1] = false;
                }
                else {
                    clickedCustomButton.isChecked[1][1] = true;
                }
            }
        });

        // toggleButton6 설정
        ToggleButton toggleButton6 = dialog.findViewById(R.id.button6);
        toggleButton6.setChecked(clickedCustomButton.isChecked[1][2]);
        toggleButton6.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (clickedCustomButton.isChecked[1][2]) {
                    clickedCustomButton.isChecked[1][2] = false;
                }
                else {
                    clickedCustomButton.isChecked[1][2] = true;
                }
            }
        });

        // toggleButton7 설정
        ToggleButton toggleButton7 = dialog.findViewById(R.id.button7);
        toggleButton7.setChecked(clickedCustomButton.isChecked[2][0]);
        toggleButton7.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (clickedCustomButton.isChecked[2][0]) {
                    clickedCustomButton.isChecked[2][0] = false;
                }
                else {
                    clickedCustomButton.isChecked[2][0] = true;
                }
            }
        });

        // toggleButton8 설정
        ToggleButton toggleButton8 = dialog.findViewById(R.id.button8);
        toggleButton8.setChecked(clickedCustomButton.isChecked[2][1]);
        toggleButton8.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (clickedCustomButton.isChecked[2][1]) {
                    clickedCustomButton.isChecked[2][1] = false;
                }
                else {
                    clickedCustomButton.isChecked[2][1] = true;
                }
            }
        });

        // toggleButton9 설정
        ToggleButton toggleButton9 = dialog.findViewById(R.id.button9);
        toggleButton9.setChecked(clickedCustomButton.isChecked[2][2]);
        toggleButton9.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (clickedCustomButton.isChecked[2][2]) {
                    clickedCustomButton.isChecked[2][2] = false;
                }
                else {
                    clickedCustomButton.isChecked[2][2] = true;
                }
            }
        });
    }

}