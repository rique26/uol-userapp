package com.uol.userapp.features.users.presentation.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.uol.userapp.databinding.FragmentUserDetailBinding
import com.uol.userapp.features.albums.presentation.list.adapter.AlbumsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserDetailFragment : Fragment() {

    private var _binding: FragmentUserDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserDetailViewModel by viewModels()

    private val albumsAdapter = AlbumsAdapter { album ->
//        val action = UserDetailFragmentDirections
//            .actionUserDetailFragmentToAlbumDetailFragment(
//                albumId = album.id,
//                albumTitle = album.title
//            )
//        findNavController().navigate(action)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupRetryButton()
        observeUiState()
    }

    private fun setupToolbar() {
        binding.toolbarUserDetail.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewAlbums.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = albumsAdapter
        }
    }

    private fun setupRetryButton() {
        binding.buttonRetryUserDetail.setOnClickListener { viewModel.loadUserDetail() }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: UserDetailUiState) {
        binding.apply {
            progressBarUserDetail.visibility = toVisibility(state is UserDetailUiState.Loading)
            layoutErrorStateUserDetail.visibility = toVisibility(state is UserDetailUiState.Error)
            layoutContentUserDetail.visibility = toVisibility(state is UserDetailUiState.Success)
        }

        when (state) {
            is UserDetailUiState.Success -> {
                with(binding) {
                    textViewUserName.text = state.user.name
                    textViewUserUsername.text = state.user.username
                    textViewUserEmail.text = state.user.email
                }
                albumsAdapter.submitList(state.albums)
            }
            is UserDetailUiState.Error -> binding.textErrorStateUserDetail.text = state.message
            UserDetailUiState.Loading -> Unit
        }
    }

    private fun toVisibility(isVisible: Boolean): Int = if (isVisible) View.VISIBLE else View.GONE

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerViewAlbums.adapter = null
        _binding = null
    }
}