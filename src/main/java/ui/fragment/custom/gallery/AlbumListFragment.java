package ui.fragment.custom.gallery;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

import adapter.custom.gallery.AlbumListAdapter;
import define.MediaType;
import define.Receiver;
import mirrortowers.custom_camera_gallery_library.R;
import model.custom_gallery.Album;
import model.custom_gallery.AlbumFileInside;
import ui.activity.custom.camera.CustomCamera;
import ui.activity.custom.gallery.CustomGallery;
import utils.Utils;

public class AlbumListFragment extends Fragment
        implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    /**
     * Data section
     */
    public static ArrayList<Album> mAlAlbums = new ArrayList<>();
    public static boolean IS_IN_ALBUM_LIST_FRAGMENT = false;
    /**
     * String section
     */
    private static int increase_index_of_file = 0;
    /**
     * Adapter
     */
    private AlbumListAdapter albumListAdapter = null;
    private ArrayList<AlbumFileInside> mAlAlbumFilesInsideAlbum = new ArrayList<>();
    /**
     * View section
     */
    private LinearLayout mLlTakeNew;
    private ListView mLv;
    private SwipeRefreshLayout mSrl;

    /**
     * Listener section
     */

    public static Fragment newInstance() {
        AlbumListFragment fragment = new AlbumListFragment();
        return fragment;
    }

    /**
     * Others section
     */

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.ll_take_new) {
            // Go to Custom Camera page.
            // If device support Camera feature,
            // call Camera activity
            Intent mIntent = new Intent(getActivity(), CustomCamera.class);
            mIntent.putExtra(Receiver.EXTRAS_ACTION, Receiver.ACTION_CHOSE_SINGLE_FILE);
            mIntent.putExtra(Receiver.EXTRAS_CASE_RECEIVER, CustomGallery.case_receiver);

            startActivity(mIntent);

            // Finish activity
            getActivity().finish();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        IS_IN_ALBUM_LIST_FRAGMENT = true;

        /**
         * Set Orientation for page
         */
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        View v = getLayoutInflater(savedInstanceState).inflate(
                R.layout.fragment_album_list, container, false);

        // Clear old array list before add new items
        if (!mAlAlbumFilesInsideAlbum.isEmpty())
            mAlAlbumFilesInsideAlbum.clear();

        // Initial views
        initialViews(v);
        initialData();

        // Always set default name of Title is "Albums"
        CustomGallery.mTvAlbumName.setText(getString(R.string.albums));

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        IS_IN_ALBUM_LIST_FRAGMENT = false;
    }

    @Override
    public void onRefresh() {
        // what's thing need refresh when pull down, need place in here
        try {
            mAlAlbums.clear();

            // Call thread get album list
            getAlbumList();
        } catch (Exception e) {
            e.printStackTrace();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Stop the animation once we are done fetching data.
                mSrl.setRefreshing(false);

                /**
                 * You can add code to update your list with the new data.
                 **/
            }
        }, 2000);
    }

    /**
     * Initialize methods
     */

    private void initialData() {
        /**
         * Set listener
         */
        mLlTakeNew.setOnClickListener(this);
        mSrl.setOnRefreshListener(this);

        // Call thread get album list
        getAlbumList();
    }

    private void initialViews(View v) {
        mLv = (ListView) v.findViewById(R.id.lv_in_fragment_album_list);
        mLlTakeNew = (LinearLayout) v.findViewById(R.id.ll_take_new);
        mSrl = (SwipeRefreshLayout) v.findViewById(R.id.srl_refresh_in_fragment_album_list);
    }

    /**
     * The others methods
     */

    private void getAlbumList() {
        // Call thread get album list
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            new GetAlbumListAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            new GetAlbumListAsync().execute();
    }

    private void getBitmap(boolean is_photo_or_video, Album album, ArrayList<File> mFileList) {
        try {
            Bitmap mBitmapFolderThumbnail = null;
            String FILE_PATH = mFileList.get(
                    mFileList.size() - 1 - increase_index_of_file).getAbsolutePath();
            File mFile = new File(FILE_PATH);

            // Get bitmap
            if (is_photo_or_video
                    & mFile.isFile()
                    & !mFile.isDirectory()
                    & !mFile.isHidden()
                    & Utils.isPhotoOrVideo(FILE_PATH) == MediaType.PHOTO) {
                // Photos
                mBitmapFolderThumbnail = Utils.getThumbnail(
                        getActivity(), true,
                        null,
                        FILE_PATH);
            } else if (!is_photo_or_video
                    & mFile.isFile()
                    & !mFile.isDirectory()
                    & !mFile.isHidden()
                    & Utils.isPhotoOrVideo(FILE_PATH) == MediaType.VIDEO) {
                // Videos
                mBitmapFolderThumbnail = Utils.getThumbnail(
                        getActivity(), false,
                        null,
                        FILE_PATH);
            } else {
                increase_index_of_file++;
                if (increase_index_of_file < mFileList.size() - 1)
                    getBitmap(is_photo_or_video, album, mFileList);
            }

            // Save bitmap folder thumbnail
            album.setBitmapFolderThumbnail(mBitmapFolderThumbnail);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getNumberOfFiles(boolean isPhotoOrVideo, String FOLDER_PATH) {
        ArrayList<File> mAl = new ArrayList<>();
        /**
         * This method to get only files inside selected folder, not included sub-folder :
         * getPhotoAndVideoFromSdCard(false, mAlFolders, mFileFolder)
         * If want to include files in sub-folder also, need use below method :
         * getPhotoAndVideoFromSdCard(mAlFolders, mFileFolder)
         */
        mAl = Utils.getPhotoAndVideoFromSdCard(false, mAl, new File(FOLDER_PATH));

        /**
         * Check to put only photo, video files to show in Custom Gallery
         */
        int number_of_files = 0;
        for (int j = 0; j < mAl.size(); j++) {
            // If the File Name is picture, increase photos++
            // If the File Name is video, increase videos++
            if (isPhotoOrVideo) {
                if (mAl.get(j).isFile()
                        && Utils.isPhotoOrVideo(mAl.get(j).getName()) == MediaType.PHOTO) {
                    number_of_files++;
                }
            } else {
                if (mAl.get(j).isFile()
                        && Utils.isPhotoOrVideo(mAl.get(j).getName()) == MediaType.VIDEO) {
                    number_of_files++;
                }
            }
        }

        return number_of_files;
    }

    /**
     * Class section
     */
    private class GetAlbumListAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            // Get folder list to shown on page
            try {
                mAlAlbums = getAlbumList();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Clear old list before add new items
            mAlAlbums = new ArrayList<>();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            try {
                // Set adapter for list view
                albumListAdapter = new AlbumListAdapter(
                        getActivity(),
                        R.layout.simple_list_item_in_album_list,
                        mAlAlbums);

                // Always Set adapter to avoid can not load list view data
                // after back from the other fragment
                mLv.setAdapter(albumListAdapter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private ArrayList<Album> getAlbumList() {
            ArrayList<Album> mAlAlbums = new ArrayList<>();

            ArrayList<String> mAlAlbumPhotoFolderPath = new ArrayList<>();
            ArrayList<String> mAlAlbumVideoFolderPath = new ArrayList<>();

            /**
             * which image, video properties are we querying
             */
            String[] PROJECTION_PHOTOS = new String[]{
                    MediaStore.Images.ImageColumns.DATA,
                    MediaStore.Images.ImageColumns.DATE_TAKEN,
                    MediaStore.Video.VideoColumns.DATA,
                    MediaStore.Video.VideoColumns.DATE_TAKEN
            };

            // Get the base URI for the People table in the Contacts content provider.
            Uri mUriPhotos = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            // Make the query.
            Cursor mCursorPhotos = getActivity().managedQuery(
                    mUriPhotos,
                    PROJECTION_PHOTOS, // Which columns to return
                    null,       // Which rows to return (all rows)
                    null,       // Selection arguments (none)
                    null        // Ordering
            );

            Uri mUriVideos = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

            Cursor mCursorVideos = getActivity().managedQuery(
                    mUriVideos,
                    PROJECTION_PHOTOS, // Which columns to return
                    null,       // Which rows to return (all rows)
                    null,       // Selection arguments (none)
                    null        // Ordering
            );

            /**
             * Move in Array Photo
             */
            while (mCursorPhotos.moveToNext()) {
                String dateTaken, photo;

                int dateTakenColumn = mCursorPhotos.getColumnIndex(
                        MediaStore.Images.ImageColumns.DATE_TAKEN);
                int photoColumn = mCursorPhotos.getColumnIndex(
                        MediaStore.Images.ImageColumns.DATA);

                do {
                    // Get the field values
                    dateTaken = mCursorPhotos.getString(dateTakenColumn);
                    photo = mCursorPhotos.getString(photoColumn);

                    // Do something with the values.
                    File mPhotoInsideInAlbum = new File(photo);

                    if (!mPhotoInsideInAlbum.isDirectory()
                            && mPhotoInsideInAlbum.isFile()
                            && Utils.isPhotoOrVideo(mPhotoInsideInAlbum.getAbsolutePath())
                            == MediaType.PHOTO) {
                        String PHOTO_IN_FOLDER_PATH = mPhotoInsideInAlbum.getParent();

                        String[] SPLIT_PHOTO = mPhotoInsideInAlbum.getParent().split("/");
                        String ALBUM_NAME_OF_PHOTO = SPLIT_PHOTO[SPLIT_PHOTO.length - 1];

                        /**
                         * Add album into array list
                         */
                        // Check if album not exists before, put that album into array list
                        if (!mAlAlbumPhotoFolderPath.contains(PHOTO_IN_FOLDER_PATH)) {
                            /**
                             * Including into Album object by :
                             * - Extract bitmap of latest file
                             * - Get file count of one album
                             */
                            File mFilePhotoFolder = new File(PHOTO_IN_FOLDER_PATH);

                            // When get the number of photos, need also
                            // include photos in sub-folder
                            File mPhotoList[] = mFilePhotoFolder.listFiles();

                            ArrayList<File> mAlPhoto = new ArrayList<>();
                            for (int i = 0; i < mPhotoList.length; i++) {
                                mAlPhoto.add(mPhotoList[i]);
                            }
                            for (int i = mAlPhoto.size() - 1; i >= 0; i--) {
                                if (Utils.isPhotoOrVideo(mAlPhoto.get(i).getAbsolutePath())
                                        != MediaType.PHOTO)
                                    mAlPhoto.remove(i);
                            }

                            int number_of_photos = getNumberOfFiles(true, PHOTO_IN_FOLDER_PATH);

                            // Add item into Array list Photos
                            Album album = new Album(
                                    getActivity(),
                                    ALBUM_NAME_OF_PHOTO,
                                    dateTaken,
                                    PHOTO_IN_FOLDER_PATH,
                                    true);

                            // Set photo number
                            album.setNumberOfPhotos(number_of_photos);

                            // Get bitmap
                            increase_index_of_file = 0;
                            getBitmap(true, album, mAlPhoto);

                            // Add item Album object into array list
                            mAlAlbums.add(album);

                            // Add album name into array list also
                            mAlAlbumPhotoFolderPath.add(PHOTO_IN_FOLDER_PATH);
                        }
                    }
                } while (mCursorPhotos != null && mCursorPhotos.moveToNext());
            }

            /**
             * Move in Array Video
             */
            while (mCursorVideos != null && mCursorVideos.moveToNext()) {
                String dateTaken, video;

                int dateTakenColumn = mCursorVideos.getColumnIndex(
                        MediaStore.Video.Media.DATE_TAKEN);
                int videoColumn = mCursorVideos.getColumnIndex(
                        MediaStore.Video.VideoColumns.DATA);

                dateTaken = mCursorVideos.getString(dateTakenColumn);
                video = mCursorVideos.getString(videoColumn);

                File mVideoInsideInAlbum = new File(video);

                if (mVideoInsideInAlbum.isFile()
                        & Utils.isPhotoOrVideo(mVideoInsideInAlbum.getAbsolutePath())
                        == MediaType.VIDEO) {
                    String VIDEO_IN_FOLDER_PATH = mVideoInsideInAlbum.getParent();

                    String[] SPLIT_VIDEO = mVideoInsideInAlbum.getParent().split("/");
                    String ALBUM_NAME_OF_VIDEO = SPLIT_VIDEO[SPLIT_VIDEO.length - 1];

                    /**
                     * Add album into array list
                     */
                    // Add album into array list
                    // Check if album not exists before, put that album into array list
                    if (!mAlAlbumVideoFolderPath.contains(VIDEO_IN_FOLDER_PATH)) {
                        /**
                         * Including into Album object by :
                         * - Extract bitmap of latest file
                         * - Get file count of one album
                         */
                        File mFileVideoFolder = new File(VIDEO_IN_FOLDER_PATH);
                        File[] mVideoList = mFileVideoFolder.listFiles();

                        ArrayList<File> mAlVideo = new ArrayList<>();

                        for (int i = 0; i < mVideoList.length; i++) {
                            mAlVideo.add(mVideoList[i]);
                        }

                        for (int i = mAlVideo.size() - 1; i >= 0; i--) {
                            if (Utils.isPhotoOrVideo(mAlVideo.get(i).getAbsolutePath())
                                    != MediaType.VIDEO) {
                                mAlVideo.remove(i);
                            }
                        }

                        int number_of_videos = getNumberOfFiles(false, VIDEO_IN_FOLDER_PATH);

                        /**
                         * Should check album name already existed or not before
                         * when putting files into array list photo,
                         * to avoid duplicate album name
                         * - If not exist, can add new Album name with new video
                         * - if already existed, just set Number of videos again.
                         */
                        if (!mAlAlbumPhotoFolderPath.contains(VIDEO_IN_FOLDER_PATH)) {
                            // If not exists
                            // Add item into Array list Videos
                            Album album = new Album(
                                    getActivity(),
                                    ALBUM_NAME_OF_VIDEO,
                                    dateTaken,
                                    VIDEO_IN_FOLDER_PATH,
                                    false);

                            // Set video number
                            album.setNumberOfVideos(number_of_videos);

                            // Get bitmap
                            increase_index_of_file = 0;
                            getBitmap(false, album, mAlVideo);

                            // Add item Album object into array list
                            mAlAlbums.add(album);
                        } else {
                            // If already existed

                            // Set Number of videos for that album
                            int j = mAlAlbumPhotoFolderPath.indexOf(VIDEO_IN_FOLDER_PATH);

                            int number_of_photos = getNumberOfFiles(true, VIDEO_IN_FOLDER_PATH);

                            mAlAlbums.get(j).setNumberOfPhotos(number_of_photos);
                            mAlAlbums.get(j).setNumberOfVideos(number_of_videos);

                            // Set Bitmap for latest video file if recognize that
                            // the taken date of video file is after than of photo
                            if (Long.valueOf(dateTaken)
                                    > Long.valueOf(mAlAlbums.get(j).getDateTaken())) {
                                increase_index_of_file = 0;
                                getBitmap(false, mAlAlbums.get(j), mAlVideo);
                            }
                        }

                        // Add album name into array list also
                        mAlAlbumVideoFolderPath.add(VIDEO_IN_FOLDER_PATH);
                    }
                }
            }

            return mAlAlbums;
        }
    }
}
