package tv.lycam.media;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import tv.lycam.player.single.StandardPlayer;
import tv.lycam.player.widget.AliVideoView;
import tv.lycam.player.widget.IVideoView;
import tv.lycam.player.widget.IjkVideoView;

public class Main2Activity extends AppCompatActivity {
    public static final String DEFAULT_TEST_URL_LIVE = "rtmp://live.hkstv.hk.lxdns.com/live/hks";
    public static final String DEFAULT_TEST_URL_VOD = "http://resource-s3.lycam.tv/apps/KQWCUPAHR3/db4ded30-b0a1-11e7-aee5-25404b4da7c9/streams/dev-f31cebf1-c5b9-11e7-8270-61eef27bc007/hls/vod.m3u8";
    public static final String DEFAULT_TEST_LOCAL_MP4 = "/storage/emulated/0/手播课/target/手播课宣传片.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        AliVideoView aliplayer = findViewById(R.id.player_ali);
        IjkVideoView ijkplayer = findViewById(R.id.player_ijk);
        StandardPlayer singleplayer = findViewById(R.id.player_single);

        initPlayer(aliplayer);
        initPlayer(ijkplayer);
        singleplayer.setVideoPath(DEFAULT_TEST_URL_LIVE);
        singleplayer.start();
    }

    private void initPlayer(IVideoView player) {
        player.setVideoPath(DEFAULT_TEST_URL_VOD);
        player.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
