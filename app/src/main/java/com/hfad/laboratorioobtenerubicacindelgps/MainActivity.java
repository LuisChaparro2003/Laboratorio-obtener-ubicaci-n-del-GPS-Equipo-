package com.hfad.laboratorioobtenerubicacindelgps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION = 44;
    private Button btLocation;
    private EditText etNombre;
    private TextView textView1;
    private TextView textView2;
    private TextView textView3;
    private TextView textView4;
    private TextView textView5;
    private TextView locationView;
    private TextView tvZonas;
    private TextView tvEstudiantes;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private DBHelper dbHelper;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btLocation = findViewById(R.id.bt_location);
        Button btZonas = findViewById(R.id.bt_zonas);
        etNombre = findViewById(R.id.et_nombre);
        textView1 = findViewById(R.id.text_view1);
        textView2 = findViewById(R.id.text_view2);
        textView3 = findViewById(R.id.text_view3);
        textView4 = findViewById(R.id.text_view4);
        textView5 = findViewById(R.id.text_view5);
        locationView = findViewById(R.id.location_view);
        tvZonas = findViewById(R.id.tv_zonas);
        tvEstudiantes = findViewById(R.id.tv_estudiantes);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        dbHelper = new DBHelper(this);

        btLocation.setOnClickListener(view -> {
            if (etNombre.getText().toString().trim().isEmpty()) {
                etNombre.setError("Escribe tu nombre");
                etNombre.requestFocus();
                return;
            }

            if (tienePermisoDeUbicacion()) {
                getLocation();
            } else {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        },
                        REQUEST_LOCATION);
            }
        });

        btZonas.setOnClickListener(view -> mostrarZonas());
        mostrarZonas();
    }

    private boolean tienePermisoDeUbicacion() {
        return ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_LOCATION) {
            return;
        }

        if (tienePermisoDeUbicacion()) {
            getLocation();
        } else {
            locationView.setText("Permiso de ubicación denegado");
        }
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        btLocation.setEnabled(false);
        locationView.setText("Obteniendo ubicación…");
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        procesarUbicacion(location);
                    } else {
                        solicitarUbicacionNueva();
                    }
                })
                .addOnFailureListener(this, error -> mostrarErrorUbicacion(error));
    }

    @SuppressLint("MissingPermission")
    private void solicitarUbicacionNueva() {
        LocationRequest request = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)
                .setFastestInterval(500)
                .setNumUpdates(1);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                fusedLocationProviderClient.removeLocationUpdates(this);
                locationCallback = null;
                Location location = result.getLastLocation();
                if (location != null) {
                    procesarUbicacion(location);
                } else {
                    btLocation.setEnabled(true);
                    locationView.setText("No se pudo obtener la ubicación");
                }
            }
        };

        fusedLocationProviderClient.requestLocationUpdates(
                        request, locationCallback, Looper.getMainLooper())
                .addOnFailureListener(this, error -> {
                    if (locationCallback != null) {
                        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                        locationCallback = null;
                    }
                    mostrarErrorUbicacion(error);
                });
    }

    private void mostrarErrorUbicacion(Exception error) {
        btLocation.setEnabled(true);
        locationView.setText("Error al obtener la ubicación: " + error.getLocalizedMessage());
    }

    private void procesarUbicacion(Location location) {
        textView1.setText("Latitud: " + location.getLatitude());
        textView2.setText("Longitud: " + location.getLongitude());
        locationView.setText("Ubicación obtenida; buscando dirección…");

        new Thread(() -> {
            String pais = "N/D";
            String localidad = "Desconocida";
            String direccion = "N/D";

            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(
                        location.getLatitude(), location.getLongitude(), 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    if (address.getCountryName() != null) {
                        pais = address.getCountryName();
                    }
                    if (address.getSubLocality() != null) {
                        localidad = address.getSubLocality();
                    } else if (address.getLocality() != null) {
                        localidad = address.getLocality();
                    }
                    if (address.getMaxAddressLineIndex() >= 0) {
                        direccion = address.getAddressLine(0);
                    }
                }
            } catch (IOException error) {
                android.util.Log.e("MainActivity", "No se pudo resolver la dirección", error);
            }

            String resultadoPais = pais;
            String resultadoLocalidad = localidad;
            String resultadoDireccion = direccion;
            runOnUiThread(() -> {
                textView3.setText("País: " + resultadoPais);
                textView4.setText("Localidad: " + resultadoLocalidad);
                textView5.setText("Dirección: " + resultadoDireccion);
                guardarEnBD(location, resultadoLocalidad, resultadoDireccion);
            });
        }).start();
    }

    private void guardarEnBD(Location location, String localidad, String direccion) {
        String nombre = etNombre.getText().toString().trim();
        boolean guardado = dbHelper.guardarEstudiante(
                nombre, location.getLatitude(), location.getLongitude(), localidad, direccion);
        btLocation.setEnabled(true);
        if (guardado) {
            locationView.setText("Ubicación de " + nombre + " reportada");
            mostrarZonas();
        } else {
            locationView.setText("Error al guardar la ubicación en la base de datos");
        }
    }

    private void mostrarZonas() {
        List<String[]> zonas = dbHelper.obtenerZonas();
        if (zonas.isEmpty()) {
            tvZonas.setText("Aún no hay estudiantes registrados.");
            tvEstudiantes.setText("");
            return;
        }

        StringBuilder ranking = new StringBuilder();
        ranking.append("Total de estudiantes: ").append(dbHelper.contarEstudiantes())
                .append("\n\nZonas con más estudiantes:\n");
        for (int i = 0; i < zonas.size(); i++) {
            String[] zona = zonas.get(i);
            int cantidad = Integer.parseInt(zona[1]);
            ranking.append(i + 1).append(". ").append(zona[0]).append(" — ")
                    .append(cantidad)
                    .append(cantidad == 1 ? " estudiante\n" : " estudiantes\n");
        }
        ranking.append("\nRecomendación: concentrar tutorías y acceso a laboratorios en ")
                .append(zonas.get(0)[0]).append(".");
        tvZonas.setText(ranking.toString());

        StringBuilder estudiantes = new StringBuilder("Estudiantes registrados:\n");
        for (String[] estudiante : dbHelper.obtenerEstudiantes()) {
            estudiantes.append("• ").append(estudiante[0])
                    .append(" (").append(estudiante[1]).append(")\n");
        }
        tvEstudiantes.setText(estudiantes.toString());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            locationCallback = null;
        }
    }
}
