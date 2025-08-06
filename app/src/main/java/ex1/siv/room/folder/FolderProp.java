package ex1.siv.room.folder;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "folders")
public class FolderProp {
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "folder_id")
    public String folderId;

    @ColumnInfo(name = "current_file")
    public String currentFile;

    @ColumnInfo(name = "last_at")
    public Date lastAt;
}
