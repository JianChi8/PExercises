package sfzd5.com.pexercises;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SendTestQuestionActivity extends AppCompatActivity {

    //发送其它练习题，直接发送到网站，不往微信中发送，可以发送为HTML格式
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_test_question);
    }
}
