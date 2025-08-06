package ex1.siv.ui.main;

import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import ex1.siv.R;
import ex1.siv.storage.StorageType;

class MainDirData {
    final int type;
    final String driveTopDir;
    private final String driveUser;
    final String localTopDir;

    MainDirData(MainActivity a) {
        this.type = ((Spinner) a.findViewById(R.id.StorageSpinner)).getSelectedItemPosition();
        this.driveTopDir = ((EditText) a.findViewById(R.id.DriveFolderText)).getText().toString();
        this.driveUser = ((TextView) a.findViewById(R.id.DriveUserText)).getText().toString();
        this.localTopDir = ((EditText) a.findViewById(R.id.LocalFolderText)).getText().toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MainDirData) {
            MainDirData tgt = (MainDirData) obj;
            if (this.type != tgt.type) {
                return false;
            }
            if (!this.driveTopDir.equals(tgt.driveTopDir)) {
                return false;
            }
            return this.localTopDir.equals(tgt.localTopDir);
        } else {
            return false;
        }
    }

    boolean hasDirString() {
        if (this.type == StorageType.STORAGE_LOCAL) {
            return !this.localTopDir.isEmpty();
        } else {
            if (this.driveTopDir.isEmpty()) {
                return false;
            }
            return !this.driveUser.isEmpty();
        }
    }

    String getTopDir() {
        if (type == StorageType.STORAGE_LOCAL) {
            return this.localTopDir;
        } else {
            return this.driveTopDir;
        }
    }

    StorageType getStorageType() {
        return new StorageType(this.type);
    }
}
