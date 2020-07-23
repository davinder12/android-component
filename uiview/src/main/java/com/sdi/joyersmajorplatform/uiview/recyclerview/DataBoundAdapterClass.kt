package com.sdi.joyersmajorplatform.uiview.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.sdi.joyersmajorplatform.uiview.NetworkState
import com.sdi.joyersmajorplatform.uiview.R
import com.sdi.joyersmajorplatform.uiview.databinding.NetworkStateItemBinding
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

abstract class DataBoundAdapterClass<T, V : ViewDataBinding>(
    diffUtil: DiffUtil.ItemCallback<T>,
    private val enableClicks: Boolean = true
) :
    ListAdapter<T, DataBoundViewHolder<*>>(diffUtil), IAdapter<T> {

    companion object {
        const val DEFAULT_LAYOUT = 84740
        const val LOADING_INDICATOR = 84741
    }

    private val clickThrottle: Long = 500L


    private var networkState: NetworkState? = null


    private val clickSource = PublishSubject.create<T>()
    override val clicks: Observable<T> = clickSource.throttleFirst(500, TimeUnit.MILLISECONDS)

    protected val retryClickSource = PublishSubject.create<NetworkState>()
    val retryClicks: Observable<NetworkState> = retryClickSource.throttleFirst(clickThrottle, TimeUnit.MILLISECONDS)

    @LayoutRes
    private val networkStateRes = R.layout.network_state_item

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBoundViewHolder<*> {
        return when (viewType) {
            DEFAULT_LAYOUT -> DataBoundViewHolder(createBinding(parent, defaultLayoutRes))
            LOADING_INDICATOR -> DataBoundViewHolder(createNetworkBinding(parent))
            else -> DataBoundViewHolder(createBinding(parent, getLayoutForViewType(viewType)))
        }
    }


    private fun createNetworkBinding(parent: ViewGroup): NetworkStateItemBinding {
        val binding = NetworkStateItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        binding.retryButton.setOnClickListener {
            binding.item?.let { retryClickSource.onNext(it) }
        }
        return binding
    }

    override fun onBindViewHolder(holder: DataBoundViewHolder<*>, position: Int) {
        when (getItemViewType(position)) {
            LOADING_INDICATOR -> (holder.binding as NetworkStateItemBinding).item = networkState
            else -> bind(holder.binding as V, getItem(position), position)
        }
    }


    fun setNetworkState(newNetworkState: NetworkState?) {
        val previousState = this.networkState
        val hadExtraRow = hasExtraRow()
        this.networkState = newNetworkState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (hasExtraRow && previousState != newNetworkState) {
            notifyItemChanged(itemCount - 1)
        }
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



    final override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == itemCount - 1) {
            LOADING_INDICATOR
        } else {
            getCustomItemViewType(position)
        }
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

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasExtraRow()) 1 else 0
    }

    private fun hasExtraRow() = networkState != null && networkState != NetworkState.success


}
