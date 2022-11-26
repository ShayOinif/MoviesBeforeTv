package com.shayo.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.load
import com.shayo.movies.Movie

class MoviesAdapter : ListAdapter<Movie, MoviesAdapter.MovieViewHolder>(MovieCallback()) {

    class MovieViewHolder(itemView: View) : ViewHolder(itemView) {
        private val title = itemView.findViewById<TextView>(R.id.movie_title)
        private val poster = itemView.findViewById<ImageView>(R.id.movie_poster)

        fun bind(movie: Movie) {
            title.text = movie.title
            poster.load("https://image.tmdb.org/t/p/w500/${movie.posterPath}")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        return MovieViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.view_movie, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(currentList[position])
    }
}

private class MovieCallback : DiffUtil.ItemCallback<Movie>() {
    override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
        return oldItem.posterPath == newItem.posterPath &&
                oldItem.title == newItem.title
    }
}