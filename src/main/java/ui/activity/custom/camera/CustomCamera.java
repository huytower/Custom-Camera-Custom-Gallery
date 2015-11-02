package ui.activity.custom.camera;

import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.View;

import define.Receiver;
import mirrortowers.custom_camera_gallery_library.R;
import ui.activity.custom.gallery.CustomGallery;
import ui.fragment.custom.camera.CameraPreviewFragment;

/**
 * Created by trek2000 on 25/2/2015.
 */
public class CustomCamera extends FragmentActivity
        implements View.OnClickListener {

    /**
     * Single ton section
     */
    public static singleton.custom_camera.Camera camera = singleton.custom_camera.Camera.getInstance();
    public static int case_camera = Receiver.case_camera_in_group_feed;

    /**
     * String section
     */
    public static int current_orientation = 0;

    /**
     * View section
     */
    public static Camera mCamera;
    public static Camera.CameraInfo mCameraInfo =
            new Camera.CameraInfo();
    public static MediaRecorder mMediaRecorder;

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance(int camera_id) {
        Camera c = null;
        try {
            c = Camera.open(camera_id); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            e.printStackTrace();
        }

        return c; // returns null if mCamera is unavailable
    }

    public static void releaseCamera() {
        // Should release camera before go to the other page
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    public static void releaseMediaRecorder() {
        // Should release camera before go to the other page
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;

            if (mCamera != null)
                mCamera.lock();           // lock camera for later use
        }
    }

    /**
     * @param view
     */
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.ibtn_back_in_activity_custom_gallery)
//        switch (view.getId()) {
//            case :
            finish();
//                break;
//        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_custom_camera);

        /**
         * Get action string from intent to check it is :
         * - Single file action
         * - Multiple file action
         */
        if (getIntent() != null && getIntent().getExtras() != null) {
            CustomGallery.ACTION = getIntent().getExtras().getString(Receiver.EXTRAS_ACTION);
            case_camera = getIntent().getExtras().getInt(Receiver.EXTRAS_CASE_RECEIVER);
        }
        // Should initialize new Camera after released
        mCamera = null;
        initialCamera();

        // Should show Fragment Custom Camera Preview firstly
        getSupportFragmentManager().beginTransaction().replace(
                R.id.fl_custom_camera, CameraPreviewFragment.newInstance())
                .commitAllowingStateLoss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Should release camera before go to the other page
        // Because it need release for the other camera apps using camera,
        // If it not release, current app will not allow the other app use
        releaseCamera();
        releaseMediaRecorder();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            CameraPreviewFragment.stopAndRelaseRecordingVideo();
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Should release camera before go to the other page
        // Because it need release for the other camera apps using camera,
        // If it not release, current app will not allow the other app use
        if (CameraPreviewFragment.IS_RECORDING_VIDEO) {
            CustomCamera.mCamera.stopPreview();

            // release camera
            releaseCamera();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Re-initialize if camera already released
        /**
         * Photo mode
         */
        if (mCamera == null || CameraPreviewFragment.IS_RECORDING_VIDEO) {
            // Initialize camera again
            initialCamera();

            // Should show Fragment Custom Camera Preview firstly
            getSupportFragmentManager().beginTransaction().replace(
                    R.id.fl_custom_camera, CameraPreviewFragment.newInstance())
                    .commitAllowingStateLoss();
        }
    }

    /**
     * Initial methods
     */

    private void initialCamera() {
        int camera_mode = -1;

        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                // Back camera
                CameraPreviewFragment.IS_BACK_CAMERA_OR_FRONT_CAMERA = false;

                camera_mode = i;
                break;
            } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                // Front camera
                CameraPreviewFragment.IS_BACK_CAMERA_OR_FRONT_CAMERA = true;

                camera_mode = i;
                break;
            }
        }

        // get Camera instance
        mCamera = getCameraInstance(camera_mode);

        // Set Camera Display Orientation
        CameraPreviewFragment.setCameraDisplayOrientation(this, camera_mode);

        // Get orientation listener
        new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                getCurrentOrientation(orientation);
            }
        }.enable();
    }

    /**
     * Basic methods
     */

    private int getCurrentOrientation(int orientation) {
        orientation = (orientation + 45) / 90 * 90;
        this.current_orientation = orientation % 360;

        return current_orientation;
    }
}
