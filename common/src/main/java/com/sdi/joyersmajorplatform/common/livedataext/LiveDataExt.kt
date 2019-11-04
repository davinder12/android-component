package com.sdi.joyersmajorplatform.common.livedataext

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

fun <T> MutableLiveData<T>.postUpdate(newValue: T) {
    if (this.value != newValue)
        this.postValue(newValue)
}

fun <T> mutableLiveData(newValue: T): MutableLiveData<T> {
    val liveData: MutableLiveData<T> = MutableLiveData()
    liveData.value = newValue
    return liveData
}

fun <TriggerType, EntityType> validationPart(src: LiveData<TriggerType>, f: (TriggerType) -> EntityType): LiveData<EntityType> {
    return src.map(f)
}
