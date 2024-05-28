package com.randeep.moviedb.ui.movieList

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.randeep.moviedb.R
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

                        when(movie.type) {
                                "movie" -> {
                                        binding.typeImageView.setImageResource(R.drawable.icon_movie)
                                        binding.movieImageView.contentDescription = "${movie.type} ${movie.title}"
                                        binding.typeImageView.contentDescription = movie.type
                                }
                                else -> {
                                        binding.typeImageView.setImageResource(R.drawable.icon_tv)
                                        binding.movieImageView.contentDescription = "${movie.type} ${movie.title}"
                                        binding.typeImageView.contentDescription = movie.type
                                }
                        }
                }
        }

        // holder class to show loader while fetching more data
        class LoadingViewHolder(binding: ViewMovieListLoaderBinding) :
                RecyclerView.ViewHolder(binding.root)


        @SuppressLint("NotifyDataSetChanged")
        fun submitList(movies: ArrayList<Movie>, totalSearchedResults: Int) {
                movieList.clear()
                movieList.addAll(movies)
                totalSearchedItems = totalSearchedResults

                setIsMoreItemsNeedToLoaded()

                // when different text is searched,
                // need to tell notify adapter that whole list will be created again
                notifyDataSetChanged()

        }

        // function to add more movies in existing list
        fun addMoreListItems(movies: ArrayList<Movie>) {

                val currentMovieItemCount = movieList.size
                movieList.addAll(movies.takeLast(movies.size - movieList.size))

                setIsMoreItemsNeedToLoaded()
                notifyItemRangeChanged(currentMovieItemCount, movies.size - currentMovieItemCount)
        }

        // function to know the end of the list, to know when to show loader at the bottom
        private fun setIsMoreItemsNeedToLoaded() {
                moreItemsToLoad = (movieList.size == totalSearchedItems).not()
        }

        interface MovieListItemListener {
                fun addMoreItemsListener()
        }
}