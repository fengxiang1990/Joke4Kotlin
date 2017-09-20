package com.example.wenba.joke4kotlin

interface JokeContract {
    interface View : BaseView<Presenter> {

        fun showJokes(jokes: List<Joke>)

        fun showLoading(loading: Boolean)

    }

    interface Presenter : BasePresenter {

        fun loadJokes(index: Int = 1)
        fun copy(joke : Joke)
    }
}