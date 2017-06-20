package rx_playground.com.jablonski.cameracomponentlib.view.api;

import android.app.Activity;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

import rx_playground.com.jablonski.cameracomponentlib.view.interfaces.ImageResultCallback;

/**
 * Created by yabol on 17.06.2017.
 */

public class CameraOldApi implements CameraAPI, SurfaceHolder.Callback {
    private Camera camera;
    private boolean frontCamera = false;
    private int currentCamera;
    private SurfaceView cameraPreview;
    private SurfaceHolder holder;
    private Activity activity;
    private ImageResultCallback callback;

    public CameraOldApi(Activity activity, SurfaceView cameraPreview){
        this.activity = activity;
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
        if(this.camera != null && holder != null){
            try {
                this.camera.setPreviewDisplay(this.holder);
                this.camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stopCamera() {
        if(this.camera != null){
            this.camera.stopPreview();
            this.camera.release();
            this.camera = null;
        }
    }

    @Override
    public void changeCamera() {
        stopCamera();
        this.currentCamera = getCameraId();
        startCamera(cameraPreview.getWidth(), cameraPreview.getHeight());
    }

    @Override
    public void setImageCaptureListener(ImageResultCallback callback) {
        this.callback = callback;
    }

    private void setUpCamera(){
        this.camera = getCameraInstance(this.currentCamera);
        this.camera.setDisplayOrientation(getCameraOrientationDegrees());

    }

    private int getCameraId(){
        if(frontCamera){
            frontCamera = false;
            return getBackCameraId();
        }else{
            frontCamera = true;
            return getFrontCameraId();
        }
    }

    private int getBackCameraId(){
        int numberOfCameras = Camera.getNumberOfCameras();
        for(int i = 0; i < numberOfCameras; i++){
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if(info.facing == Camera.CameraInfo.CAMERA_FACING_BACK){
                return i;
            }
        }
        return -1;
    }

    private int getFrontCameraId(){
        int numberOfCameras = Camera.getNumberOfCameras();
        for(int i = 0; i < numberOfCameras; i++){
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if(info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
                return i;
            }
        }
        return -1;
    }

    private int getCameraOrientationDegrees(){
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(this.currentCamera, info);
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
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
