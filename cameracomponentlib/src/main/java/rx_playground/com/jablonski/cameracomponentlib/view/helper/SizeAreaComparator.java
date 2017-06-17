package rx_playground.com.jablonski.cameracomponentlib.view.helper;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Size;

import java.util.Comparator;

/**
 * Created by yabol on 12.06.2017.
 */


public class SizeAreaComparator implements Comparator<Size> {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public int compare(Size lhs, Size rhs) {
        // We cast here to ensure the multiplications won't overflow
        return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                (long) rhs.getWidth() * rhs.getHeight());
    }
}
