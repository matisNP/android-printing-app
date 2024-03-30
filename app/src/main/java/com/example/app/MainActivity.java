package com.example.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.Socket;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView imageView;
    private TextView textView;
    private static final int REQUEST_CODE_OPEN_FILE = 123;
    private Uri uri;
    private String serverName;
    private int serverPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imageView = findViewById(R.id.image_view);
        textView = findViewById(R.id.textView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.open_file) {
            abrirArchivo();
            return true;
        } else if (id == R.id.print_file) {
            mostrarDialogoImprimir();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void mostrarDialogoImprimir() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Imprimir");

        // Inflar el diseño del AlertDialog que contiene ambos Spinners
        View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_layout, null);

        // Obtener los Spinners del diseño inflado
        Spinner colorSpinner = dialogLayout.findViewById(R.id.spinner_color);
        Spinner tamanoSpinner = dialogLayout.findViewById(R.id.spinner_tamano);

        // Datos para los Spinners
        String[] opcionesColor = {"Blanco y negro", "Color"};
        String[] opcionesTamano = {
                "A4 (21.0 x 29.7) cm",
                "A5 (14.8 x 21.0) cm",
                "A6 (10.5 x 14.8) cm",
                "A7 (7.4 x 10.5) cm",
                "A8 (5.2 x 7.4) cm",
                "A9 (3.7 x 5.2) cm",
                "A10 (2.6 x 3.7) cm"
        };

        // Crear adaptadores para los Spinners
        ArrayAdapter<String> adapterColor = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, opcionesColor);
        adapterColor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<String> adapterTamano = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, opcionesTamano);
        adapterTamano.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Configurar los Spinners con los adaptadores
        colorSpinner.setAdapter(adapterColor);
        tamanoSpinner.setAdapter(adapterTamano);

        // Agregar el diseño del AlertDialog al AlertDialog
        builder.setView(dialogLayout);

        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Obtener las opciones seleccionadas
                String seleccionColor = (String) colorSpinner.getSelectedItem();
                String seleccionTamano = (String) tamanoSpinner.getSelectedItem();
                enviarDatosImpresion(uri, seleccionColor, seleccionTamano);
                Toast.makeText(MainActivity.this, "Imprimiendo, Color: " + seleccionColor + ", Tamaño: " + seleccionTamano, Toast.LENGTH_LONG).show();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void abrirArchivo() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*"); // Selecciona todos los tipos de archivo por defecto
        startActivityForResult(intent, REQUEST_CODE_OPEN_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN_FILE && resultCode == RESULT_OK) {
            if (data != null) {
                uri = data.getData();
                try {
                    // Lee la imagen seleccionada desde su URI
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    // Muestra la imagen en el ImageView
                    imageView.setImageBitmap(bitmap);
                    textView.setVisibility(View.GONE);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void enviarDatosImpresion(Uri imageUri, String color, String size) {

        serverName = "192.168.101.11";
        serverPort = 12345;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Establecemos la conexión con el servidor en el puerto 12345
                    Socket socket = new Socket(serverName, serverPort);

                    // Creamos un flujo de salida de datos para enviar la imagen
                    DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

                    // Abrimos el archivo de imagen
                    InputStream inputStream = getContentResolver().openInputStream(uri);

                    // Creamos un buffer de bytes para leer la imagen
                    byte[] buffer = new byte[4096];

                    // Variable para almacenar el número total de bytes leídos
                    int bytesRead;

                    // Leemos el archivo de imagen y lo enviamos al servidor
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        dos.write(buffer, 0, bytesRead);
                    }

                    // Cerramos el flujo de salida de datos y el socket
                    inputStream.close();
                    dos.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}