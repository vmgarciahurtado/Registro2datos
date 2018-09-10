package com.example.victor1024.registro2datos;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class FotografiaActivity extends AppCompatActivity implements Response.Listener<JSONObject>,Response.ErrorListener{

    int posicion = 0,accion = 0;
    String departamento = "", genero = "",municipio = "";
    boolean seleccionaDepartamento = false, seleccionaGenero = false,seleccionaImagen = false,seleccionaMunicipio = false;


    private static final String CARPETA_PRINCIPAL = "misImagenesApp/";//directorio principal
    private static final String CARPETA_IMAGEN = "imagenes";//carpeta donde se guardan las fotos
    private static final String DIRECTORIO_IMAGEN = CARPETA_PRINCIPAL + CARPETA_IMAGEN;//ruta carpeta de directorios
    private String path;//almacena la ruta de la imagen
    private final int MIS_PERMISOS = 100;
    private static final int COD_SELECCIONA = 10;
    private static final int COD_FOTO = 20;

    File fileImagen;
    Bitmap bitmap;
    Spinner listaDepartamentos, listaMunicipios, listaGenero;
    EditText campoNombre, campoApellido, campoFechaNacimiento, campoCorreo;
    ImageView imagenUsuario, imagenCamara;
    Button btnRegistro;

    ArrayList<String> ArrayDepartamentos;
    ArrayList<String> ArrayMunicipios;
    ArrayList<String> ArrayGenero;
    RelativeLayout layoutRegistrar;//permisos
    RequestQueue request;
    JsonObjectRequest jsonObjectRequest;
    StringRequest stringRequest;
    ProgressDialog progreso;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fotografia);

        layoutRegistrar= (RelativeLayout) findViewById(R.id.idLayoutRegistrar);

