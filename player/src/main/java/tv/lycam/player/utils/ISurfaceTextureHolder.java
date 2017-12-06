package tv.lycam.player.utils;

import android.graphics.SurfaceTexture;

public interface ISurfaceTextureHolder {
    void setSurfaceTexture(SurfaceTexture var1);

    SurfaceTexture getSurfaceTexture();

    void setSurfaceTextureHost(ISurfaceTextureHost surfaceTextureHost);
}
