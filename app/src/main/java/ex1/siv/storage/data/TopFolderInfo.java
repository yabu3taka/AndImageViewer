package ex1.siv.storage.data;

import java.io.BufferedReader;
import java.io.InputStream;
import java.util.Objects;

import ex1.siv.R;
import ex1.siv.storage.exception.StorageStrIdException;
import ex1.siv.util.CryptUtil;
import ex1.siv.util.FileUtil;

public class TopFolderInfo {
    public static final String PASS_FILE = "pass.dat";

    public final FolderInfo topFolder;
    private final String mCrypt;
    private final String mCryptMask;
    private int scramble = -1;

    private TopFolderInfo(FolderInfo topFolder, BufferedReader br) throws Exception {
        this.topFolder = topFolder;

        this.mCrypt = br.readLine();
        this.mCryptMask = br.readLine();
    }

    public static TopFolderInfo create(FolderInfo topFolder, InputStream inputStream) {
        TopFolderInfo ret;
        try (BufferedReader br = FileUtil.toBr(inputStream)) {
            ret = new TopFolderInfo(topFolder, br);
        } catch (Exception ex) {
            throw new StorageStrIdException(R.string.err_bad_scramble);
        }
        if (ret.mCrypt == null) {
            throw new StorageStrIdException(R.string.err_bad_scramble);
        }
        return ret;
    }

    public void checkPassword(String password) {
        if (!Objects.equals(CryptUtil.md5("pass=" + password), mCrypt)) {
            scramble = -1;
            throw new StorageStrIdException(R.string.err_password);
        }

        for (scramble = 0; scramble <= 255; ++scramble) {
            String mask = CryptUtil.md5("s=" + password + "=" + scramble);
            if (Objects.equals(mask, mCryptMask)) {
                return;
            }
        }

        scramble = -1;
        throw new StorageStrIdException(R.string.err_bad_scramble);
    }

    public int getScramble() {
        return scramble;
    }
}
