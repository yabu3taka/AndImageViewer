package ex1.siv.modified;

import java.io.File;
import java.util.Collection;

public interface ModifiedChecker {
    boolean existCacheFile(File cacheFile, ModifiedFileInfo info);

    boolean rememberFileInfo(File cacheFile, ModifiedFileInfo info);

    void addNeedFile(Collection<String> needs);

    void cleanUp();
}
