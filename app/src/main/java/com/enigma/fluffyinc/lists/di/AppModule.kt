package com.enigma.fluffyinc.lists.di

import android.content.Context
import androidx.room.Room
import com.enigma.fluffyinc.lists.data.local.*
import com.enigma.fluffyinc.lists.data.repository.*

object AppModule {
    private var database: AppDatabase? = null

    fun provideDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "notesapp.db"
            )
            .fallbackToDestructiveMigration()
            .build()
            database = instance
            instance
        }
    }

    fun provideNoteRepository(context: Context) = NoteRepository(provideDatabase(context).noteDao())
    fun provideChecklistRepository(context: Context) = ChecklistRepository(provideDatabase(context).checklistDao())
    fun provideSimpleListRepository(context: Context) = SimpleListRepository(provideDatabase(context).simpleListDao())
    fun provideDiaryRepository(context: Context) = DiaryRepository(provideDatabase(context).diaryDao())
    fun provideJournalRepository(context: Context) = JournalRepository(provideDatabase(context).journalDao())
    fun provideTagRepository(context: Context) = TagRepository(provideDatabase(context).tagDao())
    fun provideReminderRepository(context: Context) = ReminderRepository(provideDatabase(context).reminderDao())
}
