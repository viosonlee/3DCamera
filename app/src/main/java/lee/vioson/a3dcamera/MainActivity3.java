package lee.vioson.a3dcamera;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Author:李烽
 * Date:2016-06-27
 * FIXME
 * Todo
 */
public class MainActivity3 extends AppCompatActivity{


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_3);
        CameraFragment  cameraFragment = new CameraFragment();
        getFragmentManager().beginTransaction().add(R.id.fragment_container, cameraFragment)
                .show(cameraFragment).commit();
    }

}
