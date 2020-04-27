package com.sdi.joyersmajorplatform.uiview.recyclerview

import io.reactivex.Observable


interface IAdapter<T> {
    val clicks: Observable<T>

}