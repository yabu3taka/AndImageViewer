package ex1.siv.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import ex1.siv.room.folder.FolderProp;
import ex1.siv.room.folder.FolderPropDao;

@Database(entities = {FolderProp.class},
        //autoMigrations = { @AutoMigration(from = 1, to = 2) },
        version = 1)
@TypeConverters({Converter.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract FolderPropDao folderPropDao();
}
