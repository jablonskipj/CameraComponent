package rx_playground.com.jablonski.cameracomponentlib.view.helper;

import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.SparseIntArray;
import android.view.Surface;

import java.util.Arrays;

import rx_playground.com.jablonski.cameracomponentlib.view.api.CameraAPI21;

/**
 * Created by yabol on 10.06.2017.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraPreviewCapture {
    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAITING_LOCK = 1;
    private static final int STATE_WAITING_PRECAPTURE = 2;
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;

    private static final int STATE_PICTURE_TAKEN = 4;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }
    private CameraAPI21 cameraAPI;
    private CaptureRequest.Builder previewBuilder;
    private CameraCaptureSession captureSession;
    private CaptureRequest captureRequest;
    private int state;

    private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        private void process(CaptureResult result) {
            switch (CameraPreviewCapture.this.state) {
                case STATE_PREVIEW: {
                    // We have nothing to do when the camera preview is working normally.
                    break;
                }
                case STATE_WAITING_LOCK: {
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (afState == null) {
                        captureStillPicture();
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                            CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        // CONTROL_AE_STATE can be null on some devices
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (aeState == null ||
                                aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            state = STATE_PICTURE_TAKEN;
                            captureStillPicture();
                        } else {
                            runPrecaptureSequence();
                        }
                    }
                    break;
                }
                case STATE_WAITING_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        state = STATE_WAITING_NON_PRECAPTURE;
                    }
                    break;
                }
                case STATE_WAITING_NON_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        state = STATE_PICTURE_TAKEN;
                        captureStillPicture();
                    }
                    break;
                }
            }
        }


        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            process(result);
        }
    };

    public CameraPreviewCapture(CameraAPI21 camera){
        this.cameraAPI = camera;
    }


    public void createPreviewSession(){
        SurfaceTexture texutre = this.cameraAPI.getTextureView().getSurfaceTexture();
        if(texutre != null) {
            Surface surface = new Surface(this.cameraAPI.getTextureView().getSurfaceTexture());
            try {
                CameraDevice camera = this.cameraAPI.getCameraDevice();
                this.previewBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                this.previewBuilder.addTarget(surface);

                camera.createCaptureSession(Arrays.asList(surface, this.cameraAPI.getImageReader().getSurface()), new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {

                        captureSession = session;

                        previewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                        captureRequest = previewBuilder.build();
                        try {
                            captureSession.setRepeatingRequest(captureRequest, captureCallback, cameraAPI.getHandler());
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                    }
                }, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void unlockCameraFocus(){
        try {
            this.previewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            this.cameraAPI.setAutoFlash(this.previewBuilder);
            this.captureSession.capture(this.previewBuilder.build(), this.captureCallback,
                    this.cameraAPI.getHandler());
            this.state = STATE_PREVIEW;
            this.captureSession.setRepeatingRequest(this.captureRequest, this.captureCallback,
                    this.cameraAPI.getHandler());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void lockFocus() {
        try {
            // This is how to tell the camera to lock focus.
            this.previewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the lock.
            this.state = STATE_WAITING_LOCK;
            this.captureSession.capture(this.previewBuilder.build(), this.captureCallback,
                    this.cameraAPI.getHandler());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void runPrecaptureSequence() {
        try {
            this.previewBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            this.state = STATE_WAITING_PRECAPTURE;
            this.captureSession.capture(this.previewBuilder.build(), this.captureCallback,
                    this.cameraAPI.getHandler());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void captureStillPicture() {
        try {
            if (this.cameraAPI.getCameraDevice() == null) {
                return;
            }
            // This is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder captureBuilder =
                    this.cameraAPI.getCameraDevice().createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(this.cameraAPI.getImageReader().getSurface());

            // Use the same AE and AF modes as the preview.
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            this.cameraAPI.setAutoFlash(captureBuilder);

            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(this.cameraAPI.getDisplayRotation()));

            CameraCaptureSession.CaptureCallback CaptureCallback
                    = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    unlockCameraFocus();
                }
            };

            this.captureSession.stopRepeating();
            this.captureSession.capture(captureBuilder.build(), CaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private int getOrientation(int rotation) {
        return (ORIENTATIONS.get(rotation) + this.cameraAPI.getCameraOrientation() + 270) % 360;
    }

    public void stopCapture(){
        if(this.captureSession != null){
            this.captureSession.close();
            this.captureSession = null;
        }
    }
}
