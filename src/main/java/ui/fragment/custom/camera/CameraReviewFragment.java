package ui.fragment.custom.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;

import controller.SaveBitmapToFileAsync;
import define.MediaType;
import define.Receiver;
import mirrortowers.custom_camera_gallery_library.R;
import ui.activity.custom.camera.CustomCamera;
import utils.Utils;

public class CameraReviewFragment extends Fragment
        implements View.OnClickListener {
    /**
     * Data section
     */

    /**
     * The others methods
     */

    /**
     * Others section
     */
    public static Button mBtnUse;
    /**
     * String section
     */
    private static String FILE_PATH;
//    private TextureVideoView mVvVideo;
    /**
     * View section
     */
    private Button mBtnRetake;
    private ImageView mIvPhoto;
    private VideoView mVvVideo;

    /**
     * Listener section
     */

    public static Fragment newInstance(String filePath) {
        FILE_PATH = filePath;

        CameraReviewFragment fragment = new CameraReviewFragment();
        return fragment;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_retake) {
            // Transfer to Camera Preview page by re-using fragment from Back Stack
            Utils.clearOldBackStack(getActivity());
        } else if (view.getId() == R.id.btn_use) {
            // Use taken photo for uploading
            // Need finish activity after set again single ton
            // Set File Path into single ton way
            // After selected files in Folder page,
            // begin upload after closed Activity Custom Gallery
            getActivity().finish();

            // transfer selected File Path to receiver
            // transfer Array List had selected files inside to Enterprise activity
            Intent mIntent = new Intent(Receiver.ACTION_CHOSE_SINGLE_FILE);

            // Put object : Array list into
            mIntent.putExtra(Receiver.EXTRAS_FILE_PATH, CustomCamera.camera.getFilePath());
            mIntent.putExtra(Receiver.EXTRAS_CASE_RECEIVER, CustomCamera.case_camera);

            // Send broadcast
            getActivity().sendBroadcast(mIntent);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /**
         * Set Orientation for page
         */
//        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        View v = getLayoutInflater(savedInstanceState).inflate(
                R.layout.fragment_camera_review, container, false);

        // Initial views
        initialViews(v);
        initialData();

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Back to previous mode already chose : Photo mode or Video mode
        if (!CameraPreviewFragment.IS_PHOTO_MODE_OR_VIDEO_MODE)
            CameraPreviewFragment.IS_PHOTO_MODE_OR_VIDEO_MODE = true;
        else
            CameraPreviewFragment.IS_PHOTO_MODE_OR_VIDEO_MODE = false;
    }

    /**
     * Initialize methods
     */
    private void initialData() {
        // Set listener
        mBtnRetake.setOnClickListener(this);
        mBtnUse.setOnClickListener(this);

        // Set Review
        // Need check the taken file is photo or video to set correctly
        if (Utils.isPhotoOrVideo(FILE_PATH) == MediaType.PHOTO) {
            // Photo

            // Show Use Photo text
            mBtnUse.setText(getString(R.string.use_photo));

            // Hide Image View, Show Video View player
            mIvPhoto.setVisibility(View.VISIBLE);
            mVvVideo.setVisibility(View.GONE);

            /**
             * Show image on View correctly
             */
            showPhotoOnUI(true);
        } else if (Utils.isPhotoOrVideo(FILE_PATH) == MediaType.VIDEO) {
            // Video

            // Show Use VDO text
            mBtnUse.setText(getString(R.string.use_video));

            // Hide Image View, Show Video View player
            mIvPhoto.setVisibility(View.GONE);
            mVvVideo.setVisibility(View.VISIBLE);

            /**
             * Set up video view
             */
            showVideoOnUI();
        }
    }

    private void initialViews(View v) {
        mBtnRetake = (Button) v.findViewById(R.id.btn_retake);
        mBtnUse = (Button) v.findViewById(R.id.btn_use);
        mIvPhoto = (ImageView) v.findViewById(R.id.iv_review_photo);
        mVvVideo = (VideoView) v.findViewById(R.id.vv_review_video);
    }

    private void showPhotoOnUI(boolean is_success) {
        try {
            Bitmap mBitmap = null;

            // Check OutOfMemory error happened or not.
            // If success, decode file normally
            // If fail, need put sampleSize when decoding
            if (is_success)
                mBitmap = BitmapFactory.decodeFile(FILE_PATH);
            else
                mBitmap = BitmapFactory.decodeFile(
                        FILE_PATH, Utils.getBitmapOptions());

            Bitmap mBitmapRotated = null;
            // Rotate Back photo only once in here
            mBitmapRotated = Utils.rotateBackImage(mBitmap);

            // Should overwrite the file inside sd card.
            //create a file to write bitmap data
            File mFile = new File(FILE_PATH);
            mFile.createNewFile();

            // Begin crop photo in here
            Bitmap bitmap = null;

            if (CustomCamera.camera.isCropModeOrFullMode()) {
                /**
                 * Crop mode
                 */
                // Width of bitmap after rotated
                int height = mBitmap.getHeight();
                // Height of bitmap after rotated
                int width = mBitmap.getWidth();

                float scaleImage = (float) height / (float) width;
                float heightBubble = ((float) Utils.getSizeOfScreen(getActivity())[1]) * scaleImage / 2 - ((float) Utils.getSizeOfScreen(getActivity())[0]) / 2;

                // eight of bitmap / width of screen
                //float scaled_rate = (float) (height) / (float) Utils.getSizeOfScreen(getActivity())[0];
                float scaled_rate = (float) (height) / (((float) Utils.getSizeOfScreen(getActivity())[0]) + 2 * heightBubble);

                // create matrix for the manipulation
                Matrix matrix = new Matrix();

                // Should use best resolution from camera
                // recreate the new Bitmap
                Bitmap resizedBitmap = null;

                /**
                 * Need check front or back image also to flip captured image
                 */
                if (!CameraPreviewFragment.IS_BACK_CAMERA_OR_FRONT_CAMERA) {
                    // Back Camera

                    // create bitmap
                    switch (CustomCamera.current_orientation) {
                        case 0:
                            resizedBitmap = Bitmap.createBitmap(
                                    mBitmapRotated,
                                    // Define X, Y where to begin crop
                                    0, (int) (CameraPreviewFragment.top_bar * scaled_rate),
                                    (int) (Utils.getSizeOfScreen(getActivity())[0] * scaled_rate),
                                    (int) (Utils.getSizeOfScreen(getActivity())[0] * scaled_rate),
                                    matrix, true);
                            break;
                        case 90:
                            resizedBitmap = Bitmap.createBitmap(
                                    mBitmapRotated,
                                    // Define X, Y where to begin crop
                                    (int) (CameraPreviewFragment.top_bar * scaled_rate), 0,
                                    (int) (Utils.getSizeOfScreen(getActivity())[0] * scaled_rate),
                                    (int) (Utils.getSizeOfScreen(getActivity())[0] * scaled_rate),
                                    matrix, true);
                            break;
                        case 180:
                            resizedBitmap = Bitmap.createBitmap(
                                    mBitmapRotated,
                                    // Define X, Y where to begin crop
                                    0, (int) (CameraPreviewFragment.top_bar * scaled_rate),
                                    (int) (Utils.getSizeOfScreen(getActivity())[0] * scaled_rate),
                                    (int) (Utils.getSizeOfScreen(getActivity())[0] * scaled_rate),
                                    matrix, true);
                            break;
                        case 270:
                            resizedBitmap = Bitmap.createBitmap(
                                    mBitmapRotated,
                                    // Define X, Y where to begin crop
                                    (int) (CameraPreviewFragment.top_bar * scaled_rate), 0,
                                    (int) (Utils.getSizeOfScreen(getActivity())[0] * scaled_rate),
                                    (int) (Utils.getSizeOfScreen(getActivity())[0] * scaled_rate),
                                    matrix, true);
                            break;
                    }

                    // Rotate Back photo need again in here
                    bitmap = resizedBitmap;
                } else {
                    // Front Camera

                    // create bitmap
                    switch (CustomCamera.current_orientation) {
                        case 0:
                            resizedBitmap = Bitmap.createBitmap(
                                    mBitmapRotated,
                                    // Define X, Y where to begin crop
                                    0, (int) ((CameraPreviewFragment.top_bar * scaled_rate)),
                                    (int) (Utils.getSizeOfScreen(getActivity())[0] * scaled_rate),
                                    (int) (Utils.getSizeOfScreen(getActivity())[0] * scaled_rate),
                                    matrix, true);
                            break;
                        case 90:
                            resizedBitmap = Bitmap.createBitmap(
                                    mBitmapRotated,
                                    // Define X, Y where to begin crop
                                    (int) ((CameraPreviewFragment.top_bar * scaled_rate)), 0,
                                    (int) (Utils.getSizeOfScreen(getActivity())[0] * scaled_rate),
                                    (int) (Utils.getSizeOfScreen(getActivity())[0] * scaled_rate),
                                    matrix, true);
                            break;
                        case 180:
                            resizedBitmap = Bitmap.createBitmap(
                                    mBitmapRotated,
                                    // Define X, Y where to begin crop
                                    0, (int) ((CameraPreviewFragment.top_bar * scaled_rate)),
                                    (int) (Utils.getSizeOfScreen(getActivity())[0] * scaled_rate),
                                    (int) (Utils.getSizeOfScreen(getActivity())[0] * scaled_rate),
                                    matrix, true);
                            break;
                        case 270:
                            resizedBitmap = Bitmap.createBitmap(
                                    mBitmapRotated,
                                    // Define X, Y where to begin crop
                                    (int) ((CameraPreviewFragment.top_bar * scaled_rate)), 0,
                                    (int) (Utils.getSizeOfScreen(getActivity())[0] * scaled_rate),
                                    (int) (Utils.getSizeOfScreen(getActivity())[0] * scaled_rate),
                                    matrix, true);
                            break;
                    }

                    // Rotate Front photo need again in here
                    resizedBitmap = Utils.flipFrontImage(resizedBitmap);
                    bitmap = resizedBitmap;
                }

                // Set image bitmap
                mIvPhoto.setImageBitmap(bitmap);
            } else {
                /**
                 * Full Screen mode
                 */

                // Set image on View
                if (!CameraPreviewFragment.IS_BACK_CAMERA_OR_FRONT_CAMERA) {
                    // Back Camera

                    // Rotate Front photo need again in here
                    bitmap = mBitmapRotated;
                    mIvPhoto.setImageBitmap(bitmap);
                } else {
                    // Front Camera

                    // Rotate Front photo need again in here
                    mBitmapRotated = Utils.rotateFrontImage(getActivity(), mBitmapRotated);

                    bitmap = mBitmapRotated;
                    mIvPhoto.setImageBitmap(bitmap);
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                new SaveBitmapToFileAsync(getActivity(), bitmap, mFile)
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else
                new SaveBitmapToFileAsync(getActivity(), bitmap, mFile).execute();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();

            showPhotoOnUI(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showVideoOnUI() {
        File mFile = new File(FILE_PATH);

        // ex. FILE PATH = /mnt/sdcard/Pictures/Enterprise/VID_20150327_143555.mp4
        mVvVideo.setVideoPath(mFile.getAbsolutePath());

        // set play video view dialog details photo
        MediaController mMc = new MediaController(getActivity());
        mMc.setAnchorView(mVvVideo);
        mMc.setMediaPlayer(mVvVideo);

        mVvVideo.requestFocus();
        mVvVideo.setBackgroundColor(Color.WHITE);
        mVvVideo.setMediaController(mMc);
        mVvVideo.setZOrderOnTop(true);
        mVvVideo.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mVvVideo.isPlaying()) {
                    mVvVideo.pause();

                    // Show full-screen button again
                    mVvVideo.setVisibility(View.VISIBLE);
                } else {
                    mVvVideo.start();
                }

                return false;
            }
        });

        if (!mVvVideo.isPlaying())
            mVvVideo.start();
    }
}
