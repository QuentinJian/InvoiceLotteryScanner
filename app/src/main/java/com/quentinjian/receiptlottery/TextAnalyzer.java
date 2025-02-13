package com.quentinjian.receiptlottery;

import android.content.Context;
import android.media.Image;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.lifecycle.Lifecycle;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.ArrayList;
import java.util.List;

public class TextAnalyzer implements ImageAnalysis.Analyzer {
    private final TextRecognizer textRecognizer;
    private final Context context;
    private final List<String> result;
    private static final String TAG = "TextAnalyzer";
    private final TextResultCallback callback;

    public interface TextResultCallback {
        void onTextFound(Text recognizedText);
    }

    public TextAnalyzer(Context context, Lifecycle lifecycle, TextResultCallback callback) {
        this.context = context;
        this.callback = callback;
        // Initialize result list to avoid NullPointerException.
        result = new ArrayList<>();
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(mediaImage,
                    imageProxy.getImageInfo().getRotationDegrees());
            recognizeTextOnDevice(image)
                    .addOnCompleteListener(task -> {
                        // Ensure we close the ImageProxy after processing
                        imageProxy.close();
                    });
        } else {
            // If mediaImage is null, close the imageProxy.
            imageProxy.close();
        }
    }

    private Task<Text> recognizeTextOnDevice(InputImage image) {
        return textRecognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    result.add(visionText.getText());
                    callback.onTextFound(visionText);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Text Recognition Error", e);
                    String message = e.getMessage();
                    if (message != null) {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public List<String> getResult() {
        return result;
    }
}