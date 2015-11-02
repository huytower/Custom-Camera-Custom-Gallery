package adapter.custom.gallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;

import custom_view.MarkableImageView;
import mirrortowers.custom_camera_gallery_library.R;
import model.File;
import ui.activity.custom.gallery.CustomGallery;

/**
 * Created by trek2000 on 21/8/2014.
 */

public class AlbumInsideAdapter extends BaseAdapter {

    /**
     * Data section
     */
    private ArrayList<File> mAlAlbumItem = new ArrayList<>();

    /**
     * Interface section
     */

    /**
     * String section
     */
    private int textViewResourceId;

    /**
     * The other section
     */
    private Context mContext;
    private DisplayImageOptions mDio;

    public AlbumInsideAdapter(Context context, int textViewResourceId, ArrayList<File> mAlAlbumItem) {
        this.mContext = context;
        this.textViewResourceId = textViewResourceId;
        this.mAlAlbumItem = mAlAlbumItem;

        mDio = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.iv_default_avatar)
                .delayBeforeLoading(100)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    /**
     * @return
     */
    public int getCount() {
        return mAlAlbumItem.size();
    }

    /**
     * @param position
     * @return
     */
    public File getItem(int position) {
        return mAlAlbumItem.get(position);
    }

    /**
     * @param position
     * @return
     */
    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(final int pos, View convertView, ViewGroup parent) {
        /**
         * Begin draw items in Grid View
         */
        MarkableImageView markableImageView = null;
        if (convertView == null) {  // if it's not recycled, initialize some attributes\
            markableImageView = new MarkableImageView(mContext);
            markableImageView.setTag(pos);

            convertView = (MarkableImageView) markableImageView;
        } else {
            markableImageView = (MarkableImageView) convertView;
            markableImageView.setTag(pos);
        }

        /**
         * check item selected or not and draw image select
         */
        if (mAlAlbumItem.get(pos).isChecked()) {
            ((MarkableImageView) markableImageView.findViewWithTag(pos)).setChecked(true);
        } else {
            ((MarkableImageView) markableImageView.findViewWithTag(pos)).setChecked(false);
        }

        /**
         * check item is video or not
         */
        if (mAlAlbumItem.get(pos).isVideo()) {
            ((MarkableImageView) markableImageView.findViewWithTag(pos)).setVideo(true);

            // Set duration of video
            ((MarkableImageView) markableImageView.findViewWithTag(pos)).setDurationOfVideo(
                    mAlAlbumItem.get(pos).getDurationOfVideo());
        } else {
            ((MarkableImageView) markableImageView.findViewWithTag(pos)).setVideo(false);
        }

        // Should load file from sd card by parsing Uri
        String FILE_PATH = mAlAlbumItem.get(pos).getFilePath();


        Uri mUri = Uri.fromFile(new java.io.File(FILE_PATH));
        // file:///storage/emulated/0/Pictures/Enterprise/VID_20150911_093859.mp4
        if (FILE_PATH != null) {
            // Display image

            /**
             * Should use separate string to void wrap these words "//" to "/"
             */
            try {
                CustomGallery.imageLoader.displayImage(
                        Uri.decode(mUri.toString().trim()),
                        markableImageView, mDio,
                        new SimpleImageLoadingListener() {
                            @Override
                            public void onLoadingStarted(String imageUri, View view) {
                            }

                            @Override
                            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            }

                            @Override
                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                if (loadedImage != null & view != null)
                                    ((ImageView) view).setImageBitmap(
                                            loadedImage);
                            }
                        }, new ImageLoadingProgressListener() {
                            @Override
                            public void onProgressUpdate(
                                    String imageUri, View view, int current, int total) {
                            }
                        });
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        return convertView;
    }

    /**
     * View section
     */
    private class ViewHolder {
        MarkableImageView mIvItem;
    }
}
