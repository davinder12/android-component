package com.sdi.joyersmajorplatform.uiview.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

abstract class DataBoundAdapterClass<T, V : ViewDataBinding>(diffUtil: DiffUtil.ItemCallback<T>,
                                                             private val enableClicks: Boolean = true) :
    ListAdapter<T, DataBoundViewHolder<*>>(diffUtil) ,IAdapter<T>{
    
    companion object {
        const val DEFAULT_LAYOUT = 84740
    }

    private val clickSource = PublishSubject.create<T>()
    override val clicks: Observable<T> = clickSource.throttleFirst(500, TimeUnit.MILLISECONDS)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBoundViewHolder<*> {
        return when (viewType) {
            DEFAULT_LAYOUT -> DataBoundViewHolder(createBinding(parent, defaultLayoutRes))
            else -> DataBoundViewHolder(createBinding(parent, getLayoutForViewType(viewType)))
        }
    }

    override fun onBindViewHolder(holder: DataBoundViewHolder<*>, position: Int) {
        bind(holder.binding as V, getItem(position), position)
        holder.binding.executePendingBindings()
    }
    
    /**
     * Override this to customize the view binding
     */
    protected open fun createBinding(parent: ViewGroup, @LayoutRes layoutRes: Int): V {
      val binding = DataBindingUtil.inflate<V>(
            LayoutInflater.from(parent.context),
            layoutRes,
            parent,
            false
        )

        if (enableClicks) {
            binding.root.setOnClickListener {
                onClick(binding)
            }
        }
        return  binding
    }

    var onClickListener: (T) -> Unit = clickSource::onNext


    protected fun onClick(binding: V) {
        map(binding)?.let { onClickListener(it) }
    }




    override fun getItemViewType(position: Int): Int {
        return getCustomItemViewType(position)
    }

    abstract fun bind(bind: V, itemType: T?, position: Int)

    /**
     * The [LayoutRes] for the RecyclerView item
     * This is used to inflate the view.
     */
    protected abstract val defaultLayoutRes: Int
        @LayoutRes get

    @LayoutRes
    protected open fun getLayoutForViewType(viewType: Int): Int {
        return defaultLayoutRes
    }

    protected open fun getCustomItemViewType(position: Int): Int {
        return DEFAULT_LAYOUT
    }

    /**
     * Should return the bound item from a binding.
     * This is used to attach a click listener
     */
    open fun map(binding: V): T? {
        // TODO: log
        return null

    }

}
