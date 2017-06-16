package com.example.sooji.myapplication;

    import java.util.List;
    import java.util.ArrayList;

    import java.io.File;
    import java.io.FileNotFoundException;
    import java.io.FileOutputStream;
    import java.io.IOException;
    import java.text.SimpleDateFormat;
    import java.util.Date;

    import android.app.Activity;
    import android.content.Context;
    import android.content.Intent;
    import android.content.SharedPreferences;
    import android.content.pm.PackageManager;
    import android.hardware.Camera;
    import android.hardware.Camera.CameraInfo;
    import android.hardware.Camera.PictureCallback;
    import android.media.AudioManager;
    import android.net.Uri;
    import android.os.Bundle;
    import android.os.Environment;
    import android.util.Log;
    import android.view.View;
    import android.view.View.OnClickListener;
    import android.view.WindowManager;
    import android.widget.Button;
    import android.widget.LinearLayout;
    import android.widget.Toast;
    import android.hardware.Camera.Parameters;

public class AndroidCameraExample extends Activity {
    private static final int EMAIL_SENT = 1; // request code
    private Camera mCamera;
    private CameraPreview mPreview;
    private PictureCallback mPicture;
    private Button capture, switchCamera, finishedButton, exitButton;
    private Context myContext;
    private LinearLayout cameraPreview;
    private boolean cameraFront = false;
    private ArrayList<String> imageFiles;
    private int counter = 0;
    private int limit = 7;
    private int minExposure;
    private int maxExposure;
    private int currentExposure;
    private String emailBody;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        myContext = this;

        Bundle extras = getIntent().getExtras();
        emailBody = extras.getString("resultString");

        Log.v("Tag", emailBody);

