package com.sdi.joyersmajorplatform.uiview.recyclerview

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

class DataBoundViewHolder<out V : ViewDataBinding> constructor(val binding: V) : RecyclerView.ViewHolder(binding.root)
