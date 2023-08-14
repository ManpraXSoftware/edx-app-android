package org.humana.mobile.model.course;

import androidx.annotation.Nullable;

import org.humana.mobile.model.db.DownloadEntry;
import org.humana.mobile.module.storage.IStorage;

public interface HasDownloadEntry {
    @Nullable
    DownloadEntry getDownloadEntry(IStorage storage);

    @Nullable
    String getDownloadUrl();
}
