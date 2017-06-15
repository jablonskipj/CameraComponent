package rx_playground.com.jablonski.cameracomponentlib.view.helper;

import android.media.Image;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import rx_playground.com.jablonski.cameracomponentlib.view.interfaces.ImageResultCallback;

/**
 * Created by yabol on 15.06.2017.
 */

public class ImageSaver implements Runnable {
    private Image image;
    private File file;
    private ImageResultCallback callback;

    public ImageSaver(Image image, File file){
        this.image = image;
        this.file = file;
    }

    @Override
    public void run() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            saveImageApi19();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void saveImageApi19(){
        ByteBuffer buffer = this.image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(this.file);
            output.write(bytes);
            if(this.callback != null){
                this.callback.onImageCaptured(this.file.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.image.close();
            if (null != output) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setCallback(ImageResultCallback callback) {
        this.callback = callback;
    }
}
