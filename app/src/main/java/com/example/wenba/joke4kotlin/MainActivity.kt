package com.example.wenba.joke4kotlin

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.SimpleAdapter
import kotlinx.android.synthetic.main.activity_main.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

class MainActivity : AppCompatActivity() {

    val tag = "MainActivity"

    val data = ArrayList<Map<String, String>>()

    private var adapter: SimpleAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        listView.setHasLoadMore(false)
        adapter = SimpleAdapter(this, data, R.layout.item_joke, arrayOf("title", "content"),
                intArrayOf(R.id.text1, R.id.text2))
        listView.adapter = adapter
        load(1)
        swipeRefreshLayout.isRefreshing = true
        swipeRefreshLayout.setOnRefreshListener({
            load(Random().nextInt(51))
        })
    }

    private fun load(index: Int) {
        Log.e(tag, index.toString())
        val map = LinkedHashMap<Element, Element>()
        Observable.just(index)
                .subscribeOn(Schedulers.newThread())
                /**
                 * step 1
                 * 根据 <div class= "clearfix dl-con">...</div> 抓取网页上的一个笑话div
                 * 其中包括 title，content
                 */
                .flatMap({ index ->
                    var url = "https://www.pengfu.com/xiaohua_$index.html"
                    val document: Document = Jsoup.connect(url).get()
                    val element = document.body()
                    val divElement = element.getElementsByClass("clearfix dl-con")
                    Observable.from(divElement)
                })
                /**
                 * step 2
                 * 根据 title 的 class =“dp-b” 抓取笑话标题Element
                 * 根据 content 的 class =“content-img clearfix pt10 relative” 抓取笑话内容Element
                 */
                .flatMap({ element ->
                    val element0 = element.firstElementSibling()
                    val titleElementsDirty = element0.getElementsByClass("dp-b")
                    val contentElementsDirty = element0.getElementsByClass("content-img clearfix pt10 relative")
                    map.put(titleElementsDirty.first(), contentElementsDirty.first())
                    Log.e(tag, "step 2 onNext")
                    Observable.just("step 2 onNext")
                }, {
                    Observable.just(Exception("step2 flat error"))
                }, {
                    Log.e(tag, "step 2 onComplete")
                    Observable.just(map)
                })
                .filter({ serialize ->
                    serialize is LinkedHashMap<*, *>
                })
                /**
                 * step 3
                 * 将获取到的笑话数据转换 添加到listview 中
                 */
                .flatMap({ serialize ->
                    Log.e(tag, "step 3")
                    data.clear()
                    val map = serialize as LinkedHashMap<Element, Element>
                    Observable.from(map.keys)
                })
                .flatMap({ title ->
                    val econtent = map[title]!!
                    val item = HashMap<String, String>()
                    item.put("title", title.text())
                    item.put("content", econtent.text())
                    data.add(item)
                    Observable.just(item)
                }, { e ->
                    Observable.just(e)
                }, {
                    Observable.just(data)
                })
                .filter({ serialize ->
                    serialize is ArrayList<*>
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            Log.e(tag, "subscribe onNext")
                        },
                        { e ->
                            e.printStackTrace()
                        },
                        {
                            Log.e(tag, "subscribe onComplete")
                            adapter?.notifyDataSetChanged()
                            swipeRefreshLayout.onRefreshComplete()
                        })
    }
}
