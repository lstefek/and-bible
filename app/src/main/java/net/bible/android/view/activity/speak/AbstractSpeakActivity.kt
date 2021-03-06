/*
 * Copyright (c) 2018 Martin Denham, Tuomas Airaksinen and the And Bible contributors.
 *
 * This file is part of And Bible (http://github.com/AndBible/and-bible).
 *
 * And Bible is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * And Bible is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with And Bible.
 * If not, see http://www.gnu.org/licenses/.
 *
 */

package net.bible.android.view.activity.speak

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.NumberPicker
import net.bible.android.activity.R
import net.bible.android.control.bookmark.BookmarkControl
import net.bible.android.control.speak.SpeakControl
import net.bible.android.control.speak.SpeakSettings
import net.bible.android.view.activity.base.CustomTitlebarActivityBase
import net.bible.android.view.activity.page.MainBibleActivity
import net.bible.service.db.bookmark.BookmarkDto
import javax.inject.Inject

abstract class AbstractSpeakActivity: CustomTitlebarActivityBase() {
    @Inject lateinit var speakControl: SpeakControl
    @Inject lateinit var bookmarkControl: BookmarkControl
    protected lateinit var currentSettings: SpeakSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        currentSettings = SpeakSettings.load()

        super.onCreate(savedInstanceState)
    }

    fun setSleepTime(sleepTimer: View) {
        if ((sleepTimer as CheckBox).isChecked) {
            val picker = NumberPicker(this)
            picker.minValue = 1
            picker.maxValue = 120
            picker.value = currentSettings.lastSleepTimer

            val layout = FrameLayout(this)
            layout.addView(picker)

            AlertDialog.Builder(this)
                    .setView(layout)
                    .setTitle(R.string.sleep_timer_title)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        currentSettings.sleepTimer = picker.value
                        currentSettings.lastSleepTimer = picker.value
                        currentSettings.save()
                        resetView(currentSettings)
                    }
                    .setNegativeButton(android.R.string.cancel) { _, _ -> resetView(currentSettings) }
                    .show()
        }
        else {
            currentSettings.sleepTimer = 0;
            currentSettings.save();
            resetView(currentSettings)
        }
    }

    abstract fun resetView(settings: SpeakSettings)

    fun onBookmarkButtonClick(button: View) {
        val bookmarkTitles = ArrayList<String>()
        val bookmarkDtos = ArrayList<BookmarkDto>()
        val labelDto = bookmarkControl.getOrCreateSpeakLabel()
        for (b in bookmarkControl.getBookmarksWithLabel(labelDto).sortedWith(
                Comparator<BookmarkDto> { o1, o2 -> o1.verseRange.start.compareTo(o2.verseRange.start) })) {

            bookmarkTitles.add("${b.verseRange.start.name} (${b.playbackSettings?.bookId?:"?"})")
            bookmarkDtos.add(b)
        }

        val adapter = ArrayAdapter<String>(this, android.R.layout.select_dialog_item, bookmarkTitles)
        AlertDialog.Builder(this)
                .setTitle(R.string.speak_bookmarks_menu_title)
                .setAdapter(adapter) { _, which ->
                    speakControl.speakFromBookmark(bookmarkDtos[which])
                    if(currentSettings.synchronize) {
                        startActivity(Intent(this, MainBibleActivity::class.java))
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
    }
}

