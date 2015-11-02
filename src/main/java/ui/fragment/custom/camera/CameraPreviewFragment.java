package ui.fragment.custom.camera;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import define.Extension;
import define.Folder;
import mirrortowers.custom_camera_gallery_library.R;
import surface.CustomCameraPreview;
import ui.activity.custom.camera.CustomCamera;
import utils.Utils;

public class CameraPreviewFragment extends Fragment
        implements View.OnClickListener {

    /**
     * Data section
     */

    /**
     * String section
     */
    public static int crop_height = 0;
    public static int crop_width = 0;
    public static int current_camera_id = define.Camera.CAMERA_BACK;

    public static int x = 0;
    //    public static float bottom_bar = 0;
    public static float top_bar = 0;
    public static boolean IS_BACK_CAMERA_OR_FRONT_CAMERA = true;
    public static boolean IS_PHOTO_MODE_OR_VIDEO_MODE = true;
    public static boolean IS_RECORDING_VIDEO = false;
    private static int minute = 0;
    private static String RECORDED_FILE_PATH = null;
    private static int second = 0;
    /**
     * View section
     */
    private static CustomCameraPreview mCameraPreview;
    private static ImageButton mIbtnTakePhotoOrRecordVideo;
    private static TextView mTvElapseTime;
    /**
     * Others section
     */

    // new Counter that counts 3000 ms with a tick each 1000 ms
    private static CountDownTimer mCdt = new CountDownTimer(24 * 60 * 60 * 1000, 1000) {
        public void onTick(long millisUntilFinished) {
            //update the UI with the new count
            second += 1;

            if (second == 2)
                // Enable again for user clicked to stop recording video
                mIbtnTakePhotoOrRecordVideo.setEnabled(true);

            if (second == 60) {
                minute = minute + 1;

                second = 0;
            }
            if (minute < 10) {
                mTvElapseTime.setText("0" + minute + " : " + second);
            } else
                mTvElapseTime.setText(minute + " : " + second);
        }

        public void onFinish() {
            //start the activity
        }
    };
    private boolean IS_ALREADY_ADDED_CAMERA_REVIEW_INSIDE = false;
    private boolean IS_FIRST_TIME_GO_TO_CAMERA_PREVIEW_PAGE = false;
    /**
     * The others methods
     */

    private Camera.PictureCallback mPhoto = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // Pass True value to indicate choose Photo mode
            File pictureFile =
                    getOutputMediaFile(getActivity(), true);
            if (pictureFile == null) {
                Log.i("", "Error creating media file, check storage permissions: ");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();

                // After take picture successfully,
                //      - Need refresh Gallery to see new image
                //      - Go to preview page to see taken picture

                // Refresh Gallery
                Utils.addPictureToGallery(getActivity(), pictureFile.getAbsolutePath());

                // Go to Review page
                ((FragmentActivity) getActivity())
                        .getSupportFragmentManager().beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.fl_custom_camera,
                                CameraReviewFragment.newInstance(pictureFile.getAbsolutePath()))
                        .commitAllowingStateLoss();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
    private FrameLayout mFlCameraPreview;
    private FrameLayout mFlSwitchFrontOrBackCamera;
    private FrameLayout mFlTakePhotoOrRecordVideo;
    private ImageButton mIbtnClose;
    private ImageButton mIbtnSwitchCropMode;
    private ImageButton mIbtnSwitchFullMode;
    private ImageButton mIbtnSwitchFrontOrBackCamera;
    private ImageButton mIbtnSwitchTakePhotoOrRecordVideo;
    private LinearLayout mLlElapseTime;

    public static Fragment newInstance() {
        CameraPreviewFragment fragment = new CameraPreviewFragment();
        return fragment;
    }

    /**
     * Get current camera ID : Back Camera or Front Camera
     */
    public static int switchCurrentCameraID() {
        int current_camera_id = -1;

        if (IS_BACK_CAMERA_OR_FRONT_CAMERA) {
            // Currently - Front, switch to Back
            IS_BACK_CAMERA_OR_FRONT_CAMERA = false;

            current_camera_id = define.Camera.CAMERA_BACK;
        } else {
            // Currently - Back, switch to Front
            IS_BACK_CAMERA_OR_FRONT_CAMERA = true;

            current_camera_id = define.Camera.CAMERA_FRONT;
        }

        return current_camera_id;
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(Context mContext, boolean mode) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), Folder.FOLDER_NAME);

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                // Log.i("", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = null;
        if (mode) {
            // Photo mode
            mediaFile = new File(
                    mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + Extension.JPG);
        } else {
            // Video mode
            mediaFile = new File(
                    mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + Extension.MP4);
        }

        String FILE_PATH = mediaFile.getAbsolutePath();

        // Set file path into single ton way
        Uri mUri = Uri.parse(FILE_PATH);

        CustomCamera.camera.setFilePath(mUri.toString());

        // Tell the media scanner about the new file so that it is
        // immediately available to the user.
        MediaScannerConnection.scanFile(mContext,
                new String[]{FILE_PATH}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                    }
                });
        return mediaFile;
    }

    public static boolean prepareVideoRecorder(Context mContext, int mode) {
        // Should release before use new Preview for Recording Video mode
        CustomCamera.releaseCamera();

        // Initialize camera
        CustomCamera.mCamera = CustomCamera.getCameraInstance(mode);

        CustomCamera.mCamera.startPreview();
        // Set orientation display
        setCameraDisplayOrientation((Activity) mContext, mode);

        // Should release before use new Preview for Recording Video mode
        CustomCamera.releaseMediaRecorder();

        CustomCamera.mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        CustomCamera.mCamera.unlock();
        CustomCamera.mMediaRecorder.setCamera(CustomCamera.mCamera);

        // Step 2: Set sources
        CustomCamera.mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        CustomCamera.mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                // Step 3: Set output format and encoding (for versions prior to API Level 8)
                if (CamcorderProfile.hasProfile(current_camera_id, CamcorderProfile.QUALITY_HIGH)) {
                    CamcorderProfile camcorderProfile = CamcorderProfile.get(
                            current_camera_id, CamcorderProfile.QUALITY_HIGH);
                    CustomCamera.mMediaRecorder.setProfile(camcorderProfile);
                } else
                    CustomCamera.mMediaRecorder.setProfile(
                            CamcorderProfile.get(current_camera_id, CamcorderProfile.QUALITY_LOW));
            } else
                CustomCamera.mMediaRecorder.setProfile(
                        CamcorderProfile.get(current_camera_id, CamcorderProfile.QUALITY_LOW));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Step 4: Set output file - pass False value to indicate choose Video mode
        RECORDED_FILE_PATH = getOutputMediaFile(mContext, false).toString();
        CustomCamera.mMediaRecorder.setOutputFile(RECORDED_FILE_PATH);

        // Step 5: Set the preview output
        /**
         * Define Orientation of image in here,
         * if in portrait mode, use value = 90,
         * if in landscape mode, use value = 0
         */
        if (current_camera_id == define.Camera.CAMERA_BACK)
            // Back Camera
            CustomCamera.mMediaRecorder = Utils.rotateBackVideo(CustomCamera.mMediaRecorder);
        else
            // Front Camera
            CustomCamera.mMediaRecorder = Utils.rotateFrontVideo(CustomCamera.mMediaRecorder);

        // Set preview display to refresh current screen
        CustomCamera.mMediaRecorder.setPreviewDisplay(mCameraPreview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            CustomCamera.mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            CustomCamera.releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            CustomCamera.releaseMediaRecorder();
            return false;
        }
        return true;
    }

    public static void refreshCameraPreview(Activity activity, int current_camera_id) {
        if (CustomCamera.mCamera != null) {
            // Stop preview
            CustomCamera.mCamera.stopPreview();

            //NB: if you don't release the current camera before switching, you app will crash
            CustomCamera.releaseCamera();

            // New setting for Camera
            // Should initialize new Camera after released
            CustomCamera.mCamera = CustomCamera.getCameraInstance(current_camera_id);

            // Set Camera Display Orientation
            setCameraDisplayOrientation(activity, current_camera_id);

            try {
                // Set preview display to refresh current face, use when change camera mode
                CustomCamera.mCamera.setPreviewDisplay(mCameraPreview.getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                CustomCamera.mCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // for the Preview - from http://developer.android.com/reference/android/hardware/Camera.html#setDisplayOrientation(int)
    // note, if orientation is locked to landscape this is only called when setting up the activity, and will always have the same orientation
    public static void setCameraDisplayOrientation(Activity activity, int camera_id) {
        CustomCamera.mCameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(camera_id, CustomCamera.mCameraInfo);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (CustomCamera.mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (CustomCamera.mCameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {
            result = (CustomCamera.mCameraInfo.orientation - degrees + 360) % 360;
        }

        // On Sony Device, has issue : Black screen was shown on it
        if (CustomCamera.mCamera != null)
            CustomCamera.mCamera.setDisplayOrientation(result);
    }

    public static void stopAndRelaseRecordingVideo() {
        if (CustomCamera.mMediaRecorder != null) {
            // stop recording and release camera
            // stop the recording
            CustomCamera.mMediaRecorder.stop();

            // release the MediaRecorder object
            CustomCamera.releaseMediaRecorder();
        }

        if (CustomCamera.mCamera != null) {
            // take camera access back from MediaRecorder
            CustomCamera.mCamera.lock();

            // inform the user that recording has stopped
            IS_RECORDING_VIDEO = false;

            // Stop the preview before transfer to Review page
            CustomCamera.mCamera.stopPreview();

            // After stop recording video, need reset time value
            mCdt.cancel();
            minute = 0;
            second = 0;
        }
    }

    /**
     * Basic methods
     */

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.ibtn_close) {
            // Close current activity
            getActivity().finish();
        } else if (view.getId() == R.id.ibtn_take_photo_or_record_video) {
            /**
             * Need check currently user choose Take Photo mode or Record Video mode.
             * Depend on which mode, use Action
             * - Take photo
             * - Record video
             * relatively.
             */
            if (IS_PHOTO_MODE_OR_VIDEO_MODE) {
                /**
                 * Take photo mode
                 */
                // Begin Take picture
                try {
                    CustomCamera.mCamera.takePicture(null, null, mPhoto);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                /**
                 * Record video mode
                 */
                // Begin Record video

                // Configure MediaRecorder
                if (IS_RECORDING_VIDEO) {
                    try {
                        // stop recording and release camera
                        stopAndRelaseRecordingVideo();

                        // Transfer to Review page to see Recording video at there
                        // Send the file path also
                        ((FragmentActivity) getActivity())
                                .getSupportFragmentManager().beginTransaction()
                                .addToBackStack(null)
                                .replace(R.id.fl_custom_camera,
                                        CameraReviewFragment.newInstance(RECORDED_FILE_PATH))
                                .commitAllowingStateLoss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    // initialize video camera with Back mode or Front mode
                    if (prepareVideoRecorder(getActivity(), current_camera_id)) {
                        // Camera is available and unlocked, MediaRecorder is prepared,
                        // now you can start recording
                        CustomCamera.mMediaRecorder.start();

                        // inform the user that recording has started
                        IS_RECORDING_VIDEO = true;

                        // Begin set Recording time in here
                        //start the countDown
                        mCdt.start();

                        // hide the other views while recording video
                        mIbtnClose.setVisibility(View.INVISIBLE);
                        mIbtnSwitchFrontOrBackCamera.setVisibility(View.INVISIBLE);
                        mIbtnSwitchCropMode.setVisibility(View.INVISIBLE);
                        mIbtnSwitchFullMode.setVisibility(View.INVISIBLE);
                        mIbtnSwitchTakePhotoOrRecordVideo.setVisibility(View.INVISIBLE);

                        mIbtnTakePhotoOrRecordVideo.setImageResource(R.drawable.ibtn_stop_record_video);

                        // Disable for a while
                        mIbtnTakePhotoOrRecordVideo.setEnabled(false);
                    } else {
                        // prepare didn't work, release the camera
                        CustomCamera.releaseMediaRecorder();
                    }
                }
            }
        } else if (view.getId() == R.id.ibtn_switch_to_crop_mode) {
            // Set Crop mode
            CustomCamera.camera.setCropModeOrFullMode(true);

            switchCropModeOrFullMode();
        } else if (view.getId() == R.id.ibtn_switch_to_full_mode) {
            // Set Full mode
            CustomCamera.camera.setCropModeOrFullMode(false);

            switchCropModeOrFullMode();
        } else if (view.getId() == R.id.ibtn_switch_take_photo_or_record_video) {
            // Show Take photo or Record video correctly
            switchPhotoOrVideoMode();
        } else if (view.getId() == R.id.ibtn_switch_back_or_front_camera) {
            /**
             * Should reset camera for camera get new setting
             */

            // Switch between Front Camera & Back Camera
            current_camera_id = switchCurrentCameraID();

            refreshCameraPreview(getActivity(), current_camera_id);

            // Should remove view parent before add child
            Utils.removeViewParent(mCameraPreview);

            /**
             * Add Camera Preview into Layout
             */
            // Create our Preview view and set it as the content of our activity.
            mCameraPreview = new CustomCameraPreview(getActivity(), CustomCamera.mCamera);
            mFlCameraPreview.addView(mCameraPreview);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Define first time go to this page, reset after destroy thi page
        IS_FIRST_TIME_GO_TO_CAMERA_PREVIEW_PAGE = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = getLayoutInflater(savedInstanceState).inflate(
                R.layout.fragment_camera_preview, container, false);

        // Initial views
        initialViews(v);
        initialData();

        // Should remove view parent before add child
        if (IS_ALREADY_ADDED_CAMERA_REVIEW_INSIDE) {
            Utils.removeViewParent(mCameraPreview);
        }

        /**
         * Add Camera Preview into Layout
         */
        // Create our Preview view and set it as the content of our activity.
        mCameraPreview = new CustomCameraPreview(getActivity(), CustomCamera.mCamera);
        mFlCameraPreview.addView(mCameraPreview);

        // This boolean detail that in the next time include Camera Preview into layout
        // Should remove current Camera Preview before add new one
        IS_ALREADY_ADDED_CAMERA_REVIEW_INSIDE = true;

        // Reset Camera every time go Back to current page from the other pages
        resetCamera();

        // Switch Crop mode | Full mode
        switchCropModeOrFullMode();

        /**
         * Should show views correctly
         * - First time go to this page with default mode is Photo
         * - After back from Review page with previous mode after chose
         */
        switchPhotoOrVideoMode();

        /**
         * Check current device has any camera,
         * if size = 2, need show Switch camera button.
         * otherwise, hide Switch camera button.
         */
        if (CustomCamera.mCamera.getNumberOfCameras() == 2)
            // Show switch camera button
            mIbtnSwitchFrontOrBackCamera.setVisibility(View.VISIBLE);
        else
            // Hide switch camera button
            mIbtnSwitchFrontOrBackCamera.setVisibility(View.INVISIBLE);

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Reset
        IS_ALREADY_ADDED_CAMERA_REVIEW_INSIDE = false;

        IS_FIRST_TIME_GO_TO_CAMERA_PREVIEW_PAGE = false;
    }

    /**
     * Initialize methods
     */
    private void initialData() {
        // Set listener
        mIbtnClose.setOnClickListener(this);
        mIbtnSwitchCropMode.setOnClickListener(this);
        mIbtnSwitchFullMode.setOnClickListener(this);
        mIbtnSwitchFrontOrBackCamera.setOnClickListener(this);
        mIbtnSwitchTakePhotoOrRecordVideo.setOnClickListener(this);
        mIbtnTakePhotoOrRecordVideo.setOnClickListener(this);
    }

    private void initialViews(View v) {
        mFlCameraPreview = (FrameLayout) v.findViewById(R.id.fl_camera_preview);
        mFlSwitchFrontOrBackCamera = (FrameLayout) v.findViewById(R.id.fl_take_photo_or_record_video);
        mFlTakePhotoOrRecordVideo = (FrameLayout) v.findViewById(R.id.fl_switch_camera_font_or_back);

        mIbtnClose = (ImageButton) v.findViewById(R.id.ibtn_close);
        mIbtnSwitchCropMode = (ImageButton) v.findViewById(R.id.ibtn_switch_to_crop_mode);
        mIbtnSwitchFullMode = (ImageButton) v.findViewById(R.id.ibtn_switch_to_full_mode);
        mIbtnSwitchFrontOrBackCamera = (ImageButton) v.findViewById(
                R.id.ibtn_switch_back_or_front_camera);
        mIbtnSwitchTakePhotoOrRecordVideo = (ImageButton) v.findViewById(
                R.id.ibtn_switch_take_photo_or_record_video);
        mIbtnTakePhotoOrRecordVideo = (ImageButton) v.findViewById(
                R.id.ibtn_take_photo_or_record_video);
        mLlElapseTime = (LinearLayout) v.findViewById(
                R.id.ll_elapse_time);
        mTvElapseTime = (TextView) v.findViewById(
                R.id.tv_elapse_time);
    }

    private void resetCamera() {
        if (CustomCamera.mCamera == null) {
//            int current_camera_id = switchCurrentCameraID();
            CustomCamera.mCamera = CustomCamera.getCameraInstance(current_camera_id);
        }
    }

    private void switchCropModeOrFullMode() {
        /**
         * In the first time go to Custom Camera, should do these things:
         * - Select Full Mode (boolean = false).
         * - After check Single ton way, use Crop Mode or Full Mode follow boolean value
         * (Crop mode : true, Full mode : false)
         */
        if (CustomCamera.camera.isCropModeOrFullMode()) {
            /**
             * Crop mode
             */

            // Set Drawable resource
            mIbtnSwitchCropMode.setImageResource(R.drawable.ibtn_square_rectangle_selected);
            mIbtnSwitchFullMode.setImageResource(R.drawable.ibtn_rectangle_unselected);

            // Set Crop Bar background is black also : Top Black bar & Bottom black bar
            mFlSwitchFrontOrBackCamera.setBackgroundColor(
                    getActivity().getResources().getColor(android.R.color.black));
            mFlTakePhotoOrRecordVideo.setBackgroundColor(
                    getActivity().getResources().getColor(android.R.color.black));

            // Pixels
            // Utils.getSizeOfScreen(getActivity())[1] : 1080
            // Utils.getSizeOfScreen(getActivity())[0] : 720

            // Need convert from pixels to dp to set for Layout Params
            int height_screen =
                    Utils.getSizeOfScreen(getActivity())[1];
            int width_screen =
                    Utils.getSizeOfScreen(getActivity())[0];

            float height_bar = ((float) height_screen) / 2 - ((float) width_screen) / 2;

            // Bottom bar
            FrameLayout.LayoutParams mLpFlSwitchFrontOrBackCamera =
                    new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, (int) (height_bar));
            mLpFlSwitchFrontOrBackCamera.gravity = Gravity.BOTTOM;

            // Top bar
            FrameLayout.LayoutParams mLpTakePhotoOrRecordVideo =
                    new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, (int) (height_bar));
            mLpTakePhotoOrRecordVideo.gravity = Gravity.TOP;

            // Set params
            mFlSwitchFrontOrBackCamera.setLayoutParams(mLpFlSwitchFrontOrBackCamera);
            mFlTakePhotoOrRecordVideo.setLayoutParams(mLpTakePhotoOrRecordVideo);

            // The bar for cropping
            top_bar = height_bar;
        } else {
            /**
             * Full mode
             */

            // Set Drawable resource
            mIbtnSwitchCropMode.setImageResource(R.drawable.ibtn_square_rectangle_unselected);
            mIbtnSwitchFullMode.setImageResource(R.drawable.ibtn_rectangle_selected);

            // Set Crop Bar background is black also : Top Black bar & Bottom black bar
            mFlSwitchFrontOrBackCamera.setBackgroundColor(
                    getActivity().getResources().getColor(android.R.color.transparent));
            mFlTakePhotoOrRecordVideo.setBackgroundColor(
                    getActivity().getResources().getColor(android.R.color.transparent));
        }
    }

    private void switchPhotoOrVideoMode() {
        /**
         * Need check to show Crop icon in Photo mode
         * and not show Crop icon in Video mode
         */
        if (!IS_PHOTO_MODE_OR_VIDEO_MODE
                | IS_FIRST_TIME_GO_TO_CAMERA_PREVIEW_PAGE) {
            /**
             * Video mode
             */
            IS_FIRST_TIME_GO_TO_CAMERA_PREVIEW_PAGE = false;

            // Switch from Video mode to Photo mode
            IS_PHOTO_MODE_OR_VIDEO_MODE = true;

            // Show views correctly
            mIbtnSwitchTakePhotoOrRecordVideo.setImageResource(
                    R.drawable.ibtn_switch_record_video);
            mIbtnTakePhotoOrRecordVideo.setImageResource(R.drawable.ibtn_take_photo);

            mLlElapseTime.setVisibility(View.INVISIBLE);

            // Show crop icon
            mIbtnSwitchCropMode.setVisibility(View.VISIBLE);

            if (CustomCamera.camera.isCropModeOrFullMode()) {
                // Set Crop Bar background is black also : Top Black bar & Bottom black bar
                mFlSwitchFrontOrBackCamera.setBackgroundColor(
                        getActivity().getResources().getColor(android.R.color.black));
                mFlTakePhotoOrRecordVideo.setBackgroundColor(
                        getActivity().getResources().getColor(android.R.color.black));
            }
        } else {
            /**
             * Photo mode
             */

            // Switch to Video mode from Photo mode
            IS_PHOTO_MODE_OR_VIDEO_MODE = false;

            mIbtnSwitchTakePhotoOrRecordVideo.setImageResource(
                    R.drawable.ibtn_switch_take_photo);
            mIbtnTakePhotoOrRecordVideo.setImageResource(R.drawable.ibtn_record_video);

            mLlElapseTime.setVisibility(View.VISIBLE);
            // Show crop icon
            mIbtnSwitchCropMode.setVisibility(View.INVISIBLE);

            // Set Crop Bar background is black also : Top Black bar & Bottom black bar
            mFlSwitchFrontOrBackCamera.setBackgroundColor(
                    getActivity().getResources().getColor(android.R.color.transparent));
            mFlTakePhotoOrRecordVideo.setBackgroundColor(
                    getActivity().getResources().getColor(android.R.color.transparent));
        }
    }
}
