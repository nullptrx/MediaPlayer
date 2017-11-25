package tv.lycam.media.basic;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alivc.player.AliVcMediaPlayer;
import com.alivc.player.MediaPlayer;
import com.aliyun.vodplayerview.utils.NetWatchdog;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import tv.lycam.media.BaseAppCompatActivity;
import tv.lycam.media.R;

/**
 * Live demo
 */
public class LiveModeActivity extends BaseAppCompatActivity {

    private static final String TAG = LiveModeActivity.class.getSimpleName();

    private SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SS");

    private SurfaceView mSurfaceView;

    private Button playBtn;
    private Button stopBtn;


    private CheckBox muteOnBtn;

    private RadioGroup scaleModeGroup;
    private RadioButton scaleModeFit;
    private RadioButton scaleModeFill;


    private SeekBar brightnessBar;
    private SeekBar volumeBar;

    private TextView videoWidthTxt;
    private TextView videoHeightTxt;

    private EditText maxBufDurationEdit;

    private boolean mMute = false;
    private List<String> logStrs = new ArrayList<>();

    private AliVcMediaPlayer mPlayer;

    private boolean isCompleted = false;

    private String mUrl = null;
    private MyTextWatcher watcher = null;

    private NetWatchdog netWatchdog;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_log, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.log) {

            LayoutInflater inflater = LayoutInflater.from(this);
            View view = inflater.inflate(R.layout.view_log, null);

            TextView textview = (TextView) view.findViewById(R.id.log);
            if (mPlayer != null) {
                for (String log : logStrs) {
                    textview.append("     " + log + "\n");
                }
            }
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle(getString(R.string.player_log));
            alertDialog.setView(view);
            alertDialog.setPositiveButton("OK", null);
            AlertDialog alert = alertDialog.create();
            alert.show();
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_live_mode);

        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        playBtn = (Button) findViewById(R.id.play);
        stopBtn = (Button) findViewById(R.id.stop);

        //不可见的view们
        findViewById(R.id.pause).setVisibility(View.GONE);
        findViewById(R.id.replay).setVisibility(View.GONE);
        findViewById(R.id.snapshot).setVisibility(View.GONE);
        findViewById(R.id.progress_layout).setVisibility(View.GONE);
        findViewById(R.id.auto_play_layout).setVisibility(View.GONE);
        findViewById(R.id.change_quality_layout).setVisibility(View.GONE);
        findViewById(R.id.speed_layout).setVisibility(View.GONE);


        muteOnBtn = (CheckBox) findViewById(R.id.muteOn);

        muteOnBtn.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    mMute = true;
                    if (mPlayer != null) {
                        mPlayer.setMuteMode(mMute);
                    }
                    volumeBar.setProgress(0);
                } else{
                    mMute = false;
                    if (mPlayer != null) {
                        mPlayer.setMuteMode(mMute);
                    }
                    volumeBar.setProgress(mPlayer.getVolume());
                }
            }

        });

        scaleModeGroup = (RadioGroup) findViewById(R.id.scalingMode);
        scaleModeFit = (RadioButton) findViewById(R.id.fit);
        scaleModeFill = (RadioButton) findViewById(R.id.fill);

        scaleModeFit.setChecked(true);

        scaleModeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                if (checkedId == scaleModeFit.getId()) {
                    if (mPlayer != null) {
                        mPlayer.setVideoScalingMode(MediaPlayer.VideoScalingMode.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                    }
                } else if (checkedId == scaleModeFill.getId()) {
                    if (mPlayer != null) {
                        mPlayer.setVideoScalingMode(MediaPlayer.VideoScalingMode.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                    }
                }
            }
        });


        brightnessBar = (SeekBar) findViewById(R.id.brightnessProgress);
        volumeBar = (SeekBar) findViewById(R.id.volumeProgress);

        videoWidthTxt = (TextView) findViewById(R.id.width);
        videoHeightTxt = (TextView) findViewById(R.id.height);
        maxBufDurationEdit = (EditText) findViewById(R.id.max_buff_duration);

        watcher = new MyTextWatcher(this);


        maxBufDurationEdit.addTextChangedListener(watcher);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logStrs.add(format.format(new Date()) + getString(R.string.log_start_get_data));

                setMaxBufferDuration();

                replay();

                if (mMute) {
                    mPlayer.setMuteMode(mMute);
                }
                brightnessBar.setProgress(mPlayer.getScreenBrightness());
                logStrs.add(format.format(new Date()) + getString(R.string.log_strart_play));
                volumeBar.setProgress(mPlayer.getVolume());
            }
        });
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
            }
        });

        brightnessBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser && mPlayer != null) {
                    mPlayer.setScreenBrightness(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser && mPlayer != null) {
                    mPlayer.setVolume(progress);
                    muteOnBtn.setChecked(false);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            public void surfaceCreated(SurfaceHolder holder) {
                holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
                holder.setKeepScreenOn(true);
                Log.d(TAG, "AlivcPlayer onSurfaceCreated." + mPlayer);

                // Important: surfaceView changed from background to front, we need reset surface to mediaplayer.
                // 对于从后台切换到前台,需要重设surface;部分手机锁屏也会做前后台切换的处理
                if (mPlayer != null) {
                    mPlayer.setVideoSurface(mSurfaceView.getHolder().getSurface());
                }

                Log.d(TAG, "AlivcPlayeron SurfaceCreated over.");
            }

            public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
                Log.d(TAG, "onSurfaceChanged is valid ? " + holder.getSurface().isValid());
                if (mPlayer != null)
                    mPlayer.setSurfaceChanged();
            }

            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d(TAG, "onSurfaceDestroy.");
            }
        });

        initVodPlayer();
        setPlaySource();
        netWatchdog = new NetWatchdog(this);
        netWatchdog.setNetChangeListener(new NetWatchdog.NetChangeListener() {
            @Override
            public void onWifiTo4G() {
                if(mPlayer.isPlaying()) {
                    pause();
                }
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(LiveModeActivity.this);
                alertDialog.setTitle(getString(R.string.net_change_to_4g));
                alertDialog.setMessage(getString(R.string.net_change_to_continue));
                alertDialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        start();
                    }
                });
                alertDialog.setNegativeButton(getString(R.string.no),null);
                AlertDialog alert = alertDialog.create();
                alert.show();

                Toast.makeText(LiveModeActivity.this.getApplicationContext(), R.string.net_change_to_4g, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void on4GToWifi() {
                Toast.makeText(LiveModeActivity.this.getApplicationContext(), R.string.net_change_to_wifi, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onNetDisconnected() {
                Toast.makeText(LiveModeActivity.this.getApplicationContext(), R.string.net_disconnect, Toast.LENGTH_SHORT).show();
            }
        });
        netWatchdog.startWatch();
    }

    private class MyTextWatcher implements TextWatcher {

        private WeakReference<LiveModeActivity> liveModeActivityWeakReference;

        public MyTextWatcher(LiveModeActivity liveModeActivity) {
            liveModeActivityWeakReference = new WeakReference<LiveModeActivity>(liveModeActivity);
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            LiveModeActivity liveModeActivity = liveModeActivityWeakReference.get();
            if (liveModeActivity != null) {
                liveModeActivity.setMaxBufferDuration();
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }

    private void setMaxBufferDuration() {
        String maxBufferDurationStr = maxBufDurationEdit.getText().toString();
        int maxBD = 0;
        try {
            maxBD = Integer.valueOf(maxBufferDurationStr);

        } catch (Exception e) {
            maxBufDurationEdit.setText("0");
        }
        if (maxBD < 0) {
            Toast.makeText(getApplicationContext(), R.string.max_buffer_duration_nagtive, Toast.LENGTH_SHORT).show();
            return;
        }
        if (mPlayer != null) {
            mPlayer.setMaxBufferDuration(maxBD);
        }
    }

    private void initVodPlayer() {
        mPlayer = new AliVcMediaPlayer(this, mSurfaceView);

        mPlayer.setPreparedListener(new MediaPlayer.MediaPlayerPreparedListener() {
            @Override
            public void onPrepared() {
                Toast.makeText(LiveModeActivity.this.getApplicationContext(), R.string.toast_prepare_success, Toast.LENGTH_SHORT).show();
                logStrs.add(format.format(new Date()) + getString(R.string.log_prepare_success));

                showVideoSizeInfo();
            }
        });
        mPlayer.setFrameInfoListener(new MediaPlayer.MediaPlayerFrameInfoListener() {
            @Override
            public void onFrameInfoListener() {
                Map<String, String> debugInfo = mPlayer.getAllDebugInfo();
                long createPts = 0;
                if (debugInfo.get("create_player") != null) {
                    String time = debugInfo.get("create_player");
                    createPts = (long) Double.parseDouble(time);
                    logStrs.add(format.format(new Date(createPts)) + getString(R.string.log_player_create_success));
                }
                if (debugInfo.get("open-url") != null) {
                    String time = debugInfo.get("open-url");
                    long openPts = (long) Double.parseDouble(time) + createPts;
                    logStrs.add(format.format(new Date(openPts)) + getString(R.string.log_open_url_success));
                }
                if (debugInfo.get("find-stream") != null) {
                    String time = debugInfo.get("find-stream");
                    Log.d(TAG + "lfj0914", "find-Stream time =" + time + " , createpts = " + createPts);
                    long findPts = (long) Double.parseDouble(time) + createPts;
                    logStrs.add(format.format(new Date(findPts)) + getString(R.string.log_request_stream_success));
                }
                if (debugInfo.get("open-stream") != null) {
                    String time = debugInfo.get("open-stream");
                    Log.d(TAG + "lfj0914", "open-Stream time =" + time + " , createpts = " + createPts);
                    long openPts = (long) Double.parseDouble(time) + createPts;
                    logStrs.add(format.format(new Date(openPts)) + getString(R.string.log_start_open_stream));
                }
                logStrs.add(format.format(new Date()) + getString(R.string.log_first_frame_played));
            }
        });
        mPlayer.setErrorListener(new MediaPlayer.MediaPlayerErrorListener() {
            @Override
            public void onError(int i, String msg) {
                Toast.makeText(LiveModeActivity.this.getApplicationContext(), getString(R.string.toast_fail_msg) + msg, Toast.LENGTH_SHORT).show();
            }
        });
        mPlayer.setCompletedListener(new MediaPlayer.MediaPlayerCompletedListener() {
            @Override
            public void onCompleted() {
                Log.d(TAG, "onCompleted--- ");
                isCompleted = true;
            }
        });
        mPlayer.setSeekCompleteListener(new MediaPlayer.MediaPlayerSeekCompleteListener() {
            @Override
            public void onSeekCompleted() {
                logStrs.add(format.format(new Date()) + getString(R.string.log_seek_completed));
            }
        });
        mPlayer.setStoppedListener(new MediaPlayer.MediaPlayerStoppedListener() {
            @Override
            public void onStopped() {
                logStrs.add(format.format(new Date()) + getString(R.string.log_play_stopped));
            }
        });
        mPlayer.setBufferingUpdateListener(new MediaPlayer.MediaPlayerBufferingUpdateListener() {
            @Override
            public void onBufferingUpdateListener(int percent) {
                Log.d(TAG, "onBufferingUpdateListener--- " + percent);
            }
        });
        mPlayer.enableNativeLog();

    }

    private void setPlaySource() {
        mUrl = getIntent().getStringExtra("url");
    }

    private void showVideoSizeInfo() {
        videoWidthTxt.setText(getString(R.string.video_width) + mPlayer.getVideoWidth() + " , ");
        videoHeightTxt.setText(getString(R.string.video_height) + mPlayer.getVideoHeight() + "   ");
    }


    private void start() {

        if (mPlayer != null) {
            mPlayer.prepareAndPlay(mUrl);
        }
    }

    private void pause() {
        if (mPlayer != null) {
            mPlayer.pause();
        }
    }

    private void stop() {
        if (mPlayer != null) {
            mPlayer.stop();
        }
    }

    private void resume() {
        if (mPlayer != null) {
            mPlayer.play();
        }
    }

    private void destroy() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.destroy();
        }
    }

    private void replay() {
        stop();
        start();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop() {
        super.onStop();
        //when view goto background,will pausethe player, so we save the player's status here,
        // and when activity resumed, we resume the player's status.
        savePlayerState();
    }

    private void savePlayerState() {
        if (mPlayer.isPlaying()) {
            //we pause the player for not playing on the background
            // 不可见，暂停播放器
            pause();
        }
    }

    @Override
    protected void onDestroy() {
        stop();
        destroy();

        netWatchdog.stopWatch();
        maxBufDurationEdit.removeTextChangedListener(watcher);

        super.onDestroy();
    }

}
