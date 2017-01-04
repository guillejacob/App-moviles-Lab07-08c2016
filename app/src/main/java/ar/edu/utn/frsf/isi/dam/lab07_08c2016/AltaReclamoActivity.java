package ar.edu.utn.frsf.isi.dam.lab07_08c2016;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AltaReclamoActivity extends AppCompatActivity implements View.OnClickListener {

    EditText descripcion;
    EditText telefono;
    EditText email;
    EditText imagenPath;
    LatLng ubicacion;
    ImageButton btnAdjuntar;
    ImageView imageView;
    Button btnReclamar;
    Button btnCancelar;
    final int REQUEST_TAKE_PHOTO = 1;
    String photoPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.formulario_reclamo);

        descripcion = (EditText) findViewById(R.id.editTextDescripcion);
        telefono = (EditText) findViewById(R.id.editTextTelefono);
        email = (EditText) findViewById(R.id.editTextEmail);
        imagenPath = (EditText) findViewById(R.id.editTextImagenPath);
        imageView = (ImageView) findViewById(R.id.imageView);
        btnAdjuntar = (ImageButton) findViewById(R.id.imageButton);
        btnAdjuntar.setOnClickListener(this);
        btnReclamar = (Button) findViewById(R.id.buttonReclamar);
        btnReclamar.setOnClickListener(this);
        btnCancelar = (Button) findViewById(R.id.buttonCancelar);
        btnCancelar.setOnClickListener(this);

        Bundle extras = getIntent().getExtras();
        ubicacion = (LatLng) extras.get("coordenadas");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imageButton:
                adjuntarImagen();
                break;
            case R.id.buttonReclamar:
                reclamar();
                break;
            case R.id.buttonCancelar:
                finish();
                break;
        }
    }

    private void adjuntarImagen() {
       Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
      if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.d("creacion archivo foto","no se creo el archivo");
            }
            if (photoFile != null) {
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
            imagenPath.setText(photoPath);
        }
    }

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
        photoPath = image.getAbsolutePath();
        return image;
    }

    private void reclamar() {

        Reclamo reclamo = new Reclamo();
        reclamo.setTitulo(descripcion.getText().toString());
        reclamo.setTelefono(telefono.getText().toString());
        reclamo.setEmail(email.getText().toString());
        reclamo.setLatitud(ubicacion.latitude);
        reclamo.setLongitud(ubicacion.longitude);
        reclamo.setImagenPath(photoPath);

        if(reclamo.getTitulo().equals("")|| reclamo.getTelefono().equals("")|| reclamo.getEmail().equals("")){
            Toast.makeText(this, "Debe ingresar todos los datos solicitados", Toast.LENGTH_LONG).show();
        }else{
            Intent iRespuesta = new Intent();
            iRespuesta.putExtra("reclamo", reclamo);
            setResult(ReclamoActivity.RESULT_OK, iRespuesta);
            finish();
        }
    }
}
