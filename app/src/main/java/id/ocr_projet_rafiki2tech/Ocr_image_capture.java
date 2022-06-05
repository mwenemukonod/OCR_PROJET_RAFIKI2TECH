package id.ocr_projet_rafiki2tech;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class Ocr_image_capture extends AppCompatActivity {
    TextView txtres,txtreconnu,txtimgvisualisation;
    ImageView visualiserimg,ouvrirdialogue;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 1001;

    String permissionDeCamera[];
    String permissionDeStockage[];
    Uri image_uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_image_capture);
        txtres = (TextView)findViewById(R.id.txtresultatocr);
        visualiserimg =(ImageView) findViewById(R.id.appercuimg);
        ouvrirdialogue=(ImageView)findViewById(R.id.ouvrirdialogue);
        txtreconnu=(TextView)findViewById(R.id.txtreconnu);
        txtimgvisualisation=(TextView)findViewById(R.id.txtimgvisualisation);
        txtreconnu.setVisibility(View.GONE);
        txtimgvisualisation.setVisibility(View.GONE);
        //permission De Camera
        permissionDeCamera = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //permission De Stockage
        permissionDeStockage = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ouvrirdialogue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                afficheLaBoiteDeDialogue();
            }
        });

        txtreconnu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Pour la copie du text dans le PRESSE-PAPIER(CLIPBORD)
                ClipboardManager clipboardManager=(ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData data=ClipData.newPlainText("TEXT COPIER",txtres.getText().toString().trim());
                if(clipboardManager!=null){
                    clipboardManager.setPrimaryClip(data);
                    Toast.makeText(getApplicationContext(),"Texte copier",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void afficheLaBoiteDeDialogue() {

        String[] items = {" Camera/Photo", " Galleryie photo/Album"};
        AlertDialog.Builder mondialogue = new AlertDialog.Builder(this);
        mondialogue.setTitle("Selectionner une image");
        mondialogue.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i == 0) {
                    if(!checkCameraPermission()) {
                        requestCameraPermission();
                    }
                    else {
                        pickCamera();
                    }
                }
                if(i == 1) {
                    if(!checkStoragePermission()) {
                        requestStoragePermission();
                    }
                    else {
                        pickGallery();
                    }
                }
            }
        });
        mondialogue.create().show();
    }

    private void pickGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "NouvellePhoto");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image en Texte");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, permissionDeStockage, STORAGE_REQUEST_CODE);
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, permissionDeCamera, CAMERA_REQUEST_CODE);
    }
// pour veriifier les autorisations
    private boolean checkCameraPermission() {
        boolean camera_result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        boolean storage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return camera_result && storage_result;
    }

    private boolean checkStoragePermission() {
        boolean storage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return storage_result;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        pickCamera();
                    }
                    else {
                        Toast.makeText(this, "autorisation refusé", Toast.LENGTH_SHORT).show();
                    }
                }

            case STORAGE_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        pickGallery();
                    }
                    else {
                        Toast.makeText(this, "autorisation refusé", Toast.LENGTH_SHORT).show();
                    }
                }
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON).start(this); // enable image guildlines
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                CropImage.activity(image_uri).setGuidelines(CropImageView.Guidelines.ON).start(this); // enable image guildlines
            }
        }

        //obtenir une image recardrée
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                visualiserimg.setImageURI(resultUri);

                //obtenir une image pour la reconnaissance de texte
                BitmapDrawable bitmapDrawable = (BitmapDrawable)visualiserimg.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();
                TextRecognizer reconnaissance = new TextRecognizer.Builder(getApplicationContext()).build();

                if (!reconnaissance.isOperational()) {
                    Toast.makeText(this,"Error",Toast.LENGTH_SHORT).show();
                }
                else {
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> blocDeText = reconnaissance.detect(frame);
                    if(blocDeText.size() == 0) {
                        Toast.makeText(this, "Aucun texte détecté", Toast.LENGTH_SHORT).show();
                        txtres.setText("Aucun texte détecté");
                       // ocrTranslateButton.setEnabled(false);
                    } else {
                        StringBuilder construchaine = new StringBuilder();
                        for (int i = 0; i < blocDeText.size(); i++) {
                            TextBlock myItem = blocDeText.valueAt(i);
                            construchaine.append(myItem.getValue());
                            construchaine.append("\n");
                        }
                        txtres.setText(construchaine.toString());
                        txtreconnu.setVisibility(View.VISIBLE);
                        txtimgvisualisation.setVisibility(View.VISIBLE);
                        if(!txtres.getText().toString().isEmpty() && !txtres.getText().toString().equals("Aucun texte détecté")) {
                        }
                    }}
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(Ocr_image_capture.this, "vous avez une erreure de :"+error, Toast.LENGTH_SHORT).show();
            }
        }
    }
}