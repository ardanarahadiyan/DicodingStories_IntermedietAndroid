package com.dicoding.dicodingstories.ui

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.dicodingstories.R
import com.dicoding.dicodingstories.data.remote.ListStoryItem
import com.dicoding.dicodingstories.databinding.ItemPostBinding
import com.dicoding.dicodingstories.ui.detail.DetailActivity

class PostAdapter : PagingDataAdapter<ListStoryItem, PostAdapter.PostViewHoder>(DIFF_CALLBACK){

    class PostViewHoder(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind (storiesResponse: ListStoryItem){
            binding.tvPostName.text = storiesResponse.name
            binding.tvPostDesc.text = storiesResponse.description
            Glide.with(binding.ivPostItem).load(storiesResponse.photoUrl).into(binding.ivPostItem)

            itemView.setOnClickListener{
                val intent = Intent(itemView.context, DetailActivity::class.java)
                intent.putExtra("StoryId", storiesResponse.id)

                val optionsCompat : ActivityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(itemView.context as Activity,
                    Pair(binding.ivPostItem, "post"),Pair(binding.tvPostName, "name"), Pair(binding.tvPostDesc, "description")
                )
                itemView.context.startActivity(intent, optionsCompat.toBundle())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHoder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        return PostViewHoder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHoder, position: Int) {
        val data = getItem(position)
        if (data != null){
            holder.bind(data)
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryItem>(){
            override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: ListStoryItem,
                newItem: ListStoryItem
            ): Boolean {
                return oldItem.id == newItem.id
            }

        }
    }


}