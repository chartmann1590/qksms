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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.bluelinelabs.conductor.archlifecycle.LifecycleController
import com.charles.messenger.R
import com.charles.messenger.databinding.ToolbarBinding

abstract class QkController<ViewContract : QkViewContract<State>, State, Presenter : QkPresenter<ViewContract, State>> : LifecycleController() {

    abstract var presenter: Presenter

    private val appCompatActivity: AppCompatActivity?
        get() = activity as? AppCompatActivity

    protected val themedActivity: QkThemedActivity?
        get() = activity as? QkThemedActivity

    @Deprecated("Use getView() instead", ReplaceWith("getView()"))
    protected var controllerView: View? = null
    private var toolbarBinding: ToolbarBinding? = null

    @LayoutRes
    var layoutRes: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(layoutRes, container, false).also {
            controllerView = it
            // Try to bind toolbar if it exists and has the standard layout
            it.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.let { toolbar ->
                // Only bind if toolbar has the standard layout with toolbarTitle
                // Check multiple times to ensure the view exists before calling bind()
                val toolbarTitleView = toolbar.findViewById<View>(R.id.toolbarTitle)
                if (toolbarTitleView != null) {
                    try {
                        // Double-check that the view is still there and is the right type
                        val titleView = toolbar.findViewById<android.widget.TextView>(R.id.toolbarTitle)
                        if (titleView != null) {
                            toolbarBinding = ToolbarBinding.bind(toolbar)
                        } else {
                            toolbarBinding = null
                        }
                    } catch (e: NullPointerException) {
                        // ToolbarBinding.bind() throws NPE if required views are missing
                        toolbarBinding = null
                    } catch (e: Exception) {
                        // Toolbar doesn't have standard layout, skip binding
                        toolbarBinding = null
                    }
                } else {
                    toolbarBinding = null
                }
            }
            onViewCreated(it)
        }
    }

    open fun onViewCreated(view: View) {
    }

    fun setTitle(@StringRes titleId: Int) {
        setTitle(activity?.getString(titleId))
    }

    fun setTitle(title: CharSequence?) {
        activity?.title = title
        toolbarBinding?.toolbarTitle?.text = title
    }

    fun showBackButton(show: Boolean) {
        appCompatActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(show)
    }

    override fun onDestroyView(view: View) {
        this.controllerView = null
        toolbarBinding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onCleared()
    }

}
