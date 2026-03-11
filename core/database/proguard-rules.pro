# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep class **.database.entity.** { *; }
-keep class **.database.dao.** { *; }
-keepclassmembers class * {
    @androidx.room.Dao *;
    @androidx.room.Query *;
    @androidx.room.Insert *;
    @androidx.room.Update *;
    @androidx.room.Delete *;
}
