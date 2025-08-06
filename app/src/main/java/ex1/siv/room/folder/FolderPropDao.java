package ex1.siv.room.folder;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.Calendar;

@Dao
public abstract class FolderPropDao {
    @Query("SELECT * FROM folders WHERE folder_id = :folderId")
    public abstract FolderProp findById(String folderId);

    @Query("SELECT EXISTS(SELECT * FROM folders WHERE folder_id = :folderId)")
    public abstract boolean existRow(String folderId);

    @Insert
    protected abstract void insert(FolderProp data);

    @Update
    protected abstract void update(FolderProp data);

    @Delete
    protected abstract void delete(FolderProp data);

    public void save(FolderProp data) {
        Calendar today = Calendar.getInstance();
        data.lastAt = today.getTime();
        if (existRow((data.folderId))) {
            update(data);
        } else {
            insert(data);
        }
    }
}
