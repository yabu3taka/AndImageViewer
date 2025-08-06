package ex1.siv.room;

import android.content.Context;

import androidx.room.Room;

public class DbConnection {
    public static AppDatabase connect(Context c) {
        return Room.databaseBuilder(c, AppDatabase.class, "folders")
                .allowMainThreadQueries()
                .build();
    }
}
