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
package com.charles.messenger.feature.blocking

import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import com.bluelinelabs.conductor.RouterTransaction
import com.jakewharton.rxbinding2.view.clicks
import com.charles.messenger.R
import com.charles.messenger.common.QkChangeHandler
import com.charles.messenger.common.base.QkController
import com.charles.messenger.common.util.Colors
import com.charles.messenger.common.util.extensions.animateLayoutChanges
import com.charles.messenger.common.widget.PreferenceView
import com.charles.messenger.feature.blocking.manager.BlockingManagerController
import com.charles.messenger.feature.blocking.messages.BlockedMessagesController
import com.charles.messenger.feature.blocking.numbers.BlockedNumbersController
import com.charles.messenger.injection.appComponent
import io.reactivex.Observable
import javax.inject.Inject

class BlockingController : QkController<BlockingView, BlockingState, BlockingPresenter>(), BlockingView {

    private lateinit var parent: ViewGroup
    private lateinit var blockingManager: PreferenceView
    private lateinit var blockedNumbers: PreferenceView
    private lateinit var blockedMessages: PreferenceView
    private lateinit var drop: PreferenceView

    override val blockingManagerIntent: Observable<*> by lazy { 
        if (::blockingManager.isInitialized) blockingManager.clicks() else Observable.never()
    }
    override val blockedNumbersIntent: Observable<*> by lazy { 
        if (::blockedNumbers.isInitialized) blockedNumbers.clicks() else Observable.never()
    }
    override val blockedMessagesIntent: Observable<*> by lazy { 
        if (::blockedMessages.isInitialized) blockedMessages.clicks() else Observable.never()
    }
    override val dropClickedIntent: Observable<*> by lazy { 
        if (::drop.isInitialized) drop.clicks() else Observable.never()
    }

    @Inject lateinit var colors: Colors
    @Inject override lateinit var presenter: BlockingPresenter

    init {
        appComponent.inject(this)
        retainViewMode = RetainViewMode.RETAIN_DETACH
        layoutRes = R.layout.blocking_controller
    }


    override fun onAttach(view: View) {
        super.onAttach(view)
        // Ensure views are initialized before binding intents
        if (::blockingManager.isInitialized && ::blockedNumbers.isInitialized && 
            ::blockedMessages.isInitialized && ::drop.isInitialized) {
            presenter.bindIntents(this)
        }
        setTitle(R.string.blocking_title)
        showBackButton(true)
    }
    
    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        parent = view.findViewById(R.id.parent)
        blockingManager = view.findViewById(R.id.blockingManager)
        blockedNumbers = view.findViewById(R.id.blockedNumbers)
        blockedMessages = view.findViewById(R.id.blockedMessages)
        drop = view.findViewById(R.id.drop)

        parent.postDelayed({ parent.animateLayoutChanges = true }, 100)
        
        // Bind intents after views are initialized
        if (::presenter.isInitialized) {
            presenter.bindIntents(this)
        }
    }

    override fun render(state: BlockingState) {
        blockingManager.summary = state.blockingManager
        // Handle both CheckBox and QkSwitch widgets
        val checkbox = drop.findViewById<View>(R.id.checkbox)
        when (checkbox) {
            is CheckBox -> checkbox.isChecked = state.dropEnabled
            is com.charles.messenger.common.widget.QkSwitch -> checkbox.isChecked = state.dropEnabled
        }
        blockedMessages.isEnabled = !state.dropEnabled
    }

    override fun openBlockedNumbers() {
        router.pushController(RouterTransaction.with(BlockedNumbersController())
                .pushChangeHandler(QkChangeHandler())
                .popChangeHandler(QkChangeHandler()))
    }

    override fun openBlockedMessages() {
        router.pushController(RouterTransaction.with(BlockedMessagesController())
                .pushChangeHandler(QkChangeHandler())
                .popChangeHandler(QkChangeHandler()))
    }

    override fun openBlockingManager() {
        router.pushController(RouterTransaction.with(BlockingManagerController())
                .pushChangeHandler(QkChangeHandler())
                .popChangeHandler(QkChangeHandler()))
    }

}
