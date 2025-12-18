/*
 * Interstitial Ad Manager for QKSMS
 */
package com.charles.messenger.common.util

import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import timber.log.Timber
import java.util.Random
import javax.inject.Inject
import javax.inject.Singleton

import com.charles.messenger.BuildConfig

@Singleton
class InterstitialAdManager @Inject constructor() {

    companion object {
        private const val AD_UNIT_ID = BuildConfig.ADMOB_INTERSTITIAL_ID
        private const val SHOW_PROBABILITY = 0.3 // 30% chance to show ad
    }

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private val random = Random()

    /**
     * Preload an interstitial ad
     */
    fun loadAd(activity: Activity) {
        if (interstitialAd != null || isLoading) return

        isLoading = true
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(activity, AD_UNIT_ID, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Timber.d("Interstitial ad failed to load: ${adError.message}")
                interstitialAd = null
                isLoading = false
            }

            override fun onAdLoaded(ad: InterstitialAd) {
                Timber.d("Interstitial ad loaded successfully")
                interstitialAd = ad
                isLoading = false

                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Timber.d("Interstitial ad dismissed")
                        interstitialAd = null
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Timber.d("Interstitial ad failed to show: ${adError.message}")
                        interstitialAd = null
                    }

                    override fun onAdShowedFullScreenContent() {
                        Timber.d("Interstitial ad showed")
                    }
                }
            }
        })
    }

    /**
     * Show interstitial ad with probability check
     * Returns true if ad was shown, false otherwise
     */
    fun maybeShowAd(activity: Activity): Boolean {
        // Check probability
        if (random.nextFloat() >= SHOW_PROBABILITY) {
            Timber.d("Skipping ad based on probability")
            loadAd(activity) // Preload for next time
            return false
        }

        return if (interstitialAd != null) {
            interstitialAd?.show(activity)
            true
        } else {
            Timber.d("Interstitial ad not ready, loading for next time")
            loadAd(activity)
            false
        }
    }
}
