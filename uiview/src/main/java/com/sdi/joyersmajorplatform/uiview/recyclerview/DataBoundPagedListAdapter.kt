package com.sdi.joyersmajorplatform.uiview.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.paging.PagedListAdapter

import androidx.recyclerview.widget.DiffUtil

abstract class DataBoundPagedListAdapter<T, V : ViewDataBinding>(diffUtil: DiffUtil.ItemCallback<T>) :
    PagedListAdapter<T, DataBoundViewHolder<V>>(diffUtil) {

    abstract var itemlayout: Int


    companion object {
        const val DEFAULT_LAYOUT = 84740
     
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBoundViewHolder<V> {
        return when(viewType){
             DEFAULT_LAYOUT -> DataBoundViewHolder(createBinding(parent,itemlayout))
             else-> DataBoundViewHolder(createBinding(parent,getLayoutForViewType(viewType)))
         }
    }

    override fun onBindViewHolder(holder: DataBoundViewHolder<V>, position: Int) {
        bind(holder.binding, getItem(position), position)
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
            false)

        return binding
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

}
