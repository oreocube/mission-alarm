package com.example.alarmapp;

import static com.example.alarmapp.ReaderContract.Entry.ID;
import static com.example.alarmapp.ReaderContract.Entry.TABLE_NAME;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MissionActivity extends AppCompatActivity {

    Button submitButton;
    TextView questionText, missionText;
    EditText inputText;
    String answer;
    SQLiteDatabase sqlDB;
    MyDBHelper myHelper;

    private final String selection = ID + " =?";
    private final String[] selectionArgs = {"1"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission);
        setTitle("오늘의 미션");

        submitButton = (Button) findViewById(R.id.submitButton);
        questionText = (TextView) findViewById(R.id.questionText);
        missionText = (TextView) findViewById(R.id.missionText);
        inputText = (EditText) findViewById(R.id.answerEditText);
        myHelper = new MyDBHelper(this);
        sqlDB = myHelper.getReadableDatabase();

        int questionType = (int) (Math.random() * 2);

        switch (questionType) {
            case 0:
                createTextMission();
                break;
            case 1:
                createNumberMission();
                break;
        }

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCorrectAnswer(questionType, inputText.getText().toString().trim())) {
                    // 정답이면 알람 해제
                    turnOffAlarm();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "틀렸습니다.", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void turnOffAlarm() {
        Toast.makeText(getApplicationContext(), "정답입니다.", Toast.LENGTH_SHORT).show();
        // 서비스 중지
        Intent intentService = new Intent(getApplicationContext(), AlarmService.class);
        getApplicationContext().stopService(intentService);
        // 데이터베이스에서 알람 삭제
        sqlDB.delete(TABLE_NAME, selection, selectionArgs);
    }

    private boolean isCorrectAnswer(int qType, String input) {
        return input.equals(answer);
    }

    private void createTextMission() {
        // 속담 배열에서 랜덤으로 하나 가져오기
        int x = (int) (Math.random() * 20);
        answer = proverbs[x];

        // 화면 갱신
        questionText.setText(getString(R.string.text_question));
        missionText.setText(answer);
        inputText.setHint(getString(R.string.text_hint));
        inputText.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
    }

    private void createNumberMission() {
        // 숫자 2개, 연산자 1개 랜덤 뽑기
        int v1 = (int) (Math.random() * 100) + 1;
        int v2 = (int) (Math.random() * 100) + 1;
        int op = (int) (Math.random() * 4);

        String mission;
        if (op == 0) {
            mission = v1 + " + " + v2;
            answer = Integer.toString(v1 + v2);
        } else if (op == 1) {
            mission = v1 + " - " + v2;
            answer = Integer.toString(v1 - v2);
        } else if (op == 2) {
            mission = v1 + " * " + v2;
            answer = Integer.toString(v1 * v2);
        } else {
            mission = v1 + " / " + v2;
            answer = Integer.toString(v1 / v2);
        }

        // 화면 갱신
        questionText.setText(getString(R.string.num_question));
        missionText.setText(mission);
        inputText.setHint(getString(R.string.num_hint));
        inputText.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_CLASS_NUMBER);
    }

    String[] proverbs = {
            "Power is dangerous unless you have humility",
            "Empty vessels make the most sound",
            "Patience conquers the world",
            "Patience is the art of hoping",
            "Patience devours the devil",
            "Our ideals are our better selves",
            "Slow and steady win the game",
            "The family is one of nature's masterpieces",
            "A sound mind in a sound body",
            "Good health is above wealth",
            "Sleep is better than medicine",
            "A friend in need is a friend indeed",
            "Books and friends should be few and good",
            "Old friend is better than two new ones",
            "Books cannot never teach the use of books",
            "Example is better than precept",
            "The language of truth is simple",
            "Education is the best provision for old age",
            "I'm youth, I'm joy, I'm a little bird that has broken out of the egg",
            "A long life may not be good enough, but a good life is long enough"
    };

    @Override
    public void onAttachedToWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            );
        }
    }

    @Override
    protected void onDestroy() {
        sqlDB.close();
        myHelper.close();
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(false);
            setTurnScreenOn(false);
        } else {
            getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            );
        }
    }
}