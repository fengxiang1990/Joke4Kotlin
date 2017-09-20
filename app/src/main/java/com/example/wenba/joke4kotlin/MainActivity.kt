package com.example.wenba.joke4kotlin

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration
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
        recycleView.overScrollMode = View.OVER_SCROLL_NEVER
        recycleView.layoutManager = LinearLayoutManager(this)
        recycleView.addItemDecoration(HorizontalDividerItemDecoration.Builder(this)
                .color(Color.WHITE)
                .sizeResId(R.dimen.recycler_divider_height)
                .build())
        adapter = JokeAdapter(jokes)
        recycleView.adapter = adapter
        presenter = JokePresenter(this, this)
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

    inner class JokeAdapter : RecyclerView.Adapter<JokeAdapter.JokeViewHolder> {

        var jokes: List<Joke>

        constructor(jokes: List<Joke>) {
            this.jokes = jokes
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): JokeViewHolder {
            val view = LayoutInflater.from(this@MainActivity).inflate(R.layout.item_joke, parent, false);
            return JokeViewHolder(view)
        }

        override fun onBindViewHolder(holder: JokeViewHolder?, position: Int) {
            val joke = jokes[position]
            holder!!.text1.text = joke.title
            holder!!.text2.text = joke.content
            holder!!.img_copy.setOnClickListener({
                presenter!!.copy(joke)
            })
        }

        override fun getItemCount(): Int {
            return jokes.size
        }


        inner class JokeViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
            var text1 = itemView!!.findViewById(R.id.text1) as TextView
            var text2 = itemView!!.findViewById(R.id.text2) as TextView
            var img_copy = itemView!!.findViewById(R.id.img_copy) as ImageView
        }
    }


}
