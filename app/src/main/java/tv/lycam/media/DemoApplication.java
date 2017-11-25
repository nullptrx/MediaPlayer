package tv.lycam.media;

import android.app.Application;

import com.alivc.player.AliVcMediaPlayer;
import com.alivc.player.VcPlayerLog;

/**
 * @Author: lifujun@alibaba-inc.com
 * @Date: 2016/12/29.
 * @Description:
 */

public class DemoApplication extends Application {
    public static final String DEFAULT_TEST_URL_LIVE = "rtmp://live.hkstv.hk.lxdns.com/live/hks";
    public static final String DEFAULT_TEST_URL = "http://resource-s3.lycam.tv/apps/KQWCUPAHR3/db4ded30-b0a1-11e7-aee5-25404b4da7c9/streams/dev-f31cebf1-c5b9-11e7-8270-61eef27bc007/hls/vod.m3u8";

    @Override
    public void onCreate() {
        super.onCreate();

        VcPlayerLog.enableLog();
        initLeakcanary();//初始化内存检测


        //初始化播放器
        AliVcMediaPlayer.init(getApplicationContext());

        //设置保存密码。此密码如果更换，则之前保存的视频无法播放
//        AliyunDownloadConfig config = new AliyunDownloadConfig();
//        config.setSecretImagePath(Environment.getExternalStorageDirectory().getAbsolutePath()+"/aliyun/encryptedApp.dat");
////        config.setDownloadPassword("123456789");
//        //设置保存路径。请确保有SD卡访问权限。
//        config.setDownloadDir(Environment.getExternalStorageDirectory().getAbsolutePath()+"/test_save/");
//        //设置同时下载个数
//        config.setMaxNums(2);
//
//        AliyunDownloadManager.getInstance(this).setDownloadConfig(config);

    }


    private void initLeakcanary() {
    }
}
