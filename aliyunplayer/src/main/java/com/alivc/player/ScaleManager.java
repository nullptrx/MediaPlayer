//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.alivc.player;

import android.graphics.Matrix;

public class ScaleManager {
    private Size mViewSize;
    private Size mVideoSize;

    public ScaleManager(Size viewSize, Size videoSize) {
        this.mViewSize = viewSize;
        this.mVideoSize = videoSize;
    }

    public Matrix getScaleMatrix(ScalableType scalableType) {
        if (scalableType == null) {
            return null;
        }
        switch (scalableType.ordinal()) {
            case 1:
                return this.getNoScale();
            case 2:
                return this.fitXY();
            case 3:
                return this.fitCenter();
            case 4:
                return this.fitStart();
            case 5:
                return this.fitEnd();
            case 6:
                return this.getOriginalScale(PivotPoint.LEFT_TOP);
            case 7:
                return this.getOriginalScale(PivotPoint.LEFT_CENTER);
            case 8:
                return this.getOriginalScale(PivotPoint.LEFT_BOTTOM);
            case 9:
                return this.getOriginalScale(PivotPoint.CENTER_TOP);
            case 10:
                return this.getOriginalScale(PivotPoint.CENTER);
            case 11:
                return this.getOriginalScale(PivotPoint.CENTER_BOTTOM);
            case 12:
                return this.getOriginalScale(PivotPoint.RIGHT_TOP);
            case 13:
                return this.getOriginalScale(PivotPoint.RIGHT_CENTER);
            case 14:
                return this.getOriginalScale(PivotPoint.RIGHT_BOTTOM);
            case 15:
                return this.getCropScale(PivotPoint.LEFT_TOP);
            case 16:
                return this.getCropScale(PivotPoint.LEFT_CENTER);
            case 17:
                return this.getCropScale(PivotPoint.LEFT_BOTTOM);
            case 18:
                return this.getCropScale(PivotPoint.CENTER_TOP);
            case 19:
                return this.getCropScale(PivotPoint.CENTER);
            case 20:
                return this.getCropScale(PivotPoint.CENTER_BOTTOM);
            case 21:
                return this.getCropScale(PivotPoint.RIGHT_TOP);
            case 22:
                return this.getCropScale(PivotPoint.RIGHT_CENTER);
            case 23:
                return this.getCropScale(PivotPoint.RIGHT_BOTTOM);
            case 24:
                return this.startInside();
            case 25:
                return this.centerInside();
            case 26:
                return this.endInside();
            default:
                return null;
        }
    }

    private Matrix getMatrix(float sx, float sy, float px, float py) {
        Matrix matrix = new Matrix();
        matrix.setScale(sx, sy, px, py);
        return matrix;
    }

    private Matrix getMatrix(float sx, float sy, PivotPoint pivotPoint) {
        if (pivotPoint == null) {
            return null;
        }
        switch (pivotPoint.ordinal()) {
            case 1:
                return this.getMatrix(sx, sy, 0.0F, 0.0F);
            case 2:
                return this.getMatrix(sx, sy, 0.0F, (float) this.mViewSize.getHeight() / 2.0F);
            case 3:
                return this.getMatrix(sx, sy, 0.0F, (float) this.mViewSize.getHeight());
            case 4:
                return this.getMatrix(sx, sy, (float) this.mViewSize.getWidth() / 2.0F, 0.0F);
            case 5:
                return this.getMatrix(sx, sy, (float) this.mViewSize.getWidth() / 2.0F, (float) this.mViewSize.getHeight() / 2.0F);
            case 6:
                return this.getMatrix(sx, sy, (float) this.mViewSize.getWidth() / 2.0F, (float) this.mViewSize.getHeight());
            case 7:
                return this.getMatrix(sx, sy, (float) this.mViewSize.getWidth(), 0.0F);
            case 8:
                return this.getMatrix(sx, sy, (float) this.mViewSize.getWidth(), (float) this.mViewSize.getHeight() / 2.0F);
            case 9:
                return this.getMatrix(sx, sy, (float) this.mViewSize.getWidth(), (float) this.mViewSize.getHeight());
            default:
                throw new IllegalArgumentException("Illegal PivotPoint");
        }
    }

    private Matrix getNoScale() {
        float sx = (float) this.mVideoSize.getWidth() / (float) this.mViewSize.getWidth();
        float sy = (float) this.mVideoSize.getHeight() / (float) this.mViewSize.getHeight();
        return this.getMatrix(sx, sy, PivotPoint.LEFT_TOP);
    }

    private Matrix getFitScale(PivotPoint pivotPoint) {
        float sx = (float) this.mViewSize.getWidth() / (float) this.mVideoSize.getWidth();
        float sy = (float) this.mViewSize.getHeight() / (float) this.mVideoSize.getHeight();
        float minScale = Math.min(sx, sy);
        sx = minScale / sx;
        sy = minScale / sy;
        return this.getMatrix(sx, sy, pivotPoint);
    }

    private Matrix fitXY() {
        return this.getMatrix(1.0F, 1.0F, PivotPoint.LEFT_TOP);
    }

    private Matrix fitStart() {
        return this.getFitScale(PivotPoint.LEFT_TOP);
    }

    private Matrix fitCenter() {
        return this.getFitScale(PivotPoint.CENTER);
    }

    private Matrix fitEnd() {
        return this.getFitScale(PivotPoint.RIGHT_BOTTOM);
    }

    private Matrix getOriginalScale(PivotPoint pivotPoint) {
        float sx = (float) this.mVideoSize.getWidth() / (float) this.mViewSize.getWidth();
        float sy = (float) this.mVideoSize.getHeight() / (float) this.mViewSize.getHeight();
        return this.getMatrix(sx, sy, pivotPoint);
    }

    private Matrix getCropScale(PivotPoint pivotPoint) {
        float sx = (float) this.mViewSize.getWidth() / (float) this.mVideoSize.getWidth();
        float sy = (float) this.mViewSize.getHeight() / (float) this.mVideoSize.getHeight();
        float maxScale = Math.max(sx, sy);
        sx = maxScale / sx;
        sy = maxScale / sy;
        return this.getMatrix(sx, sy, pivotPoint);
    }

    private Matrix startInside() {
        return this.mVideoSize.getHeight() <= this.mViewSize.getWidth() && this.mVideoSize.getHeight() <= this.mViewSize.getHeight() ? this.getOriginalScale(PivotPoint.LEFT_TOP) : this.fitStart();
    }

    private Matrix centerInside() {
        return this.mVideoSize.getHeight() <= this.mViewSize.getWidth() && this.mVideoSize.getHeight() <= this.mViewSize.getHeight() ? this.getOriginalScale(PivotPoint.CENTER) : this.fitCenter();
    }

    private Matrix endInside() {
        return this.mVideoSize.getHeight() <= this.mViewSize.getWidth() && this.mVideoSize.getHeight() <= this.mViewSize.getHeight() ? this.getOriginalScale(PivotPoint.RIGHT_BOTTOM) : this.fitEnd();
    }
}
