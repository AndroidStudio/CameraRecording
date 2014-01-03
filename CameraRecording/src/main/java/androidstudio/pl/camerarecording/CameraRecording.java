package androidstudio.pl.camerarecording;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("deprecation")
public class CameraRecording extends Activity {
    private final String TAG_LOG = getClass().getName();
    private SurfaceHolder surfaceHolderCamera;
    private MediaRecorder mediaRecorder;
    private final Handler handler = new Handler();
    private int defaultCamera;
    private Camera camera;
    private Button recordVideo;
    private TextView textViewrecording;
    private int videoLenght = 5;
    private EditText editTextVideoLenght;
    private EditText editTextVideoName;
    private Button switchCamera;
    private int numbersOfCamera;
    private Button previewVideo;
    private Button stoprecording;
    private String fileName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_camera_recording);
        numbersOfCamera = Camera.getNumberOfCameras();

        final SurfaceView surfaceViewCamera = (SurfaceView) findViewById(R.id.surfaceViewCamera);

        surfaceHolderCamera = surfaceViewCamera.getHolder();
        if (surfaceHolderCamera != null) {
            surfaceHolderCamera.addCallback(surfaceCallbackCamera);
            surfaceHolderCamera.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        recordVideo = (Button) findViewById(R.id.startRecording);
        recordVideo.setOnClickListener(onClickListener);

        stoprecording = (Button) findViewById(R.id.stopRecording);
        stoprecording.setOnClickListener(onStopClickListener);
        stoprecording.setEnabled(false);

        textViewrecording = (TextView) findViewById(R.id.textViewRecordingInfo);
        textViewrecording.setVisibility(View.INVISIBLE);

        editTextVideoLenght = (EditText) findViewById(R.id.editTextVideoLenght);
        editTextVideoLenght.addTextChangedListener(textWatcherLenght);
        editTextVideoName = (EditText) findViewById(R.id.editTextVideoName);
        editTextVideoName.addTextChangedListener(textWatcherName);
        switchCamera = (Button) findViewById(R.id.switchCamera);
        if (numbersOfCamera < 2) {
            switchCamera.setEnabled(false);
        } else {
            switchCamera.setOnClickListener(onCameraSwitch);
        }
        previewVideo = (Button) findViewById(R.id.previewVideo);
        previewVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CameraRecording.this, VideoList.class));
            }
        });
    }

    private final Button.OnClickListener onCameraSwitch = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (defaultCamera == 0) {
                defaultCamera = 1;
            } else {
                defaultCamera = 0;
            }
            relaseCamera();
            try {
                camera = Camera.open(defaultCamera);
                camera.setPreviewDisplay(surfaceHolderCamera);
                camera.startPreview();
            } catch (IOException e) {
                Log.e(TAG_LOG, "Error startPreview()");
            }
        }
    };

    private final TextWatcher textWatcherLenght = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            if (charSequence.length() > 0 && charSequence.length() < 4)
                setVideoLenght(Integer.parseInt(charSequence.toString()));
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };
    private final TextWatcher textWatcherName = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            setVideoName(charSequence.toString());
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private void setVideoName(String fileName) {
        this.fileName = fileName;
    }

    private String getFileName() {
        return this.fileName;
    }

    private final Button.OnClickListener onClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            //noinspection ConstantConditions
            if (editTextVideoLenght.getText().length() > 0 && editTextVideoLenght.getText().length() < 4) {
                setVideoLenght(Integer.parseInt(editTextVideoLenght.getText().toString()));
            } else {
                Toast.makeText(CameraRecording.this, "Wpisz poprawnie długość nagrania", Toast.LENGTH_LONG).show();
                editTextVideoLenght.setText("10");
                setVideoLenght(10);
            }
            if (prepareVideoRecorder()) {
                recordVideo.setEnabled(false);
                editTextVideoLenght.setEnabled(false);
                editTextVideoName.setEnabled(false);
                switchCamera.setEnabled(false);
                previewVideo.setEnabled(false);

                mediaRecorder.start();
                handler.postDelayed(runnableTime, 1000);
                textViewrecording.setVisibility(View.VISIBLE);
                textViewrecording.setText("Nagrywanie: " + getVideoLenght() + " s");

                stoprecording.setEnabled(true);
            }
        }
    };

    final private Button.OnClickListener onStopClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            relaseMediaRecorder();
        }
    };

    final private Runnable runnableTime = new Runnable() {
        @Override
        public void run() {
            setVideoLenght(getVideoLenght() - 1);
            if (getVideoLenght() <= 0) {
                textViewrecording.setText("Nagrywanie: " + 0 + " s");
                relaseMediaRecorder();
                Toast.makeText(CameraRecording.this, "Nagranie zapisane", Toast.LENGTH_LONG).show();
            } else {
                handler.postDelayed(runnableTime, 1000);
                textViewrecording.setText("Nagrywanie: " + getVideoLenght() + " s");
            }
        }
    };

    private boolean prepareVideoRecorder() {
        mediaRecorder = new MediaRecorder();
        try {
            camera.unlock();
            mediaRecorder.setCamera(camera);
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mediaRecorder.setProfile(CamcorderProfile.get(defaultCamera, CamcorderProfile.QUALITY_HIGH));
            mediaRecorder.setOutputFile(getOutputMediaFile().toString());
            mediaRecorder.setPreviewDisplay(surfaceHolderCamera.getSurface());
            mediaRecorder.prepare();
        } catch (Exception e) {
            Log.d(TAG_LOG, "Error prepareVideoRecorder()");
            relaseMediaRecorder();
            return false;
        }
        return true;
    }

    private File getOutputMediaFile() {
        final File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Videos");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        return new File(mediaStorageDir.getPath() + File.separator + "VID_" + getFileName() + ".mp4");
    }

    private void relaseMediaRecorder() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;
                camera.lock();
            } catch (Exception e) {
                Log.d(TAG_LOG, "Error relaseMediaRecorder()");
            }
        }

        editTextVideoLenght.setEnabled(true);
        recordVideo.setEnabled(true);
        editTextVideoName.setEnabled(true);
        if (numbersOfCamera > 1) switchCamera.setEnabled(true);
        previewVideo.setEnabled(true);
        stoprecording.setEnabled(false);

        textViewrecording.setVisibility(View.INVISIBLE);
        handler.removeCallbacks(runnableTime);

    }

    private Camera getCameraInstance() {
        Camera camera = null;
        final Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        try {
            for (int i = 0; i < numbersOfCamera; i++) {
                Camera.getCameraInfo(i, cameraInfo);
                this.defaultCamera = i;
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    camera = Camera.open(defaultCamera);
                    return camera;
                }
            }
            camera = Camera.open();
        } catch (Exception e) {
            Log.e(TAG_LOG, "Error open camera");
        }
        return camera;
    }

    final private SurfaceHolder.Callback surfaceCallbackCamera = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {

        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int w, int h) {
            try {
                camera = getCameraInstance();
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
            } catch (IOException e) {
                Log.e(TAG_LOG, "Error startPreview()");
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            Log.d(TAG_LOG, "surfaceDestroyed");
            relaseCamera();
        }
    };

    private void setVideoLenght(int videoLenght) {
        this.videoLenght = videoLenght;
    }

    private int getVideoLenght() {
        return this.videoLenght;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG_LOG, "onResume");
        editTextVideoLenght.setEnabled(true);
        recordVideo.setEnabled(true);
        editTextVideoName.setEnabled(true);
        if (numbersOfCamera > 1) switchCamera.setEnabled(true);
        previewVideo.setEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG_LOG, "onPause");
        relaseMediaRecorder();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG_LOG, "onPause");
        relaseMediaRecorder();
        System.exit(0);
    }

    private void relaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }
}