        initialize();
    }

    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }

        }
        return cameraId;
    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        //Search for the back facing camera
        //get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        //for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;
            }
        }
        return cameraId;
    }

    public void onResume() {
        super.onResume();
        if (!hasCamera(myContext)) {
            Toast toast = Toast.makeText(myContext, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
        if (mCamera == null) {
            //if the front facing camera does not exist
            if (findFrontFacingCamera() < 0) {
                Toast.makeText(this, "No front facing camera found.", Toast.LENGTH_LONG).show();
                switchCamera.setVisibility(View.GONE);
            }
            mCamera = Camera.open(findBackFacingCamera());
            mPicture = getPictureCallback();
            mPreview.refreshCamera(mCamera);
        }
    }

    public void initialize() {
        cameraPreview = (LinearLayout) findViewById(R.id.camera_preview);
        mPreview = new CameraPreview(myContext, mCamera);

        cameraPreview.addView(mPreview);

        capture = (Button) findViewById(R.id.button_capture);
        capture.setOnClickListener(captrureListener);

        switchCamera = (Button) findViewById(R.id.button_ChangeCamera);
        //switchCamera.setOnClickListener(switchCameraListener);
        switchCamera.setVisibility(View.GONE);

        finishedButton = (Button) findViewById(R.id.finishedButton);
        //finishedButton.setOnClickListener(finishedListener);

        exitButton = (Button) findViewById(R.id.exitButton);
        exitButton.setOnClickListener(exitListener);
        imageFiles = new ArrayList<>();

//        Parameters params = mCamera.getParameters();
//
//        minExposure = mCamera.getParameters().getMinExposureCompensation();
//        maxExposure = mCamera.getParameters().getMaxExposureCompensation();
//        //currentExposure  = mCamera.getParameters().getExposureCompensation();
//        currentExposure = minExposure;
//        // Set it to it's lowest
//        params.setExposureCompensation(currentExposure);
//        mCamera.setParameters(params);

    }

    OnClickListener switchCameraListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            //get the number of cameras
            int camerasNumber = Camera.getNumberOfCameras();
            if (camerasNumber > 1) {
                //release the old camera instance
                //switch camera, from the front and the back and vice versa
                releaseCamera();
                chooseCamera();

            } else {
                Toast toast = Toast.makeText(myContext, "Sorry, your phone has only one camera!", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    };

    public void chooseCamera() {
        //if the camera preview is the front
        if (cameraFront) {
            int cameraId = findBackFacingCamera();
            if (cameraId >= 0) {
                //open the backFacingCamera
                //set a picture callback
                //refresh the preview

                mCamera = Camera.open(cameraId);
                mPicture = getPictureCallback();
                mPreview.refreshCamera(mCamera);
            }
        } else {
            int cameraId = findFrontFacingCamera();
            if (cameraId >= 0) {
                //open the backFacingCamera
                //set a picture callback
                //refresh the preview

                mCamera = Camera.open(cameraId);
                mPicture = getPictureCallback();
                mPreview.refreshCamera(mCamera);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //when on Pause, release camera in order to be used from other applications
        releaseCamera();
    }

    private boolean hasCamera(Context context) {
        //check if the device has camera
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    private PictureCallback getPictureCallback() {
        PictureCallback picture = new PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                //make a new picture file
                File pictureFile = getOutputMediaFile();

                if (pictureFile == null) {
                    return;
                }
                try {
                    //write the file
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();

                    imageFiles.add(pictureFile.getName());
                    //Toast toast = Toast.makeText(myContext, "Picture saved: " + pictureFile.getAbsolutePath(), Toast.LENGTH_LONG);
                    //toast.show();

                } catch (FileNotFoundException e) {
                } catch (IOException e) {
                } finally {
                    // Re enable button
                    finishedButton.setEnabled(true);
                    finishedButton.setVisibility(View.VISIBLE);
                    mPreview.refreshCamera(mCamera);
                    if(counter==limit) {
                        sendEmail();
                    } else {
                        counter++;
                        mCamera.takePicture(null, null, mPicture);
                        finishedButton.setText(Integer.toString(counter) + "/7");
                        finishedButton.setEnabled(false);

                    }
                }

                //refresh camera to continue preview



            }

        };
        return picture;
    }

    OnClickListener captrureListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (counter != limit) {

                mCamera.takePicture(null, null, mPicture);
                counter++;



//                // Change exposure for the next picture
//                Parameters params = mCamera.getParameters();
//
//                if(currentExposure < maxExposure + 1) {
//                    currentExposure += 2;
//                }
//
//                params.setExposureCompensation(currentExposure);
//                mCamera.setParameters(params)

                //finishedButton.setEnabled(false);
                //finishedButton.setVisibility(View.GONE);


                Toast toast = Toast.makeText(myContext, "Taking Multiple Photos... \n Please Wait. ", Toast.LENGTH_LONG);
                toast.show();
            } else {
                Toast toast = Toast.makeText(myContext, "Can't take anymore pictures. Exit Now.", Toast.LENGTH_LONG);
                toast.show();

            }
        }
    };



    OnClickListener finishedListener = new OnClickListener() {
        public void onClick(View v) {
            cameraPreview.removeView(mPreview);
        }
    };

    OnClickListener exitListener = new OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(myContext, MainActivity.class);
            startActivity(intent);
        }
    };

    //make picture and save to a folder
    private static File getOutputMediaFile() {
        //make a new file directory inside the "sdcard" folder
        File root = Environment.getExternalStorageDirectory();
        File mediaStorageDir = new File(root, "surveyCameraApp");

        Log.d("CameraActivity", "Root folder found " + root);

        //if this "JCGCamera folder does not exist
        if (!mediaStorageDir.exists()) {
            //if you cannot make this folder return
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        //take the current timeStamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        //and make a media file:
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    private void releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    private void sendEmail() {
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE );
        //emailIntent.setType("application/image");
        // Edit email body

        emailBody = "Hello Supervisor, \n\n\nBelow is the survey: " + emailBody + "\n\n and attached are the 7 images";

        SharedPreferences settings = getSharedPreferences(Settings.SETTINGS, MODE_PRIVATE);
        String emailValue = settings.getString("email","nachosandmagic@gmail.com");

        emailIntent.setType("text/plain");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{emailValue});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,"Survey Camera App");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, emailBody);

        File root = Environment.getExternalStorageDirectory();
        ArrayList<Uri> uris = new ArrayList<Uri>();

        for(String imageFile : imageFiles) {
//            File fileToAttach = new File(root, "surveyCameraApp/" + imageFile );
//            uris.add(Uri.fromFile(fileToAttach));

            File fileIn = new File(root, "surveyCameraApp/" + imageFile);
            Uri u = Uri.fromFile(fileIn);
            uris.add(u);
        }
        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }


}