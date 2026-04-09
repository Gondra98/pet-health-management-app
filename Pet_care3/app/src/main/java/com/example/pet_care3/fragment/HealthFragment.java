package com.example.pet_care3.fragment;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.pet_care3.R;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HealthFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1; // 이미지 선택 요청 코드
    private static final int REQUEST_IMAGE_CAPTURE = 2; // Or any value you prefer
    private ImageView imageView; // 이미지를 표시할 이미지뷰
    private Button startDiagnosisButton;
    private TextView diagnosisTextView, solutionTextView;

    private Interpreter tflite;
    private List<String> classLabels;

    private RadioButton catRadioButton, dogRadioButton, eyeDiagnosisRadioButton, skinDiagnosisRadioButton;
    private String currentPhotoPath;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // fragment_health.xml 레이아웃을 인플레이트하여 반환
        View view = inflater.inflate(R.layout.fragment_health, container, false);

        // View 초기화
        imageView = view.findViewById(R.id.imageView4);
        startDiagnosisButton = view.findViewById(R.id.start);
        diagnosisTextView = view.findViewById(R.id.diagnosis);
        solutionTextView = view.findViewById(R.id.solution);

        // 라디오 버튼 초기화
        catRadioButton = view.findViewById(R.id.cat_button);
        dogRadioButton = view.findViewById(R.id.dog_button);
        eyeDiagnosisRadioButton = view.findViewById(R.id.eye_diagnosis_Button);
        skinDiagnosisRadioButton = view.findViewById(R.id.skin_diagnosis_Button);

        // 모델 및 클래스 라벨 로드
        loadModelAndLabels();

        // 라디오 버튼 상태 변경 리스너 설정
        catRadioButton.setOnCheckedChangeListener(radioButtonChangeListener);
        dogRadioButton.setOnCheckedChangeListener(radioButtonChangeListener);
        eyeDiagnosisRadioButton.setOnCheckedChangeListener(radioButtonChangeListener);
        skinDiagnosisRadioButton.setOnCheckedChangeListener(radioButtonChangeListener);

        // 이미지뷰 클릭 리스너 설정
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 사용자가 갤러리 모드를 선택할 수 있는 대화상자를 표시합니다.
                showImageSourceDialog();
            }
        });

        // 진단 버튼 클릭 리스너 설정
        startDiagnosisButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performDiagnosis(); // 진단 수행
            }
        });

        return view;
    }

    // 라디오 버튼 상태 변경 리스너
    private final CompoundButton.OnCheckedChangeListener radioButtonChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                // Clear the checked state of the other radio button
                if (buttonView == eyeDiagnosisRadioButton) {
                    skinDiagnosisRadioButton.setChecked(false);
                } else if (buttonView == skinDiagnosisRadioButton) {
                    eyeDiagnosisRadioButton.setChecked(false);
                }
                if (buttonView == catRadioButton) {
                    dogRadioButton.setChecked(false);
                } else if (buttonView == dogRadioButton) {
                    catRadioButton.setChecked(false);
                }
                // Load model and labels based on the checked radio buttons
                loadModelAndLabels();
            }
        }
    };

    // 이미지 소스를 선택할 수 있는 다이얼로그를 표시하는 메서드
    private void showImageSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("이미지 소스 선택");
        builder.setItems(new CharSequence[]{"갤러리", "카메라"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    openGallery(); // 갤러리에서 이미지 선택
                } else if (which == 1) {
                    dispatchTakePictureIntent(); // 카메라 모드 시작
                }
            }
        });
        builder.show();
    }


    // 갤러리를 열어 이미지를 선택하는 메서드
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        // 갤러리 액티비티 시작
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    private void loadModelAndLabels() {
        Log.d("HealthFragment", "loadModelAndLabels() called");
        Log.d("HealthFragment", "catRadioButton.isChecked(): " + catRadioButton.isChecked());
        Log.d("HealthFragment", "dogRadioButton.isChecked(): " + dogRadioButton.isChecked());
        Log.d("HealthFragment", "eyeDiagnosisRadioButton.isChecked(): " + eyeDiagnosisRadioButton.isChecked());
        Log.d("HealthFragment", "skinDiagnosisRadioButton.isChecked(): " + skinDiagnosisRadioButton.isChecked());

        try {
            if (catRadioButton.isChecked() && eyeDiagnosisRadioButton.isChecked()) {
                Log.d("HealthFragment", "Loading cat eye diagnosis model...");
                tflite = new Interpreter(loadModelFile("cat_eye_diagnosis_model.tflite"));
                classLabels = loadClassLabels("cat_eye_diagnosis_class_labels.txt");
            } else if (catRadioButton.isChecked() && skinDiagnosisRadioButton.isChecked()) {
                Log.d("HealthFragment", "Loading cat skin diagnosis model...");
                tflite = new Interpreter(loadModelFile("cat_skin_diagnosis_model.tflite"));
                classLabels = loadClassLabels("cat_skin_diagnosis_class_labels.txt");
            } else if (dogRadioButton.isChecked() && eyeDiagnosisRadioButton.isChecked()) {
                Log.d("HealthFragment", "Loading dog eye diagnosis model...");
                tflite = new Interpreter(loadModelFile("dog_eye_diagnosis_model.tflite"));
                classLabels = loadClassLabels("dog_eye_diagnosis_class_labels.txt");
            } else if (dogRadioButton.isChecked() && skinDiagnosisRadioButton.isChecked()) {
                Log.d("HealthFragment", "Loading dog skin diagnosis model...");
                tflite = new Interpreter(loadModelFile("dog_skin_diagnosis_model.tflite"));
                classLabels = loadClassLabels("dog_skin_diagnosis_class_labels.txt");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // TensorFlow Lite 모델 로드
    private MappedByteBuffer loadModelFile(String modelFileName) throws IOException {
        AssetFileDescriptor fileDescriptor = getActivity().getAssets().openFd(modelFileName);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // 클래스 라벨 로드
    private List<String> loadClassLabels(String labelsFileName) throws IOException {
        List<String> labels = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(getActivity().getAssets().open(labelsFileName)));
        String line;
        while ((line = reader.readLine()) != null) {
            labels.add(line);
        }
        reader.close();
        return labels;
    }

    // 카메라 앱 실행하여 사진 찍기
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(requireContext(),
                        "com.example.pet_care3.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file path for use with ACTION_VIEW intents
        currentPhotoPath = imageFile.getAbsolutePath();
        return imageFile;
    }



    // onActivityResult 메서드 수정
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null) {
                // 갤러리에서 이미지 선택
                Uri selectedImage = data.getData();
                imageView.setImageURI(selectedImage);
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // 카메라 모드에서 사진 찍기
                File photoFile = new File(currentPhotoPath);
                Uri photoURI = FileProvider.getUriForFile(requireContext(),
                        "com.example.pet_care3.fileprovider",
                        photoFile);
                imageView.setImageURI(photoURI);
            }
        } else {
            Toast.makeText(requireContext(), "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 진단 수행 메서드
    private void performDiagnosis() {
        // 클래스 라벨이 초기화되지 않은 경우, 오류 메시지를 출력하고 종료
        if (classLabels == null) {
            Log.e("HealthFragment", "Class labels are not initialized.");
            return;
        }

        // 이미지 추론 수행
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        float[] result = doInference(bitmap);

        // 최고 예측 결과를 출력
        int maxIndex = getMaxResultIndex(result);
        String predictedDisease = classLabels.get(maxIndex);
        String translatedDisease = translateLabel(predictedDisease);
        float confidence = result[maxIndex] * 100;
        String diagnosisResult = String.format("진단 결과: %s\n신뢰도: %.2f%%", translatedDisease, confidence);

        // 진단 결과 텍스트뷰에 설정
        diagnosisTextView.setText(diagnosisResult);
    }

    // 클래스 라벨을 한글로 번역하는 메서드
    private String translateLabel(String label) {
        // 클래스 라벨과 그에 해당하는 한글 번역을 매핑한 딕셔너리 생성
        Map<String, String> labelTranslations = new HashMap<>();
        labelTranslations.put("Blepharitis", "눈꺼풀염");
        labelTranslations.put("Conjunctivitis", "결막염");
        labelTranslations.put("Corneal_Osseous", "각막골질");
        labelTranslations.put("Corneal_Ulcer", "각막궤양");
        labelTranslations.put("Nonulcerative_Keratitis", "비궤양성각막염");
        labelTranslations.put("Normal", "정상");
        labelTranslations.put("Dandruff_Scales_EpidermalCollarettes", "비듬_규모_표피단 경피 칼르렛");
        labelTranslations.put("Nodules_Tumors", "결절_종양");
        labelTranslations.put("Pustules_Acne", "뾰루지_여드름");
        labelTranslations.put("Entropion", "내위성");
        labelTranslations.put("Glaucoma", "녹내장");
        labelTranslations.put("Nuclear Sclerosis", "핵성경화");
        labelTranslations.put("Ocular Tumors", "안구 종양");

        // 주어진 label에 대한 번역을 찾고, 없으면 label 그대로 반환
        String translatedLabel = labelTranslations.get(label);
        if (translatedLabel != null) {
            return translatedLabel;
        } else {
            return label; // 번역이 없는 경우 원래 label 그대로 반환
        }
    }

    // 이미지 추론 수행
    private float[] doInference(Bitmap bitmap) {
        if (classLabels == null) {
            // 클래스 라벨이 초기화되지 않은 경우, 오류 메시지를 출력하고 빈 배열을 반환
            Log.e("HealthFragment", "Class labels are not initialized.");
            return new float[0];
        }

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 150, 150, true);
        ByteBuffer input = convertBitmapToByteBuffer(resizedBitmap);

        float[][] output = new float[1][classLabels.size()];
        tflite.run(input, output);

        return output[0];
    }

    // 최고 결과 값의 인덱스 가져오기
    private int getMaxResultIndex(float[] result) {
        int maxIndex = 0;
        float maxProb = -1;
        for (int i = 0; i < result.length; i++) {
            if (result[i] > maxProb) {
                maxIndex = i;
                maxProb = result[i];
            }
        }
        return maxIndex;
    }

    // 이미지를 압축하여 Bitmap 반환
    private Bitmap compressBitmap(Bitmap bitmap) {
        int maxWidth = 800; // 최대 너비
        int maxHeight = 800; // 최대 높이

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();

        // 이미지 크기가 최대 크기보다 작으면 압축하지 않고 그대로 반환
        if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
            return bitmap;
        }

        // 이미지 크기가 최대 크기를 넘어갈 경우, 크기를 조절하여 반환
        float aspectRatio = (float) originalWidth / (float) originalHeight;
        int newWidth = Math.min(originalWidth, maxWidth);
        int newHeight = Math.round(newWidth / aspectRatio);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    // 이미지를 ByteBuffer로 변환
    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer imgData = ByteBuffer.allocateDirect(4 * 1 * 150 * 150 * 3);
        imgData.order(ByteOrder.nativeOrder());
        imgData.rewind();

        int[] intValues = new int[150 * 150];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        for (int i = 0; i < 150; ++i) {
            for (int j = 0; j < 150; ++j) {
                int pixelValue = intValues[i * 150 + j];

                imgData.putFloat(((pixelValue >> 16) & 0xFF) / 255.0f);
                imgData.putFloat(((pixelValue >> 8) & 0xFF) / 255.0f);
                imgData.putFloat((pixelValue & 0xFF) / 255.0f);
            }
        }
        return imgData;
    }
}
