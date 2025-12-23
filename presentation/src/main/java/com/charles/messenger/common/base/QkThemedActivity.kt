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
import android.app.ActivityManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.view.iterator
import androidx.lifecycle.Lifecycle
import com.charles.messenger.R
import com.charles.messenger.common.util.Colors
import com.charles.messenger.common.util.extensions.resolveThemeBoolean
import com.charles.messenger.common.util.extensions.resolveThemeColor
import com.charles.messenger.extensions.Optional
import com.charles.messenger.extensions.asObservable
import com.charles.messenger.extensions.mapNotNull
import com.charles.messenger.repository.ConversationRepository
import com.charles.messenger.repository.MessageRepository
import com.charles.messenger.util.PhoneNumberUtils
import com.charles.messenger.util.Preferences
import com.charles.messenger.manager.BillingManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Base activity that automatically applies any necessary theme theme settings and colors
 *
 * In most cases, this should be used instead of the base QkActivity, except for when
 * an activity does not depend on the theme
 */
abstract class QkThemedActivity : QkActivity() {

    @Inject lateinit var billingManager: BillingManager
    @Inject lateinit var colors: Colors
    @Inject lateinit var conversationRepo: ConversationRepository
    @Inject lateinit var messageRepo: MessageRepository
    @Inject lateinit var phoneNumberUtils: PhoneNumberUtils
    @Inject lateinit var prefs: Preferences

    /**
     * In case the activity should be themed for a specific conversation, the selected conversation
     * can be changed by pushing the threadId to this subject
     */
    val threadId: Subject<Long> = BehaviorSubject.createDefault(0)

    /**
     * Switch the theme if the threadId changes
     * Set it based on the latest message in the conversation
     */
    val theme: Observable<Colors.Theme> = threadId
            .distinctUntilChanged()
            .switchMap { threadId ->
                val conversation = conversationRepo.getConversation(threadId)
                when {
                    conversation == null -> Observable.just(Optional(null))

                    conversation.recipients.size == 1 -> Observable.just(Optional(conversation.recipients.first()))

                    else -> messageRepo.getLastIncomingMessage(conversation.id)
                            .asObservable()
                            .mapNotNull { messages -> messages.firstOrNull() }
                            .distinctUntilChanged { message -> message.address }
                            .mapNotNull { message ->
                                conversation.recipients.find { recipient ->
                                    phoneNumberUtils.compare(recipient.address, message.address)
                                }
                            }
                            .map { recipient -> Optional(recipient) }
                            .startWith(Optional(conversation.recipients.firstOrNull()))
                            .distinctUntilChanged()
                }
            }
            .switchMap { colors.themeObservable(it.value) }

