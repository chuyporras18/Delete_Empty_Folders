package com.lightappsdev.deleteemptyfolders.activity.main.viewmodel

import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor() : ViewModel() {

    private val _foldersDeleted: MutableLiveData<Array<String>> = MutableLiveData()
    val foldersDeleted: LiveData<Array<String>> = _foldersDeleted

    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun deleteEmptyFolders() {
        viewModelScope.launch {
            val home = Environment.getExternalStorageDirectory() ?: return@launch

            _foldersDeleted.value = arrayOf("Carpetas Eliminadas:")
            _isLoading.postValue(true)

            home.listFiles()?.forEach { file ->
                deleteEmptyFolder(file)
            }

            _foldersDeleted.value =
                _foldersDeleted.value?.plus("\n\n" + "Se terminaron de borrar las carpetas vacías.")
            _isLoading.postValue(false)
        }
    }

    private suspend fun deleteEmptyFolder(file: File) {
        withContext(Dispatchers.IO) {
            if (!file.isDirectory) return@withContext

            if (file.listFiles()?.isNotEmpty() == true) {
                file.listFiles()?.forEach { directory -> deleteEmptyFolder(directory) }
            } else {
                if (file.delete()) {
                    _foldersDeleted.postValue(_foldersDeleted.value?.plus(file.path))
                }
            }
        }
    }
}