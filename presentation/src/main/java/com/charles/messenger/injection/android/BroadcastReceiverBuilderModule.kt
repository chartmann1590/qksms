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
package com.charles.messenger.injection.android

import com.charles.messenger.feature.widget.WidgetProvider
import com.charles.messenger.injection.scope.ActivityScope
import com.charles.messenger.receiver.BlockThreadReceiver
import com.charles.messenger.receiver.BootReceiver
import com.charles.messenger.receiver.DefaultSmsChangedReceiver
import com.charles.messenger.receiver.DeleteMessagesReceiver
import com.charles.messenger.receiver.MarkArchivedReceiver
import com.charles.messenger.receiver.MarkReadReceiver
import com.charles.messenger.receiver.MarkSeenReceiver
import com.charles.messenger.receiver.MmsReceivedReceiver
import com.charles.messenger.receiver.MmsReceiver
import com.charles.messenger.receiver.MmsSentReceiver
import com.charles.messenger.receiver.MmsUpdatedReceiver
import com.charles.messenger.receiver.NightModeReceiver
import com.charles.messenger.receiver.RemoteMessagingReceiver
import com.charles.messenger.receiver.SendScheduledMessageReceiver
import com.charles.messenger.receiver.SmsDeliveredReceiver
import com.charles.messenger.receiver.SmsProviderChangedReceiver
import com.charles.messenger.receiver.SmsReceiver
import com.charles.messenger.receiver.SmsSentReceiver
import com.charles.messenger.receiver.AiAutoReplyReceiver
import com.charles.messenger.receiver.DisableAutoReplyReceiver
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BroadcastReceiverBuilderModule {

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindBlockThreadReceiver(): BlockThreadReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindBootReceiver(): BootReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindDefaultSmsChangedReceiver(): DefaultSmsChangedReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindDeleteMessagesReceiver(): DeleteMessagesReceiver

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun bindMarkArchivedReceiver(): MarkArchivedReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindMarkReadReceiver(): MarkReadReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindMarkSeenReceiver(): MarkSeenReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindMmsReceivedReceiver(): MmsReceivedReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindMmsReceiver(): MmsReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindMmsSentReceiver(): MmsSentReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindMmsUpdatedReceiver(): MmsUpdatedReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindNightModeReceiver(): NightModeReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindRemoteMessagingReceiver(): RemoteMessagingReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindSendScheduledMessageReceiver(): SendScheduledMessageReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindSmsDeliveredReceiver(): SmsDeliveredReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindSmsProviderChangedReceiver(): SmsProviderChangedReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindSmsReceiver(): SmsReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindSmsSentReceiver(): SmsSentReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindWidgetProvider(): WidgetProvider

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindAiAutoReplyReceiver(): AiAutoReplyReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindDisableAutoReplyReceiver(): DisableAutoReplyReceiver

}