/*        if(solicitaPermisosVersionesSuperiores()){
            imagenCamara.setEnabled(true);
        }else{
            imagenCamara.setEnabled(false);
        }*/

        request = Volley.newRequestQueue(getApplicationContext());
        ArrayGenero = new ArrayList<>();
        ArrayGenero.add("Seleccioar genero");
        ArrayGenero.add("Masculino");
        ArrayGenero.add("Femenino");

        ArrayDepartamentos = new ArrayList<>();
        ArrayDepartamentos.add("Seleccione su departamento");
        ArrayDepartamentos.add("Antioquia");
        ArrayDepartamentos.add("Atlántico");
        ArrayDepartamentos.add("Bogotá");
        ArrayDepartamentos.add("Bolívar");
        ArrayDepartamentos.add("Boyacá");
        ArrayDepartamentos.add("Caldas");
        ArrayDepartamentos.add("Caquetá");
        ArrayDepartamentos.add("Cauca");
        ArrayDepartamentos.add("Cesar");
        ArrayDepartamentos.add("Córdoba");
        ArrayDepartamentos.add("Cundinamarca");
        ArrayDepartamentos.add("Chocó");
        ArrayDepartamentos.add("Huila");
        ArrayDepartamentos.add("La Guajira");
        ArrayDepartamentos.add("Magdalena");
        ArrayDepartamentos.add("Meta");
        ArrayDepartamentos.add("Nariño");
        ArrayDepartamentos.add("Norte de Santander");
        ArrayDepartamentos.add("Quindío");
        ArrayDepartamentos.add("Risaralda");
        ArrayDepartamentos.add("Santander");
        ArrayDepartamentos.add("Sucre");
        ArrayDepartamentos.add("Tolima");
        ArrayDepartamentos.add("Valle del Cauca");
        ArrayDepartamentos.add("Arauca");
        ArrayDepartamentos.add("Casanare");
        ArrayDepartamentos.add("Putumayo");
        ArrayDepartamentos.add("San Andrés y Providencia");
        ArrayDepartamentos.add("Amazonas");
        ArrayDepartamentos.add("Guainía");
        ArrayDepartamentos.add("Guaviare");
        ArrayDepartamentos.add("Vaupés");
        ArrayDepartamentos.add("Vichada");

        campoNombre = findViewById(R.id.campoNombreRegistro);
        campoApellido = findViewById(R.id.campoApellidosRegistro);
        campoCorreo = findViewById(R.id.campoCorreoRegistro);
        campoFechaNacimiento = findViewById(R.id.campoFechaRegistro);
        imagenUsuario = findViewById(R.id.imagenUsuario);
        imagenCamara = findViewById(R.id.imagenCamara);
        listaDepartamentos = findViewById(R.id.spinnerDepartamentoRegistro);
        listaMunicipios = findViewById(R.id.spinnerCuidadRegistro);
        listaGenero = findViewById(R.id.spinnerGeneroRegistro);

        ArrayAdapter<CharSequence> adapterDepartamentos = new ArrayAdapter(getApplicationContext(), R.layout.support_simple_spinner_dropdown_item, ArrayDepartamentos);
        listaDepartamentos.setAdapter(adapterDepartamentos);
        listaDepartamentos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (position != 0) {
                    posicion = position;
                    departamento = ArrayDepartamentos.get(position);
                    cargarListaMunicipios();
                    seleccionaDepartamento = true;
                } else {
                    seleccionaDepartamento = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter<CharSequence> adapterGenero = new ArrayAdapter(getApplicationContext(), R.layout.support_simple_spinner_dropdown_item, ArrayGenero);
        listaGenero.setAdapter(adapterGenero);
        listaGenero.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    genero = ArrayGenero.get(i);
                    seleccionaGenero = true;
                } else {
                    seleccionaGenero = false;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        imagenCamara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //opcionesCapturaFoto();
                mostrarDialogOpciones();
            }
        });

        btnRegistro = findViewById(R.id.btnRegistrar);
        btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* if (campoNombre.getText().equals("") || campoApellido.getText().equals("")|| seleccionaGenero==false || seleccionaDepartamento==false || seleccionaMunicipio==false || seleccionaImagen==false ){
                    Toast.makeText(getApplicationContext(),"Debe llenar todos los campos",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getApplicationContext(),"Noebe llenar todos los campos",Toast.LENGTH_SHORT).show();
                    registrarUsuarios();
                }*/
                registrarUsuarios();
            }
        });

        if(solicitaPermisosVersionesSuperiores()){
            //imagenCamara.setEnabled(true);
        }else{
            //imagenCamara.setEnabled(false);
        }


    }

    private void mostrarDialogOpciones() {
        final CharSequence[] opciones={"Tomar Foto","Elegir de Galeria","Cancelar"};
        final android.support.v7.app.AlertDialog.Builder builder=new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle("Elige una Opción");
        builder.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (opciones[i].equals("Tomar Foto")){
                    abrirCamara();
                }else{
                    if (opciones[i].equals("Elegir de Galeria")){
                        Intent intent=new Intent(Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/");
                        startActivityForResult(intent.createChooser(intent,"Seleccione"),COD_SELECCIONA);
                    }else{
                        dialogInterface.dismiss();
                    }
                }
            }
        });
        builder.show();
    }

    private void opcionesCapturaFoto() {
        final CharSequence[] opciones={"Tomar Foto","Elegir de Galeria","Cancelar"};
        final AlertDialog.Builder builder=new AlertDialog.Builder(getApplicationContext());
        builder.setTitle("Elige una Opción");
        builder.setItems(opciones, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (opciones[i].equals("Tomar Foto")){
                    seleccionaImagen = true;
                    abrirCamara();
                }else{
                    if (opciones[i].equals("Elegir de Galeria")){
                        seleccionaImagen = true;
                        Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/");
                        startActivityForResult(intent.createChooser(intent,"Seleccione"),COD_SELECCIONA);
                    }else{
                        dialogInterface.dismiss();
                        seleccionaImagen = false;
                    }
                }
            }
        });
        builder.show();
    }

