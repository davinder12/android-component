package com.sdi.joyersmajorplatform.uiview.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.sdi.joyersmajorplatform.uiview.NetworkState
import com.sdi.joyersmajorplatform.uiview.R
import com.sdi.joyersmajorplatform.uiview.databinding.NetworkStateItemBinding
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

abstract class DataBoundAdapterClass<T, V : ViewDataBinding>(diffUtil: DiffUtil.ItemCallback<T>,
                                                             private val enableClicks: Boolean = true,
                                                             private val frontLoadingIndicator: Boolean = true,
                                                             var lifecycleOwner: LifecycleOwner? = null
                                                             ) :
    ListAdapter<T, DataBoundViewHolder<*>>(diffUtil) ,IAdapter<T>{
    
    companion object {
        const val DEFAULT_LAYOUT = 84740
        const val FRONT_LOADING_INDICATOR = 84741
    }

    private val clickThrottle: Long = 500L


    private var frontLoadingState: NetworkState? = null


    private val clickSource = PublishSubject.create<T>()
    override val clicks: Observable<T> = clickSource.throttleFirst(500, TimeUnit.MILLISECONDS)

    protected val retryClickSource = PublishSubject.create<NetworkState>()
    val retryClicks: Observable<NetworkState> = retryClickSource.throttleFirst(clickThrottle, TimeUnit.MILLISECONDS)

    @LayoutRes
    private val networkStateRes = R.layout.network_state_item



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBoundViewHolder<*> {
        return when (viewType) {
            DEFAULT_LAYOUT -> DataBoundViewHolder(createBinding(parent, defaultLayoutRes))
            FRONT_LOADING_INDICATOR-> DataBoundViewHolder(createNetworkBinding(parent))
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
        holder.binding.lifecycleOwner = lifecycleOwner
        when (getItemViewType(position)) {
            FRONT_LOADING_INDICATOR -> (holder.binding as NetworkStateItemBinding).item = frontLoadingState
            else -> {
                val actualPosition = if (isLoadingAtFront()) position - 1 else position
                bind(holder.binding as V, getItem(actualPosition), position)
                holder.binding.executePendingBindings()
            }
        }
    }

    private fun isLoadingAtFront() =
        frontLoadingState != null && frontLoadingState != NetworkState.success



    open fun updateFrontLoadingState(newPagingState: NetworkState?) {
        if (!frontLoadingIndicator) return
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
