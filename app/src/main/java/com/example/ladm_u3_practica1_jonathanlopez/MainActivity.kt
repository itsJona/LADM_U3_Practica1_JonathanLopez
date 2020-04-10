package com.example.ladm_u3_practica1_jonathanlopez

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {
    var listaID= ArrayList<String>()
    var bitmap :Bitmap ?= null
    private val PHOTO_SELECTED = 1
    var act_actual =0
    @RequiresApi(Build.VERSION_CODES.O)
    var fechaC =fechaActual()
    @RequiresApi(Build.VERSION_CODES.O)
    var fechaE =fechaActual()
    var dd =""
    var mm = ""
    var dialogoImagen :Dialog ?= null
    var infoAct =""


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rgroup.setOnCheckedChangeListener { group, checkedId ->

            if (rbTodos.isChecked) {
                txtBuscar.setHint("Buscador ")
            }
            if (rbID.isChecked) {
                txtBuscar.setHint("ID a buscar ")
            }
            if (rbDescripcion.isChecked) {
                txtBuscar.setHint("Descripción a buscar")
            }
            if (rbFechaC.isChecked) {
                txtBuscar.setHint("DD/MM/AAAA")
            }
            if (rbFechaE.isChecked) {
                txtBuscar.setHint("DD/MM/AAAA")
            }

        }
        btnBuscar.setOnClickListener {

            var campo=""
            var texto = txtBuscar.text.toString()
            if(rbTodos.isChecked){ cargarLista()
                return@setOnClickListener}

            if (rbID.isChecked) {
                campo = "ID_ACTIVIDAD"
                txtBuscar.setHint("ID a buscar ")
            }
            if (rbDescripcion.isChecked) {
                campo = "DESCRIPCION"
                txtBuscar.setHint("Descripción a buscar")
            }
            if (rbFechaC.isChecked) {
                campo = "FECHA_C"
                txtBuscar.setHint("DD/MM/AAAA")
            }
            if (rbFechaE.isChecked) {
                campo = "FECHA_E"
                txtBuscar.setHint("DD/MM/AAAA")
            }
            busquedaPersonalizada(campo,texto)
        }
        //AGREGAR ACTIVIDAD
        btnAgregar.setOnClickListener {
            var dialogo = Dialog(this)
            dialogo.setContentView(R.layout.agregar_actividad)

            var descripcion = dialogo.findViewById<EditText>(R.id.txtDescripcion)
            var calendario = dialogo.findViewById<CalendarView>(R.id.calendar)
            var agregar = dialogo.findViewById<Button>(R.id.btnAgregarActividad)
            var cancelar = dialogo.findViewById<Button>(R.id.btnCancelar)


            agregar.setOnClickListener {
                if(descripcion.text.isEmpty()){mensaje("No se ha capturado una descripción.");return@setOnClickListener}
                AlertDialog.Builder(this)
                    .setTitle("CONFIRMAR DATOS")
                    .setMessage("Se agregará la actividad con los siguientes datos:\n\n" +
                            "Descripción: ${descripcion.text.toString()}\n" +
                            "Fecha de captura: ${fechaC}\n" +
                            "Fecha de entrega: ${fechaE}")
                    .setPositiveButton("Aceptar"){d,i->
                       insertar(descripcion)
                        dialogo.dismiss()
                        d.dismiss()
                    }
                    .setNegativeButton("Cancelar"){d,i->
                        d.cancel()
                    }
                    .show()

            }
            cancelar.setOnClickListener {
                if(!descripcion.text.isEmpty()){
                    AlertDialog.Builder(this)
                        .setTitle("Atención")
                        .setMessage("¿salir sin guardar los datos?")
                        .setPositiveButton("Aceptar"){d,i->d.dismiss(); dialogo.dismiss()}
                        .setNegativeButton("Seguir editando"){d,i->d.cancel()}
                        .show()
                }else{
                    dialogo.dismiss()
                }
            }
            calendario?.setOnDateChangeListener { view, year, month, dayOfMonth ->
                // Note that months are indexed from 0. So, 0 means January, 1 means february, 2 means march etc.
                if(dayOfMonth<=9){dd="0"+dayOfMonth}else{dd=""+dayOfMonth}
                if((month+1)<=9){ mm= "0"+(month + 1) }else{mm=""+(month+1)}

                fechaE = dd+"/"+mm+"/"+ year
            }

            dialogo.show()
        }
        cargarLista()

    }
    fun busquedaPersonalizada(c:String,t:String){

            try {
                var conexion = Actividad("", "", "")
                conexion.asignarPuntero(this)
                var data = conexion.mostrarTodosPersonalizado(c,t)
                if (data.size == 0) {

                    var vacio = Array<String>(data.size, { "" })
                    lista.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,vacio)
                    mensaje("Sin Resultados")
                    return
                }
                var total = data.size - 1
                var vector = Array<String>(data.size, { "" })
                listaID = ArrayList<String>()
                (0..total).forEach {
                    var actividad = data[it]
                    var item=""
                    if (rbID.isChecked) {
                        item ="\nID:" +actividad.id+"\n"
                    }
                    if (rbDescripcion.isChecked) {
                        item ="\nDescripción: "+actividad.descripcion+"\n"
                    }
                    if (rbFechaC.isChecked) {
                        item ="\nFecha de captura: "+actividad.fecha_c + "\n"
                    }
                    if (rbFechaE.isChecked) {
                        item ="\nFecha de entrega:  "+actividad.fecha_e+"\n"
                    }
                    
                    vector[it] = item
                    listaID.add(actividad.id.toString())
                }
                lista.adapter =
                    ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, vector)
                lista.setOnItemClickListener { parent, view, position, id ->

                    var con = Actividad("", "", "")
                    con.asignarPuntero(this)
                    var actividadEncontrada = con.buscar(listaID[position])
                    act_actual = actividadEncontrada.id

                    if (con.error == 4) {
                        dialogo("Error 4: No se encontro ID")
                        return@setOnItemClickListener
                    }
                    infoAct ="\nID:" +actividadEncontrada.id+"\n\n"+
                            "Descripción: "+actividadEncontrada.descripcion+"\n\n"+
                            "Fecha de captura: "+actividadEncontrada.fecha_c + "\n\n" +
                            "Fecha de entrega:  "+actividadEncontrada.fecha_e+"\n"

                    AlertDialog.Builder(this)
                        .setTitle("DETALLES DE LA ACTIVIDAD")
                        .setMessage(infoAct)
                        .setPositiveButton("Ver Evidencias") { d, i ->
                            agregarEvidencia()
                        }
                        .setNegativeButton("Eliminar Actividad") { d, i ->
                            eliminarActividad(act_actual.toString())
                            cargarLista()
                        }
                        .setNeutralButton("Cancelar") { d, i ->
                            d.cancel()
                        }.show()

                }


            } catch (e: Exception) {
                dialogo(e.message.toString())
            }

    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun insertar(des:EditText){
        var actividad =  Actividad(des.text.toString(),fechaC,fechaE)
        actividad.asignarPuntero(this)
        var resultado = actividad.insertar()
        if(resultado==true){
            mensaje("Se capturó actividad")
            cargarLista()
            des.setText("")
            fechaC=fechaActual()
            fechaE=fechaActual()
        }else{
            when(actividad.error){
                1->{
                    dialogo("Error en tabla, no se creó o no se conectó a la base de datos")
                }
                2->{
                    dialogo("Error: No se pudo insertar")
                }
            }
        }

    }
    fun cargarLista(){
        try{
            var conexion = Actividad("","","")
            conexion.asignarPuntero(this)
            var data=conexion.mostrarTodos()
            if(data.size==0){
                if(conexion.error==3){
                    var vacio = Array<String>(data.size, { "" })
                    lista.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,vacio)
                    //dialogo("No se pudo realizar consulta o tabla vacia")

                }
                return
            }
            var total=data.size-1
            var vector = Array<String>(data.size,{""})
            listaID= ArrayList<String>()
            (0..total).forEach{
                var actividad= data[it]
                var item = "\nDescripción: "+actividad.descripcion+"\n\n"+
                           "Fecha de captura: "+actividad.fecha_c + "\n\n" +
                           "Fecha de entrega: "+actividad.fecha_e+"\n"
                vector[it]=item
                listaID.add(actividad.id.toString())
            }
            lista.adapter= ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,vector)
            lista.setOnItemClickListener { parent, view, position, id ->

               var con = Actividad("","","")
                con.asignarPuntero(this)
                var actividadEncontrada = con.buscar(listaID[position])
                act_actual=actividadEncontrada.id

                if (con.error==4){
                    dialogo("Error 4: No se encontro ID")
                    return@setOnItemClickListener
                }
                infoAct="\nID:" +actividadEncontrada.id+"\n\n"+
                        "Descripción: "+actividadEncontrada.descripcion+"\n\n"+
                        "Fecha de captura: "+actividadEncontrada.fecha_c + "\n\n" +
                        "Fecha de entrega: "+actividadEncontrada.fecha_e+"\n"

                    AlertDialog.Builder(this)
                        .setTitle("DETALLES DE LA ACTIVIDAD")
                        .setMessage(infoAct)
                        .setPositiveButton("Ver Evidencias"){d,i->
                            agregarEvidencia()
                            d.dismiss()
                        }
                        .setNegativeButton("Eliminar Actividad"){d,i->
                            AlertDialog.Builder(this)
                                .setTitle("Atención")
                                .setMessage("¿Desea borrar esta actividad?")
                                .setPositiveButton("Aceptar"){d,i->
                                    eliminarActividad(act_actual.toString())
                                    cargarLista()
                                    d.dismiss()
                                }
                                .setNegativeButton("Cancelar"){d,i->
                                    d.cancel()
                                }.show()

                        }
                        .setNeutralButton("Cancelar"){d,i->
                            d.cancel()
                        }.show()

            }


        }catch (e:Exception){
            dialogo(e.message.toString())
        }
    }

    fun agregarEvidencia(){

        dialogoImagen = Dialog(this)
        dialogoImagen?.setContentView(R.layout.evidencias)
        var btnAgregarEvidencia = dialogoImagen?.findViewById<Button>(R.id.btnAgregarEvidencia)
        var btnCancelarEv = dialogoImagen?.findViewById<Button>(R.id.btnCancelarE)
        var lblInfo=dialogoImagen?.findViewById<TextView>(R.id.lblDatosActividad)

        btnAgregarEvidencia?.setOnClickListener {
            abrirGaleria()
        }
        btnCancelarEv?.setOnClickListener {
            dialogoImagen!!.cancel()
        }

        dialogoImagen?.show()
        lblInfo?.setText(infoAct)
        cargarImagenes()
    }
    fun mensaje(mensaje:String){
        Toast.makeText(this,mensaje,Toast.LENGTH_LONG).show()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun fechaActual():String{
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        var fechaA = current.format(formatter)
        return fechaA
    }
    fun dialogo(mensaje:String){
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Atención")
            .setMessage(mensaje)
            .setPositiveButton("OK"){d,i->}
            .show()
    }
    fun abrirGaleria(){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PHOTO_SELECTED)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PHOTO_SELECTED) {
                val selectedImage: Uri? = data?.data
            if( selectedImage==null){
                return}
                bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,selectedImage)

            var listaEvidencias = dialogoImagen?.findViewById<LinearLayout>(R.id.llBotonera)
            val img= ImageView(this)
            img.setImageBitmap(bitmap)
            listaEvidencias?.addView(img)

            val bos = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.PNG, 100, bos)
            val bArray: ByteArray = bos.toByteArray()

            insertarEvidencia(act_actual,bArray)
        }
    }

    fun insertarEvidencia(id_a :Int,img:ByteArray){
        var evidencia = Evidencia(id_a,img)
        evidencia.asignarPuntero(this)
        var resultado = evidencia.insertar()
        if(resultado==true){
            mensaje("Se capturó evidencia")
            cargarLista()
        }else{
            when(evidencia.error){
                1->{
                    dialogo("Error en tabla, no se creó o no se conectó a la base de datos")
                }
                2->{
                    dialogo("Error: No se pudo insertar")
                }
            }
        }

    }

    fun eliminarActividad(id:String){
        var conexion = Actividad("","","")
        conexion.asignarPuntero(this)
        var resultado = conexion.eliminar(id)
        if(resultado==true){
            mensaje("Se eliminó actividad")

        }else{
            when(conexion.error){
                1->{
                    dialogo("Error en tabla, no se creó o no se conectó a la base de datos")
                }
                2->{
                    dialogo("Error: No se pudo eliminar")
                }
            }
        }
    }
    fun cargarImagenes(){
        val arr = byteArrayOf(0)
        try{
            var conexion = Evidencia(0,arr)
            conexion.asignarPuntero(this)
            var data=conexion.mostrarTodos(act_actual.toString())
            if(data.size==0){
                if(conexion.error==3){
                    //dialogo("No se pudo realizar consulta o tabla vacia")
                }
                return
            }
            var total=data.size-1
            var listaEvidencias = dialogoImagen?.findViewById<LinearLayout>(R.id.llBotonera)
            (0..total).forEach{
                var evidencia= data[it]
                var img= ImageView(this)
                img.setImageBitmap(ByteArrayToBitmap(evidencia))
                listaEvidencias?.addView(img)
            }
        }catch (e:Exception){
            dialogo(e.message.toString())
        }
    }
    fun ByteArrayToBitmap(byteArray: ByteArray?): Bitmap? {
        val arrayInputStream = ByteArrayInputStream(byteArray)
        return BitmapFactory.decodeStream(arrayInputStream)
    }
    }