    @SuppressLint("InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(getActivityThemeRes(prefs.black.get()))
        super.onCreate(savedInstanceState)

        // Initialize AdMob
        MobileAds.initialize(this) {}

        // When certain preferences change, we need to recreate the activity
        val triggers = listOf(prefs.nightMode, prefs.night, prefs.black, prefs.textSize, prefs.systemFont)
        Observable.merge(triggers.map { it.asObservable().skip(1) })
                .debounce(400, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .autoDisposable(scope())
                .subscribe { recreate() }

        // ...
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Initialize and load AdMob banner (only for non-upgraded users)
        try {
            val adView = findViewById<AdView>(R.id.adView)
            // #region agent log
            com.charles.messenger.util.DebugLogger.log(
                location = "QkThemedActivity.kt:128",
                message = "Banner ad view lookup",
                data = mapOf("adViewFound" to (adView != null).toString()),
                hypothesisId = "H9"
            )
            // #endregion
            adView?.let { ad ->
                // #region agent log
                com.charles.messenger.util.DebugLogger.log(
                    location = "QkThemedActivity.kt:130",
                    message = "Subscribing to upgrade status",
                    data = mapOf("adUnitId" to (ad.adUnitId ?: "null")),
                    hypothesisId = "H9"
                )
                // #endregion
                billingManager.upgradeStatus
                    .take(1)
                    .autoDisposable(scope(Lifecycle.Event.ON_DESTROY))
                    .subscribe { upgraded ->
                        // #region agent log
                        com.charles.messenger.util.DebugLogger.log(
                            location = "QkThemedActivity.kt:133",
                            message = "Upgrade status received",
                            data = mapOf("upgraded" to upgraded.toString()),
                            hypothesisId = "H7"
                        )
                        // #endregion
                        if (upgraded) {
                            ad.visibility = View.GONE
                            timber.log.Timber.d("User is upgraded, hiding banner ad")
                        } else {
                            // #region agent log
                            com.charles.messenger.util.DebugLogger.log(
                                location = "QkThemedActivity.kt:138",
                                message = "Loading banner ad",
                                data = mapOf("adUnitId" to (ad.adUnitId ?: "null")),
                                hypothesisId = "H8"
                            )
                            // #endregion
                            val adRequest = AdRequest.Builder().build()
                            ad.loadAd(adRequest)
                            timber.log.Timber.d("Banner ad loading with unit ID: ${ad.adUnitId}")

                            ad.adListener = object : com.google.android.gms.ads.AdListener() {
                                override fun onAdLoaded() {
                                    // #region agent log
                                    com.charles.messenger.util.DebugLogger.log(
                                        location = "QkThemedActivity.kt:143",
                                        message = "Banner ad loaded successfully",
                                        hypothesisId = "H8"
                                    )
                                    // #endregion
                                    timber.log.Timber.d("Banner ad loaded successfully")
                                }

                                override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                                    // #region agent log
                                    com.charles.messenger.util.DebugLogger.log(
                                        location = "QkThemedActivity.kt:147",
                                        message = "Banner ad failed to load",
                                        data = mapOf(
                                            "errorCode" to error.code.toString(),
                                            "errorMessage" to (error.message ?: "null"),
                                            "errorDomain" to (error.domain ?: "null")
                                        ),
                                        hypothesisId = "H8"
                                    )
                                    // #endregion
                                    timber.log.Timber.e("Banner ad failed to load: ${error.message} (${error.code})")
                                }
                            }
                        }
                    }
            } ?: run {
                // #region agent log
                com.charles.messenger.util.DebugLogger.log(
                    location = "QkThemedActivity.kt:129",
                    message = "Banner ad view not found in layout",
                    hypothesisId = "H9"
                )
                // #endregion
            }
        } catch (e: Exception) {
            // #region agent log
            com.charles.messenger.util.DebugLogger.log(
                location = "QkThemedActivity.kt:154",
                message = "Exception loading banner ad",
                data = mapOf("error" to e.message, "errorType" to e.javaClass.simpleName),
                hypothesisId = "H10"
            )
            // #endregion
            timber.log.Timber.e(e, "Error loading banner ad")
        }

        // Set the color for the overflow and navigation icon
        val textSecondary = resolveThemeColor(android.R.attr.textColorSecondary)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar?.overflowIcon = toolbar?.overflowIcon?.apply { setTint(textSecondary) }

        // Update the colours of the menu items
        Observables.combineLatest(menu, theme) { menu, theme ->
            menu.iterator().forEach { menuItem ->
                val tint = when (menuItem.itemId) {
                    in getColoredMenuItems() -> theme.theme
                    else -> textSecondary
                }

                menuItem.icon = menuItem.icon?.apply { setTint(tint) }
            }
        }.autoDisposable(scope(Lifecycle.Event.ON_DESTROY)).subscribe()
    }

    open fun getColoredMenuItems(): List<Int> {
        return listOf()
    }

    /**
     * This can be overridden in case an activity does not want to use the default themes
     */
    open fun getActivityThemeRes(black: Boolean) = when {
        black -> R.style.AppTheme_Black
        else -> R.style.AppTheme
    }

}