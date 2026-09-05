package com.uol.userapp.features.users.presentation.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.uol.userapp.databinding.FragmentUserListBinding
import com.uol.userapp.features.users.presentation.list.adapter.UsersAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserListFragment : Fragment() {

    private var _binding: FragmentUserListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UsersViewModel by viewModels()

    private val usersAdapter = UsersAdapter {

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchView()
        setupSwipeRefresh()
        observeUiState()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewUsers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = usersAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupSearchView() {
        binding.searchViewUsers.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.onSearchQueryChanged(newText.orEmpty())
                return true
            }
        })
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshUsers.setOnRefreshListener {
            viewModel.loadUsers()
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: UsersUiState) {
        binding.apply {
            swipeRefreshUsers.isRefreshing = false
            progressBarUsers.visibility = toVisibility(state is UsersUiState.Loading)
            layoutEmptyStateUsers.visibility = toVisibility(state is UsersUiState.Empty)
            layoutErrorStateUsers.visibility = toVisibility(state is UsersUiState.Error)
            recyclerViewUsers.visibility = toVisibility(state is UsersUiState.Success)
        }
        when (state) {
            is UsersUiState.Success -> usersAdapter.submitList(state.users)
            is UsersUiState.Error -> {
                binding.textErrorStateUsers.text = state.message
                binding.buttonRetryUsers.setOnClickListener { viewModel.loadUsers() }
            }
            UsersUiState.Empty, UsersUiState.Loading -> Unit
        }
    }

    private fun toVisibility(isVisible: Boolean): Int =
        if (isVisible) View.VISIBLE else View.GONE

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerViewUsers.adapter = null
        _binding = null
    }
}