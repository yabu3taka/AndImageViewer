package ex1.siv.modified;

import java.io.File;
import java.util.Collection;

public class ModifiedCheckerDirect implements ModifiedChecker {
    @Override
    public boolean existCacheFile(File cacheFile, ModifiedFileInfo info) {
        return cacheFile.exists();
    }

    @Override
    public boolean rememberFileInfo(File cacheFile, ModifiedFileInfo info) {
        return true;
    }

    @Override
    public void addNeedFile(Collection<String> needs) {
    }

    @Override
    public void cleanUp() {
    }
}
