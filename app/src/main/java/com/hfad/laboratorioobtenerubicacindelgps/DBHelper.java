package com.hfad.laboratorioobtenerubicacindelgps;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "estudiantes.db";
    private static final int DB_VERSION = 2;
    private static final String TABLE = "estudiantes";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "nombre TEXT NOT NULL UNIQUE, "
                + "latitud REAL, "
                + "longitud REAL, "
                + "localidad TEXT, "
                + "direccion TEXT, "
                + "fecha INTEGER)");

        insertarEjemplo(db, "Laura Gómez", 4.7425, -74.0840, "Suba", "Calle 147 # 91-20");
        insertarEjemplo(db, "Carlos Ruiz", 4.7380, -74.0900, "Suba", "Carrera 104 # 139-15");
        insertarEjemplo(db, "Andrés Pérez", 4.6285, -74.1580, "Kennedy", "Carrera 78K # 38A Sur");
        insertarEjemplo(db, "María Torres", 4.7110, -74.0300, "Usaquén", "Calle 127 # 7-40");
        insertarEstudiantesAdicionales(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            insertarEstudiantesAdicionales(db);
        }
    }

    private void insertarEstudiantesAdicionales(SQLiteDatabase db) {
        insertarEjemplo(db, "Sofía Martínez", 4.6950, -74.0820, "Engativá", "Calle 80 # 72-15");
        insertarEjemplo(db, "Daniel Rodríguez", 4.6097, -74.0817, "Santa Fe", "Carrera 7 # 26-20");
        insertarEjemplo(db, "Valentina Herrera", 4.5794, -74.1267, "Bosa", "Calle 65 Sur # 80-30");
        insertarEjemplo(db, "Mateo Castillo", 4.6460, -74.0630, "Chapinero", "Carrera 13 # 60-18");
        insertarEjemplo(db, "Isabella Moreno", 4.5680, -74.0930, "Rafael Uribe Uribe", "Calle 27 Sur # 14-50");
        insertarEjemplo(db, "Samuel Vargas", 4.6010, -74.0700, "Antonio Nariño", "Carrera 24 # 18-35 Sur");
        insertarEjemplo(db, "Camila Rojas", 4.6660, -74.0550, "Barrios Unidos", "Calle 72 # 24-10");
        insertarEjemplo(db, "Nicolás Díaz", 4.5500, -74.1450, "Ciudad Bolívar", "Carrera 18C # 65A-20 Sur");
        insertarEjemplo(db, "Gabriela Sánchez", 4.6180, -74.1050, "Puente Aranda", "Avenida 1 de Mayo # 40-25");
        insertarEjemplo(db, "Tomás Cárdenas", 4.6800, -74.1400, "Fontibón", "Calle 20 # 100-15");
    }

    private void insertarEjemplo(SQLiteDatabase db, String nombre, double latitud,
                                 double longitud, String localidad, String direccion) {
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("latitud", latitud);
        values.put("longitud", longitud);
        values.put("localidad", localidad);
        values.put("direccion", direccion);
        values.put("fecha", System.currentTimeMillis());
        db.insertOrThrow(TABLE, null, values);
    }

    public boolean guardarEstudiante(String nombre, double latitud, double longitud,
                                     String localidad, String direccion) {
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("latitud", latitud);
        values.put("longitud", longitud);
        values.put("localidad", localidad);
        values.put("direccion", direccion);
        values.put("fecha", System.currentTimeMillis());
        return getWritableDatabase().insertWithOnConflict(
                TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE) != -1;
    }

    public List<String[]> obtenerZonas() {
        List<String[]> zonas = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT localidad, COUNT(*) FROM " + TABLE
                        + " GROUP BY localidad ORDER BY COUNT(*) DESC, localidad ASC",
                null)) {
            while (cursor.moveToNext()) {
                zonas.add(new String[]{
                        cursor.getString(0),
                        String.valueOf(cursor.getInt(1))
                });
            }
        }
        return zonas;
    }

    public List<String[]> obtenerEstudiantes() {
        List<String[]> estudiantes = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT nombre, localidad FROM " + TABLE + " ORDER BY nombre ASC",
                null)) {
            while (cursor.moveToNext()) {
                estudiantes.add(new String[]{cursor.getString(0), cursor.getString(1)});
            }
        }
        return estudiantes;
    }

    public int contarEstudiantes() {
        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + TABLE, null)) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }
}
