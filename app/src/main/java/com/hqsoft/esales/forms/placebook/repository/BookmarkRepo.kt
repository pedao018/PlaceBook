package com.hqsoft.esales.forms.placebook.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.hqsoft.esales.forms.placebook.db.BookmarkDao
import com.hqsoft.esales.forms.placebook.db.PlaceBookDatabase
import com.hqsoft.esales.forms.placebook.model.Bookmark

// 1
class BookmarkRepo(context: Context) {
    // 2
    private val db = PlaceBookDatabase.getInstance(context)
    private val bookmarkDao: BookmarkDao = db.bookmarkDao()

    // 3
    fun addBookmark(bookmark: Bookmark): Long? {
        val newId = bookmarkDao.insertBookmark(bookmark)
        bookmark.id = newId
        return newId
    }

    // 4
    fun createBookmark(): Bookmark {
        return Bookmark()
    }

    // 5
    val allBookmarks: LiveData<List<Bookmark>>
        get() {
            return bookmarkDao.loadAll()
        }

    fun getLiveBookmark(bookmarkId: Long): LiveData<Bookmark> =
        bookmarkDao.loadLiveBookmark(bookmarkId)

    fun updateBookmark(bookmark: Bookmark) {
        bookmarkDao.updateBookmark(bookmark)
    }

    fun getBookmark(bookmarkId: Long): Bookmark {
        return bookmarkDao.loadBookmark(bookmarkId)
    }
}
