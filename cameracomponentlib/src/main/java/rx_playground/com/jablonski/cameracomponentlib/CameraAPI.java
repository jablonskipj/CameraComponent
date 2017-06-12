package rx_playground.com.jablonski.cameracomponentlib;

/**
 * Created by yabol on 10.06.2017.
 */

public interface CameraAPI {
    void takePhoto();
    void startCamera(int width, int height);
    void stopCamera();
    void changeCamera();
}
