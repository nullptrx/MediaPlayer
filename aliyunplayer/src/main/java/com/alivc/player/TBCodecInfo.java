package com.alivc.player;

import android.media.MediaCodecInfo;
import android.os.Build.VERSION;
import java.util.Locale;

/* compiled from: NDKCallback */
class TBCodecInfo {
    public MediaCodecInfo mCodecInfo;
    public String mMimeType;
    public int mRank = 0;

    public TBCodecInfo(MediaCodecInfo codecInfo, String mimeType) {
        int rank;
        String name = codecInfo.getName().toLowerCase(Locale.US);
        if (!name.startsWith("omx.")) {
            rank = 100;
        } else if (name.startsWith("omx.pv")) {
            rank = 200;
        } else if (name.startsWith("omx.google.")) {
            rank = 200;
        } else if (name.startsWith("omx.ffmpeg.")) {
            rank = 200;
        } else if (name.startsWith("omx.k3.ffmpeg.")) {
            rank = 200;
        } else if (name.startsWith("omx.avcodec.")) {
            rank = 200;
        } else if (name.startsWith("omx.ittiam.")) {
            rank = 0;
        } else if (!name.startsWith("omx.mtk.")) {
            Integer knownRank = (Integer) NDKCallback.getKnownCodecList().get(name);
            if (knownRank != null) {
                rank = knownRank.intValue();
            } else {
                try {
                    if (codecInfo.getCapabilitiesForType(mimeType) != null) {
                        rank = 700;
                    } else {
                        rank = 600;
                    }
                } catch (Throwable th) {
                    rank = 600;
                }
            }
        } else if (VERSION.SDK_INT < 18) {
            rank = 0;
        } else {
            rank = 800;
        }
        this.mCodecInfo = codecInfo;
        this.mRank = rank;
        this.mMimeType = mimeType;
    }
}
