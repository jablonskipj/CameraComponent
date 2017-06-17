package rx_playground.com.jablonski.cameracomponentlib.view.api;

import rx_playground.com.jablonski.cameracomponentlib.view.interfaces.ImageResultCallback;

/**
 * Created by yabol on 10.06.2017.
 */

public interface CameraAPI {
    void takePhoto();
    void startCamera(int width, int height);
    void stopCamera();
    void changeCamera();
    void setImageCaptureListener(ImageResultCallback callback);
}
