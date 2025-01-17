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
package com.charles.messenger.injection

import com.charles.messenger.common.QKApplication
import com.charles.messenger.common.QkDialog
import com.charles.messenger.common.util.QkChooserTargetService
import com.charles.messenger.common.widget.AvatarView
import com.charles.messenger.common.widget.PagerTitleView
import com.charles.messenger.common.widget.PreferenceView
import com.charles.messenger.common.widget.QkEditText
import com.charles.messenger.common.widget.QkSwitch
import com.charles.messenger.common.widget.QkTextView
import com.charles.messenger.common.widget.RadioPreferenceView
import com.charles.messenger.feature.backup.BackupController
import com.charles.messenger.feature.blocking.BlockingController
import com.charles.messenger.feature.blocking.manager.BlockingManagerController
import com.charles.messenger.feature.blocking.messages.BlockedMessagesController
import com.charles.messenger.feature.blocking.numbers.BlockedNumbersController
import com.charles.messenger.feature.compose.editing.DetailedChipView
import com.charles.messenger.feature.conversationinfo.injection.ConversationInfoComponent
import com.charles.messenger.feature.settings.SettingsController
import com.charles.messenger.feature.settings.about.AboutController
import com.charles.messenger.feature.settings.swipe.SwipeActionsController
import com.charles.messenger.feature.themepicker.injection.ThemePickerComponent
import com.charles.messenger.feature.widget.WidgetAdapter
import com.charles.messenger.injection.android.ActivityBuilderModule
import com.charles.messenger.injection.android.BroadcastReceiverBuilderModule
import com.charles.messenger.injection.android.ServiceBuilderModule
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidSupportInjectionModule::class,
    AppModule::class,
    ActivityBuilderModule::class,
    BroadcastReceiverBuilderModule::class,
    ServiceBuilderModule::class])
interface AppComponent {

    fun conversationInfoBuilder(): ConversationInfoComponent.Builder
    fun themePickerBuilder(): ThemePickerComponent.Builder

    fun inject(application: QKApplication)

    fun inject(controller: AboutController)
    fun inject(controller: BackupController)
    fun inject(controller: BlockedMessagesController)
    fun inject(controller: BlockedNumbersController)
    fun inject(controller: BlockingController)
    fun inject(controller: BlockingManagerController)
    fun inject(controller: SettingsController)
    fun inject(controller: SwipeActionsController)

    fun inject(dialog: QkDialog)

    fun inject(service: WidgetAdapter)

    /**
     * This can't use AndroidInjection, or else it will crash on pre-marshmallow devices
     */
    fun inject(service: QkChooserTargetService)

    fun inject(view: AvatarView)
    fun inject(view: DetailedChipView)
    fun inject(view: PagerTitleView)
    fun inject(view: PreferenceView)
    fun inject(view: RadioPreferenceView)
    fun inject(view: QkEditText)
    fun inject(view: QkSwitch)
    fun inject(view: QkTextView)

}
