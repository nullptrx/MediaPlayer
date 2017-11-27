package tv.lycam.media;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import tv.lycam.alivc.PlayerState;
import tv.lycam.alivc.StandardPlayer;
import tv.lycam.alivc.utils.CommonUtil;
import tv.lycam.alivc.utils.NetWatchdog;
import tv.lycam.alivc.utils.OrientationUtils;

public class MainActivity extends AppCompatActivity {
    public static final String DEFAULT_TEST_URL_LIVE = "rtmp://live.hkstv.hk.lxdns.com/live/hks";
    public static final String DEFAULT_TEST_URL_VOD = "http://resource-s3.lycam.tv/apps/KQWCUPAHR3/db4ded30-b0a1-11e7-aee5-25404b4da7c9/streams/dev-f31cebf1-c5b9-11e7-8270-61eef27bc007/hls/vod.m3u8";
    private static final int REQUEST_CODE_WRITE_SETTINGS = 1;
    //    public static final String DEFAULT_TEST_URL_VOD = "http://video.mb.moko.cc/2017-10-16/d4908bbd-97ab-443b-8742-f446498e2695.mp4/0306e508-9d4b-4578-9874-35b82f33fa14.m3u8";
//    public static final String DEFAUL\T_TEST_URL = "http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f30.mp4";
    private StandardPlayer mPlayer;

    NetWatchdog netWatchdog;
    OrientationUtils orientationUtils;
    private int mSystemUiVisibility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.mipmap.ic_launcher_round);
        mPlayer = findViewById(R.id.player);
        mPlayer.setThumbImageView(imageView);
        View view = View.inflate(this, R.layout.item_main_topcontainner, null);

//        mPlayer.setTopContainerView(view);
        //设置旋转
        orientationUtils = new OrientationUtils(this);
        mPlayer.setVideoPath(DEFAULT_TEST_URL_LIVE);
        mPlayer.start();
        netWatchdog = new NetWatchdog(this);
        netWatchdog.setNetChangeListener(new NetWatchdog.NetChangeListener() {
            @Override
            public void onWifiTo4G() {
                if (mPlayer.isPlaying()) {
                    mPlayer.pause();
                }
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle(getString(R.string.net_change_to_4g));
                alertDialog.setMessage(getString(R.string.net_change_to_continue));
                alertDialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mPlayer.start();
                    }
                });
                alertDialog.setNegativeButton(getString(R.string.no), null);
                AlertDialog alert = alertDialog.create();
                alert.show();

                Toast.makeText(MainActivity.this.getApplicationContext(), R.string.net_change_to_4g, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void on4GToWifi() {
                Toast.makeText(MainActivity.this.getApplicationContext(), R.string.net_change_to_wifi, Toast.LENGTH_SHORT).show();
                if (mPlayer.getPlayerState() == PlayerState.CURRENT_STATE_PAUSE) {
                    mPlayer.resume();
                }
            }

            @Override
            public void onNetDisconnected() {
                Toast.makeText(MainActivity.this.getApplicationContext(), R.string.net_disconnect, Toast.LENGTH_SHORT).show();
                if (mPlayer.isPlaying()) {
                    mPlayer.pause();
                }
            }
        });
        netWatchdog.startWatch();
        CommonUtil.hideSupportActionBar(this, true, true);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPlayer.restorePlayerState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPlayer.savePlayerState();
    }

    @Override
    protected void onDestroy() {
        netWatchdog.stopWatch();
        mPlayer.destroy();
        super.onDestroy();
        if (orientationUtils != null)
            orientationUtils.releaseListener();
    }

    public void grant(View v) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE_WRITE_SETTINGS);
        }
    }

    public void fullscreen(View v) {
        orientationUtils.resolveByClick();
        mSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
        CommonUtil.hideNavKey(this);
    }

    public void switchsource(View v) {
        mPlayer.setVideoPath(mPlayer.isLiveMode() ? DEFAULT_TEST_URL_VOD : DEFAULT_TEST_URL_LIVE);
        mPlayer.start();
    }

    int mTransformSize;

    public void transform(View view) {
       /* if (mTransformSize == 0) {
            mTransformSize = 1;
        } else if (mTransformSize == 1) {
            mTransformSize = 2;
        } else */
        if (mTransformSize == 2) {
            mTransformSize = 0;
        } else {
            mTransformSize = 2;
        }
        mPlayer.setmTransformSize(mTransformSize);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_WRITE_SETTINGS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(this)) {
                    Log.d("MainActivity", "onActivityResult write settings granted");
                    Settings.System.putInt(getContentResolver(), "enable_navbar", 1);
                    Settings.System.putInt(getContentResolver(), "hide_virtual_key", 1);
                    Settings.System.putInt(getContentResolver(), "virtual_notification_key_toggle", 0);
                }

            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        //先返回正常状态
        if (orientationUtils.getScreenType() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            orientationUtils.resolveByClick();
            CommonUtil.showNavKey(this, mSystemUiVisibility);
            return;
        }
        super.onBackPressed();
    }

}
