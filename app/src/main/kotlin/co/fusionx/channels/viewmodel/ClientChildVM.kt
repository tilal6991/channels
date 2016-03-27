package co.fusionx.channels.viewmodel

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.databinding.ObservableArrayList
import android.databinding.ObservableList
import co.fusionx.channels.BR

abstract class ClientChildVM : BaseObservable() {
    abstract val name: CharSequence
    val message: CharSequence
        @Bindable get() = buffer.lastOrNull() ?: "No message to show"
    val buffer: ObservableList<CharSequence> = ObservableArrayList()

    fun add(message: String) {
        buffer.add(message)
        notifyPropertyChanged(BR.message)
    }
}