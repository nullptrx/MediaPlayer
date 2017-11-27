/*
 * Copyright (C) 2015 Zhang Rui <bbcallen@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tv.lycam.alivc.utils;

import android.graphics.SurfaceTexture;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;

import com.alivc.player.AliVcMediaPlayer;

/**
 * 视频渲染view
 */
public interface IRenderView {
    /**
     * 1. PlayStateParams.fitParent:可能会剪裁,保持原视频的大小，显示在中心,当原视频的大小超过view的大小超过部分裁剪处理
     * 2. PlayStateParams.fillParent:可能会剪裁,等比例放大视频，直到填满View为止,超过View的部分作裁剪处理
     * 3. PlayStateParams.wrapcontent:将视频的内容完整居中显示，如果视频大于view,则按比例缩视频直到完全显示在view中
     * 4. PlayStateParams.fitXY:不剪裁,非等比例拉伸画面填满整个View
     * 5. PlayStateParams.f16_9:不剪裁,非等比例拉伸画面到16:9,并完全显示在View中
     * 6. PlayStateParams.f4_3:不剪裁,非等比例拉伸画面到4:3,并完全显示在View中
     */
    int AR_ASPECT_FIT_PARENT = 0; // without clip
    int AR_ASPECT_FILL_PARENT = 1; // may clip
    int AR_ASPECT_WRAP_CONTENT = 2;
    int AR_MATCH_PARENT = 3;
    int AR_16_9_FIT_PARENT = 4;
    int AR_4_3_FIT_PARENT = 5;

    /**
     * 获取外层界面
     */
    View getView();

    /**
     * 是否需要等待重置大小
     */
    boolean shouldWaitForResize();

    /**
     * 设置视频界面大小
     */
    void setVideoSize(int videoWidth, int videoHeight);

    /**
     * 设置视频裁剪方式
     */
    void setVideoSampleAspectRatio(int videoSarNum, int videoSarDen);

    /**
     * 设置视频旋转角度
     */
    void setVideoRotation(int degree);

    /**
     * 设置视频裁剪方式
     */
    void setAspectRatio(int aspectRatio);

    /**
     * 添加视频渲染回调
     */
    void addRenderCallback(@NonNull IRenderCallback callback);

    /**
     * 移除视频渲染回调
     */
    void removeRenderCallback(@NonNull IRenderCallback callback);

    interface ISurfaceHolder {

        /**
         * surface界面绑定到mediaplay上
         */
        void bindToMediaPlayer(AliVcMediaPlayer mp);

        /**
         * 获取渲染的view
         */
        @NonNull
        IRenderView getRenderView();

        /**
         * 获取渲染使用的具体view surface
         */
        @Nullable
        SurfaceHolder getSurfaceHolder();

        /**
         * 打开surface界面
         */
        @Nullable
        Surface openSurface();

        /**
         * 获取渲染使用的具体view texture
         */
        @Nullable
        SurfaceTexture getSurfaceTexture();
    }

    interface IRenderCallback {
        /**
         * 创建surface界面大小
         *
         * @param holder
         * @param width  could be 0
         * @param height could be 0
         */
        void onSurfaceCreated(@NonNull ISurfaceHolder holder, int width, int height);

        /**
         * surface界面大小改变监听
         *
         * @param holder
         * @param format could be 0
         * @param width
         * @param height
         */
        void onSurfaceChanged(@NonNull ISurfaceHolder holder, int format, int width, int height);

        /**
         * 界面回收
         */
        void onSurfaceDestroyed(@NonNull ISurfaceHolder holder);

        void onSurfaceTextureUpdated();
    }
}
