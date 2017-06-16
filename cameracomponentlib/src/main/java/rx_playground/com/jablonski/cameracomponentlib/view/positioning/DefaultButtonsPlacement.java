package rx_playground.com.jablonski.cameracomponentlib.view.positioning;

import android.view.ViewGroup;
import android.widget.RelativeLayout;

import rx_playground.com.jablonski.cameracomponentlib.R;

/**
 * Created by yabol on 15.06.2017.
 */

public class DefaultButtonsPlacement implements ButtonPlacement {


    @Override
    public RelativeLayout.LayoutParams photoButtonPosition() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.RIGHT_OF, R.id.centerElement);
        return params;
    }

    @Override
    public RelativeLayout.LayoutParams changeCameraButtonPosition() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.LEFT_OF, R.id.centerElement);
        return params;
    }
}
