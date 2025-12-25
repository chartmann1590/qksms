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
package com.charles.messenger.feature.rewards

import android.app.Activity
import com.charles.messenger.common.base.QkViewModel
import com.charles.messenger.common.util.RewardedAdManager
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import io.reactivex.rxkotlin.plusAssign
import timber.log.Timber
import javax.inject.Inject

class RewardsViewModel @Inject constructor(
    private val activity: Activity,
    private val rewardedAdManager: RewardedAdManager
) : QkViewModel<RewardsView, RewardsState>(RewardsState()) {

    init {
        // Update current points
        updateState()

        // Listen to ad loaded events
        disposables += rewardedAdManager.adLoadedSubject
            .subscribe { isLoaded ->
                Timber.d("Ad loaded: $isLoaded")
                newState { copy(isAdLoading = false, isAdReady = isLoaded) }
            }

        // Load initial ad if not already loaded
        if (!rewardedAdManager.isAdReady()) {
            newState { copy(isAdLoading = true) }
            rewardedAdManager.loadAd(activity)
        } else {
            newState { copy(isAdReady = true) }
        }
    }

    override fun bindView(view: RewardsView) {
        super.bindView(view)

        // Listen to ad earned reward events
        rewardedAdManager.adEarnedRewardSubject
            .autoDisposable(view.scope())
            .subscribe {
                Timber.d("User earned reward!")
                updateState()
                view.showAdEarned()
            }

        // Listen to ad dismissed events
        rewardedAdManager.adDismissedSubject
            .autoDisposable(view.scope())
            .subscribe { wasRewarded ->
                Timber.d("Ad dismissed, was rewarded: $wasRewarded")
                newState { copy(isAdLoading = false, isAdReady = false) }
                // Reload ad for next time
                rewardedAdManager.loadAd(activity)
            }

        view.watchAdIntent
            .autoDisposable(view.scope())
            .subscribe {
                if (rewardedAdManager.isAdReady()) {
                    Timber.d("Showing rewarded ad")
                    rewardedAdManager.showAd(activity)
                } else {
                    Timber.w("Ad not ready, loading...")
                    view.showAdLoadingError()
                    newState { copy(isAdLoading = true) }
                    rewardedAdManager.loadAd(activity)
                }
            }

        view.redeemPointsIntent
            .autoDisposable(view.scope())
            .subscribe { points ->
                Timber.d("Redeeming $points points")
                val success = rewardedAdManager.redeemPoints(points)
                if (success) {
                    val hours = (points * 30) / 60
                    view.showRedemptionSuccess(hours)
                    updateState()
                } else {
                    view.showInsufficientPointsError()
                }
            }
    }

    private fun updateState() {
        val currentPoints = rewardedAdManager.getCurrentPoints()
        val isAdFree = rewardedAdManager.isAdFree()
        val remainingTime = rewardedAdManager.getRemainingAdFreeTime()

        newState {
            copy(
                currentPoints = currentPoints,
                isAdFree = isAdFree,
                remainingAdFreeTime = remainingTime
            )
        }
    }
}
