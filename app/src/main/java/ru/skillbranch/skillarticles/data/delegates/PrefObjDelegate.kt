package ru.skillbranch.skillarticles.data.delegates

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.skillbranch.skillarticles.data.PrefManager
import ru.skillbranch.skillarticles.data.adapters.JsonAdapter
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class PrefObjDelegate<T>(
    private val adapter: JsonAdapter<T>,
    private val customKey: String? = null
) :
    ReadWriteProperty<PrefManager, T?> {
    private var _storedValue: T? = null

    override fun getValue(thisRef: PrefManager, property: KProperty<*>): T? {
        if (_storedValue == null) {
            // async flow
            val flowValue = thisRef.dataStore.data
                .map { prefs ->
                    prefs[stringPreferencesKey(customKey ?: property.name)]
                }
            // sync read
            _storedValue = adapter.fromJson(runBlocking(Dispatchers.IO) { flowValue.first() } ?: "")
        }
        return _storedValue
    }

    override fun setValue(thisRef: PrefManager, property: KProperty<*>, value: T?) {
        _storedValue = value
        //set not blocking
        thisRef.scope.launch {
            thisRef.dataStore.edit { prefs ->
                prefs[stringPreferencesKey(customKey ?: property.name)] = adapter.toJson(value)
            }
        }
    }
}