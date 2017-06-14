package rx_playground.com.jablonski.cameracomponentlib.view.helper;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Size;
import android.view.Surface;

import rx_playground.com.jablonski.cameracomponentlib.view.CameraAPI21;

/**
 * Created by yabol on 12.06.2017.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class PreviewTransformer {
    CameraAPI21 camera;

    public PreviewTransformer(CameraAPI21 camera){
        this.camera = camera;
    }
    public void transform(int viewWidth, int viewHeight, int rotation) {
        if (this.camera.getTextureView() == null || this.camera.getPreviewSize() == null) {
            return;
        }

        Size previewSize = this.camera.getPreviewSize();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / previewSize.getHeight(),
                    (float) viewWidth / previewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        this.camera.getTextureView().setTransform(matrix);
    }

}
