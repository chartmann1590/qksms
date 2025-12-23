/*
 * Copyright (C) 2019 Moez Bhatti <charles.bhatti@gmail.com>
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
package com.charles.messenger.common.widget

import android.app.Activity
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.charles.messenger.R
import com.charles.messenger.common.base.QkAdapter

class QkDialog(private val context: Activity) : AlertDialog(context) {

    private val view = LayoutInflater.from(context).inflate(R.layout.qk_dialog, null)
    private val titleView: TextView = view.findViewById(R.id.title)
    private val subtitleView: TextView = view.findViewById(R.id.subtitle)
    private val listView: RecyclerView = view.findViewById(R.id.list)
    private val positiveButtonView: Button = view.findViewById(R.id.positiveButton)
    private val negativeButtonView: Button = view.findViewById(R.id.negativeButton)

    @StringRes
    var titleRes: Int? = null
        set(value) {
            field = value
            title = value?.let(context::getString)
        }

    var title: String? = null
        set(value) {
            field = value
            titleView.text = value
            titleView.isVisible = !value.isNullOrBlank()
        }

    @StringRes
    var subtitleRes: Int? = null
        set(value) {
            field = value
            subtitle = value?.let(context::getString)
        }

    var subtitle: String? = null
        set(value) {
            field = value
            subtitleView.text = value
            subtitleView.isVisible = !value.isNullOrBlank()
        }

    var adapter: QkAdapter<*>? = null
        set(value) {
            field = value
            listView.isVisible = value != null
            listView.adapter = value
        }

    var positiveButtonListener: (() -> Unit)? = null

    @StringRes
    var positiveButton: Int? = null
        set(value) {
            field = value
            value?.run(positiveButtonView::setText)
            positiveButtonView.isVisible = value != null
            positiveButtonView.setOnClickListener {
                positiveButtonListener?.invoke() ?: dismiss()
            }
        }

    var negativeButtonListener: (() -> Unit)? = null

    @StringRes
    var negativeButton: Int? = null
        set(value) {
            field = value
            value?.run(negativeButtonView::setText)
            negativeButtonView.isVisible = value != null
            negativeButtonView.setOnClickListener {
                negativeButtonListener?.invoke() ?: dismiss()
            }
        }

    var cancelListener: (() -> Unit)? = null
        set(value) {
            field = value
            setOnCancelListener { value?.invoke() }
        }

    init {
        setView(view)
    }

}
