package com.example.wenba.joke4kotlin

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import org.jetbrains.anko.toast
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class JokePresenter : JokeContract.Presenter {

    val tag = "JokePresenter"

    private val view: JokeContract.View
    private val context: Activity
    private val cm: ClipboardManager

    constructor(context: Activity, view: JokeContract.View) {
        this.view = view
        this.context = context
        this.view.setPresenter(this)
        this.cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }


    override fun start() {
        loadJokes()
    }

    override fun loadJokes(index: Int) {
        load(index)
    }

    override fun copy(joke: Joke) {
        cm.text = joke.title + "\n\n" + joke.content
        context.toast("已复制")
    }


    private fun load(index: Int) {
        Log.e(tag, index.toString())
        val map = LinkedHashMap<Element, Element>()
        val data = ArrayList<Joke>()
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
                    val map = serialize as LinkedHashMap<Element, Element>
                    Observable.from(map.keys)
                })
                .flatMap({ title ->
                    val joke = Joke(title.text(), map[title]!!.text())
                    data.add(joke)
                    Observable.just(joke)
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
                            view.showLoading(false)
                            view.showJokes(data)
                        })
    }

}