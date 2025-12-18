/*
 * Copyright (C) 2017 Moez Bhatti <charles.bhatti@gmail.com>
 *
 * This file is part of messenger.
 *
 * messenger is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * messenger is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with messenger.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.charles.messenger.common.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.charles.messenger.common.util.extensions.setVisible
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import io.realm.OrderedRealmCollection
import io.realm.OrderedCollectionChangeSet
import io.realm.RealmChangeListener
import io.realm.RealmList
import io.realm.RealmModel
import io.realm.RealmResults
import timber.log.Timber

abstract class QkRealmAdapter<T : RealmModel> : RecyclerView.Adapter<QkViewHolder>() {

    private var realmData: OrderedRealmCollection<T>? = null

    // Public accessor for compatibility
    val data: OrderedRealmCollection<T>? get() = realmData

    /**
     * This view can be set, and the adapter will automatically control the visibility of this view
     * based on the data
     */
    var emptyView: View? = null
        set(value) {
            if (field === value) return

            field = value
            value?.setVisible(realmData?.isLoaded == true && realmData?.isEmpty() == true)
        }

    @Suppress("UNCHECKED_CAST")
    private val resultsListener = RealmChangeListener<RealmResults<T>> { data ->
        emptyView?.setVisible(data.isLoaded && data.isEmpty())
        notifyDataSetChanged()
    }

    @Suppress("UNCHECKED_CAST")
    private val listListener = RealmChangeListener<RealmList<T>> { data ->
        emptyView?.setVisible(data.isLoaded && data.isEmpty())
        notifyDataSetChanged()
    }

    val selectionChanges: Subject<List<Long>> = BehaviorSubject.create()

    private var selection = listOf<Long>()

    /**
     * Toggles the selected state for a particular view
     *
     * If we are currently in selection mode (we have an active selection), then the state will
     * toggle. If we are not in selection mode, then we will only toggle if [force]
     */
    protected fun toggleSelection(id: Long, force: Boolean = true): Boolean {
        if (!force && selection.isEmpty()) return false

        selection = when (selection.contains(id)) {
            true -> selection - id
            false -> selection + id
        }

        selectionChanges.onNext(selection)
        return true
    }

    protected fun isSelected(id: Long): Boolean {
        return selection.contains(id)
    }

    fun clearSelection() {
        selection = listOf()
        selectionChanges.onNext(selection)
        notifyDataSetChanged()
    }

    open fun getItem(index: Int): T? {
        if (index < 0 || realmData == null) {
            return null
        }
        return realmData!![index]
    }

    override fun getItemCount(): Int = realmData?.size ?: 0

    open fun updateData(data: OrderedRealmCollection<T>?) {
        if (this.realmData === data) return

        removeListener(this.realmData)
        this.realmData = data
        addListener(data)

        if (data != null && data.isValid && data.isLoaded) {
            emptyView?.setVisible(data.isEmpty())
        }
        notifyDataSetChanged()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        addListener(realmData)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        removeListener(realmData)
    }

    @Suppress("UNCHECKED_CAST")
    private fun addListener(data: OrderedRealmCollection<T>?) {
        if (data != null && data.isValid) {
            when (data) {
                is RealmResults<*> -> (data as RealmResults<T>).addChangeListener(resultsListener)
                is RealmList<*> -> (data as RealmList<T>).addChangeListener(listListener)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun removeListener(data: OrderedRealmCollection<T>?) {
        if (data != null && data.isValid) {
            when (data) {
                is RealmResults<*> -> (data as RealmResults<T>).removeChangeListener(resultsListener)
                is RealmList<*> -> (data as RealmList<T>).removeChangeListener(listListener)
            }
        }
    }

}