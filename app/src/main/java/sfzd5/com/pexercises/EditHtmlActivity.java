package sfzd5.com.pexercises;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

public class EditHtmlActivity extends AppCompatActivity {

    //以文本的形式显示内容，然后进行编辑

    EditText htmlEdit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_html);

        htmlEdit = (EditText) findViewById(R.id.htmlEdit);

    }
}
