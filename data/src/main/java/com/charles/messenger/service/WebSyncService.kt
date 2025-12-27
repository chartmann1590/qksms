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
package com.charles.messenger.service

import android.annotation.SuppressLint
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import com.charles.messenger.common.util.extensions.jobScheduler
import com.charles.messenger.interactor.SyncToWebServer
import com.charles.messenger.util.Preferences
import dagger.android.AndroidInjection
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Background service that performs periodic incremental syncs to the web server
 * when web sync is enabled. Runs every 30 minutes.
 */
class WebSyncService : JobService() {

    companion object {
        private const val JobId = 8120236

        /**
         * Schedule periodic incremental sync job (every 30 minutes)
         */
        @SuppressLint("MissingPermission") // Added in [presentation]'s AndroidManifest.xml
        fun scheduleJob(context: Context) {
            Timber.i("Scheduling web sync job")
            val serviceComponent = ComponentName(context, WebSyncService::class.java)
            val periodicJob = JobInfo.Builder(JobId, serviceComponent)
                .setPeriodic(TimeUnit.MINUTES.toMillis(30))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY) // Requires network connection
                .setPersisted(true) // Persist across device reboots
                .build()

            context.jobScheduler.schedule(periodicJob)
        }

        /**
         * Cancel the periodic sync job
         */
        fun cancelJob(context: Context) {
            Timber.i("Canceling web sync job")
            context.jobScheduler.cancel(JobId)
        }
    }

    @Inject lateinit var syncToWebServer: SyncToWebServer
    @Inject lateinit var preferences: Preferences

    private val disposables = CompositeDisposable()

    override fun onStartJob(params: JobParameters?): Boolean {
        Timber.i("WebSyncService: onStartJob")
        AndroidInjection.inject(this)

        // Only sync if web sync is enabled
        if (!preferences.webSyncEnabled.get()) {
            Timber.i("WebSyncService: Web sync disabled, skipping")
            jobFinished(params, false)
            return false
        }

        // Perform incremental sync in background
        syncToWebServer.execute(SyncToWebServer.Params(isFullSync = false)) {
            Timber.i("WebSyncService: Incremental sync completed")
            jobFinished(params, false)
        }

        return true // Job is running asynchronously
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Timber.i("WebSyncService: onStopJob")
        disposables.dispose()
        return true // Reschedule if job was interrupted
    }
}
