package rx_playground.com.jablonski.cameracomponentlib;

import android.app.Activity;
import android.os.Build;

/**
 * Created by yabol on 10.06.2017.
 */

public class CameraManager implements CameraAPI{
    CameraAPI camera;

    public CameraManager(Activity activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.camera = new CameraAPI21(activity);
        }else{
            //todo implementation of camera for older API
        }
    }

    public void startCamera(){
        this.camera.startCamera();
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
}
