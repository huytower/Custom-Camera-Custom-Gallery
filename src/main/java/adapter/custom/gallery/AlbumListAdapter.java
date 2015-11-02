package adapter.custom.gallery;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import custom_view.MarkableImageView;
import mirrortowers.custom_camera_gallery_library.R;
import model.custom_gallery.Album;
import ui.fragment.custom.gallery.AlbumInsideFragment;

/**
 * Created by trek2000 on 17/10/2014.
 */
public class AlbumListAdapter extends BaseAdapter {

    /**
     * Data section
     */
    private ArrayList<Album> mAlAlbumName = new ArrayList<>();

    /**
     * String section
     */
    private int resource;

    /**
     * The other section
     */
    private Context mContext;

    public AlbumListAdapter(Context context, int resource, ArrayList<Album> mAl) {
        this.mContext = context;
        this.resource = resource;

        this.mAlAlbumName = mAl;
    }

    @Override
    public int getCount() {
        return mAlAlbumName.size();
    }

    @Override
    public Album getItem(int position) {
        return mAlAlbumName.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int pos, View convertView, ViewGroup parent) {
        // Initialize variables
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater mLiInflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = mLiInflater.inflate(resource, null);

            viewHolder.mIvFolderThumbnail = (MarkableImageView) convertView.findViewById(
                    R.id.iv_folder_thumbnail_in_simple_list_item_in_custom_gallery);
            viewHolder.mIvMediaType = (ImageView) convertView.findViewById(
                    R.id.iv_photos_icon_in_simple_list_item_in_custom_gallery);
            viewHolder.mLl = (LinearLayout) convertView.findViewById(
                    R.id.ll_in_simple_list_item_in_custom_gallery);
            viewHolder.mTvAlbumName = (TextView) convertView.findViewById(
                    R.id.tv_album_name_in_simple_list_item_in_custom_gallery);
            viewHolder.mTvNumberOfPhotos = (TextView) convertView.findViewById(
                    R.id.tv_number_of_photos_in_simple_list_item_in_custom_gallery);
            viewHolder.mTvNumberOfVideos = (TextView) convertView.findViewById(
                    R.id.tv_number_of_videos_in_simple_list_item_in_custom_gallery);

            viewHolder.mIvFolderThumbnail.setTag(pos);
            viewHolder.mIvMediaType.setTag(pos);
//            viewHolder.mLl.setTag(pos);
            viewHolder.mTvAlbumName.setTag(pos);
            viewHolder.mTvNumberOfPhotos.setTag(pos);
            viewHolder.mTvNumberOfVideos.setTag(pos);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();

            viewHolder.mIvFolderThumbnail.setTag(pos);
            viewHolder.mIvMediaType.setTag(pos);
//            viewHolder.mLl.setTag(pos);
            viewHolder.mTvAlbumName.setTag(pos);
            viewHolder.mTvNumberOfPhotos.setTag(pos);
            viewHolder.mTvNumberOfVideos.setTag(pos);
        }

        if (!mAlAlbumName.isEmpty()) {
            // Set OnClick listener for item
            viewHolder.mLl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Pass Folder Path to Fragment Album Inside:
                    Bundle arguments = new Bundle();
                    arguments.putString(AlbumInsideFragment.ARGUMENT_ALBUM_NAME,
                            mAlAlbumName.get(pos).getAlbumName());
                    arguments.putString(AlbumInsideFragment.ARGUMENT_FOLDER_PATH,
                            mAlAlbumName.get(pos).getFolderPath());

                    AlbumInsideFragment fragment =
                            (AlbumInsideFragment) AlbumInsideFragment.newInstance();
                    fragment.setArguments(arguments);

                    /**
                     * Transfer Folder path for :
                     *  1 - list all photos & videos in album.
                     *  2 - if is video, should show Play icon on it.
                     *  3 - Upload text should be visible to prepare uploading.
                     */
                    ((FragmentActivity) mContext)
                            .getSupportFragmentManager().beginTransaction()
                            .addToBackStack(null)
                            .replace(R.id.fl_in_activity_custom_gallery, fragment)
                            .commitAllowingStateLoss();
                }
            });

            // Set Folder thumbnail : Set latest image in every folder
            ((MarkableImageView) viewHolder.mIvFolderThumbnail).setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            if (mAlAlbumName.get(pos).getBitmapFolderThumbnail() != null)
                ((MarkableImageView) viewHolder.mIvFolderThumbnail.findViewWithTag(pos))
                        .setImageBitmap(mAlAlbumName.get(pos).getBitmapFolderThumbnail());
            else
                ((MarkableImageView) viewHolder.mIvFolderThumbnail.findViewWithTag(pos))
                        .setImageResource(R.drawable.iv_empty_album);

            // Set Album Name
            if (mAlAlbumName.get(pos).getAlbumName() != null)
                ((TextView) viewHolder.mTvAlbumName.findViewWithTag(pos))
                        .setText(mAlAlbumName.get(pos).getAlbumName());

            // Set Media Type
            if (mAlAlbumName.get(pos).getMediaTypeOfLatestBitmapFolderThumbnail() == true)
                // Set type : Photo
                ((MarkableImageView) viewHolder.mIvFolderThumbnail.findViewWithTag(pos))
                        .setVideo(false);
            else
                // Set type : Video
                ((MarkableImageView) viewHolder.mIvFolderThumbnail.findViewWithTag(pos))
                        .setVideo(true);

            // Set number of files : Image & Video
            ((TextView) viewHolder.mTvNumberOfPhotos.findViewWithTag(pos))
                    .setText(mAlAlbumName.get(pos).getNumberOfPhotos() + "");
            ((TextView) viewHolder.mTvNumberOfVideos.findViewWithTag(pos))
                    .setText(mAlAlbumName.get(pos).getNumberOfVideos() + "");

            // If both number of photo & number of video are empty
            // should show empty album
            if (Integer.valueOf(mAlAlbumName.get(pos).getNumberOfPhotos().split(" ")[0]) == 0
                    & Integer.valueOf(mAlAlbumName.get(pos).getNumberOfVideos().split(" ")[0]) == 0)
                ((MarkableImageView) viewHolder.mIvFolderThumbnail.findViewWithTag(pos))
                        .setImageResource(R.drawable.iv_empty_album);
        }

        return convertView;
    }

    private class ViewHolder {
        private MarkableImageView mIvFolderThumbnail;
        private ImageView mIvMediaType;
        private LinearLayout mLl;
        private TextView mTvAlbumName;
        private TextView mTvNumberOfPhotos;
        private TextView mTvNumberOfVideos;
    }

    /**
     * Basic methods
     */
}
