package com.quentinjian.receiptlottery;

import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraX;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.CameraController;
import androidx.camera.view.LifecycleCameraController;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.interfaces.Detector;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.quentinjian.receiptlottery.databinding.ActivityCameraBinding;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kotlin.Unit;

public class CameraActivity extends AppCompatActivity implements TextAnalyzer.TextResultCallback {
    private ImageCapture imageCapture;
    private File outputDirectory;
    private ExecutorService cameraExecutor;
    private PreviewView viewFinder;
    private OverlayView overlayView;
    private ImageAnalysis imageAnalyzer;
    private TextView showNumber;
    private List<String> result;
//    private Detector detector = TextRecognition.getClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        viewFinder = findViewById(R.id.viewFinder);
        overlayView = findViewById(R.id.overlay_view);
        showNumber = findViewById(R.id.show_number);
//        Button cameraCaptureButton = findViewById(R.id.image_capture_button);
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, CamConfig.REQUIRED_PERMISSIONS, CamConfig.REQUEST_CODE_PERMISSIONS);
        }
        cameraExecutor = Executors.newSingleThreadExecutor();
        outputDirectory = getOutputDirectory();

//        cameraCaptureButton.setOnClickListener(v -> takePhoto());
    }

    private void takePhoto() {
        if (imageCapture != null) {
            File photoFile = new File(outputDirectory,
                    new SimpleDateFormat(CamConfig.FILENAME_FORMAT,
                            Locale.getDefault()).format(System.currentTimeMillis()) + ".jpg");
            ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions
                    .Builder(photoFile).build();
            imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this),
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults
                                                         outputFileResults) {
                            Uri savedUri = Uri.fromFile(photoFile);
                            String msg = "Image Captured Successfully! " + savedUri;
                            Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
                            Log.d(CamConfig.TAG, msg);
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            Log.e(CamConfig.TAG, "Photo Capture Failed: " + exception);
                        }
                    });
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider
                .getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider processCameraProvider = cameraProviderFuture.get();

                // Build the preview use case.
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                // Select back camera as a default
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // Build the image analyzer use case which will handle text recognition.
                imageAnalyzer = new ImageAnalysis.Builder()
                        .setTargetRotation(viewFinder.getDisplay().getRotation())
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalyzer.setAnalyzer(cameraExecutor,
                        new TextAnalyzer(CameraActivity.this, getLifecycle(), this));

                // Unbind use cases before rebinding
                processCameraProvider.unbindAll();

                // Bind the camera use cases to the lifecycle including both preview and analysis.
                processCameraProvider.bindToLifecycle((LifecycleOwner) CameraActivity.this,
                        cameraSelector, preview, imageAnalyzer);
            } catch (Exception e) {
                Log.e(CamConfig.TAG, "Camera binding failed" + e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private boolean allPermissionsGranted() {
        for (String permission : CamConfig.REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private File getOutputDirectory() {
        File mediaDir = new File(getExternalMediaDirs()[0], getString(R.string.app_name));
        boolean isExist = mediaDir.exists() || mediaDir.mkdir();
        return isExist ? mediaDir : null;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    private String findDesiredText(Text result) {
        Pattern pattern = Pattern.compile("[a-z][a-z]-[0-9]{8}",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(result.getText());
        if (matcher.find()) {
            Log.d("TestTextFound", Objects.requireNonNull(matcher.group(0)));
            showNumber.setText(matcher.group(0));
            return Objects.requireNonNull(matcher.group(0)).substring(3);
        }
        showNumber.setText("無辨識到號碼");
        return "00000000";
    }

    private final ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (allPermissionsGranted())
                    startCamera();
                else {
                    Toast toast = Toast.makeText(this, "Permission not granted.",
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
            });

    @Override
    public void onTextFound(Text recognizedText) {
        String invoiceNumber = findDesiredText(recognizedText);
//        overlayView.updateDetectedCode(invoiceNumber);
        PrizeType type = CheckPrize.determinePrice(invoiceNumber);
        TextView numberIndicator = findViewById(R.id.number_indicator);
        switch (type) {
            case SPECIAL:
                numberIndicator.setText("特別獎");
                numberIndicator.setTextColor(Color.RED);
                break;
            case GRAND:
                numberIndicator.setText("特獎");
                numberIndicator.setTextColor(Color.RED);
                break;
            case FIRST:
                numberIndicator.setText("一獎");
                numberIndicator.setTextColor(Color.GREEN);
                break;
            case SECOND:
                numberIndicator.setText("二獎");
                numberIndicator.setTextColor(Color.GREEN);
                break;
            case THIRD:
                numberIndicator.setText("三獎");
                numberIndicator.setTextColor(Color.GREEN);
                break;
            case FIFTH:
                numberIndicator.setText("五獎");
                numberIndicator.setTextColor(Color.GREEN);
                break;
            case SIXTH:
                numberIndicator.setText("六獎");
                numberIndicator.setTextColor(Color.GREEN);
                break;
            case NONE:
                numberIndicator.setText("未中獎");
                numberIndicator.setTextColor(Color.BLACK);
            default:
                break;
        }
    }


    static class CamConfig {
        private static final String TAG = "Camera";
        public static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
        public static final int REQUEST_CODE_PERMISSIONS = 10;
        public static final String[] REQUIRED_PERMISSIONS = new String[]
                {android.Manifest.permission.CAMERA};
    }

    private boolean CheckNumValid(@NonNull String num) {
        return num.matches("^\\d{8}$");
    }

}