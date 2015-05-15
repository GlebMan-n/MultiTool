package com.example.glebmanyagin.multitool;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class CameraActivity extends ActionBarActivity {

    SurfaceView surfaceView;
    Camera camera;
    int iCamId = 0;
    SurfaceHolder holder = null;
    boolean bFrontCam = false;
    int numCAMS = Camera.getNumberOfCameras();

    File videoFile;
    final File pictures = Environment

            .getExternalStorageDirectory();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);



        //videoFile = new File(pictures, date + ".3gp");

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        holder = surfaceView.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    camera.setPreviewDisplay(holder);
                    camera.startPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format,
                                       int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });


        ImageButton makePhotoBut = (ImageButton) findViewById(R.id.makePhotoBut);
        ImageButton anotherCamBut = (ImageButton) findViewById(R.id.anotherCam);
        ImageButton flashLight = (ImageButton) findViewById(R.id.flashlight);


        makePhotoBut.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {


                camera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        try {
                            Bitmap bmp;
                            bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
                            String title = "IMG_" + df.format(System.currentTimeMillis());
                            MediaStore.Images.Media.insertImage(getContentResolver(), bmp, title , "PhotoMultiTool");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        openCam();
                    }
                });

            }
        });

        anotherCamBut.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                bFrontCam = !bFrontCam;
                openCam();
            }
        });

        flashLight.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    if(camera == null)
                        return;
                    Camera.Parameters p = camera.getParameters();
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    camera.setParameters(p);
                    camera.startPreview();
                }
                catch (Exception e)
                {
                    Log.d("camera",e.toString());
                }
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camera, menu);
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

    @Override
    protected void onResume() {
        super.onResume();
        openCam();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camera != null)
            camera.release();
        camera = null;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (camera != null) {
            camera.stopPreview();
            camera.release();
        }
    }

    private boolean isCameraSupported(PackageManager packageManager) {
        // if device support camera?
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        }
        return false;
    }

    //front 0 | 1
    private int getFrontCam(boolean front)
    {
        if(numCAMS <2) {

            return 0;
        }
        int iFront = 1;

        if (!front)
            iFront = 0;

        for (int i = 0; i < numCAMS; i++)
        {
            ArrayList<Camera.CameraInfo> infoCamArray = new ArrayList<Camera.CameraInfo>();
            Camera.CameraInfo camInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(i,camInfo);
            infoCamArray.add(camInfo);
            if(camInfo.facing == iFront)
                return i;
        }
        return -1;
    }

    private void openCam()
    {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }

        int frCam = getFrontCam(bFrontCam);
        camera = Camera.open(frCam);
        try {
            camera.setPreviewDisplay(holder);

        } catch (Exception e) {
            e.printStackTrace();
        }

        camera.startPreview();

        int rotate = getWindowManager().getDefaultDisplay().getRotation();
        switch (rotate) {
            case Surface.ROTATION_0:
                camera.setDisplayOrientation(270);
                break;
            case Surface.ROTATION_90:
                camera.setDisplayOrientation(180);
                break;
            case Surface.ROTATION_180:
                camera.setDisplayOrientation(90);
                break;
            case Surface.ROTATION_270:
                camera.setDisplayOrientation(0);
                break;
            default:
                break;
        }
    }
}
