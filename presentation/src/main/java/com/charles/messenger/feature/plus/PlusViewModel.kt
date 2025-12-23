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
package com.charles.messenger.feature.plus

import com.charles.messenger.common.Navigator
import com.charles.messenger.common.base.QkViewModel
import com.charles.messenger.manager.AnalyticsManager
import com.charles.messenger.manager.BillingManager
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import timber.log.Timber
import javax.inject.Inject

class PlusViewModel @Inject constructor(
    private val analyticsManager: AnalyticsManager,
    private val billingManager: BillingManager,
    private val navigator: Navigator
) : QkViewModel<PlusView, PlusState>(PlusState()) {

    init {
        disposables += billingManager.upgradeStatus
                .subscribe { upgraded -> newState { copy(upgraded = upgraded) } }

        disposables += billingManager.products
                .subscribe { products ->
                    newState {
                        val upgrade = products.firstOrNull { it.sku == BillingManager.SKU_PLUS }
                        val upgradeDonate = products.firstOrNull { it.sku == BillingManager.SKU_PLUS_DONATE }
                        // Use Google Play price if available, otherwise default to $4.99
                        val upgradePrice = upgrade?.price?.takeIf { it.isNotEmpty() } ?: "$4.99"
                        copy(upgradePrice = upgradePrice, 
                                upgradeDonatePrice = upgradeDonate?.price ?: "",
                                currency = upgrade?.priceCurrencyCode ?: upgradeDonate?.priceCurrencyCode ?: "USD")
                    }
                }

        disposables += billingManager.trialStatus
                .subscribe { trialState -> newState { copy(trialState = trialState) } }

        disposables += billingManager.trialDaysRemaining
                .subscribe { days -> newState { copy(trialDaysRemaining = days) } }
    }

    override fun bindView(view: PlusView) {
        super.bindView(view)

        Observable.merge(
                view.upgradeIntent.map { BillingManager.SKU_PLUS },
                view.upgradeDonateIntent.map { BillingManager.SKU_PLUS_DONATE })
                .doOnNext { sku -> 
                    // #region agent log
                    com.charles.messenger.util.DebugLogger.log(
                        location = "PlusViewModel.kt:64",
                        message = "Upgrade button clicked",
                        data = mapOf("sku" to sku),
                        hypothesisId = "H1"
                    )
                    // #endregion
                    analyticsManager.track("Clicked Upgrade", Pair("sku", sku))
                }
                .autoDisposable(view.scope())
                .subscribe(
                    { sku -> 
                        // #region agent log
                        com.charles.messenger.util.DebugLogger.log(
                            location = "PlusViewModel.kt:69",
                            message = "Calling view.initiatePurchaseFlow",
                            data = mapOf("sku" to sku),
                            hypothesisId = "H1"
                        )
                        // #endregion
                        view.initiatePurchaseFlow(billingManager, sku)
                    },
                    { error ->
                        // #region agent log
                        com.charles.messenger.util.DebugLogger.log(
                            location = "PlusViewModel.kt:69",
                            message = "Error in upgrade intent stream",
                            data = mapOf("error" to error.message, "errorType" to error.javaClass.simpleName),
                            hypothesisId = "H1"
                        )
                        // #endregion
                        Timber.e(error, "Error in upgrade intent")
                    }
                )

        view.startTrialIntent
                .doOnNext { analyticsManager.track("Started Trial") }
                .autoDisposable(view.scope())
                .subscribe { billingManager.startTrial() }

        view.donateIntent
                .autoDisposable(view.scope())
                .subscribe { navigator.showDonation() }

        view.themeClicks
                .autoDisposable(view.scope())
                .subscribe { navigator.showSettings() }

        view.scheduleClicks
                .autoDisposable(view.scope())
                .subscribe { navigator.showScheduled() }

        view.backupClicks
                .autoDisposable(view.scope())
                .subscribe { navigator.showBackup() }

        view.delayedClicks
                .autoDisposable(view.scope())
                .subscribe { navigator.showSettings() }

        view.nightClicks
                .autoDisposable(view.scope())
                .subscribe { navigator.showSettings() }
    }

}
