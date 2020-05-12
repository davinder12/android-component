package com.sdi.joyersmajorplatform.common.livedataext

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations

fun <T> MutableLiveData<T>.postUpdate(newValue: T) {
    if (this.value != newValue)
        this.postValue(newValue)
}

fun <T> mutableLiveData(newValue: T): MutableLiveData<T> {
    val liveData: MutableLiveData<T> = MutableLiveData()
    liveData.value = newValue
    return liveData
}

fun <TSOURCE, TOUT> mediatorLiveData(
    source: LiveData<TSOURCE>,
    initial: TOUT? = null,
    onChanged: MediatorLiveData<TOUT>.(TSOURCE?) -> Unit
): MediatorLiveData<TOUT> {
    val liveData = MediatorLiveData<TOUT>()
    initial?.let { liveData.postValue(it) }
    liveData.addSource(source) { onChanged(liveData, it) }
    return liveData
}

fun <T> liveData(value: T?): LiveData<T> {
    return mutableLiveData(value)
}

/**
 * Extension wrapper for [Transformations.switchMap]
 */
fun <X, Y> LiveData<X>.switchMap(func: (X) -> LiveData<Y>?): LiveData<Y> = Transformations.switchMap(this, func)


fun <TriggerType, EntityType> validationPart(src: LiveData<TriggerType>, f: (TriggerType) -> EntityType): LiveData<EntityType> {
    return src.map(f)
}
