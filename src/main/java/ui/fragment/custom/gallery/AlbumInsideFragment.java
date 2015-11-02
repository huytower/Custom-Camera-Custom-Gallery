package ui.fragment.custom.gallery;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.io.File;
import java.util.ArrayList;

import adapter.custom.gallery.AlbumInsideAdapter;
import custom_view.MarkableImageView;
import define.MediaType;
import define.Receiver;
import mirrortowers.custom_camera_gallery_library.R;
import ui.activity.custom.gallery.CustomGallery;
import utils.Utils;

public class AlbumInsideFragment extends Fragment
        implements AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    public static final String ARGUMENT_ALBUM_NAME = "ALBUM_NAME";
    public static final String ARGUMENT_FOLDER_PATH = "FOLDER_PATH";
    /**
     * Data section
     */
    public static ArrayList<model.File> mAlFilesInAlbum = new ArrayList<>();
    public static ArrayList<Integer> mAlSelectedIndex = new ArrayList<>();

    /**
     * String section
     */
    public static boolean IS_IN_ALBUM_INSIDE_PAGE = false;
    /**
     * Adapter section
     */
    private AlbumInsideAdapter albumInsideAdapter;
    /**
     * View section
     */
    private GridView mGv;
    private SwipeRefreshLayout mSrl;

    /**
     * @return
     */
    public static Fragment newInstance() {
        AlbumInsideFragment fragment = new AlbumInsideFragment();
        return fragment;
    }

    /**
     * Others section
     */

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        IS_IN_ALBUM_INSIDE_PAGE = true;
        /**
         * Set Orientation for page
         */
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        View v = getLayoutInflater(savedInstanceState).inflate(
                R.layout.fragment_album_inside, container, false);

        // Initial views
        initialViews(v);
        initialData();

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        /**
         * Need set text color correctly :
         * - if chose some files, need show orange text
         * - if nothing, need show gray text
         */
        if (mAlSelectedIndex.size() == 0)
            CustomGallery.mTvUpload.setTextColor(
                    getActivity().getResources().getColor(R.color.gray));
        else
            CustomGallery.mTvUpload.setTextColor(
                    getActivity().getResources().getColor(R.color.orange_light));

        // Not be in Album Inside page anymore
        IS_IN_ALBUM_INSIDE_PAGE = false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
        // on Select Item
        onSelectItem(v, pos, mAlFilesInAlbum);
    }

    @Override
    public void onRefresh() {
        // what's thing need refresh when pull down, need place in here
        try {
            mAlFilesInAlbum.clear();

            // Call thread get album list
            getAlbumInside();
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
        }, 1000);
    }

    /**
     * Initialize methods
     */

    private void initialData() {
        // Set listener
        mGv.setOnItemClickListener(this);
        mSrl.setOnRefreshListener(this);

        // Call thread to get album inside album
        getAlbumInside();
    }

    private void initialViews(View v) {
        mGv = (GridView) v.findViewById(R.id.gv_in_fragment_album_inside);
        mSrl = (SwipeRefreshLayout) v.findViewById(R.id.srl_refresh_in_fragment_album_inside);
    }

    /**
     * The others methods
     */

    private void getAlbumInside() {
        // Call thread to get album inside album
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            new GetAlbumInsideAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            new GetAlbumInsideAsync().execute();
    }

    private void onSelectItem(View v, int pos, ArrayList<model.File> mAl) {
        /**
         * Need check action is single choice or multiple choice, to consider :
         * - Single choice : Replace & Upload avatar
         * - Multiple choice : Upload feature
         */

        if (CustomGallery.ACTION.equals(Receiver.ACTION_CHOSE_MULTIPLE_FILE)) {
            /**
             * Multiple choice
             */

            /**
             * Show Overlay image to check
             */
            if (mAl.get(pos).isChecked()) {
                /**
                 * If selected item already chose
                 */
                /**
                 * Hide checked image
                 */
                ((MarkableImageView) v).setChecked(false);
                mAl.get(pos).setChecked(false);

                /**
                 * Remove item out of array list was defined to store selected items
                 */
                mAlSelectedIndex.remove(mAlSelectedIndex.indexOf(pos));
            } else {
                /**
                 * If selected item has not already chose
                 */

                /**
                 * Show checked image
                 */
                ((MarkableImageView) v).setChecked(true);
                mAl.get(pos).setChecked(true);

                /**
                 * Add to array list was defined to store selected items
                 */
                mAlSelectedIndex.add(pos);
            }

            /**
             * Always Update new selected items in Text View also
             */
            CustomGallery.mTvUpload.setText(getString(R.string.upload));

            if (mAlSelectedIndex.size() == 0)
                CustomGallery.mTvUpload.setTextColor(getActivity().getResources().getColor(R.color.gray));
            else
                CustomGallery.mTvUpload.setTextColor(getActivity().getResources().getColor(R.color.orange_light));
        } else if (CustomGallery.ACTION.equals(Receiver.ACTION_CHOSE_SINGLE_FILE)) {
            /**
             * Single choice
             */

            // Clear old list before get new check item
            mAlSelectedIndex.clear();
            for (model.File item : mAl)
                item.setChecked(false);

            /**
             * Show checked image
             */
            ((MarkableImageView) v).setChecked(true);
            mAl.get(pos).setChecked(true);

            /**
             * Add to array list was defined to store selected items
             */
            mAlSelectedIndex.add(pos);

            // Enable upload text
            if (mAlSelectedIndex.size() == 0)
                CustomGallery.mTvUpload.setTextColor(getActivity().getResources().getColor(R.color.gray));
            else
                CustomGallery.mTvUpload.setTextColor(getActivity().getResources().getColor(R.color.orange_light));

            // notify data set change
            albumInsideAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Class section
     */
    private class GetAlbumInsideAsync extends AsyncTask<String, Void, Void> {

        private ArrayList<File> mAlFileInside = new ArrayList<>();

        private String ALBUM_NAME;
        private String FOLDER_PATH;

        @Override
        protected Void doInBackground(String... params) {
            // Get folder list to shown on page
            /**
             * Read all photos & videos inside selected Album without taking care
             * about sub-folder inside.
             * Need list all of them.
             */
            mAlFileInside = getAlbumInside(FOLDER_PATH);
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /**
             * Get folder path after selected Album
             */
            ALBUM_NAME = getArguments().getString(ARGUMENT_ALBUM_NAME);
            FOLDER_PATH = getArguments().getString(ARGUMENT_FOLDER_PATH);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            /**
             * Need set Title is selected Album
             */
            CustomGallery.mTvAlbumName.setText(ALBUM_NAME);

            // Should clear old data before add new data
            if (!mAlFilesInAlbum.isEmpty()) mAlFilesInAlbum.clear();

            // Add item into array list
            try {
                for (int i = 0; i < mAlFileInside.size(); i++) {
                    File mFile = new File(mAlFileInside.get(i).getAbsolutePath());

                    if (mAlFileInside.get(i).isFile()) {
                        // Get image Uri from File path
                        Uri mUri = null;
                        if (Utils.isPhotoOrVideo(mFile.getAbsolutePath()) == MediaType.PHOTO)
                            mUri = Utils.getImagePreviewOfUri(
                                    true, getActivity(), mFile);
                        else if (Utils.isPhotoOrVideo(mFile.getAbsolutePath()) == MediaType.VIDEO)
                            mUri = Utils.getImagePreviewOfUri(
                                    false, getActivity(), mFile);

                        // Should get real file path from URI also
                        String FILE_PATH = Utils.getRealPathFromURI(getActivity(), mUri);

                        if (mFile.exists() && mFile.isFile()) {
                            /**
                             * Is file inside selected folder
                             */
                            // Add into array list
                            model.File file = new model.File(
                                    getActivity(),
                                    mAlFileInside.get(i).getName(),
                                    FILE_PATH, false);

                            // Check file type to see it is photo or video
                            if (Utils.isPhotoOrVideo(mFile.getAbsolutePath()) == MediaType.PHOTO)
                                file.setVideo(false);
                            else if (Utils.isPhotoOrVideo(mFile.getAbsolutePath()) == MediaType.VIDEO) {
                                file.setVideo(true);

                                // set duration of video
                                file.setDurationOfVideo(Utils.getDurationOfVideo(
                                        getActivity(), mFile.getAbsolutePath()));
                            }

                            // Add item into array list
                            mAlFilesInAlbum.add(0, file);
                        } else if (mFile.exists() & mFile.isDirectory()) {
                            /**
                             * Is directory, need access more files inside sub-folder
                             */
                            ArrayList<File> mAl = new ArrayList<>();
                            mAl = Utils.getPhotoAndVideoFromSdCard(
                                    mAl, new File(mFile.getAbsolutePath()));

                            for (int j = 0; j < mAl.size(); j++) {
                                // Add into array list
                                if (mAl.get(j).isFile()) {
                                    model.File file = new model.File(
                                            getActivity(),
                                            mAlFileInside.get(j).getName(),
                                            FILE_PATH, false);

                                    // Check file type to see it is photo or video
                                    if (Utils.isPhotoOrVideo(mFile.getAbsolutePath()) == MediaType.PHOTO)
                                        file.setVideo(false);
                                    else if (Utils.isPhotoOrVideo(mFile.getAbsolutePath()) == MediaType.VIDEO) {
                                        file.setVideo(true);

                                        // set duration of video
                                        file.setDurationOfVideo(Utils.getDurationOfVideo(
                                                getActivity(), mFile.getAbsolutePath()));
                                    }

                                    /**
                                     * Add into array list as files inside the selected folder
                                     * to show on UI of Custom Gallery
                                     */
                                    mAlFilesInAlbum.add(0, file);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Set adapter
            albumInsideAdapter = new AlbumInsideAdapter(
                    getActivity(),
                    R.layout.simple_grid_item_in_fragment_album_inside,
                    mAlFilesInAlbum);
            mGv.setAdapter(albumInsideAdapter);
        }

        private ArrayList<File> getAlbumInside(String FOLDER_PATH) {
            // Get thumbnail list from Gallery
            File mFileFolder = new File(FOLDER_PATH);

            ArrayList<File> mAlFolders = new ArrayList<>();
            /**
             * This method to get only files inside selected folder, not included sub-folder :
             * getPhotoAndVideoFromSdCard(false, mAlFolders, mFileFolder)
             * If want to include files in sub-folder also, need use below method :
             * getPhotoAndVideoFromSdCard(mAlFolders, mFileFolder)
             */
            mAlFolders = Utils.getPhotoAndVideoFromSdCard(false, mAlFolders, mFileFolder);

            return mAlFolders;
        }

    }
}
