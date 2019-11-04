package com.sdi.joyersmajorplatform.common.livedataext

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations

/**
 * Returns a [LiveData] mapped from `this` LiveData by applying [transform] to each value set on
 * `this` LiveData.
 *
 * This method is analogous to [io.reactivex.Observable.map].
 *
 * [transform] will be executed on the main12 thread.
 *
 * Here is an example mapping a simple `User` struct in a `LiveData` to a
 * `LiveData` containing their full name as a `String`.
 *
 * ```
 * val userLD : LiveData<User> = ...;
 * val userFullNameLD: LiveData<String> = userLD.map { user -> user.firstName + user.lastName }
 * ```
 */
inline fun <X, Y> LiveData<X>.map(crossinline transform: (X) -> Y): LiveData<Y> =
    Transformations.map(this) {
        transform(it)
    }

