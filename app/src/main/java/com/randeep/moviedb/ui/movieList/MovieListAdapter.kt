package com.randeep.moviedb.ui.movieList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.randeep.moviedb.data.model.Movie
import com.randeep.moviedb.databinding.ViewMovieItemBinding
import com.randeep.moviedb.databinding.ViewMovieListLoaderBinding

class MovieListAdapter(private val movieListItemListener: MovieListItemListener) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
                private const val VIEW_TYPE_MOVIE = 0
                private const val VIEW_TYPE_LOADING = 1
        }

        private var movieList = ArrayList<Movie>()
        private var moreItemsToLoad = true
        private var totalSearchedItems = 0
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

                return when (viewType) {
                        VIEW_TYPE_MOVIE -> {
                                MovieViewHolder(
                                        ViewMovieItemBinding.inflate(
                                                LayoutInflater.from(parent.context),
                                                parent,
                                                false
                                        )
                                )
                        }

                        VIEW_TYPE_LOADING -> {
                                LoadingViewHolder(
                                        ViewMovieListLoaderBinding.inflate(
                                                LayoutInflater.from(
                                                        parent.context
                                                ), parent, false
                                        )
                                )
                        }

                        else -> throw IllegalArgumentException("Unknown viewType ($viewType)")
                }

        }

        override fun getItemCount(): Int {
                return when (movieList.size > 0 && moreItemsToLoad) {
                        true -> movieList.size + 1
                        else -> movieList.size
                }
        }

        override fun getItemViewType(position: Int): Int {
                return when (position) {
                        movieList.size -> {
                                VIEW_TYPE_LOADING
                        }

                        else -> VIEW_TYPE_MOVIE
                }
        }


        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                when (holder) {
                        is MovieViewHolder -> {
                                holder.bind(movieList[position])
                                if (position == movieList.size - 3 && moreItemsToLoad) {
                                        movieListItemListener.addMoreItemsListener()
                                }
                        }
                }
        }

        class MovieViewHolder(private val binding: ViewMovieItemBinding) :
                RecyclerView.ViewHolder(binding.root) {
                fun bind(movie: Movie) {

                        binding.movieImageView.load(movie.posterUrl) {
                                crossfade(true)
                        }
                        binding.movieTitleTextView.text = movie.title
                        binding.yearTextView.text = movie.year
                }
        }

        class LoadingViewHolder(private val binding: ViewMovieListLoaderBinding) :
                RecyclerView.ViewHolder(binding.root)

        fun submitList(movies: ArrayList<Movie>, totalSearchedResults: Int) {

                movieList.clear()
                movieList.addAll(movies)
                totalSearchedItems = totalSearchedResults
                setIsMoreItemsNeedToLoaded()
                notifyDataSetChanged()

        }

        fun addMoreListItems(movies: ArrayList<Movie>) {
                val currentMovieItemCount = movieList.size
                movieList.addAll(movies)
                setIsMoreItemsNeedToLoaded()
                notifyItemRangeChanged(currentMovieItemCount, movies.size)
        }

        private fun setIsMoreItemsNeedToLoaded() {
                moreItemsToLoad = (movieList.size == totalSearchedItems).not()
        }

        interface MovieListItemListener {
                fun addMoreItemsListener()
        }
}