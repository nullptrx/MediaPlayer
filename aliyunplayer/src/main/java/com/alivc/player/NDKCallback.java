package com.alivc.player;

import android.media.AudioTrack;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Build.VERSION;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class NDKCallback {
    private static Map<Integer, IPlayingHandler> mHandlerMap = new HashMap();
    private static Map<Integer, Boolean> sAudioStatusMap = new HashMap();
    private static AudioTrack sAudioTrack = null;
    private static Map<Integer, AudioTrack> sAudioTrackMap = new HashMap();
    private static Map<Integer, Float> sAudioVolumn = new HashMap();
    private static int sDecodeType = 0;
    private static float sDefaultVolumn = 0.5f;
    private static Map<String, Integer> sKnownCodecList;
    private static Map<String, Integer> sKnownLeastSDKList;

    public static synchronized Map<String, Integer> getKnownCodecList() {
        Map<String, Integer> map;
        synchronized (NDKCallback.class) {
            if (sKnownCodecList != null) {
                map = sKnownCodecList;
            } else {
                sKnownCodecList = new TreeMap(String.CASE_INSENSITIVE_ORDER);
                sKnownCodecList.put("OMX.Nvidia.h264.decode", Integer.valueOf(RankConst.RANK_TESTED));
                sKnownCodecList.put("OMX.Nvidia.h264.decode.secure", Integer.valueOf(RankConst.RANK_SECURE));
                sKnownCodecList.put("OMX.Intel.hw_vd.h264", Integer.valueOf(801));
                sKnownCodecList.put("OMX.Intel.VideoDecoder.AVC", Integer.valueOf(RankConst.RANK_TESTED));
                sKnownCodecList.put("OMX.qcom.video..avc", Integer.valueOf(RankConst.RANK_TESTED));
                sKnownCodecList.put("OMX.ittiam.video.decoder.avc", Integer.valueOf(0));
                sKnownCodecList.put("OMX.SEC.avc.dec", Integer.valueOf(RankConst.RANK_TESTED));
                sKnownCodecList.put("OMX.SEC.AVC.Decoder", Integer.valueOf(799));
                sKnownCodecList.put("OMX.SEC.avcdec", Integer.valueOf(798));
                sKnownCodecList.put("OMX.SEC.avc.sw.dec", Integer.valueOf(RankConst.RANK_SOFTWARE));
                sKnownCodecList.put("OMX.Exynos.avc.dec", Integer.valueOf(RankConst.RANK_TESTED));
                sKnownCodecList.put("OMX.Exynos.AVC.Decoder", Integer.valueOf(799));
                sKnownCodecList.put("OMX.k3.video.decoder.avc", Integer.valueOf(RankConst.RANK_TESTED));
                sKnownCodecList.put("OMX.IMG.MSVDX.Decoder.AVC", Integer.valueOf(RankConst.RANK_TESTED));
                sKnownCodecList.put("OMX.TI.DUCATI1.VIDEO.DECODER", Integer.valueOf(RankConst.RANK_TESTED));
                sKnownCodecList.put("OMX.rk.video_decoder.avc", Integer.valueOf(RankConst.RANK_TESTED));
                sKnownCodecList.put("OMX.amlogic.avc.decoder.awesome", Integer.valueOf(RankConst.RANK_TESTED));
                sKnownCodecList.put("OMX.MARVELL.VIDEO.HW.CODA7542DECODER", Integer.valueOf(RankConst.RANK_TESTED));
                sKnownCodecList.put("OMX.MARVELL.VIDEO.H264DECODER", Integer.valueOf(RankConst.RANK_SOFTWARE));
                sKnownCodecList.remove("OMX.BRCM.vc4.decoder.avc");
                sKnownCodecList.remove("OMX.brcm.video.h264.hw.decoder");
                sKnownCodecList.remove("OMX.brcm.video.h264.decoder");
                sKnownCodecList.remove("OMX.ST.VFM.H264Dec");
                sKnownCodecList.remove("OMX.allwinner.video.decoder.avc");
                sKnownCodecList.remove("OMX.MS.AVC.Decoder");
                sKnownCodecList.remove("OMX.hantro.81x0.video.decoder");
                sKnownCodecList.remove("OMX.hisi.video.decoder");
                sKnownCodecList.remove("OMX.cosmo.video.decoder.avc");
                sKnownCodecList.remove("OMX.duos.h264.decoder");
                sKnownCodecList.remove("OMX.bluestacks.hw.decoder");
                sKnownCodecList.put("OMX.google.h264.decoder", Integer.valueOf(RankConst.RANK_SOFTWARE));
                sKnownCodecList.put("OMX.google.h264.lc.decoder", Integer.valueOf(RankConst.RANK_SOFTWARE));
                sKnownCodecList.put("OMX.k3.ffmpeg.decoder", Integer.valueOf(RankConst.RANK_SOFTWARE));
                sKnownCodecList.put("OMX.ffmpeg.video.decoder", Integer.valueOf(RankConst.RANK_SOFTWARE));
                map = sKnownCodecList;
            }
        }
        return map;
    }

    public static void setPlayingHandler(int playerId, IPlayingHandler h) {
        mHandlerMap.put(Integer.valueOf(playerId), h);
    }

    public static void removePlayingHandler(int playerId) {
        mHandlerMap.remove(Integer.valueOf(playerId));
    }

    public static int onNotification(int playerId, int what, int arg0, int arg1, String obj_id) {
        IPlayingHandler mHandler;
        if (what != 4 || arg1 == 6) {
            mHandler = (IPlayingHandler) mHandlerMap.get(Integer.valueOf(playerId));
        } else {
            mHandler = (IPlayingHandler) mHandlerMap.get(Integer.valueOf(playerId));
        }
        if (mHandler != null) {
            return mHandler.onStatus(what, arg0, arg1, obj_id);
        }
        VcPlayerLog.d("MPlayer", "not find handle. " + playerId);
        return -1;
    }

    public static int onDataNotification(int playerId, int what, int arg0, int arg1, byte[] data) {
        IPlayingHandler mHandler = (IPlayingHandler) mHandlerMap.get(Integer.valueOf(playerId));
        if (mHandler != null) {
            return mHandler.onData(what, arg0, arg1, data);
        }
        VcPlayerLog.d("MPlayer", "not find handle. " + playerId);
        return -1;
    }

    public static int getAndroidVersion() {
        return VERSION.SDK_INT;
    }

    private static AudioTrack getAudioTrack(int audioPlayerId) {
        if (sAudioTrackMap.containsKey(Integer.valueOf(audioPlayerId))) {
            return (AudioTrack) sAudioTrackMap.get(Integer.valueOf(audioPlayerId));
        }
        return null;
    }

    private static Boolean getAudioPlayingStatus(int audioPlayerId) {
        boolean status = false;
        if (sAudioStatusMap.containsKey(Integer.valueOf(audioPlayerId))) {
            status = ((Boolean) sAudioStatusMap.get(Integer.valueOf(audioPlayerId))).booleanValue();
        }
        return Boolean.valueOf(status);
    }

    private static void setAudioPlayingStatus(int audioPlayerId, boolean status) {
        sAudioStatusMap.put(Integer.valueOf(audioPlayerId), Boolean.valueOf(status));
    }

    public static int audioInit(int audioPlayerId, int sampleRate, boolean is16Bit, boolean isStereo, int desired_buf_size) {
        int i;
        int i2;
        int channelConfig = isStereo ? 12 : 4;
        int audioFormat = is16Bit ? 2 : 3;
        if (isStereo) {
            i = 2;
        } else {
            i = 1;
        }
        if (is16Bit) {
            i2 = 2;
        } else {
            i2 = 1;
        }
        int frameSize = i * i2;
        desired_buf_size = Math.max(desired_buf_size, AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat));
        VcPlayerLog.e("lfj0926", "to new desired_buf_size :" + desired_buf_size);
        AudioTrack audioTrack = getAudioTrack(audioPlayerId);
        if (audioTrack == null) {
            VcPlayerLog.e("Audio", "to new audioTrack :" + audioPlayerId + ", size :" + sAudioTrackMap.size());
            setAudioPlayingStatus(audioPlayerId, false);
            AudioTrack audioTrack2;
            try {
                VcPlayerLog.e("Audio", "to new audioTrack sampleRate:" + sampleRate + ", channelConfig :" + channelConfig + " , audioFormat = " + audioFormat + " , desired_buf_size = " + desired_buf_size);
                audioTrack2 = new AudioTrack(3, sampleRate, channelConfig, audioFormat, desired_buf_size, 1);
                audioTrack2.setStereoVolume(sDefaultVolumn, sDefaultVolumn);
                sAudioTrack = audioTrack2;
                sAudioTrackMap.put(Integer.valueOf(audioPlayerId), audioTrack2);
                if (audioTrack2.getState() == 1) {
                    return desired_buf_size;
                }
                VcPlayerLog.e("Audio", "NDKCallback Failed during initialization of Audio Track");
                return -1;
            } catch (Exception e) {
                VcPlayerLog.e("Audio", "to new audioTrack Exception :" + e.getMessage());
                audioTrack2 = audioTrack;
                return -1;
            }
        }
        return desired_buf_size;
    }

    public static int audioPause(int audioPlayerId) {
        AudioTrack audioTrack = getAudioTrack(audioPlayerId);
        if (audioTrack != null && getAudioPlayingStatus(audioPlayerId).booleanValue()) {
            setAudioPlayingStatus(audioPlayerId, false);
            try {
                audioTrack.pause();
            } catch (IllegalStateException e) {
                VcPlayerLog.w("Audio", "IllegalStateException .. " + e.getMessage());
            }
        }
        return 0;
    }

    public static int audioStart(int audioPlayerId) {
        AudioTrack audioTrack = getAudioTrack(audioPlayerId);
        if (!(audioTrack == null || getAudioPlayingStatus(audioPlayerId).booleanValue())) {
            setAudioPlayingStatus(audioPlayerId, true);
            if (audioTrack.getState() != 1) {
                VcPlayerLog.e("Audio", "NDKCallback Failed during initialization of Audio Track");
                return -1;
            }
            try {
                audioTrack.play();
            } catch (IllegalStateException e) {
                VcPlayerLog.w("Audio", "IllegalStateException .. " + e.getMessage());
            }
        }
        return 0;
    }

    public static int audioStop(int audioPlayerId) {
        VcPlayerLog.w("Audio", "audioStop :" + audioPlayerId);
        AudioTrack audioTrack = getAudioTrack(audioPlayerId);
        if (audioTrack != null) {
            if (audioTrack != null) {
                try {
                    if (getAudioPlayingStatus(audioPlayerId).booleanValue()) {
                        audioTrack.flush();
                        audioTrack.stop();
                    }
                } catch (IllegalStateException e) {
                    VcPlayerLog.w("Audio", "IllegalStateException .. " + e.getMessage());
                }
            }
            audioTrack.release();
            sAudioTrackMap.remove(Integer.valueOf(audioPlayerId));
            setAudioPlayingStatus(audioPlayerId, false);
        }
        return 0;
    }

    public static int audioFlush(int audioPlayerId) {
        return 0;
    }

    public static void audioWriteData(int audioPlayerId, byte[] buffer, int size) {
        AudioTrack audioTrack = getAudioTrack(audioPlayerId);
        if (audioTrack != null) {
            if (buffer == null) {
                VcPlayerLog.w("Audio", "NDKCallback audio: buffer = NULL");
                return;
            }
            int i = 0;
            while (i < size) {
                if (audioTrack != null) {
                    try {
                        int result = audioTrack.write(buffer, i, size - i);
                        if (result > 0) {
                            i += result;
                        } else if (result == 0) {
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                            }
                        } else {
                            VcPlayerLog.w("Audio", "NDKCallback audio: error return from write(byte)");
                            return;
                        }
                    } catch (Exception e2) {
                        VcPlayerLog.w("Audio", "NDKCallback audio: error :" + e2.getMessage());
                    }
                }
            }
        }
    }

    public static void setVolume(int audioPlayerId, int vol) {
        AudioTrack audioTrack = getAudioTrack(audioPlayerId);
        if (audioTrack != null) {
            audioTrack.setStereoVolume(((float) vol) / 100.0f, ((float) vol) / 100.0f);
        }
    }

    public static void saveDecoderType(int decoderType) {
        sDecodeType = decoderType;
    }

    public static int getDecoderType() {
        return sDecodeType;
    }

    public static String getCodecNameByType(String mime) {
        int num_codecs = MediaCodecList.getCodecCount();
        ArrayList<TBCodecInfo> candidateList = new ArrayList();
        for (int i = 0; i < num_codecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {
                String[] types = codecInfo.getSupportedTypes();
                if (types != null) {
                    for (String type : types) {
                        if (!TextUtils.isEmpty(type) && type.equalsIgnoreCase(mime)) {
                            candidateList.add(new TBCodecInfo(codecInfo, mime));
                        }
                    }
                }
            }
        }
        if (candidateList.isEmpty()) {
            return null;
        }
        TBCodecInfo bestCodec = (TBCodecInfo) candidateList.get(0);
        Iterator it = candidateList.iterator();
        while (it.hasNext()) {
            TBCodecInfo codec = (TBCodecInfo) it.next();
            if (codec.mRank > bestCodec.mRank) {
                bestCodec = codec;
            }
        }
        if (bestCodec.mRank >= RankConst.RANK_LAST_CHANCE) {
            return bestCodec.mCodecInfo.getName();
        }
        return null;
    }

    public static void setMuteModeOn(boolean muteModeOn) {
        if (muteModeOn) {
            sDefaultVolumn = 0.0f;
        } else {
            sDefaultVolumn = 0.5f;
        }
        if (sAudioTrack != null) {
            sAudioTrack.setStereoVolume(sDefaultVolumn, sDefaultVolumn);
        }
    }
}
