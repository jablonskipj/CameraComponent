package rx_playground.com.jablonski.cameracomponentlib.view.positioning;

import android.support.v4.view.ViewPager;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * Created by yabol on 15.06.2017.
 */

public interface ButtonPlacement {
    /**
     * CameraComponent uses relative layout so to relocate buttons
     * there is a possibility to relocate buttons using standard
     * mechanism.
     *
     * If you are using default buttons use those id's:
     * photo button id R.id.photoButton
     * switch button id R.id.switchButton
     *
     * Fro your own buttons you need to use your id's
     */
    RelativeLayout.LayoutParams photoButtonPosition();
    RelativeLayout.LayoutParams changeCameraButtonPosition();
}
