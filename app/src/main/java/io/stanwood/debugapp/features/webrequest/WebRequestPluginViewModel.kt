package io.stanwood.debugapp.features.webrequest

import android.content.res.Resources
import android.databinding.BaseObservable
import android.databinding.Bindable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.stanwood.debugapp.BR
import io.stanwood.debugapp.R
import io.stanwood.debugapp.databinding.ObservableArrayListEx
import io.stanwood.debugapp.features.HasViewType
import io.stanwood.debugapp.features.KeyValueViewModel
import io.stanwood.debugapp.features.SectionViewModel
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WebRequestPluginViewModel @Inject constructor(private val resources: Resources, private val webRequestDataProvider: WebRequestDataProvider) : BaseObservable() {
    private val disposables = CompositeDisposable()
    val items = ObservableArrayListEx<WebRequestItemViewModel>()
    val detailItems = ObservableArrayListEx<HasViewType>()

    var selectedItem: WebRequestItemViewModel? = null
        @Bindable get
        set(value) {
            if (field != value) {
                field?.selected = false
                field = value
                field?.selected = true
                notifyPropertyChanged(BR.selectedItem)
            }
        }

    val callback: (WebRequestItemViewModel) -> Unit = {
        selectedItem = if (it.webRequestData != selectedItem?.webRequestData) it
                .apply {
                    detailItems.swapItems(mapData(it.webRequestData), null)
                }
        else it.let {
            detailItems.clear()
            null
        }
    }

    private fun mapData(item: WebRequestData) =
            mutableListOf(
                    SectionViewModel(resources.getString(R.string.webrequest_detail_title_general)),
                    KeyValueViewModel(resources.getString(R.string.webrequest_detail_url), item.url),
                    KeyValueViewModel(resources.getString(R.string.webrequest_detail_method), item.method.orEmpty()),
                    KeyValueViewModel(resources.getString(R.string.webrequest_detail_protocol), item.protocol.orEmpty()),
                    KeyValueViewModel(resources.getString(R.string.webrequest_detail_duration), item.duration.toString()))
                    .apply {
                        item.error?.apply {
                            add(KeyValueViewModel(resources.getString(R.string.webrequest_detail_error), this))
                        }
                        add(SectionViewModel(resources.getString(R.string.webrequest_detail_title_request)))
                        add(KeyValueViewModel(resources.getString(R.string.webrequest_detail_date), SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT).format(item.requestDate)))
                        item.requestContentType?.apply {
                            add(KeyValueViewModel(resources.getString(R.string.webrequest_detail_content_type), this))
                        }
                        item.requestContentLength.apply {
                            if (this >= 0) {
                                add(KeyValueViewModel(resources.getString(R.string.webrequest_detail_content_length), this.toString()))
                            }
                        }
                        item.requestHeaders?.joinToString("\r\n", transform = { "${it.first} : ${it.second}" })
                                ?.apply {
                                    add(KeyValueViewModel(resources.getString(R.string.webrequest_detail_headers), this))
                                }
                        add(SectionViewModel(resources.getString(R.string.webrequest_detail_title_response)))
                        add(KeyValueViewModel(resources.getString(R.string.webrequest_detail_date), SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT).format(item.responseDate)))
                        item.responseCode.apply {
                            if (this >= 0) {
                                add(KeyValueViewModel(resources.getString(R.string.webrequest_detail_code), this.toString()))
                            }
                        }
                        item.responseContentType?.apply {
                            add(KeyValueViewModel(resources.getString(R.string.webrequest_detail_content_type), this))
                        }
                        item.responseContentLength.apply {
                            if (this >= 0) {
                                add(KeyValueViewModel(resources.getString(R.string.webrequest_detail_content_length), this.toString()))
                            }
                        }
                        item.responseHeaders?.joinToString("\r\n", transform = { "${it.first} : ${it.second}" })
                                ?.apply {
                                    add(KeyValueViewModel(resources.getString(R.string.webrequest_detail_headers), this))
                                }
                        item.responseMessage?.apply {
                            add(KeyValueViewModel(resources.getString(R.string.webrequest_detail_message), this))
                        }
                    }


    init {
        disposables.add(webRequestDataProvider.webRequestsDataStream
                .sample(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = {
                    val newId = if (it.size > 0) it[0].id else null
                    val currentId = if (items.size > 0) items[0].webRequestData.id else null
                    if (newId != currentId || currentId == null) {
                        items.swapItems(it
                                .map {
                                    WebRequestItemViewModel(it, callback)
                                }.toList(), null)

                    } else {
                        for (i in 0 until Math.min(items.size, it.size)) {
                            if (items[i].webRequestData != it[i]) {
                                items[i].update(it[i])
                            }
                        }
                        if (it.size > items.size) {
                            items.addAll(it.subList(items.size, it.size)
                                    .map {
                                        WebRequestItemViewModel(it, callback)
                                    }.toList())
                        }
                    }
                    selectedItem = selectedItem?.let { selected ->
                        items.firstOrNull {
                            it.webRequestData.id == selected.webRequestData.id
                        }
                    }
                }, onError = { it.printStackTrace() }))
    }

    fun clear() {
        selectedItem = null
        detailItems.clear()
        items.clear()
        webRequestDataProvider.clear()
    }

    fun destroy() {
        disposables.dispose()
    }


}
