package ar.edu.utn.frsf.isi.dam.lab07_08c2016;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.Console;
import java.util.ArrayList;

import static android.location.Location.distanceBetween;

public class ReclamoActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnInfoWindowClickListener {

    private int CODIGO_RESULTADO_ALTA_RECLAMO = 28;
    public GoogleMap myMap;
    public ArrayList<Reclamo> reclamos;
    public ArrayList<Reclamo> reclamosTemporal;
    public Polyline polilinea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reclamo);

        reclamos = new ArrayList<>();
        reclamosTemporal = new ArrayList<>();
        polilinea = null;

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_reclamo, menu);
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
    public void onMapReady(GoogleMap googleMap) {
        myMap = googleMap;
        actualizarMapa();
        myMap.setOnMapLongClickListener(this);
        myMap.setOnInfoWindowClickListener(this);
    }

    private void actualizarMapa() {
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},9999);
            return;
        }
        myMap.setMyLocationEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch(requestCode) {
            case 9999: {
                if(grantResults.length> 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    actualizarMapa();
                } else {
                    Toast.makeText(this, "No permission for location", Toast.LENGTH_SHORT).show();
                    }
                return;
            }
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if(polilinea != null) polilinea.remove();
        Intent i = new Intent(this, AltaReclamoActivity.class);
        i.putExtra("coordenadas", latLng);
        startActivityForResult(i, CODIGO_RESULTADO_ALTA_RECLAMO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODIGO_RESULTADO_ALTA_RECLAMO) {
            if (resultCode == RESULT_OK) {
                Reclamo reclamo = (Reclamo) data.getExtras().get("reclamo");
                //CREAR MARCADOR
                LatLng ubicacionReclamo = new LatLng(reclamo.getLatitud(),reclamo.getLongitud());
                Marker marcador = myMap.addMarker( new MarkerOptions()
                    .position(ubicacionReclamo)
                    .title("Reclamo de "+reclamo.getEmail())
                    .snippet(reclamo.getTitulo()));

                //agregar reclamo a la lista
                reclamos.add(reclamo);
            }
        }
    }

    @Override
    public void onInfoWindowClick(final Marker marker) {
        if(polilinea != null) polilinea.remove();
        //Dialog para ingresar los kilometros de distancia a mostrar reclamos
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Reclamos cercanos");
        alertDialog.setMessage("Introduzca la cantidad de kilometros de distancia para obtener los reclamos:");
        final EditText input = new EditText(this);
        input.setInputType( InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        // Setting Positive "Acept" Button
        alertDialog.setPositiveButton("Aceptar",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        if(!input.getText().toString().equals("")) {
                            unirReclamosCercanos(Double.parseDouble(input.getText().toString()), marker.getPosition());
                        }else{
                            Toast.makeText(ReclamoActivity.this,"Debe ingresar una distancia",Toast.LENGTH_SHORT);
                        }
                    }
                });
        // Setting Negative "Cancel" Button
        alertDialog.setNegativeButton("Cancelar",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to execute after dialog
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

    public void unirReclamosCercanos(Double kilometros, LatLng posicion){
        reclamosTemporal.clear();
        PolylineOptions puntosLinea = new PolylineOptions().add(posicion);//crear opciones polylinea

        for(Reclamo reclamo: reclamos){
            //calcular distancia con posicion
            float[] result = {0};
            distanceBetween(posicion.latitude, posicion.longitude, reclamo.getLatitud(), reclamo.getLongitud(), result);
            if(result[0] != 0.0 && result[0] <= kilometros * 1000) { //de km a metros multiplicar por 1000
                    reclamosTemporal.add(reclamo);
                    LatLng ubicacionReclamo = new LatLng(reclamo.getLatitud(),reclamo.getLongitud());
                    puntosLinea.add(ubicacionReclamo).color(Color.GREEN);//agregar latlng a polylinea
            }
        }
        Log.d("cantidadReclamos", " " + reclamosTemporal.toArray().length);
        polilinea = myMap.addPolyline(puntosLinea);//dibujar polilynea
    }
}


