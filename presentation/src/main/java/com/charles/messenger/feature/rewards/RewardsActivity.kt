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

import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding2.view.clicks
import com.charles.messenger.R
import com.charles.messenger.common.base.QkThemedActivity
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RewardsActivity : QkThemedActivity(), RewardsView {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    override val watchAdIntent: Subject<Unit> = PublishSubject.create()
    override val redeemPointsIntent: Subject<Int> = PublishSubject.create()

    private lateinit var pointsText: TextView
    private lateinit var watchAdButton: Button
    private lateinit var adLoadingProgress: ProgressBar
    private lateinit var adFreeStatusText: TextView
    private lateinit var redemptionContainer: android.view.ViewGroup

    // Redemption buttons
    private lateinit var redeem1Button: Button
    private lateinit var redeem2Button: Button
    private lateinit var redeem4Button: Button
    private lateinit var redeem8Button: Button
    private lateinit var redeem12Button: Button

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory)[RewardsViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rewards_activity)

        setTitle(R.string.title_rewards)
        showBackButton(true)

        pointsText = findViewById(R.id.pointsText)
        watchAdButton = findViewById(R.id.watchAdButton)
        adLoadingProgress = findViewById(R.id.adLoadingProgress)
        adFreeStatusText = findViewById(R.id.adFreeStatusText)
        redemptionContainer = findViewById(R.id.redemptionContainer)

        redeem1Button = findViewById(R.id.redeem1Button)
        redeem2Button = findViewById(R.id.redeem2Button)
        redeem4Button = findViewById(R.id.redeem4Button)
        redeem8Button = findViewById(R.id.redeem8Button)
        redeem12Button = findViewById(R.id.redeem12Button)

        viewModel.bindView(this)

        // Set up click listeners
        watchAdButton.clicks()
            .autoDisposable(scope())
            .subscribe(watchAdIntent)

        redeem1Button.clicks()
            .map { 1 }
            .autoDisposable(scope())
            .subscribe(redeemPointsIntent)

        redeem2Button.clicks()
            .map { 2 }
            .autoDisposable(scope())
            .subscribe(redeemPointsIntent)

        redeem4Button.clicks()
            .map { 4 }
            .autoDisposable(scope())
            .subscribe(redeemPointsIntent)

        redeem8Button.clicks()
            .map { 8 }
            .autoDisposable(scope())
            .subscribe(redeemPointsIntent)

        redeem12Button.clicks()
            .map { 12 }
            .autoDisposable(scope())
            .subscribe(redeemPointsIntent)
    }

    override fun render(state: RewardsState) {
        pointsText.text = getString(R.string.rewards_points_available, state.currentPoints)

        // Show/hide ad loading progress
        adLoadingProgress.isVisible = state.isAdLoading
        watchAdButton.isEnabled = !state.isAdLoading && state.isAdReady
        watchAdButton.text = when {
            state.isAdLoading -> getString(R.string.rewards_loading_ad)
            state.isAdReady -> getString(R.string.rewards_watch_ad)
            else -> getString(R.string.rewards_ad_not_ready)
        }

        // Show ad-free status
        if (state.isAdFree) {
            val hours = state.remainingAdFreeTime / (60 * 60 * 1000)
            val minutes = (state.remainingAdFreeTime % (60 * 60 * 1000)) / (60 * 1000)
            val timeString = when {
                hours > 0 -> getString(R.string.rewards_ad_free_hours_minutes, hours, minutes)
                else -> getString(R.string.rewards_ad_free_minutes, minutes)
            }
            adFreeStatusText.text = getString(R.string.rewards_ad_free_status, timeString)
            adFreeStatusText.isVisible = true
        } else {
            adFreeStatusText.isVisible = false
        }

        // Enable/disable redemption buttons based on available points
        redeem1Button.isEnabled = state.currentPoints >= 1
        redeem2Button.isEnabled = state.currentPoints >= 2
        redeem4Button.isEnabled = state.currentPoints >= 4
        redeem8Button.isEnabled = state.currentPoints >= 8
        redeem12Button.isEnabled = state.currentPoints >= 12
    }

    override fun showAdLoadingError() {
        Toast.makeText(this, R.string.rewards_ad_loading_error, Toast.LENGTH_SHORT).show()
    }

    override fun showAdEarned() {
        Toast.makeText(this, R.string.rewards_ad_earned, Toast.LENGTH_SHORT).show()
    }

    override fun showRedemptionSuccess(hours: Int) {
        val message = when (hours) {
            0 -> getString(R.string.rewards_redemption_success_minutes, 30)
            else -> getString(R.string.rewards_redemption_success_hours, hours)
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun showInsufficientPointsError() {
        Toast.makeText(this, R.string.rewards_insufficient_points, Toast.LENGTH_SHORT).show()
    }
}
