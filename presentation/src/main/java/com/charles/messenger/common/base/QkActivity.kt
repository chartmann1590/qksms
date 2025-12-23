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

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import com.charles.messenger.*

abstract class QkActivity : AppCompatActivity() {

    protected val menu: Subject<Menu> = BehaviorSubject.create()
    private var toolbarTitle: TextView? = null

    @SuppressLint("InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onNewIntent(intent)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Setup window insets for activities that don't extend QkThemedActivity
        // (QkThemedActivity will override this and provide more specific handling)
        setupWindowInsets()
    }

    /**
     * Sets up window insets handling to respect system bars.
     * Can be overridden by subclasses for custom handling.
     */
    protected open fun setupWindowInsets() {
        val rootView = window.decorView.findViewById<View>(android.R.id.content)
        rootView?.let { root ->
            ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                // For QkActivity (non-themed), apply padding to root or find a content view
                val contentView = root.findViewById<View>(R.id.root) ?: root
                contentView.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
                )
                insets
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        // Find and bind toolbar if it exists
        try {
            val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
            if (toolbar != null) {
                // Only set toolbarTitle if it exists (not all toolbars have toolbarTitle)
                // Do NOT use ToolbarBinding.bind() here as it requires toolbarTitle to exist
                toolbarTitle = toolbar.findViewById(R.id.toolbarTitle)
                setSupportActionBar(toolbar)
                title = title // The title may have been set before layout inflation
            }
        } catch (e: Exception) {
            // Ignore any toolbar binding errors
        }
    }

    override fun setTitle(titleId: Int) {
        title = getString(titleId)
    }

    override fun setTitle(title: CharSequence?) {
        super.setTitle(title)
        toolbarTitle?.text = title
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val result = super.onCreateOptionsMenu(menu)
        if (menu != null) {
            this.menu.onNext(menu)
        }
        return result
    }

    protected open fun showBackButton(show: Boolean) {
        supportActionBar?.setDisplayHomeAsUpEnabled(show)
    }

}