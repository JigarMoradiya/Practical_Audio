package com.jigar.audioplayer.adapter

import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.jigar.audioplayer.R
import com.jigar.audioplayer.databinding.RawAudioListBinding
import com.jigar.audioplayer.datamodel.AudioDataModel
import com.jigar.audioplayer.utils.extensions.layoutInflater
import com.jigar.audioplayer.utils.extensions.onClick

class AudioListAdapter(
    var listData: MutableList<AudioDataModel>,
    private val mListener: OnItemClickListener
) :
    RecyclerView.Adapter<AudioListAdapter.ViewHolder>() {
    var currPositionPlaying = -1
    interface OnItemClickListener {
        fun onItemPlayStopClick(position: Int,isPlayClick : Boolean)
    }

    fun addItem(data: AudioDataModel) {
        listData.add(data)
        notifyItemRangeChanged(0, listData.size)
    }
    fun setData(listData: MutableList<AudioDataModel>) {
        this.listData = listData
        notifyItemRangeChanged(0, listData.size)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(parent.context.layoutInflater,R.layout.raw_audio_list,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data: AudioDataModel = listData[position]
        holder.itemBinding.dataModel = data
        holder.itemBinding.imgPlay.onClick {
            mListener.onItemPlayStopClick(holder.layoutPosition,true)
            holder.itemBinding.isPlaying = true
        }
        holder.itemBinding.imgPause.setOnClickListener {
            mListener.onItemPlayStopClick(holder.layoutPosition,false)
        }
    }

    class ViewHolder(val itemBinding: RawAudioListBinding) : RecyclerView.ViewHolder(itemBinding.root)

    override fun getItemCount(): Int {
        return listData.size
    }

}