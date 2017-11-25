package tv.lycam.media;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import tv.lycam.media.basic.DemoUrlActivity;


public class DemoBasePlayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_base_player);

        findViewById(R.id.vod).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("type", "vod");
                intent.setClass(DemoBasePlayerActivity.this, DemoUrlActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.live).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("type", "live");
                intent.setClass(DemoBasePlayerActivity.this, DemoUrlActivity.class);
                startActivity(intent);
            }
        });
    }

}
