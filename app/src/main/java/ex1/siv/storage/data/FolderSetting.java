package ex1.siv.storage.data;

import java.io.BufferedReader;
import java.io.Reader;

import ex1.siv.util.FileUtil;

public class FolderSetting {
    public final static String SETTING_FILE = "setting.dat";

    public final boolean blankFlag;

    public FolderSetting() {
        this.blankFlag = false;
    }

    private FolderSetting(BufferedReader br) throws Exception {
        boolean blankFlag = false;
        String line;
        while ((line = br.readLine()) != null) {
            if (line.equalsIgnoreCase("BLANK")) {
                blankFlag = true;
            }
        }

        this.blankFlag = blankFlag;
    }

    public static FolderSetting create(Reader reader) throws Exception {
        if (reader == null) {
            return new FolderSetting();
        }
        try (BufferedReader br = FileUtil.toBr(reader)) {
            return new FolderSetting(br);
        }
    }
}
