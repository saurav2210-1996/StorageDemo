package com.rvt.storagedemo.ui.helper

import android.os.FileObserver
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.rvt.storagedemo.ui.State

sealed class PathState{
    class Create(val event : String) : PathState() {

        //avoid same value call multiple time
        //from generate implement this method & in observer add distinctUntilChanged
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Create

            if (event != other.event) return false

            return true
        }

        override fun hashCode(): Int { return event.hashCode() }
    }

    class Delete(val event : String) : PathState()  {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Delete

            if (event != other.event) return false

            return true
        }

        override fun hashCode(): Int {
            return event.hashCode()
        }
    }
}

class FileObserver(path : String) : FileObserver(path)  {

    private val _file = MutableLiveData<PathState>()
    val fileState : LiveData<PathState> = _file


    override fun onEvent(event: Int, file: String?) {
        when(event){
            FileObserver.CLOSE_WRITE/*CREATE*/ -> {
                _file.postValue(PathState.Create(event.toString()))
            }
            FileObserver.DELETE -> {
                _file.postValue(PathState.Delete(event.toString()))
            }
            FileObserver.MODIFY -> {
                _file.postValue(PathState.Create(event.toString()))
            }
        }
    }

    companion object {
        const val FILE_OBSERVER_GARBAGE_COLLECTED_VALUE = 32768
    }
}