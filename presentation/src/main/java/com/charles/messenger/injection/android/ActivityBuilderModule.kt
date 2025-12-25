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

import com.charles.messenger.feature.backup.BackupActivity
import com.charles.messenger.feature.blocking.BlockingActivity
import com.charles.messenger.feature.compose.ComposeActivity
import com.charles.messenger.feature.compose.ComposeActivityModule
import com.charles.messenger.feature.contacts.ContactsActivity
import com.charles.messenger.feature.contacts.ContactsActivityModule
import com.charles.messenger.feature.conversationinfo.ConversationInfoActivity
import com.charles.messenger.feature.gallery.GalleryActivity
import com.charles.messenger.feature.gallery.GalleryActivityModule
import com.charles.messenger.feature.main.MainActivity
import com.charles.messenger.feature.main.MainActivityModule
import com.charles.messenger.feature.notificationprefs.NotificationPrefsActivity
import com.charles.messenger.feature.notificationprefs.NotificationPrefsActivityModule
import com.charles.messenger.feature.plus.PlusActivity
import com.charles.messenger.feature.plus.PlusActivityModule
import com.charles.messenger.feature.qkreply.QkReplyActivity
import com.charles.messenger.feature.qkreply.QkReplyActivityModule
import com.charles.messenger.feature.rewards.RewardsActivity
import com.charles.messenger.feature.rewards.RewardsActivityModule
import com.charles.messenger.feature.scheduled.ScheduledActivity
import com.charles.messenger.feature.scheduled.ScheduledActivityModule
import com.charles.messenger.feature.settings.SettingsActivity
import com.charles.messenger.injection.scope.ActivityScope
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilderModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    abstract fun bindMainActivity(): MainActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [PlusActivityModule::class])
    abstract fun bindPlusActivity(): PlusActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindBackupActivity(): BackupActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ComposeActivityModule::class])
    abstract fun bindComposeActivity(): ComposeActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ContactsActivityModule::class])
    abstract fun bindContactsActivity(): ContactsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindConversationInfoActivity(): ConversationInfoActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [GalleryActivityModule::class])
    abstract fun bindGalleryActivity(): GalleryActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [NotificationPrefsActivityModule::class])
    abstract fun bindNotificationPrefsActivity(): NotificationPrefsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [QkReplyActivityModule::class])
    abstract fun bindQkReplyActivity(): QkReplyActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ScheduledActivityModule::class])
    abstract fun bindScheduledActivity(): ScheduledActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindSettingsActivity(): SettingsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindBlockingActivity(): BlockingActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [RewardsActivityModule::class])
    abstract fun bindRewardsActivity(): RewardsActivity

}
