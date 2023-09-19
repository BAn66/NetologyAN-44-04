package ru.netologia.nmedia.util

import android.os.Bundle
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object StringArg : ReadWriteProperty<Bundle, String?> {
    //Делегирование работы определенному свойству. проброс значений между фрагментами
    override fun getValue(thisRef: Bundle, property: KProperty<*>): String? = //чтение
        thisRef.getString(property.name)


    override fun setValue(thisRef: Bundle, property: KProperty<*>, value: String?) { //запись
        thisRef.putString(property.name, value)
    }
}