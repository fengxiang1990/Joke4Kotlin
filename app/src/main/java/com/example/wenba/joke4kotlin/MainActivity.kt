package com.example.wenba.joke4kotlin

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), JokeContract.View {

    val data = ArrayList<Map<String, String>>()

    val jokes = ArrayList<Joke>()

    private var adapter: JokeAdapter? = null

    private var presenter: JokeContract.Presenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        listView.setHasLoadMore(false)
        listView.overScrollMode = View.OVER_SCROLL_NEVER


        adapter = JokeAdapter(jokes)
        listView.adapter = adapter
        presenter = JokePresenter(this)
        presenter?.start()
        swipeRefreshLayout.isRefreshing = true
        swipeRefreshLayout.setOnRefreshListener({
            presenter?.loadJokes(Random().nextInt(51))
        })
    }

    override fun showJokes(jokes: List<Joke>) {
        this.jokes.clear()
        this.jokes.addAll(jokes)
        adapter?.notifyDataSetChanged()
    }

    override fun showLoading(loading: Boolean) {
        if (loading) {
            swipeRefreshLayout.isRefreshing = true
        } else {
            swipeRefreshLayout.onRefreshComplete()
        }
    }

    override fun setPresenter(presenter: JokeContract.Presenter) {
        this.presenter = presenter
    }

    inner class JokeAdapter : BaseAdapter {

        var jokes: List<Joke>

        constructor(jokes: List<Joke>) {
            this.jokes = jokes
        }

        override fun getItem(position: Int): Joke {
            return jokes[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getCount(): Int {
            return jokes.size
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
            var viewHolder: ViewHolder
            var view: View
            if (convertView == null) {
                viewHolder = ViewHolder()
                view = LayoutInflater.from(this@MainActivity).inflate(R.layout.item_joke, parent, false)
                view.tag = viewHolder
                viewHolder.text1 = view.findViewById(R.id.text1) as TextView
                viewHolder.text2 = view.findViewById(R.id.text2) as TextView
            } else {
                view = convertView
                viewHolder = view.tag as ViewHolder
            }
            val joke = jokes[position]
            viewHolder.text1?.text = joke.title
            viewHolder.text2?.text = joke.content
            return view
        }
    }

    inner class ViewHolder {
        var text1: TextView? = null
        var text2: TextView? = null
    }
}
