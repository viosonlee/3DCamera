package lee.vioson.a3dcamera;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Author:李烽
 * Date:2016-06-23
 * FIXME
 * Todo
 */
public class Camera2Activity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2);
        Camera2Fragment fragment = Camera2Fragment.newInstance();
        getFragmentManager().beginTransaction().add(R.id.fragment_layout, fragment)
                .show(fragment).commit();
    }
}
