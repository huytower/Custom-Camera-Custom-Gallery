package ui.activity.custom.gallery;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.util.ArrayList;

import define.Receiver;
import mirrortowers.custom_camera_gallery_library.R;
import ui.fragment.custom.gallery.AlbumInsideFragment;
import ui.fragment.custom.gallery.AlbumListFragment;
import utils.Utils;

/**
 * Created by trek2000 on 25/2/2015.
 */
public class CustomGallery extends FragmentActivity
        implements View.OnClickListener {

    /**
     * String section
     */
    public static String ACTION = null;
    public static int case_receiver = 0;

    public static boolean IS_BACK_FROM_CUSTOM_GALLERY_PAGE = false;
    public static LinearLayout mLlAlbumName;
    public static TextView mTvAlbumName;
    public static TextView mTvUpload;
    /**
     * Image loader to load image flexible, avoid OutOfMemory exception
     */
    public static ImageLoader imageLoader = ImageLoader.getInstance();

    /**
     * The other section
     */
    public static ImageLoaderConfiguration imageLoaderConfiguration;
    /**
     * View section
     */
    private ImageButton mIbtnBack;

    /**
     * @param view
     */
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.ibtn_back_in_activity_custom_gallery) {
            /**
             * Need check current page is which page for us
             * to know need click Back to which page
             * - In Album List page, should clear selected files list & close activity
             * - In Album Inside page, should clear selected files list & go to previous page
             */
            if (!AlbumInsideFragment.mAlSelectedIndex.isEmpty())
                AlbumInsideFragment.mAlSelectedIndex.clear();

            if (AlbumListFragment.IS_IN_ALBUM_LIST_FRAGMENT)
                finish();
            else if (AlbumInsideFragment.IS_IN_ALBUM_INSIDE_PAGE)
                Utils.clearOldBackStack(this);
        } else if (view.getId() == R.id.tv_upload_in_activity_custom_gallery) {
            // After selected files in Folder page,
            // begin upload after closed Activity Custom Gallery
            finish();

            // transfer Array List had selected files inside to Enterprise activity
            Intent mIntent = new Intent(ACTION);
            mIntent.putExtra(Receiver.EXTRAS_CASE_RECEIVER, case_receiver);

            if (ACTION.equals(Receiver.ACTION_CHOSE_SINGLE_FILE)) {
                if (!AlbumInsideFragment.mAlSelectedIndex.isEmpty()) {
                    mIntent.putExtra(
                            Receiver.EXTRAS_FILE_PATH,
                            AlbumInsideFragment.mAlFilesInAlbum.get(
                                    AlbumInsideFragment.mAlSelectedIndex.get(0))
                                    .getFilePath());

                    // Send broadcast
                    sendBroadcast(mIntent);

                }
            } else if (ACTION.equals(Receiver.ACTION_CHOSE_MULTIPLE_FILE)) {
                ArrayList<String> mAlFilePath = new ArrayList<>();
                for (int i = 0; i < AlbumInsideFragment.mAlSelectedIndex.size(); i++) {

                    mAlFilePath.add(AlbumInsideFragment.mAlFilesInAlbum.get(
                            AlbumInsideFragment.mAlSelectedIndex.get(i))
                            .getFilePath());
                }

                // Put object : Array list into
                if (!mAlFilePath.isEmpty()) {
                    mIntent.putStringArrayListExtra(Receiver.EXTRAS_FILE_PATH, mAlFilePath);

                    // Send broadcast
                    sendBroadcast(mIntent);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_custom_gallery);

        /**
         * Set Orientation for page
         */
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        IS_BACK_FROM_CUSTOM_GALLERY_PAGE = false;

        // This configuration tuning is custom.
        // You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method
        imageLoaderConfiguration =
                new ImageLoaderConfiguration.Builder(this)
                        .threadPriority(Thread.NORM_PRIORITY - 2)
                        .denyCacheImageMultipleSizesInMemory()
                        .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                        .diskCacheSize(50 * 1024 * 1024) // 50 Mb.
                        .tasksProcessingOrder(QueueProcessingType.LIFO)
                        .writeDebugLogs() // Remove for release app
                        .build();
        // Initialize ImageLoader with configuration.
        imageLoader.init(imageLoaderConfiguration);

        // Should show Fragment Custom Gallery firstly
        getSupportFragmentManager().beginTransaction().replace(
                R.id.fl_in_activity_custom_gallery, AlbumListFragment.newInstance())
                .commitAllowingStateLoss();

        initialViews();
        initialData();

        // Clear selected index list from Album Inside fragment
        AlbumInsideFragment.mAlSelectedIndex.clear();

        /**
         * Get action string from intent to check it is :
         * - Single file action
         * - Multiple file action
         */
        if (getIntent().getExtras() != null) {
            ACTION = getIntent().getExtras().getString(Receiver.EXTRAS_ACTION);
            case_receiver = getIntent().getExtras().getInt(Receiver.EXTRAS_CASE_RECEIVER);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        IS_BACK_FROM_CUSTOM_GALLERY_PAGE = true;
    }

    /**
     * Initial methods
     */

    private void initialData() {
        // Set listener
        mIbtnBack.setOnClickListener(this);
        mTvUpload.setOnClickListener(this);
    }

    private void initialViews() {
        mIbtnBack = (ImageButton) findViewById(R.id.ibtn_back_in_activity_custom_gallery);
        mLlAlbumName = (LinearLayout) findViewById(R.id.ll_album_name_in_activity_custom_gallery);
        mTvAlbumName = (TextView) findViewById(R.id.tv_album_name_in_activity_custom_gallery);
        mTvUpload = (TextView) findViewById(R.id.tv_upload_in_activity_custom_gallery);
    }

    /**
     * Basic methods
     */
}
