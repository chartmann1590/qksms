/*
 * Copyright (C) 2020 Moez Bhatti <charles.bhatti@gmail.com>
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

package com.charles.messenger.common.util

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.android.billingclient.api.PurchasesResult
import com.android.billingclient.api.QueryPurchaseHistoryParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryPurchaseHistory
import com.android.billingclient.api.queryPurchases
import com.android.billingclient.api.querySkuDetails
import com.charles.messenger.manager.AnalyticsManager
import com.charles.messenger.manager.BillingManager
import com.charles.messenger.util.Preferences
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManagerImpl @Inject constructor(
    context: Context,
    private val analyticsManager: AnalyticsManager,
    private val prefs: Preferences
) : BillingManager, BillingClientStateListener, PurchasesUpdatedListener {

    private val productsSubject: Subject<List<SkuDetails>> = BehaviorSubject.create()
    override val products: Observable<List<BillingManager.Product>> = productsSubject
            .map { skuDetailsList ->
                skuDetailsList.map { skuDetails ->
                    BillingManager.Product(skuDetails.sku, skuDetails.price, skuDetails.priceCurrencyCode)
                }
            }

    private val trialStatusSubject: BehaviorSubject<BillingManager.TrialState> = BehaviorSubject.createDefault(getTrialState())
    override val trialStatus: Observable<BillingManager.TrialState> = trialStatusSubject.distinctUntilChanged()

    override val trialDaysRemaining: Observable<Int> = trialStatusSubject.map { calculateTrialDaysRemaining() }

    private val purchaseListSubject = BehaviorSubject.create<List<Purchase>>()
    private val purchasedStatus: Observable<Boolean> = purchaseListSubject
            .map { purchases ->
                purchases
                        .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                        .any { it.sku in skus }
            }
            .distinctUntilChanged()
            .doOnNext { upgraded -> analyticsManager.setUserProperty("Upgraded", upgraded) }

    override val upgradeStatus: Observable<Boolean> = Observable.combineLatest(
            purchasedStatus.startWith(false),
            trialStatus
    ) { purchased, trial ->
        purchased || trial == BillingManager.TrialState.ACTIVE
    }.distinctUntilChanged()

    private val skus = listOf(BillingManager.SKU_PLUS, BillingManager.SKU_PLUS_DONATE)
    private val billingClient: BillingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

    private val billingClientState = MutableSharedFlow<Int>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    init {
        billingClientState.tryEmit(BillingClient.BillingResponseCode.SERVICE_DISCONNECTED)
    }

    override suspend fun checkForPurchases() = executeServiceRequest {
        // Load the cached data
        queryPurchases()

        // On a fresh device, the purchase might not be cached, and so we'll need to force a refresh
        val historyParams = QueryPurchaseHistoryParams.newBuilder()
                .setProductType(com.android.billingclient.api.ProductType.INAPP)
                .build()
        billingClient.queryPurchaseHistory(historyParams)
        queryPurchases()
    }

    override suspend fun queryProducts() = executeServiceRequest {
        val params = SkuDetailsParams.newBuilder()
                .setSkusList(skus)
                .setType(BillingClient.SkuType.INAPP)

        val (billingResult, skuDetailsList) = billingClient.querySkuDetails(params.build())
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            productsSubject.onNext(skuDetailsList.orEmpty())
        }
    }

    override suspend fun initiatePurchaseFlow(activity: Activity, sku: String) = executeServiceRequest {
        val skuDetails = withContext(Dispatchers.IO) {
            val params = SkuDetailsParams.newBuilder()
                    .setType(BillingClient.SkuType.INAPP)
                    .setSkusList(listOf(sku))
                    .build()

            billingClient.querySkuDetails(params).skuDetailsList?.firstOrNull()!!
        }

        val params = BillingFlowParams.newBuilder().setSkuDetails(skuDetails)
        billingClient.launchBillingFlow(activity, params.build())
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            GlobalScope.launch(Dispatchers.IO) {
                handlePurchases(purchases.orEmpty())
            }
        }
    }

    private suspend fun queryPurchases() {
        val params = com.android.billingclient.api.QueryPurchasesParams.newBuilder()
                .setProductType(com.android.billingclient.api.ProductType.INAPP)
                .build()
        val result = billingClient.queryPurchases(params)
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            handlePurchases(result.purchases)
        }
    }

    private suspend fun handlePurchases(purchases: List<Purchase>) = executeServiceRequest {
        purchases.forEach { purchase ->
            if (!purchase.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()

                Timber.i("Acknowledging purchase ${purchase.orderId}")
                val result = billingClient.acknowledgePurchase(params)
                Timber.i("Acknowledgement result: ${result.responseCode}, ${result.debugMessage}")
            }
        }

        purchaseListSubject.onNext(purchases)
    }

    private suspend fun executeServiceRequest(runnable: suspend () -> Unit) {
        if (billingClientState.first() != BillingClient.BillingResponseCode.OK) {
            Timber.i("Starting billing service")
            billingClient.startConnection(this)
        }

        billingClientState.first { state -> state == BillingClient.BillingResponseCode.OK }
        runnable()
    }

    override fun onBillingSetupFinished(result: BillingResult) {
        Timber.i("Billing response: ${result.responseCode}")
        billingClientState.tryEmit(result.responseCode)
    }

    override fun onBillingServiceDisconnected() {
        Timber.i("Billing service disconnected")
        billingClientState.tryEmit(BillingClient.BillingResponseCode.SERVICE_DISCONNECTED)
    }

    override fun startTrial() {
        // Check if trial has already been used for this device (persists across reinstalls)
        val existingTrialStart = prefs.getTrialStartTimestampForDevice()
        if (existingTrialStart != 0L) {
            Timber.i("Trial already used for this device. Start: $existingTrialStart")
            // Update the status to reflect current state (might be expired)
            trialStatusSubject.onNext(getTrialState())
            return
        }
        
        // Check legacy preference as well
        if (prefs.trialStartTimestamp.get() != 0L) {
            Timber.i("Trial already used (legacy). Migrating to device-specific tracking.")
            // Migrate to device-specific tracking
            prefs.setTrialStartTimestampForDevice(prefs.trialStartTimestamp.get())
            trialStatusSubject.onNext(getTrialState())
            return
        }
        
        // Start new trial
        val now = System.currentTimeMillis()
        prefs.setTrialStartTimestampForDevice(now)
        trialStatusSubject.onNext(getTrialState())
        Timber.i("Trial started for device at: $now")
    }

    private fun getTrialState(): BillingManager.TrialState {
        // Check device-specific trial first (persists across reinstalls)
        var trialStart = prefs.getTrialStartTimestampForDevice()
        // Fallback to legacy preference for backward compatibility
        if (trialStart == 0L) {
            trialStart = prefs.trialStartTimestamp.get()
            // Migrate legacy preference to device-specific if it exists
            if (trialStart != 0L) {
                prefs.setTrialStartTimestampForDevice(trialStart)
            }
        }
        
        if (trialStart == 0L) {
            return BillingManager.TrialState.NOT_STARTED
        }
        val trialDurationMillis = TimeUnit.DAYS.toMillis(BillingManager.TRIAL_DURATION_DAYS.toLong())
        val trialEnd = trialStart + trialDurationMillis
        return if (System.currentTimeMillis() < trialEnd) {
            BillingManager.TrialState.ACTIVE
        } else {
            BillingManager.TrialState.EXPIRED
        }
    }

    private fun calculateTrialDaysRemaining(): Int {
        // Check device-specific trial first (persists across reinstalls)
        var trialStart = prefs.getTrialStartTimestampForDevice()
        // Fallback to legacy preference for backward compatibility
        if (trialStart == 0L) {
            trialStart = prefs.trialStartTimestamp.get()
        }
        
        if (trialStart == 0L) return 0
        val trialDurationMillis = TimeUnit.DAYS.toMillis(BillingManager.TRIAL_DURATION_DAYS.toLong())
        val trialEnd = trialStart + trialDurationMillis
        val remaining = trialEnd - System.currentTimeMillis()
        return if (remaining > 0) {
            TimeUnit.MILLISECONDS.toDays(remaining).toInt() + 1
        } else {
            0
        }
    }

}
