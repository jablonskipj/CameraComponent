package rx_playground.com.jablonski.cameracomponentlib.view;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import rx_playground.com.jablonski.cameracomponentlib.view.helper.CameraPreviewCapture;
import rx_playground.com.jablonski.cameracomponentlib.view.helper.OptimalPreviewSizeEvaluator;
import rx_playground.com.jablonski.cameracomponentlib.view.helper.PreviewTransformer;
import rx_playground.com.jablonski.cameracomponentlib.view.helper.SizeAreaComparator;
import rx_playground.com.jablonski.cameracomponentlib.view.interfaces.CameraAPI;

/**
 * Created by yabol on 10.06.2017.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraAPI21 implements CameraAPI {
    private Activity activity;
    private CameraDevice camera;
    private CameraManager manager;
    private String cameraId;
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private Semaphore cameraOpenCloseLock = new Semaphore(1);
    private TextureView cameraPreview;
    private ImageReader reader;
    private Size cameraPreviewSize;
    private boolean flashSupported;
    private int displayRotation;
    private int cameraSensorOrientation;
    private CameraPreviewCapture cameraPreviewCaptureHandler;


    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    private final CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            cameraOpenCloseLock.release();
            camera = cameraDevice;
            cameraPreviewCaptureHandler = new CameraPreviewCapture(CameraAPI21.this);
            cameraPreviewCaptureHandler.createPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraOpenCloseLock.release();
            cameraDevice.close();
            camera = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            cameraOpenCloseLock.release();
            cameraDevice.close();
            camera = null;
            if (activity != null) {
                activity.finish();
            }
        }

    };

    private final ImageReader.OnImageAvailableListener imageCapturedListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            //backgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mFile));
        }

    };

    public CameraAPI21(Activity activity, TextureView cameraPreview){
        this.activity = activity;
        this.manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        this.cameraPreview = cameraPreview;
    }
    @Override
    public void takePhoto() {
        this.cameraPreviewCaptureHandler.lockFocus();
    }

    @Override
    public void startCamera(int width, int height) {
        if(this.cameraPreview.isAvailable()){
        startBackgroundThread();
        if(ContextCompat.checkSelfPermission(this.activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

            setUpCamera(width, height);
            new PreviewTransformer(this).transform(width, height, this.displayRotation);
            try {
                if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                    throw new RuntimeException("Time out waiting to lock camera opening.");
                }
                this.manager.openCamera(this.cameraId, cameraStateCallback, backgroundHandler);
            } catch (CameraAccessException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        }else{
            setPreviewTextureListener();
        }
    }

    private void setPreviewTextureListener(){

        this.cameraPreview.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {

            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
                startCamera(width, height);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
                new PreviewTransformer(CameraAPI21.this).transform(width, height, displayRotation);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture texture) {
            }

        });

    }

    private void setUpCamera(int width, int height){
        CameraManager manager = (CameraManager) this.activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            for(String cameraId : manager.getCameraIdList()){
                configureSingleCamera(cameraId, width, height);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void configureSingleCamera(String cameraId, int width, int height) throws CameraAccessException {
        CameraCharacteristics characteristics
                = manager.getCameraCharacteristics(cameraId);

        StreamConfigurationMap map = characteristics.get(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (map == null) {
            return;
        }

        Size largest = Collections.max(
                Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                new SizeAreaComparator());

        this.reader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                ImageFormat.JPEG, 1);
        this.reader.setOnImageAvailableListener(
                this.imageCapturedListener, this.backgroundHandler);

        this.displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();


        this.cameraPreviewSize = getCameraPreviewSize(characteristics, map, largest, width, height);

        Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
        this.flashSupported = available == null ? false : available;

        this.cameraId = cameraId;
    }

    private Size getCameraPreviewSize(CameraCharacteristics characteristics, StreamConfigurationMap map, Size largest, int width, int height){

        Point displaySize = new Point();
        this.activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
        int rotatedPreviewWidth = width;
        int rotatedPreviewHeight = height;
        int maxPreviewWidth = displaySize.x;
        int maxPreviewHeight = displaySize.y;

        if (isDimensionSwapped(characteristics)) {
            rotatedPreviewWidth = height;
            rotatedPreviewHeight = width;
            maxPreviewWidth = displaySize.y;
            maxPreviewHeight = displaySize.x;
        }

        if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
            maxPreviewWidth = MAX_PREVIEW_WIDTH;
        }

        if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
            maxPreviewHeight = MAX_PREVIEW_HEIGHT;
        }
        return new OptimalPreviewSizeEvaluator().chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                maxPreviewHeight, largest);
    }

    private boolean isDimensionSwapped(CameraCharacteristics characteristics){
        boolean swappedDimensions = false;
        Integer cameraSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        if(cameraSensorOrientation != null) {
            this.cameraSensorOrientation = cameraSensorOrientation;
            switch (displayRotation) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_180:
                    if (cameraSensorOrientation == 90 || cameraSensorOrientation == 270) {
                        swappedDimensions = true;
                    }
                    break;
                case Surface.ROTATION_90:
                case Surface.ROTATION_270:
                    if (cameraSensorOrientation == 0 || cameraSensorOrientation == 180) {
                        swappedDimensions = true;
                    }
                    break;
                default:
                    Log.e("Camera Component", "Display rotation is invalid: " + displayRotation);
            }
        }
        return swappedDimensions;
    }

    public void setAutoFlash(CaptureRequest.Builder requestBuilder) {
        if (this.flashSupported) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
        }
    }

    @Override
    public void stopCamera() {
        cameraOpenCloseLock.release();
        if (this.cameraPreviewCaptureHandler != null) {
            this.cameraPreviewCaptureHandler.stopCapture();
        }
        if (this.camera != null) {
            this.camera.close();
            this.camera = null;
        }
        if (this.reader != null) {
            this.reader.close();
            this.reader = null;
        }
        stopBackgroundThread();
    }
    private void startBackgroundThread() {
        this.backgroundThread = new HandlerThread("CameraBackground");
        this.backgroundThread.start();
        this.backgroundHandler = new Handler(this.backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        if(this.backgroundThread != null) {
            this.backgroundThread.quitSafely();
            try {
                this.backgroundThread.join();
                this.backgroundThread = null;
                this.backgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void changeCamera() {

    }

    public Handler getHandler(){
        return this.backgroundHandler;
    }

    public TextureView getTextureView(){
        return this.cameraPreview;
    }

    public CameraDevice getCameraDevice(){
        return this.camera;
    }

    public ImageReader getImageReader(){
        return this.reader;
    }

    public Size getPreviewSize(){
        return this.cameraPreviewSize;
    }

    public int getDisplayRotation(){
        return this.displayRotation;
    }

    public int getCameraOrientation(){
        return this.cameraSensorOrientation;
    }
}