/*    private void abrirCamara() {

        File miFile=new File(Environment.getExternalStorageDirectory(),DIRECTORIO_IMAGEN);
        boolean isCreada=miFile.exists();

        if(isCreada==false){
            isCreada=miFile.mkdirs();
        }

        if(isCreada==true){
            Long consecutivo= System.currentTimeMillis()/1000;
            String nombre=consecutivo.toString()+".jpg";

            path=Environment.getExternalStorageDirectory()+File.separator+DIRECTORIO_IMAGEN
                    +File.separator+nombre;//indicamos la ruta de almacenamiento

            fileImagen=new File(path);

            Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fileImagen));

            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N)
            {
                String authorities=getApplicationContext().getPackageName()+".provider";
                Uri imageUri= FileProvider.getUriForFile(getApplicationContext(),authorities,fileImagen);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            }else
            {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fileImagen));
            }
            startActivityForResult(intent,COD_FOTO);

        }
    }*/


    private void abrirCamara() {
        seleccionaImagen = true;
        File miFile = new File(Environment.getExternalStorageDirectory(),DIRECTORIO_IMAGEN);
        boolean isCreada = miFile.exists();
        if (isCreada==false){
            isCreada=miFile.mkdirs();//por si la variable no fue creada, se crea de nuevo
        }
        if (isCreada==true){
            Long consecutivo= System.currentTimeMillis()/1000;//aqui iba un 100, por si no funciona el codigo este es el error
            String nombre=consecutivo.toString()+".jpg";

            path=Environment.getExternalStorageDirectory()+File.separator+DIRECTORIO_IMAGEN
                    +File.separator+nombre;//indicamos la ruta de almacenamiento

            fileImagen=new File(path);

            Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fileImagen));

            ////
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N)
            {
                String authorities=this.getPackageName()+".provider";
                Uri imageUri= FileProvider.getUriForFile(this,authorities,fileImagen);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            }else
            {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fileImagen));
            }
            startActivityForResult(intent,COD_FOTO);
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case COD_SELECCIONA:
                Uri miPath=data.getData();
                imagenUsuario.setImageURI(miPath);

                try {
                    bitmap=MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(),miPath);
                    imagenUsuario.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;
            case COD_FOTO:
                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{path}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(String path, Uri uri) {
                                Log.i("Path",""+path);
                            }
                        });

                bitmap= BitmapFactory.decodeFile(path);
                imagenUsuario.setImageBitmap(bitmap);

                break;
        }
        bitmap=redimensionarImagen(bitmap,400,400);
    }

    private Bitmap redimensionarImagen(Bitmap bitmap, float anchoNuevo, float altoNuevo) {

        int ancho=bitmap.getWidth();
        int alto=bitmap.getHeight();

        if(ancho>anchoNuevo || alto>altoNuevo){
            float escalaAncho=anchoNuevo/ancho;
            float escalaAlto= altoNuevo/alto;

            Matrix matrix=new Matrix();
            matrix.postScale(escalaAncho,escalaAlto);

            return Bitmap.createBitmap(bitmap,0,0,ancho,alto,matrix,false);

        }else{
            return bitmap;
        }

    }


    //permisos
    ////////////////

    private boolean solicitaPermisosVersionesSuperiores() {
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.M){//validamos si estamos en android menor a 6 para no buscar los permisos
            return true;
        }

        //validamos si los permisos ya fueron aceptados
        if((getApplicationContext().checkSelfPermission(WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)&&getApplicationContext().checkSelfPermission(CAMERA)==PackageManager.PERMISSION_GRANTED){
            return true;
        }


        if ((shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)||(shouldShowRequestPermissionRationale(CAMERA)))){
            cargarDialogoRecomendacion();
        }else{
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, MIS_PERMISOS);
        }

        return false;//implementamos el que procesa el evento dependiendo de lo que se defina aqui
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==MIS_PERMISOS){
            if(grantResults.length==2 && grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED){//el dos representa los 2 permisos
                Toast.makeText(getApplicationContext(),"Permisos aceptados",Toast.LENGTH_SHORT);
                imagenCamara.setEnabled(true);
            }
        }else{
            solicitarPermisosManual();
        }
    }


    private void solicitarPermisosManual() {
        final CharSequence[] opciones={"si","no"};
        final android.support.v7.app.AlertDialog.Builder alertOpciones=new android.support.v7.app.AlertDialog.Builder(getApplicationContext());//estamos en fragment
        alertOpciones.setTitle("¿Desea configurar los permisos de forma manual?");
        alertOpciones.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (opciones[i].equals("si")){
                    Intent intent=new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri=Uri.fromParts("package",getApplicationContext().getPackageName(),null);
                    intent.setData(uri);
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(),"Los permisos no fueron aceptados",Toast.LENGTH_SHORT).show();
                    dialogInterface.dismiss();
                }
            }
        });
        alertOpciones.show();
    }


    private void cargarDialogoRecomendacion() {
        android.support.v7.app.AlertDialog.Builder dialogo=new android.support.v7.app.AlertDialog.Builder(getApplicationContext());
        dialogo.setTitle("Permisos Desactivados");
        dialogo.setMessage("Debe aceptar los permisos para el correcto funcionamiento de la App");

        dialogo.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,CAMERA},100);
            }
        });
        dialogo.show();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void cargarListaMunicipios() {
        String url = getApplicationContext().getString(R.string.ipTraerMunicipio)+posicion;
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, this, this);
        request.add(jsonObjectRequest);
        accion = 1;
    }


    private void registrarUsuarios() {
        //progreso = new ProgressDialog(Registro.this);
        //progreso.setMessage("Cargando...");
        //progreso.show();

        String url;
        url = getApplicationContext().getString(R.string.ipRegistro1);
        stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //progreso.hide();
                if (response.trim().equalsIgnoreCase("registra")) {
                    Toast.makeText(getApplicationContext(), "SE REGISTRA " + response, Toast.LENGTH_LONG).show();
                    Log.i("response",response);

                } else {
                    Toast.makeText(getApplicationContext(),"Se ha registrado exitosamente" + response, Toast.LENGTH_LONG).show();
//                    progreso.hide();
                    /*txtDocumento.setText("");
                    txtNombre.setText("");
                    txtApellido.setText("");
                    txtFicha.setText("");
                    txtTelefono.setText("");
                    txtTalla.setText("");
                    txtPeso.setText("");
                    txtEdad.setText("");
                    imgFoto.setImageDrawable(getDrawable(R.drawable.camara));*/
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                progreso.hide();
                Toast.makeText(getApplicationContext(), "No se pudo Registrar" + error.toString(), Toast.LENGTH_LONG).show();
                Log.i("RESULTADO", "NO SE REGISTRA desde onError " + error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                /*String nombres = campoNombre.getText().toString();
                String apellidos = campoApellido.getText().toString();
                String genero = ();
                String correo = campoCorreo.getText().toString();
                String fechaNacimiento = campoFechaNacimiento.getText().toString();
                String departamento = getDepartamento();
                String municipio = getMunicipio();
                String rutaImagen = convertirImgString(bitmap);*/

                String nombres = "victor";
                String apellidos = "garcia";
                String genero = "masculino";
                String correo = "victorsmanuels@outlook.es";
                String fechaNacimiento = "1999-10-24";
                String departamento = "quindio";
                String municipio = "armenia";
                String rutaImagen = convertirImgString(bitmap);

                Map<String, String> parametros = new HashMap<>();
                parametros.put("nombres", nombres);
                parametros.put("apellidos", apellidos);
                parametros.put("genero", genero);
                parametros.put("correo", correo);
                parametros.put("fechaNacimiento", fechaNacimiento);
                parametros.put("departamento", departamento);
                parametros.put("municipio", municipio);
                parametros.put("rutaImagen", rutaImagen);
                return parametros;
            }
        };

        //guardarCredenciales();
        //stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //VolleySingleton.getIntanciaVolley(getApplicationContext()).addToRequestQueue(stringRequest);
        request.add(stringRequest);
        accion = (2);
    }


    private String convertirImgString(Bitmap bitmap) {
        ByteArrayOutputStream array=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,array);
        byte[] imagenByte=array.toByteArray();
        String imagenString= Base64.encodeToString(imagenByte, Base64.DEFAULT);
        return imagenString;
    }


    @Override
    public void onErrorResponse(VolleyError error) {
        if ( accion == 1){
            Toast.makeText(getApplicationContext(), "Se produjo un error al cargar la lista de departamentos " + error.toString(), Toast.LENGTH_LONG).show();
        }else if (accion == 2){
            Toast.makeText(getApplicationContext(), "No se pudo registrar " + error.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResponse(JSONObject response) {
        if ( accion == 1){
//            progreso.hide();
            JSONArray json = response.optJSONArray("usuario");
            JSONObject jsonObject = null;
            ArrayMunicipios = new ArrayList<String>();

            try {
                ArrayMunicipios.add("Seleccione su municipio");
                for (int i = 0; i < json.length(); i++) {
                    jsonObject = json.getJSONObject(i);
                    ArrayMunicipios.add(jsonObject.getString("municipio"));
                }
                ArrayAdapter<CharSequence> adapterMunicipios=new ArrayAdapter(getApplicationContext(),R.layout.support_simple_spinner_dropdown_item,ArrayMunicipios);
                listaMunicipios.setAdapter(adapterMunicipios);
                listaMunicipios.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        if (i!=0){
                            municipio = ArrayMunicipios.get(i);
                            seleccionaMunicipio = true;
                        }else {
                            seleccionaMunicipio = false;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "No se ha podido establecer conexión con el servidor" + " " + response, Toast.LENGTH_LONG).show();

            }
        }else if (accion == 2){
            // progreso.hide();
            Toast.makeText(getApplicationContext(), "Se ha registrado exitosamente", Toast.LENGTH_LONG).show();
        }
    }
}