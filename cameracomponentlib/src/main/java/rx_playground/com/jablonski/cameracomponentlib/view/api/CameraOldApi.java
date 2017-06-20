package rx_playground.com.jablonski.cameracomponentlib.view.api;

import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import java.io.IOException;

import rx_playground.com.jablonski.cameracomponentlib.view.AutoFitTextureView;
import rx_playground.com.jablonski.cameracomponentlib.view.interfaces.ImageResultCallback;

/**
 * Created by yabol on 17.06.2017.
 */

public class CameraOldApi implements CameraAPI, SurfaceHolder.Callback {
    public static final int BACK_CAMERA = 0;
    public static final int FRONT_CAMERA = 1;
    private Camera camera;
    private int currentCamera = BACK_CAMERA;
    private SurfaceView cameraPreview;
    private SurfaceHolder holder;
    private ImageResultCallback callback;

    public CameraOldApi(SurfaceView cameraPreview){
        this.cameraPreview = cameraPreview;
        this.holder = this.cameraPreview.getHolder();
        this.holder.addCallback(this);
    }
    @Override
    public void takePhoto() {

    }

    @Override
    public void startCamera(int width, int height) {
        setUpCamera();
    }

    @Override
    public void stopCamera() {
        if(camera != null){
            camera.stopPreview();
            camera = null;
        }
    }

    @Override
    public void changeCamera() {
        stopCamera();
        this.currentCamera = this.currentCamera == BACK_CAMERA ? FRONT_CAMERA : BACK_CAMERA;
        startCamera(cameraPreview.getWidth(), cameraPreview.getHeight());
    }

    @Override
    public void setImageCaptureListener(ImageResultCallback callback) {
        this.callback = callback;
    }

    private void setUpCamera(){
        this.camera = getCameraInstance(this.currentCamera);

    }

    private Camera getCameraInstance(int cameraId){
        Camera camera = null;
        try {
            camera = Camera.open(cameraId);
        }catch (Exception e){
            //Camera not available according to Android documentation
            //https://developer.android.com/guide/topics/media/camera.html
        }
        return camera;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if(this.camera != null) {
                this.camera.setPreviewDisplay(holder);
                this.camera.startPreview();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if(this.holder == null) return;

        try {
            this.camera.stopPreview();
        }catch(Exception e){
            //preview not existing according to Android documentation
            //https://developer.android.com/guide/topics/media/camera.html
        }

        try {
            if(this.camera != null) {
                this.camera.setPreviewDisplay(holder);
                this.camera.startPreview();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            this.camera.stopPreview();
        }catch(Exception e){
            //preview not existing according to Android documentation
            //https://developer.android.com/guide/topics/media/camera.html
        }
    }
}
