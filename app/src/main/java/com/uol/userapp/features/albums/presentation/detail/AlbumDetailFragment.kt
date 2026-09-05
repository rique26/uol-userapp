package com.uol.userapp.features.albums.presentation.detail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.uol.userapp.R
import com.uol.userapp.databinding.FragmentAlbumDetailBinding
import com.uol.userapp.features.albums.domain.model.Photo
import com.uol.userapp.features.albums.presentation.detail.adapter.PhotosAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.uol.userapp.core.extensions.applyWindowInsets

private const val GRID_SPAN_COUNT = 3

/**
 * Requisito do enunciado: grid de fotos + clique abre no app padrão de fotos
 * do device (não visualiza dentro do próprio app).
 */
@AndroidEntryPoint
class AlbumDetailFragment : Fragment() {

    private var _binding: FragmentAlbumDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AlbumDetailViewModel by viewModels()
    private val photosAdapter = PhotosAdapter { photo -> openPhotoInDefaultApp(photo) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyWindowInsets(
            rootView = binding.rootAlbumDetail,
            bottomView = binding.recyclerViewPhotos
        )
        setupRecyclerView()
        setupRetryButton()
        observeUiState()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewPhotos.apply {
            layoutManager = GridLayoutManager(requireContext(), GRID_SPAN_COUNT)
            adapter = photosAdapter
        }
    }

    private fun setupRetryButton() {
        binding.buttonRetryAlbumDetail.setOnClickListener { viewModel.loadPhotos() }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: AlbumDetailUiState) {
        binding.progressBarAlbumDetail.visibility = toVisibility(state is AlbumDetailUiState.Loading)
        binding.layoutEmptyStateAlbumDetail.visibility = toVisibility(state is AlbumDetailUiState.Empty)
        binding.layoutErrorStateAlbumDetail.visibility = toVisibility(state is AlbumDetailUiState.Error)
        binding.recyclerViewPhotos.visibility = toVisibility(state is AlbumDetailUiState.Success)

        when (state) {
            is AlbumDetailUiState.Success -> photosAdapter.submitList(state.photos)
            is AlbumDetailUiState.Error -> binding.textErrorStateAlbumDetail.text = state.message
            AlbumDetailUiState.Empty, AlbumDetailUiState.Loading -> Unit
        }
    }

    private fun openPhotoInDefaultApp(photo: Photo) {
        val photoUri = photo.url.toUri()

        // 1. Tenta abrir especificando que o conteúdo é uma imagem
        val imageIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(photoUri, "image/*")
        }

        try {
            startActivity(imageIntent)
        } catch (_: ActivityNotFoundException) {
            // 2. Fallback: se nenhum visualizador de imagem aceitar a URL HTTP, abre via Intent padrão
            try {
                startActivity(Intent(Intent.ACTION_VIEW, photoUri))
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_no_app_to_open_photo),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun toVisibility(isVisible: Boolean): Int = if (isVisible) View.VISIBLE else View.GONE

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerViewPhotos.adapter = null
        _binding = null
    }
}