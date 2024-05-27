package com.randeep.moviedb.ui.movieList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.randeep.moviedb.data.remote.networkUtil.HTTPError
import com.randeep.moviedb.data.remote.networkUtil.IOError
import com.randeep.moviedb.data.remote.networkUtil.NoInternet
import com.randeep.moviedb.data.remote.networkUtil.Other
import com.randeep.moviedb.data.remote.networkUtil.TimeOut
import com.randeep.moviedb.databinding.FragmentMovieListSearchBinding
import com.randeep.moviedb.util.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MovieListSearchFragment : Fragment(), MovieListAdapter.MovieListItemListener {

        private var _binding: FragmentMovieListSearchBinding? = null

        private val binding get() = _binding!!

        private val viewModel: MovieListSearchViewModel by viewModels()

        private lateinit var movieListAdapter: MovieListAdapter

        override fun onCreateView(
                inflater: LayoutInflater, container: ViewGroup?,
                savedInstanceState: Bundle?
        ): View {
                _binding = FragmentMovieListSearchBinding.inflate(inflater, container, false)
                val view = binding.root
                return view
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
                super.onViewCreated(view, savedInstanceState)

                movieListAdapter = MovieListAdapter(this)
                binding.searchedMoviesRecyclerView.adapter = movieListAdapter

                initializeSearchBar()
                initializeObservers()
        }

        private fun initializeSearchBar() {

                binding.searchEditText.addTextChangedListener {
                        val text = it?.toString() ?: ""
                        when (text.isEmpty()) {
                                true -> binding.searchCloseImageView.visibility = View.GONE
                                else -> binding.searchCloseImageView.visibility = View.VISIBLE
                        }
                }

                binding.searchCloseImageView.setOnClickListener {
                        binding.searchEditText.setText("")
                        hideKeyboard()
                }

                binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
                        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                                if (binding.searchEditText.text.toString().isEmpty().not()) {
                                        viewModel.searchMovieList(binding.searchEditText.text.toString().trim())
                                        binding.searchHintContainer.visibility = View.GONE
                                }
                                hideKeyboard()
                        }
                        false
                }
        }

        private fun initializeObservers() {

                viewModel.movieList.observe(viewLifecycleOwner) {
                        it?.let {
                                binding.searchedMoviesRecyclerView.visibility = View.VISIBLE
                                binding.searchHintContainer.visibility = View.GONE
                                if (viewModel.getPageNumber() == 1) {
                                        movieListAdapter.submitList(
                                                it,
                                                viewModel.getTotalNumberOfSearchResults()
                                        )
                                } else {
                                        movieListAdapter.addMoreListItems(
                                                movies = it,
                                        )
                                }
                        }

                }

                viewModel.loading.observe(viewLifecycleOwner) {
                        it?.let { isLoading ->
                                when (isLoading) {
                                        true -> binding.progressBarContainer.visibility =
                                                View.VISIBLE

                                        else -> binding.progressBarContainer.visibility = View.GONE
                                }
                        }
                }

                viewModel.remoteError.observe(viewLifecycleOwner) {
                        it?.let { remoteError ->


                                val message = when (remoteError) {
                                        is NoInternet, is TimeOut, is IOError -> getString(
                                                remoteError.messageResId
                                        )

                                        is HTTPError -> remoteError.httpErrorMessage ?: getString(
                                                remoteError.messageResId
                                        )

                                        is Other -> {
                                                if(remoteError.message == "Movie not found!") {
                                                        binding.searchedMoviesRecyclerView.visibility = View.GONE
                                                }
                                                remoteError.message
                                                        ?: getString(remoteError.messageResId)
                                        }
                                }

                                val snackbar = Snackbar.make(
                                        binding.root,
                                        message,

                                        if (remoteError is NoInternet || remoteError is TimeOut) {
                                                Snackbar.LENGTH_INDEFINITE
                                        } else {
                                                Snackbar.LENGTH_SHORT
                                        }
                                )

                                if (remoteError is NoInternet || remoteError is TimeOut) {

                                        snackbar.setAction("RETRY") {
                                                viewModel.retryApiCall()
                                        }
                                }

                                snackbar.show()
                        }
                }
        }

        override fun onDestroyView() {
                super.onDestroyView()
                _binding = null
        }

        override fun addMoreItemsListener() {
                viewModel.getMovieList()
        }


}