package com.hiiyl.piqual;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.orm.query.Select;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 5;
    GalleryAdapter mAdapter;
    RecyclerView mRecyclerView;

    private FloatingActionButton downloadFAB;

    ArrayList<ImageModel> data = new ArrayList<>();


//    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;

    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e("FILE","IO EXCEPTION BRUH");
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.hiiyl.piqual.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }else{
                Log.e("FILE2","IO EXCEPTION BRUH");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");

            Bitmap imageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            LayoutInflater inflater = MainActivity.this.getLayoutInflater();
            ViewGroup vg = (ViewGroup)inflater.inflate(R.layout.image_taken_dialog, null);
            ImageView image = (ImageView) vg.findViewById(R.id.captured_image);
            image.setImageBitmap(imageBitmap);

            final TextView ratingTextView = (TextView) vg.findViewById(R.id.rating_textview);
            builder.setView(vg);

            final ImageModel _imageModel = new ImageModel();
            _imageModel.setName("test");
            _imageModel.setUrl(mCurrentPhotoPath);
            _imageModel.save();

            String uploadId = UUID.randomUUID().toString();

            //Creating a multi part request
            try {
                new MultipartUploadRequest(MainActivity.this, uploadId, "http://192.168.1.130:5000/api")
                        .addFileToUpload(mCurrentPhotoPath, "file") //Adding file
                        .addParameter("name", uploadId) //Adding text parameter to the request
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
                                // if you have ma pped your server response to a POJO, you can easily get it:
                                try {
                                    JSONObject mainObject = new JSONObject(serverResponse.getBodyAsString());
                                    String  score = mainObject.getString("score");
                                    _imageModel.setRating(Float.parseFloat(score));
                                    _imageModel.save();
                                    ratingTextView.setText(String.format("%.2f", Float.parseFloat(score)));

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }

                            @Override
                            public void onCancelled(UploadInfo uploadInfo) {

                            }
                        })
                        .startUpload(); //Starting the upload
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }


//            builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int id) {
//                            // sign in the user ...
//                        }
//                    })
//                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            dialog.cancel();
//                        }
//                    });
            builder.show();
//
//            ImageModel imageModel = new ImageModel();
//            imageModel.setUrl(((Uri) extras.get(MediaStore.EXTRA_OUTPUT)).getPath());
        }else{
//            Log.e("WTF", String.valueOf(resultCode));
        }
    }

    public static ArrayList<String> getImagesPath(Activity activity) {
        Uri uri;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        String PathOfImage = null;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = { MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME };

        cursor = activity.getContentResolver().query(uri, projection, null,
                null, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            PathOfImage = cursor.getString(column_index_data);
            listOfAllImages.add(PathOfImage);
        }
        cursor.close();
        return listOfAllImages;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {



                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
    
                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                }
    
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
    
                // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                // app-defined int constant
    
                return;
            }
        }

        data = new ArrayList<ImageModel>(Select.from(ImageModel.class).orderBy("rating desc").list());
        if(data.size() <= 0){
            Log.d("Images", "Reading from Cursor");
            ArrayList<String>localImages = getImagesPath(this);
            for(String imageUrl : localImages) {
                ImageModel imageModel = new ImageModel();
                int index = imageUrl.lastIndexOf(File.separator);
                String fileName = imageUrl.substring(index + 1);
                imageModel.setName("Test");
                //Log.d("LOADING IMAGES",imageUrl);
                imageModel.setUrl(imageUrl);
//                data.add(imageModel);
                imageModel.save();
            }
        }else {
            Log.d("Images", "Loaded from memory");
        }
        data = new ArrayList<ImageModel>(Select.from(ImageModel.class).orderBy("rating desc").list());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.setHasFixedSize(true);


        mAdapter = new GalleryAdapter(MainActivity.this, data);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                        intent.putParcelableArrayListExtra("data", data);
                        intent.putExtra("pos", position);
                        startActivity(intent);
                    }
                }));
        downloadFAB = (FloatingActionButton) findViewById(R.id.download_fab);

        downloadFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
//                for(ImageModel imageModel : data) {
//                    if(imageModel.getRating() == 0.0f) {
//                        new CompressAndUploadRunnable(imageModel).execute();
//                    }
//                }
            }
        });
    }
    public void uploadMultipart(String imagePath, final ImageModel _imageModel) {
        Log.d("UPLOAD", "UPLOAD MULTIPART STARTED");
        //Uploading code
        try {
            String uploadId = UUID.randomUUID().toString();

            //Creating a multi part request
            new MultipartUploadRequest(MainActivity.this, uploadId, "http://192.168.0.151:5000/api")
                    .addFileToUpload(imagePath, "file") //Adding file
                    .addParameter("name", uploadId) //Adding text parameter to the request
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
                                Toast.makeText(MainActivity.this, "Score of Image is : " + score , Toast.LENGTH_SHORT).show();

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
            Toast.makeText(MainActivity.this, exc.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    public class CompressAndUploadRunnable extends AsyncTask<Void,Void,Void> {
        String compressedImageUrl;

        ImageModel _imageModel;

        CompressAndUploadRunnable(ImageModel imageModel) {
            this._imageModel = imageModel;
        }

        @Override
        protected Void doInBackground(Void... params) {
            compressedImageUrl = compressImage(_imageModel.getUrl());
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            Log.d("UPLOAD", "POST EXECUTE");
            uploadMultipart(compressedImageUrl, _imageModel);
        }


    }


    public String compressImage(String file_to_compress) {
        Bitmap image = Bitmap.createScaledBitmap (BitmapFactory.decodeFile(file_to_compress), 224, 224, false);
        return putBitmapInDiskCache(file_to_compress, image);
    }
    private String putBitmapInDiskCache(String url, Bitmap avatar) {
        // Create a path pointing to the system-recommended cache dir for the app, with sub-dir named
        // thumbnails
        File cacheDir = new   File(MainActivity.this.getCacheDir(), "thumbnails");

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

}
