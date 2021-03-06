package rx_playground.com.jablonski.cameracomponentlib.view.managers;

import android.app.Activity;
import android.os.Build;

import rx_playground.com.jablonski.cameracomponentlib.view.AutoFitTextureView;
import rx_playground.com.jablonski.cameracomponentlib.view.api.CameraAPI21;
import rx_playground.com.jablonski.cameracomponentlib.view.api.CameraAPI;
import rx_playground.com.jablonski.cameracomponentlib.view.interfaces.ImageResultCallback;

/**
 * Created by yabol on 10.06.2017.
 */

public class CameraManager {
    private CameraAPI camera;
    private AutoFitTextureView textureView;

    public CameraManager(Activity activity, AutoFitTextureView cameraPreview){
        this.textureView = cameraPreview;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.camera = new CameraAPI21(activity, cameraPreview);
        }else{
            //todo implementation of camera for older API
        }
    }

    public void startCamera(){
        this.camera.startCamera(this.textureView.getWidth(), this.textureView.getHeight());
    }

    public void stopCamera(){
        this.camera.stopCamera();
    }

    public void changeCamera(){
        this.camera.changeCamera();
    }

    public void takePhoto(){
        this.camera.takePhoto();
    }

    public void setImageCapturedListener(ImageResultCallback callback){
        this.camera.setImageCaptureListener(callback);
    }


}
