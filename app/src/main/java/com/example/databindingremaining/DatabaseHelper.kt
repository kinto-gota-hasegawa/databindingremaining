package com.example.databindingremaining

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context? = null) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    
    companion object {
        private const val DATABASE_NAME = "users.db"
        private const val DATABASE_VERSION = 1
        
        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_AGE = "age"
        private const val COLUMN_PROFILE_IMAGE = "profile_image_url"
        private const val COLUMN_IS_ACTIVE = "is_active"
        private const val COLUMN_CREATED_AT = "created_at"
        private const val COLUMN_UPDATED_AT = "updated_at"
    }
    
    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_EMAIL TEXT NOT NULL,
                $COLUMN_AGE INTEGER NOT NULL,
                $COLUMN_PROFILE_IMAGE TEXT,
                $COLUMN_IS_ACTIVE INTEGER DEFAULT 1,
                $COLUMN_CREATED_AT INTEGER NOT NULL,
                $COLUMN_UPDATED_AT INTEGER NOT NULL
            )
        """.trimIndent()
        
        db.execSQL(createTableQuery)
        createIndexes(db)
    }
    
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }
    
    private fun createIndexes(db: SQLiteDatabase) {
        db.execSQL("CREATE INDEX idx_email ON $TABLE_USERS($COLUMN_EMAIL)")
        db.execSQL("CREATE INDEX idx_name ON $TABLE_USERS($COLUMN_NAME)")
    }
    
    fun insertUser(user: User): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID, user.id)
            put(COLUMN_NAME, user.name)
            put(COLUMN_EMAIL, user.email)
            put(COLUMN_AGE, user.age)
            put(COLUMN_PROFILE_IMAGE, user.profileImageUrl)
            put(COLUMN_IS_ACTIVE, if (user.isActive) 1 else 0)
            put(COLUMN_CREATED_AT, user.createdAt)
            put(COLUMN_UPDATED_AT, System.currentTimeMillis())
        }
        
        val result = db.insert(TABLE_USERS, null, values)
        db.close()
        return result != -1L
    }
    
    fun updateUser(user: User): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, user.name)
            put(COLUMN_EMAIL, user.email)
            put(COLUMN_AGE, user.age)
            put(COLUMN_PROFILE_IMAGE, user.profileImageUrl)
            put(COLUMN_IS_ACTIVE, if (user.isActive) 1 else 0)
            put(COLUMN_UPDATED_AT, System.currentTimeMillis())
        }
        
        val result = db.update(TABLE_USERS, values, "$COLUMN_ID = ?", arrayOf(user.id))
        db.close()
        return result > 0
    }
    
    fun deleteUser(userId: String): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_USERS, "$COLUMN_ID = ?", arrayOf(userId))
        db.close()
        return result > 0
    }
    
    fun getUserById(userId: String): User? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            null,
            "$COLUMN_ID = ?",
            arrayOf(userId),
            null,
            null,
            null
        )
        
        var user: User? = null
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor)
        }
        
        cursor.close()
        db.close()
        return user
    }
    
    fun getAllUsers(): List<User> {
        val users = mutableListOf<User>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_NAME ASC"
        )
        
        if (cursor.moveToFirst()) {
            do {
                users.add(cursorToUser(cursor))
            } while (cursor.moveToNext())
        }
        
        cursor.close()
        db.close()
        return users
    }
    
    fun saveUsers(users: List<User>) {
        val db = writableDatabase
        db.beginTransaction()
        
        try {
            users.forEach { user ->
                val values = ContentValues().apply {
                    put(COLUMN_ID, user.id)
                    put(COLUMN_NAME, user.name)
                    put(COLUMN_EMAIL, user.email)
                    put(COLUMN_AGE, user.age)
                    put(COLUMN_PROFILE_IMAGE, user.profileImageUrl)
                    put(COLUMN_IS_ACTIVE, if (user.isActive) 1 else 0)
                    put(COLUMN_CREATED_AT, user.createdAt)
                    put(COLUMN_UPDATED_AT, user.updatedAt)
                }
                
                db.insertWithOnConflict(
                    TABLE_USERS,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }
    }
    
    fun searchUsers(query: String): List<User> {
        val users = mutableListOf<User>()
        val db = readableDatabase
        
        val cursor = db.query(
            TABLE_USERS,
            null,
            "$COLUMN_NAME LIKE ? OR $COLUMN_EMAIL LIKE ?",
            arrayOf("%$query%", "%$query%"),
            null,
            null,
            "$COLUMN_NAME ASC"
        )
        
        if (cursor.moveToFirst()) {
            do {
                users.add(cursorToUser(cursor))
            } while (cursor.moveToNext())
        }
        
        cursor.close()
        db.close()
        return users
    }
    
    fun deleteAllUsers() {
        val db = writableDatabase
        db.delete(TABLE_USERS, null, null)
        db.close()
    }
    
    private fun cursorToUser(cursor: Cursor): User {
        return User(
            id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
            email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
            age = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AGE)),
            profileImageUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROFILE_IMAGE)),
            isActive = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ACTIVE)) == 1,
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
            updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT))
        )
    }
}