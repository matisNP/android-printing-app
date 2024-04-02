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

        // Inicializar la barra de herramientas
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Inicializar vistas
        imageView = findViewById(R.id.image_view);
        textView = findViewById(R.id.textView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflar el menú; esto agrega elementos a la barra de herramientas si está presente.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Manejar los clics en los elementos del menú
        int id = item.getItemId();
        if (id == R.id.open_file) {
            // Acción para abrir un archivo
            abrirArchivo();
            return true;
        } else if (id == R.id.print_file) {
            // Acción para imprimir un archivo
            mostrarDialogoImprimir();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void mostrarDialogoImprimir() {
        // Crear un diálogo para imprimir
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

        // Establecer el botón positivo para confirmar la acción de imprimir
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Obtener las opciones seleccionadas
                String seleccionColor = (String) colorSpinner.getSelectedItem();
                String seleccionTamano = (String) tamanoSpinner.getSelectedItem();
                // Enviar los datos para imprimir
                enviarDatosImpresion(uri, seleccionColor, seleccionTamano);
                // Mostrar un mensaje de confirmación
                Toast.makeText(MainActivity.this, "Imprimiendo, Color: " + seleccionColor + ", Tamaño: " + seleccionTamano, Toast.LENGTH_LONG).show();
            }
        });

        // Mostrar el diálogo
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void abrirArchivo() {
        // Intent para abrir un archivo
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
                // Obtener la URI del archivo seleccionado
                uri = data.getData();
                try {
                    // Leer la imagen seleccionada desde su URI
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    // Mostrar la imagen en el ImageView
                    imageView.setImageBitmap(bitmap);
                    textView.setVisibility(View.GONE);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void enviarDatosImpresion(Uri imageUri, String color, String size) {
        // Configurar los datos de conexión al servidor de impresión
        serverName = "Tu direccion IPv4";
        serverPort = 12345;
        // Iniciar un hilo para enviar los datos de impresión al servidor
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Establecer la conexión con el servidor en el puerto 12345
                    Socket socket = new Socket(serverName, serverPort);

                    // Crear un flujo de salida de datos para enviar la imagen
                    DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

                    // Abrir el archivo de imagen
                    InputStream inputStream = getContentResolver().openInputStream(uri);

                    // Crear un buffer de bytes para leer la imagen
                    byte[] buffer = new byte[4096];

                    // Variable para almacenar el número total de bytes leídos
                    int bytesRead;

                    // Leer el archivo de imagen y enviarlo al servidor
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        dos.write(buffer, 0, bytesRead);
                    }

                    // Cerrar los flujos de datos y el socket
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
