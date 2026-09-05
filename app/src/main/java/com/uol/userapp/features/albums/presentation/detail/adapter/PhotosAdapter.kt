package com.uol.userapp.features.albums.presentation.detail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.uol.userapp.R
import com.uol.userapp.databinding.ItemPhotoBinding
import com.uol.userapp.features.albums.domain.model.Photo

/**
 * Único ponto do app que usa Picasso (requisito obrigatório do enunciado).
 * Só o thumbnailUrl é carregado na grid — a url completa é usada ao abrir a
 * foto no app padrão de fotos do device.
 */
class PhotosAdapter(
    private val onPhotoClick: (Photo) -> Unit
) : ListAdapter<Photo, PhotosAdapter.PhotoViewHolder>(PhotoDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PhotoViewHolder(
        private val binding: ItemPhotoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(photo: Photo) {
            Picasso.get()
                .load(photo.thumbnailUrl)
                .placeholder(android.R.color.darker_gray)
                .error(R.drawable.ic_broken_image)
                .into(binding.imageViewPhotoThumbnail)

            binding.root.setOnClickListener { onPhotoClick(photo) }
        }
    }

    private object PhotoDiffCallback : DiffUtil.ItemCallback<Photo>() {
        override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean = oldItem == newItem
    }
}