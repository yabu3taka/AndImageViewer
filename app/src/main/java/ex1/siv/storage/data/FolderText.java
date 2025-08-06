package ex1.siv.storage.data;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import ex1.siv.util.BitmapUtil;
import ex1.siv.util.FileUtil;

public class FolderText {
    public final static String FOLDER_FILE = "folder.tsd";

    public final String title;
    public final String comment;

    public FolderText() {
        this.title = "";
        this.comment = "";
    }

    private FolderText(String title, String comment) {
        this.title = title;
        this.comment = comment;
    }

    public boolean hasText() {
        if (!this.title.isBlank()) {
            return true;
        }
        //noinspection RedundantIfStatement
        if (!this.comment.isBlank()) {
            return true;
        }
        return false;
    }

    public static FolderText createFromFolderFile(InputStream inputStream) throws Exception {
        if (inputStream == null) {
            return new FolderText();
        }
        try (InputStreamReader reader = BitmapUtil.getTextReader(inputStream, FolderText.FOLDER_FILE)) {
            return FolderText.create(reader);
        }
    }

    public static FolderText create(Reader reader) throws Exception {
        String title = "";
        StringBuilder comment = new StringBuilder();
        boolean areaTitle = false;
        boolean areaComment = false;
        try (BufferedReader br = FileUtil.toBr(reader)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.equalsIgnoreCase("==Start:Titles")) {
                    areaTitle = true;
                    continue;
                } else if (line.equalsIgnoreCase("==End:Titles")) {
                    areaComment = true;
                    continue;
                }

                if (areaTitle) {
                    title = line;
                    areaTitle = false;
                }
                if (areaComment) {
                    comment.append(line).append(System.lineSeparator());
                }
            }
            return new FolderText(title, comment.toString());
        }
    }
}
