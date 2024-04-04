# android-printing-app

Esta es una aplicación de Android que permite seleccionar una imagen y enviarla a un servidor para su impresión.

## Requisitos

- Dispositivo Android con sistema operativo Android 10 o superior.
- Conexión a internet.
- Servidor de impresión configurado y en ejecución.
```sh
https://github.com/matisNP/tcp-file-printer
```

## Uso

1. Abre la aplicación en tu dispositivo Android.
2. Selecciona la opción "Abrir archivo" para seleccionar una imagen desde tu dispositivo.
3. Selecciona la imagen que deseas imprimir.
4. Selecciona la opción "Imprimir" para confirmar la impresión.
5. Selecciona el color y tamaño de la impresión en el diálogo emergente y confirma.
6. La aplicación enviará la imagen al servidor de impresión para su procesamiento.

## Configuración del Servidor

Antes de utilizar la aplicación, debes asegurarte de que tu servidor de impresión esté configurado correctamente. Esto incluye:

- Tener una dirección IPv4 válida y un puerto disponible para la conexión. Puedes modificar la dirección IPv4 y el puerto en el archivo MainActivity.java de la aplicación Android.

```sh
serverName = "Tu direccion IPv4";
serverPort = 12345;
```
