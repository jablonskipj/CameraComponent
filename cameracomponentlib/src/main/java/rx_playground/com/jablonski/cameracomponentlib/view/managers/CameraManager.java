package rx_playground.com.jablonski.cameracomponentlib.view.managers;

import android.app.Activity;
import android.os.Build;

import rx_playground.com.jablonski.cameracomponentlib.view.AutoFitTextureView;
import rx_playground.com.jablonski.cameracomponentlib.view.CameraComponent;
import rx_playground.com.jablonski.cameracomponentlib.view.api.CameraAPI21;
import rx_playground.com.jablonski.cameracomponentlib.view.api.CameraAPI;
import rx_playground.com.jablonski.cameracomponentlib.view.api.CameraOldApi;
import rx_playground.com.jablonski.cameracomponentlib.view.interfaces.ImageResultCallback;

/**
 * Created by yabol on 10.06.2017.
 */

public class CameraManager {
    private CameraAPI camera;
    private CameraComponent component;

    public CameraManager(Activity activity, CameraComponent component){
        this.component = component;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            this.camera = new CameraAPI21(activity, component.getNewAPiPreview());
        }else{
            this.component.setUpOldApiView();
            this.camera = new CameraOldApi(this.component.getOldApiPreview());
        }
    }

    public void startCamera(){
        this.camera.startCamera(getPreviewWidth(), getPreviewHeight());
    }

    private int getPreviewWidth(){
        int width;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            width = this.component.getNewAPiPreview().getWidth();
        }else{
            width = this.component.getOldApiPreview().getWidth();
        }
        return width;
    }
    private int getPreviewHeight(){
        int height;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            height = this.component.getNewAPiPreview().getWidth();
        }else{
            height = this.component.getOldApiPreview().getHeight();
        }
        return height;
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
