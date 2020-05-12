package com.sdi.joyersmajorplatform.uiview.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.sdi.joyersmajorplatform.uiview.NetworkState
import com.sdi.joyersmajorplatform.uiview.R
import com.sdi.joyersmajorplatform.uiview.databinding.NetworkStateItemBinding
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

abstract class DataBoundPagedListAdapter<T, V : ViewDataBinding>(diffUtil: DiffUtil.ItemCallback<T>,
                                                                 private val endLoadingIndicator: Boolean = true,
                                                                 var lifecycleOwner: LifecycleOwner? = null,
                                                                 private val frontLoadingIndicator: Boolean = true,
                                                                 private val enableClicks: Boolean = true) :
    PagedListAdapter<T, DataBoundViewHolder<*>>(diffUtil) , IAdapter<T> {

    companion object {
        const val DEFAULT_LAYOUT = 84740
        const val FRONT_LOADING_INDICATOR = 84741
        const val END_LOADING_INDICATOR = 84742
    }

    private var endLoadingState: NetworkState? = null
    private var frontLoadingState: NetworkState? = null

    private val clickThrottle: Long = 500L


    @LayoutRes
    private val networkStateRes = R.layout.network_state_item


    private val clickSource = PublishSubject.create<T>()
    override val clicks: Observable<T> = clickSource.throttleFirst(500, TimeUnit.MILLISECONDS)

    protected val retryClickSource = PublishSubject.create<NetworkState>()
    val retryClicks: Observable<NetworkState> = retryClickSource.throttleFirst(clickThrottle, TimeUnit.MILLISECONDS)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBoundViewHolder<*> {
        return when (viewType) {
            DEFAULT_LAYOUT -> DataBoundViewHolder(createBinding(parent, defaultLayoutRes))
            FRONT_LOADING_INDICATOR, END_LOADING_INDICATOR -> DataBoundViewHolder(
                createNetworkBinding(parent)
            )
            else -> DataBoundViewHolder(createBinding(parent, getLayoutForViewType(viewType)))
        }
    }

    override fun onBindViewHolder(holder: DataBoundViewHolder<*>, position: Int) {
        holder.binding.lifecycleOwner = lifecycleOwner
        when (getItemViewType(position)) {
            FRONT_LOADING_INDICATOR -> {
                (holder.binding as NetworkStateItemBinding).item = frontLoadingState
            }
            END_LOADING_INDICATOR -> {
                (holder.binding as NetworkStateItemBinding).item = endLoadingState
            }
            else -> {
                val actualPosition = if (isLoadingAtFront()) position - 1 else position
                bind(holder.binding as V, getItem(actualPosition), position)
            }
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


    private fun isLoadingAtEnd() =
        endLoadingState != null && endLoadingState != NetworkState.success

    private fun isLoadingAtFront() =
        frontLoadingState != null && frontLoadingState != NetworkState.success


    private fun getExtraRows(): Int {
        var count = 0
        if (isLoadingAtEnd()) ++count
        if (isLoadingAtFront()) ++count
        return count
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + getExtraRows()
    }

    open fun updateEndLoadingState(newPagingState: NetworkState?) {
        if (!endLoadingIndicator || isLoadingAtFront()) return
        val previousState = this.endLoadingState
        val hadExtraRow = isLoadingAtEnd()
        this.endLoadingState = newPagingState
        val hasExtraRow = isLoadingAtEnd()
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(itemCount + 1)
            } else {
                notifyItemInserted(itemCount)
            }
        } else if (hasExtraRow && previousState != newPagingState) {
            notifyItemChanged(itemCount)
        }
    }

    open fun updateFrontLoadingState(newPagingState: NetworkState?) {
        if (!frontLoadingIndicator || isLoadingAtEnd()) return
        val previousState = this.frontLoadingState
        val hadExtraRow = isLoadingAtFront()
        this.frontLoadingState = newPagingState
        val hasExtraRow = isLoadingAtFront()
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(0)
            } else {
                notifyItemInserted(0)
            }
        } else if (hasExtraRow && previousState != newPagingState) {
            notifyItemChanged(0)
        }
    }


    final override fun getItemViewType(position: Int): Int {
        return if (isLoadingAtEnd() && position == itemCount - getExtraRows()) {
            END_LOADING_INDICATOR
        } else if (isLoadingAtFront() && position == 0) {
            FRONT_LOADING_INDICATOR
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

}
