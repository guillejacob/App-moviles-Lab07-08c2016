package ar.edu.utn.frsf.isi.dam.lab07_08c2016;

import android.content.DialogInterface;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

public class AltaReclamoActivity extends AppCompatActivity implements View.OnClickListener {

    EditText descripcion;
    EditText telefono;
    EditText email;
    LatLng ubicacion;
    Button btnReclamar;
    Button btnCancelar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.formulario_reclamo);

        descripcion = (EditText) findViewById(R.id.editTextDescripcion);
        telefono = (EditText) findViewById(R.id.editTextTelefono);
        email = (EditText) findViewById(R.id.editTextEmail);
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
            case R.id.buttonReclamar:
                reclamar();
                break;
            case R.id.buttonCancelar:
                finish();
                break;
        }
    }

    private void reclamar() {

        Reclamo reclamo = new Reclamo();
        reclamo.setTitulo(descripcion.getText().toString());
        reclamo.setTelefono(telefono.getText().toString());
        reclamo.setEmail(email.getText().toString());
        reclamo.setLatitud(ubicacion.latitude);
        reclamo.setLongitud(ubicacion.longitude);

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
