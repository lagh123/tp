package com.laghridat.opencv;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class MainActivity extends AppCompatActivity {
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    public static final int GALLERY_REQUEST_CODE = 105;
    ImageView selectedImage;
    Button cameraBtn, galleryBtn, resetBtn;
    String currentPhotoPath;
    Bitmap bitmap;

    List<PointF> pointsList = new ArrayList<>();

    Bitmap edgesBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Inside onCreate method
        long studentId = getIntent().getLongExtra("studentid", -1);
        long selectedPwId = getIntent().getLongExtra("idpw", -1);
        Log.d("MainActivity", "Student ID: " + studentId);
        Log.d("MainActivity", "Selected PW ID: " + selectedPwId);

        selectedImage = findViewById(R.id.displayImageView);
        cameraBtn = findViewById(R.id.cameraBtn);
        galleryBtn = findViewById(R.id.galleryBtn);
        resetBtn = findViewById(R.id.reset);

        bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888); // Ajout de cette ligne
        if (!OpenCVLoader.initDebug()) Log.e("OpenCV", "Unable to load OpenCV!");
        else Log.d("OpenCV", "OpenCV loaded Successfully!");

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askCameraPermissions();
            }
        });

        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(gallery, GALLERY_REQUEST_CODE);
            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pointsList.clear(); // Efface la liste des points

                // Effacez également les dessins en redessinant la bitmap de base
                Bitmap mutableBitmap = edgesBitmap.copy(Bitmap.Config.ARGB_8888, true);
                Canvas canvas = new Canvas(mutableBitmap);
                selectedImage.setImageBitmap(mutableBitmap);

            }
        });
    }

    private void askCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        } else {
            dispatchTakePictureIntent();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERM_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Camera Permission is Required to Use camera.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            try {
                File f = new File(currentPhotoPath);
                // Load the image as a color image
                Mat colorMat = new Mat();
                Utils.bitmapToMat(BitmapFactory.decodeFile(f.getAbsolutePath()), colorMat);

                // Convert the color image to grayscale
                Mat grayscaleMat = new Mat();
                Imgproc.cvtColor(colorMat, grayscaleMat, Imgproc.COLOR_BGR2GRAY);

                // Apply Gaussian blur to the grayscale image
                Mat blurredMat = new Mat();
                Imgproc.GaussianBlur(grayscaleMat, blurredMat, new Size(5, 5), 0);

                // Detect edges with Canny
                Mat edgesMat = new Mat();
                Imgproc.Canny(blurredMat, edgesMat, 50, 150);

                // Convert Mat back to Bitmap
                Bitmap edgesBitmap = Bitmap.createBitmap(edgesMat.cols(), edgesMat.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(edgesMat, edgesBitmap);

                // Display the edges image
                selectedImage.setImageBitmap(edgesBitmap);

                // Broadcast the media scan intent
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(f);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri contentUri = data.getData();

                try {
                    // Convert the selected image to grayscale
                    Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentUri);
                    Bitmap grayscaleBitmap = convertToGrayscale(originalBitmap);

                    // Apply Gaussian blur to the grayscale image
                    Mat blurredMat = new Mat();
                    Utils.bitmapToMat(grayscaleBitmap, blurredMat);
                    Imgproc.GaussianBlur(blurredMat, blurredMat, new org.opencv.core.Size(5, 5), 0);

                    // Detect edges with Canny
                    Mat edgesMat = new Mat();
                    Imgproc.Canny(blurredMat, edgesMat, 50, 150);


                    // Convertir Mat en Bitmap
                    edgesBitmap = Bitmap.createBitmap(edgesMat.cols(), edgesMat.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(edgesMat, edgesBitmap);
                    Canvas canvas = new Canvas(edgesBitmap);

                    int pixelSpacing = 4;

                    for (int i = 0; i < edgesMat.rows(); i += pixelSpacing) {
                        for (int j = 0; j < edgesMat.cols(); j += pixelSpacing) {
                            if (edgesMat.get(i, j)[0] == 255) {
                                Paint paint = new Paint();
                                paint.setColor(Color.WHITE);
                                paint.setStyle(Paint.Style.FILL);
                                canvas.drawCircle(j, i, 2, paint);
                            }
                        }
                    }

                    // Display the edges image
                    selectedImage.setImageBitmap(edgesBitmap);

                    // Set touch listener on selectedImage
                    selectedImage.setOnTouchListener(new View.OnTouchListener() {
                        @SuppressLint("ClickableViewAccessibility")
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            Matrix inverse = new Matrix();
                            selectedImage.getImageMatrix().invert(inverse);
                            float[] touchPoint = new float[]{event.getX(), event.getY()};
                            inverse.mapPoints(touchPoint);

                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    if (pointsList.size() < 4) {
                                        float touchX = touchPoint[0];
                                        float touchY = touchPoint[1];

                                        if (edgesMat.get((int) touchY, (int) touchX)[0] == 255) {
                                            pointsList.add(new PointF(touchX, touchY)); // Ajouter le point s'il appartient aux bords
                                        } else {
                                            // Trouver le point le plus proche des bords
                                            Point closestPoint = findClosestPoint(touchX, touchY, edgesMat);
                                            if (closestPoint != null) {
                                                pointsList.add(new PointF((float) closestPoint.x, (float) closestPoint.y));
                                            }
                                        }

                                        Bitmap mutableBitmap = edgesBitmap.copy(Bitmap.Config.ARGB_8888, true);
                                        Canvas canvas = new Canvas(mutableBitmap);

                                        Paint paint = new Paint();
                                        paint.setColor(Color.RED);
                                        paint.setStyle(Paint.Style.FILL);

                                        // Draw all stored points onto the bitmap
                                        for (PointF point : pointsList) {
                                            canvas.drawCircle(point.x, point.y, 3, paint);
                                        }

                                        if (pointsList.size() == 4) {

                                            // Identifier les points les plus à gauche et les plus à droite
                                            PointF leftMostPoint1 = pointsList.get(0);
                                            PointF leftMostPoint2 = null;
                                            PointF rightMostPoint1 = pointsList.get(0);
                                            PointF rightMostPoint2 = null;

                                            // Identifier les points les plus à gauche
                                            for (PointF point : pointsList) {
                                                if (point.x < leftMostPoint1.x) {
                                                    leftMostPoint1 = point;
                                                }
                                            }

                                            // Identifier les autres points à gauche
                                            for (PointF point : pointsList) {
                                                if (point.x != leftMostPoint1.x) {
                                                    if (leftMostPoint2 == null || point.x < leftMostPoint2.x) {
                                                        leftMostPoint2 = point;
                                                    }
                                                }
                                            }

                                            // Identifier les points les plus à droite
                                            for (PointF point : pointsList) {
                                                if (point.x > rightMostPoint1.x) {
                                                    rightMostPoint1 = point;
                                                }
                                            }

                                            // Identifier les autres points à droite
                                            for (PointF point : pointsList) {
                                                if (point.x != rightMostPoint1.x) {
                                                    if (rightMostPoint2 == null || point.x > rightMostPoint2.x) {
                                                        rightMostPoint2 = point;
                                                    }
                                                }
                                            }

                                            // Calculer les coefficients de pente des deux lignes
                                            float m1 = (leftMostPoint2.y - leftMostPoint1.y) / (leftMostPoint2.x - leftMostPoint1.x);
                                            float m2 = (rightMostPoint2.y - rightMostPoint1.y) / (rightMostPoint2.x - rightMostPoint1.x);

                                            // Calculer les ordonnées à l'origine (y-intercepts) des deux lignes
                                            float b1 = leftMostPoint1.y - (m1 * leftMostPoint1.x);
                                            float b2 = rightMostPoint1.y - (m2 * rightMostPoint1.x);

                                            // Calculer le point d'intersection des deux lignes
                                            float intersectionX = (b2 - b1) / (m1 - m2);
                                            float intersectionY = m1 * intersectionX + b1;

                                            paint.setColor(Color.GREEN);
                                            paint.setStrokeWidth(2);
                                            // Dessiner les droites passant par les deux points à gauche
                                            if (leftMostPoint2 != null) {
                                                canvas.drawLine(leftMostPoint1.x, leftMostPoint1.y, intersectionX, intersectionY, paint);
                                            }

                                            // Dessiner les droites passant par les deux points à droite
                                            if (rightMostPoint2 != null) {
                                                canvas.drawLine(rightMostPoint1.x, rightMostPoint1.y, intersectionX, intersectionY, paint);
                                            }


                                            PointF bottomPoint1 = null;
                                            PointF bottomPoint2 = null;

                                            for (PointF point : pointsList) {
                                                if (bottomPoint1 == null || point.y > bottomPoint1.y) {
                                                    bottomPoint2 = bottomPoint1;
                                                    bottomPoint1 = point;
                                                } else if (bottomPoint2 == null || point.y > bottomPoint2.y) {
                                                    bottomPoint2 = point;
                                                }
                                            }

                                            if (bottomPoint1 != null && bottomPoint2 != null) {
                                                canvas.drawLine(bottomPoint1.x, bottomPoint1.y, bottomPoint1.x, 0, paint);
                                                canvas.drawLine(bottomPoint2.x, bottomPoint2.y, bottomPoint2.x, 0, paint);
                                            }

                                            // Calculer les vecteurs des droites
                                            PointF vector1 = new PointF(leftMostPoint2.x - leftMostPoint1.x, leftMostPoint2.y - leftMostPoint1.y);
                                            PointF vector2 = new PointF(rightMostPoint2.x - rightMostPoint1.x, rightMostPoint2.y - rightMostPoint1.y);

                                            // Calculer le produit scalaire
                                            float dotProduct = vector1.x * vector2.x + vector1.y * vector2.y;

                                            // Calculer les longueurs des vecteurs
                                            float magnitude1 = (float) Math.sqrt(vector1.x * vector1.x + vector1.y * vector1.y);
                                            float magnitude2 = (float) Math.sqrt(vector2.x * vector2.x + vector2.y * vector2.y);

                                            // Calculer le cosinus de l'angle entre les vecteurs
                                            float cosineAngle = dotProduct / (magnitude1 * magnitude2);

                                            // Calculer l'angle en radians
                                            double angleBetweenLines = Math.acos(cosineAngle);

                                            // Convertir l'angle en degrés si nécessaire
                                            angleBetweenLines = Math.toDegrees(angleBetweenLines);

                                            // Calculer les coefficients de pente des deux lignes reliant les points les plus à gauche et les plus à droite
                                            float leftLineSlope = (leftMostPoint2.y - leftMostPoint1.y) / (leftMostPoint2.x - leftMostPoint1.x);
                                            float rightLineSlope = (rightMostPoint2.y - rightMostPoint1.y) / (rightMostPoint2.x - rightMostPoint1.x);

                                            // Calculer les angles entre les lignes et les verticales
                                            double angleRight = Math.atan(rightLineSlope);

                                            // Convertir les angles en degrés si nécessaire
                                            angleRight = Math.toDegrees(angleRight);

                                            // Calculer l'angle avec la verticale
                                            angleRight = Math.abs(90 - angleRight);
                                            double angleLeft = angleBetweenLines - angleRight;

                                            int note = 0;
                                            float somme1 = 0;
                                            somme1= (float) (angleRight+angleLeft);
                                            if(somme1>6 && somme1<16){
                                                note=17;
                                            }else if (somme1>4 && somme1<18){
                                                note=14;
                                            }else if(somme1>2 && somme1<20){
                                                note=12;
                                            }else note=10;

                                            // Create and show a dialog to display angles
                                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                            builder.setTitle("Angles");
                                            builder.setMessage("Angle de convergence: " + angleBetweenLines + "\nAngle de dépouille 1 : " + angleLeft + "\nAngle de dépouille 2 : " + angleRight+ "\nVotre note est:" +note);
                                            builder.setPositiveButton("OK", null);
                                            AlertDialog dialog = builder.create();
                                            dialog.show();
                                            try {
                                                createImageFile();
                                                Log.d("ImagePath2", "Chemin de l'image : " + currentPhotoPath); // Log the image path
                                                getIntent().putExtra("imagePath",currentPhotoPath);
                                            } catch (IOException e) {
                                                throw new RuntimeException(e);
                                            }
                                            // Pass angles and image path to the next activity
                                            try {
                                                goToAnglesActivity(angleBetweenLines, angleLeft, angleRight, note, currentPhotoPath);
                                            } catch (IOException e) {
                                                throw new RuntimeException(e);
                                            }


                                        }

                                        selectedImage.setImageBitmap(mutableBitmap);
                                    }
                                    break;
                            }
                            return true;
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    private String getFileExt(Uri contentUri) {
        ContentResolver c = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(c.getType(contentUri));
    }

    private Bitmap convertToGrayscale(Bitmap original) {
        Mat originalMat = new Mat();
        Mat grayscaleMat = new Mat();

        // Convert Bitmap to Mat
        Utils.bitmapToMat(original, originalMat);

        // Convert to grayscale
        Imgproc.cvtColor(originalMat, grayscaleMat, Imgproc.COLOR_BGR2GRAY);

        // Convert Mat back to Bitmap
        Bitmap grayscaleBitmap = Bitmap.createBitmap(grayscaleMat.cols(), grayscaleMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(grayscaleMat, grayscaleBitmap);

        return grayscaleBitmap;
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        // Change the storage directory to "images"
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "images");
        if (!storageDir.exists()) {
            storageDir.mkdirs(); // Create the directory if it doesn't exist
        }
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        Log.d("ImagePath1", "Chemin de l'image : " + currentPhotoPath); // Log the image path
        return image;
    }

    private void goToAnglesActivity(double angleBetweenLines, double angleLeft, double angleRight, double note, String imagePath) throws IOException {
        createImageFile();
        //Log.d("ImagePath", "Chemin de l'image : " + currentPhotoPath); // Log the image path
        Intent intent = new Intent(MainActivity.this, AnglesActivity.class);
        intent.putExtra("angleBetweenLines", angleBetweenLines);
        intent.putExtra("angleLeft", angleLeft);
        intent.putExtra("angleRight", angleRight);
        intent.putExtra("note", note);
        //intent.putExtra("imagePath", imagePath);
        long studentId = getIntent().getLongExtra("studentid", -1);
        long selectedPwId = getIntent().getLongExtra("idpw", -1);
        intent.putExtra("studentId", studentId);
        intent.putExtra("selectedPwId", selectedPwId);
        startActivity(intent);

        // After starting the activity, send data to the server
        sendDataToServer(studentId, selectedPwId, angleBetweenLines, angleLeft, angleRight);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.laghridat.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }

        }
    }

    private float getIntersectionX(PointF start, PointF end, int y) {
        float slope = (end.y - start.y) / (end.x - start.x);
        float intersectionX = start.x + (y - start.y) / slope;
        return intersectionX;
    }


    private Bitmap applyGaussianBlur(Bitmap inputBitmap, int radius) {
        Mat inputMat = new Mat();
        Mat blurredMat = new Mat();

        // Convert Bitmap to Mat
        Utils.bitmapToMat(inputBitmap, inputMat);

        // Apply Gaussian blur
        Imgproc.GaussianBlur(inputMat, blurredMat, new Size(radius, radius), 0);

        // Convert Mat back to Bitmap
        Bitmap blurredBitmap = Bitmap.createBitmap(blurredMat.cols(), blurredMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(blurredMat, blurredBitmap);

        return blurredBitmap;
    }

    private void drawPoint(Bitmap bitmap, float x, float y, int color) {
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(x, y, 10, paint);
        selectedImage.invalidate();
    }

    private void drawLine(Bitmap bitmap, Point start, Point end, int color) {
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStrokeWidth(5);
        canvas.drawLine((float) start.x, (float) start.y, (float) end.x, (float) end.y, paint);
        selectedImage.invalidate();
    }

    private double calculateAngle(Point point1, Point point2) {
        double deltaX = point2.x - point1.x;
        double deltaY = point2.y - point1.y;
        return Math.toDegrees(Math.atan2(deltaY, deltaX));
    }

    private Point findClosestPoint(float x, float y, Mat edgesMat) {
        Point closestPoint = null;
        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < edgesMat.rows(); i++) {
            for (int j = 0; j < edgesMat.cols(); j++) {
                if (edgesMat.get(i, j)[0] == 255) {
                    double distance = Math.sqrt(Math.pow(i - y, 2) + Math.pow(j - x, 2));
                    if (distance < minDistance) {
                        minDistance = distance;
                        closestPoint = new Point(j, i);
                    }
                }
            }
        }
        return closestPoint;
    }

    private float getIntersectionY(PointF start, PointF end, int x) {
        float slope = (end.y - start.y) / (end.x - start.x);
        float intersectionY = start.y + slope * (x - start.x);
        return intersectionY;
    }

    private void sendDataToServer(long studentId, long selectedPwId, double angleBetweenLines, double angleLeft, double angleRight) {
        String url = ApiConfig.BASE_URL+"/api/studentpws/" + studentId + "/" + selectedPwId;

        Map<String, String> params = new HashMap<>();
        /*params.put("date", "2024-01-01");
        params.put("angle1Front", String.valueOf(angleLeft));
        params.put("angle2Front", String.valueOf(angleRight));
        params.put("noteFront", String.valueOf(10));  // Assuming 'note' is a variable holding the note value*/

        // Convert the image bitmap to base64
        String imageBase64 = convertBitmapToBase64(bitmap);  // Replace 'bitmap' with your actual bitmap
        Log.d("chemin",imageBase64);
        Log.d("chemin",currentPhotoPath);

        //params.put("imageFront", imageBase64);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle the server response if needed
                        Log.d("VolleyResponse", response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle errors
                        Log.e("VolleyError", "Error: " + error.toString());
                    }
                });

        // Add the request to the RequestQueue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    // Utility method to convert Bitmap to base64
    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }



}