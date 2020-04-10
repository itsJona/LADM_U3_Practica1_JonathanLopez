package com.example.ladm_u3_practica1_jonathanlopez

import android.app.Activity
import android.content.ContentValues
import android.database.sqlite.SQLiteException
import java.sql.Date

class Actividad(d:String, fc:String, fe:String) {
    var id = 0
    var descripcion = d
    var fecha_c = fc
    var fecha_e = fe

    var error = -1
    /*
    valores de error:
        1=Error en tabla, no se creó o no se conectó a la base de datos
        2=Error: No se pudo insertar
        3=No se pudo realizar consulta o tabla vacia
        4=No se encontro ID
        5=Error a la hora de actualizar

     */
    val nombreBaseDeDatos = "tareas"
    var puntero :Activity ?= null

    fun asignarPuntero (p:MainActivity){
        puntero = p
    }





    fun insertar():Boolean{
        error=-1
       try{
           var base= BaseDeDatos(puntero!!,nombreBaseDeDatos,null,1)
           var insertar = base.writableDatabase
           var datos= ContentValues()

           datos.put("DESCRIPCION",descripcion)
           datos.put("FECHA_C",fecha_c)
           datos.put("FECHA_E",fecha_e)

           var respuesta =insertar.insert("ACTIVIDAD","ID_ACTIVIDAD",datos)
           if(respuesta.toInt()==-1){
               error=2
               return false
           }
       }catch (e:SQLiteException){
           error=1
           return false
       }
        return true
    }

    fun mostrarTodos():ArrayList<Actividad>{
        var data = ArrayList<Actividad>()
        error = -1
        try {
            var base= BaseDeDatos(puntero!!,nombreBaseDeDatos,null,1)
            var select = base.readableDatabase
            var columnas = arrayOf("*")

            var cursor = select.query("ACTIVIDAD",columnas,null,null,null,null,null)
            if(cursor.moveToFirst()){
                do{
                    var actividadTemporal = Actividad(cursor.getString(1),cursor.getString(2),cursor.getString(3))
                    actividadTemporal.id = cursor.getInt(0)
                    data.add(actividadTemporal)
                }while (cursor.moveToNext())
            }else{
                error = 3
            }

        }catch (e:SQLiteException){
            error=1
        }

        return data
    }


    fun buscar(id:String): Actividad{
        var actividadEncontrada = Actividad("-1","-1","-1")
        error = -1
        try {
            var base=BaseDeDatos(puntero!!,nombreBaseDeDatos,null,1)
            var select = base.readableDatabase
            var columnas = arrayOf("*")
            var idBuscar = arrayOf(id)
            var cursor = select.query("ACTIVIDAD",columnas,"ID_ACTIVIDAD=?",idBuscar,null,null,null)
            if(cursor.moveToFirst()){
                actividadEncontrada.id=id.toInt()
                actividadEncontrada.descripcion=cursor.getString(1)
                actividadEncontrada.fecha_c=cursor.getString(2)
                actividadEncontrada.fecha_e=cursor.getString(3)
            }else{
                error=4
            }

        }catch (e:SQLiteException){
                error=1
        }
        return actividadEncontrada
    }
    fun mostrarTodosPersonalizado(campo:String,texto:String):ArrayList<Actividad>{
        var data = ArrayList<Actividad>()
        error = -1
        try {
            var base= BaseDeDatos(puntero!!,nombreBaseDeDatos,null,1)
            var select = base.readableDatabase
            var columnas = arrayOf("*")
            var t= arrayOf(texto)

            var cursor = select.query("ACTIVIDAD",columnas,"${campo}=?",t,null,null,null)
            if(cursor.moveToFirst()){
                do{
                    var actividadTemporal = Actividad(cursor.getString(1),cursor.getString(2),cursor.getString(3))
                    actividadTemporal.id = cursor.getInt(0)
                    data.add(actividadTemporal)
                }while (cursor.moveToNext())
            }else{
                error = 3
            }

        }catch (e:SQLiteException){
            error=1
        }

        return data
    }

    fun eliminar(id:String):Boolean{
        error=-1
        try{
            var base= BaseDeDatos(puntero!!,nombreBaseDeDatos,null,1)
            var eliminar = base.writableDatabase
            var idBuscar = arrayOf(id)
            var respuesta1 =eliminar.delete("EVIDENCIA","ID_ACTIVIDAD=?",idBuscar)
            var respuesta2 =eliminar.delete("ACTIVIDAD","ID_ACTIVIDAD=?",idBuscar)

            if(respuesta1==-1 && respuesta2==-1){
                error=6
                return false
            }
        }catch (e:SQLiteException){
            error=1
            return false
        }
        return true
    }
}