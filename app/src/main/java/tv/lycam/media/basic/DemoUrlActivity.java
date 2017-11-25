package tv.lycam.media.basic;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import tv.lycam.media.BaseAppCompatActivity;
import tv.lycam.media.DemoApplication;
import tv.lycam.media.R;


public class DemoUrlActivity extends BaseAppCompatActivity {

    private EditText mUrlEdit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_demo_url);

        mUrlEdit = (EditText) findViewById(R.id.url);

        findViewById(R.id.start_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNoSkinActivity();
            }
        });


        String type = getIntent().getStringExtra("type");
        if(type.equals("vod")) {
//            String url = "http://player.alicdn.com/video/aliyunmedia.mp4";
            String url = DemoApplication.DEFAULT_TEST_URL;
            mUrlEdit.setText(url);
        }else if(type.equals("live"))
        {
//            String url = "rtmp://live.hkstv.hk.lxdns.com/live/hks";
            String url = DemoApplication.DEFAULT_TEST_URL_LIVE;
            mUrlEdit.setText(url);
        }
    }

    private void startNoSkinActivity() {

        String mUrl = mUrlEdit.getText().toString();

        if (TextUtils.isEmpty(mUrl)) {
            Toast.makeText(getApplicationContext(), R.string.url_is_empty, Toast.LENGTH_LONG).show();
            return;
        }


        Intent intent = new Intent();
        //pass the url and type to intent
        intent.putExtra("url", mUrl);
        String type = getIntent().getStringExtra("type");
        if(type.equals("vod")) {
            intent.setClass(this, VodModeActivity.class);
        }else if(type.equals("live")){
            intent.setClass(this, LiveModeActivity.class);
        }

        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

