package com.hiiyl.piqual;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hiiyl.piqual.R;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class DetailActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    public ArrayList<ImageModel> data = new ArrayList<>();
    int pos;

    Toolbar toolbar;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        data = getIntent().getParcelableArrayListExtra("data");
        pos = getIntent().getIntExtra("pos", 0);

        setTitle("Gallery");

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), data);
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setPageTransformer(true, new DepthPageTransformer());

        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(pos);

        ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mSectionsPagerAdapter.getCurrentFragment().displayScore();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };

        mViewPager.addOnPageChangeListener(onPageChangeListener);
//        onPageChangeListener.onPageSelected(pos);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public ArrayList<ImageModel> data = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager fm, ArrayList<ImageModel> data) {
            super(fm);
            this.data = data;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position, data.get(position));
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return data.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "TEST";
        }

        private PlaceholderFragment mCurrentFragment;

        public PlaceholderFragment getCurrentFragment() {
            return mCurrentFragment;
        }
        //...
        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            if (getCurrentFragment() != object) {
                mCurrentFragment = ((PlaceholderFragment) object);
            }
            super.setPrimaryItem(container, position, object);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        private static final String KEY_IMAGE = "image";
        private static final String KEY_NAME = "name";
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        ImageModel _imageModel;
        int pos;
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final String ARG_IMG_TITLE = "image_title";
        private static final String ARG_IMG_URL = "image_url";
        private static final String ARG_IMG = "image";

        private TextView ratingTextView;
        private ImageView likeImageView;

        @Override
        public void setArguments(Bundle args) {
            super.setArguments(args);
            this.pos = args.getInt(ARG_SECTION_NUMBER);
            this._imageModel = args.getParcelable(ARG_IMG);
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber, ImageModel imageModel ) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putParcelable(ARG_IMG, imageModel);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            // handle fragment arguments
            Bundle args = getArguments();
            if(args != null)
            {
                _imageModel = args.getParcelable(ARG_IMG);
                pos = args.getInt(ARG_SECTION_NUMBER);
            }
        }

        public PlaceholderFragment() {
        }

        @Override
        public void onStart() {
            super.onStart();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            final ImageView imageView = (ImageView) rootView.findViewById(R.id.detail_image);

            Glide.with(getActivity()).load(_imageModel.getUrl()).thumbnail(0.1f).into(imageView);

            ratingTextView = (TextView) rootView.findViewById(R.id.rating_textview);
            likeImageView = (ImageView) rootView.findViewById(R.id.like_imageview);

            if(_imageModel.getRating() != 0.0f) {
                likeImageView.setVisibility(View.VISIBLE);
                ratingTextView.setText(String.valueOf(_imageModel.getRating()));
                if (_imageModel.getRating() < 5.0f) {
                    likeImageView.setRotationX(180);
                }
            }
            return rootView;
        }

        public class CompressAndUploadRunnable extends AsyncTask<Void,Void,Void>{
            String file_to_compress,compressedImageUrl;

            CompressAndUploadRunnable(String file_to_compress) {
                this.file_to_compress = file_to_compress;
            }

            public void setFile(String file_to_compress) {
                this.file_to_compress = file_to_compress;
            }

            @Override
            protected Void doInBackground(Void... params) {
                compressedImageUrl = compressImage(file_to_compress);
                return null;
            }
            @Override
            protected void onPostExecute(Void result) {
                uploadMultipart(compressedImageUrl);
            }


        }


        public String compressImage(String file_to_compress) {
            Bitmap image = Bitmap.createScaledBitmap (BitmapFactory.decodeFile(file_to_compress), 224, 224, false);
            return putBitmapInDiskCache(file_to_compress, image);
        }
        private String putBitmapInDiskCache(String url, Bitmap avatar) {
            // Create a path pointing to the system-recommended cache dir for the app, with sub-dir named
            // thumbnails
            File cacheDir = new   File(getActivity().getCacheDir(), "thumbnails");

            if(!cacheDir.exists()) cacheDir.mkdir();
            // Create a path in that dir for a file, named by the default hash of the url
            String[] temp = url.split("/");
            String filename = temp[temp.length -1];
            File cacheFile = new File(cacheDir, filename);
            try {
                // Create a file at the file path, and open it for writing obtaining the output stream
                cacheFile.createNewFile();
                FileOutputStream fos = new FileOutputStream(cacheFile);
                // Write the bitmap to the output stream (and thus the file) in PNG format (lossless compression)
                avatar.compress(Bitmap.CompressFormat.PNG, 100, fos);
                // Flush and close the output stream
                fos.flush();
                fos.close();
            } catch (Exception e) {
                // Log anything that might go wrong with IO to file
                Log.e("TEST", "Error when saving image to cache. ", e);
            }
            Log.d("CACHEFILEPATH", cacheFile.getPath());
            return cacheFile.getPath();
        }

        public void uploadMultipart(String imagePath) {
            //Uploading code
            try {
                String uploadId = UUID.randomUUID().toString();

                //Creating a multi part request
                new MultipartUploadRequest(getActivity(), uploadId, "http://192.168.0.105:5000/api")
                        .addFileToUpload(imagePath, "file") //Adding file
                        .addParameter("name", _imageModel.getName()) //Adding text parameter to the request
                        .setNotificationConfig(new UploadNotificationConfig().setAutoClearOnSuccess(true).setRingToneEnabled(false))
                        .setMaxRetries(2)
                        .setDelegate(new UploadStatusDelegate() {
                            @Override
                            public void onProgress(UploadInfo uploadInfo) {

                            }

                            @Override
                            public void onError(UploadInfo uploadInfo, Exception exception) {

                            }

                            @Override
                            public void onCompleted(UploadInfo uploadInfo, ServerResponse serverResponse) {
                                // your code here
                                // if you have mapped your server response to a POJO, you can easily get it:
                                try {
                                    JSONObject mainObject = new JSONObject(serverResponse.getBodyAsString());
                                    String  score = mainObject.getString("score");
                                    _imageModel.setRating(Float.parseFloat(score));
                                    _imageModel.save();
                                    Toast.makeText(getActivity(), "Score of Image is : " + score , Toast.LENGTH_SHORT).show();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }

                            @Override
                            public void onCancelled(UploadInfo uploadInfo) {

                            }
                        })
                        .startUpload(); //Starting the upload

            } catch (Exception exc) {
                Toast.makeText(getActivity(), exc.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }


        public void displayScore() {
            if (_imageModel.getRating() == 0.0f) {
                new CompressAndUploadRunnable(_imageModel.getUrl()).execute();
            } else {
//                Toast.makeText(getActivity(), "(CACHED) Score of Image is : " + _imageModel.getRating() , Toast.LENGTH_SHORT).show();
            }
        }
    }
}
