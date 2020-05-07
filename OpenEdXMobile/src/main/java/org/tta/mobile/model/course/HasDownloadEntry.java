package org.tta.mobile.model.course;

import androidx.annotation.Nullable;

import org.tta.mobile.model.db.DownloadEntry;
import org.tta.mobile.module.storage.IStorage;

public interface HasDownloadEntry {
    @Nullable
    DownloadEntry getDownloadEntry(IStorage storage);

    @Nullable
    String getDownloadUrl();
}
