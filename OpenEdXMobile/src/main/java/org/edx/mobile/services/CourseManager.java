package org.edx.mobile.services;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;
import android.util.LruCache;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.Filter;
import org.edx.mobile.model.course.CourseComponent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * This class manages the caching mechanism of courses data.
 */
@Singleton
public class CourseManager {
    protected final Logger logger = new Logger(getClass().getName());

    /**
     * This count represents the maximum no of courses that will be cached via app level cache.
     */
    private final int NO_OF_COURSES_TO_CACHE = 15;

    /**
     * An app level cache to keep courses data in memory till ending of app session.
     */
    private final LruCache<String, CourseComponent> cachedComponent;

    @Inject
    CourseAPI courseApi;

    public CourseManager() {
        cachedComponent = new LruCache<>(NO_OF_COURSES_TO_CACHE);
    }

    public void clearAllAppLevelCache() {
        cachedComponent.evictAll();
    }

    public void addCourseDataInAppLevelCache(@NonNull String courseId,
                                             @NonNull CourseComponent courseComponent) {
        cachedComponent.put(courseId, courseComponent);
    }

    /**
     * Obtain the course data from app level cache.
     *
     * @param courseId Id of the course.
     * @return Cached course data. In case course data is not present in cache it will return null.
     */
    @Nullable
    public CourseComponent getCourseDataFromAppLevelCache(@NonNull final String courseId) {
        return cachedComponent.get(courseId);
    }

    /**
     * Obtain the course data from persistable cache.
     * <p>
     * <b>WARNING:</b> This function takes time and should call asynchronously.
     * {@link CourseManager#getCourseDataFromAppLevelCache} can be used as an alternate, specially
     * if its sure course data will be available in app level cache.
     *
     *
     * @param blocksApiVersion Version of the API.
     * @param courseId Id of the course.
     * @return Cached course data. In case course data is not present in cache it will return null.
     */
    @Nullable
    public CourseComponent getCourseDataFromPersistableCache(@NonNull String blocksApiVersion, @NonNull String courseId) {
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            Future<CourseComponent> futureComponent = courseApi.getCourseStructureFromCache(executorService,blocksApiVersion, courseId);
            CourseComponent component = futureComponent.get(); // This blocks until the result is available
            if (component != null) {
                addCourseDataInAppLevelCache(courseId, component);
            }
            executorService.shutdown();
            return component;
        } catch (ExecutionException | InterruptedException e) {
            executorService.shutdown();
            // Log the exception
            Log.e("CourseDataManager", "Error retrieving course data from cache", e);
            // Course data doesn't exist in cache or there was an error
            return null;
        }
    }


    @Deprecated
    @Nullable
    private CourseComponent getCachedCourseData(@NonNull String blocksApiVersion, @NonNull String courseId) {
        final CourseComponent component = getCourseDataFromAppLevelCache(courseId);
        if (component != null) {
            return component;
        }
        return getCourseDataFromPersistableCache(blocksApiVersion, courseId);
    }

    /**
     * Obtain the course data from app level cache and then find the specified course component in it.
     *
     * @param courseId    Id of the course.
     * @param componentId Id of the course component.
     * @return Searched out course component. In case course data is not present in
     * app level cache or specified component doesn't exist in the course it will return null.
     */
    @Nullable
    public CourseComponent getComponentByIdFromAppLevelCache(@NonNull final String courseId,
                                                             @NonNull final String componentId) {
        CourseComponent courseComponent = getCourseDataFromAppLevelCache(courseId);
        if (courseComponent == null)
            return null;
        CourseComponent found = courseComponent.find(new Filter<CourseComponent>() {
            @Override
            public boolean apply(CourseComponent cc) {
                return componentId.equals(cc.getId());
            }
        });
        if (found != null) return found;
        // Fallback: search API may return block_id instead of full id
        return courseComponent.find(new Filter<CourseComponent>() {
            @Override
            public boolean apply(CourseComponent cc) {
                return componentId.equals(cc.getBlockId());
            }
        });
    }

    /**
     * This function should be avoided to use because it tries to obtain data from persistable cache
     * which takes time and should happen asynchronously.
     * <p>
     * {@link CourseManager#getComponentByIdFromAppLevelCache} can be used as an alternate,
     * specially if its sure course data will be available in app level cache.
     */
    @Deprecated
    @Nullable
    public CourseComponent getComponentById(@NonNull String blocksApiVersion,
                                            @NonNull String courseId,
                                            @NonNull String componentId) {
        CourseComponent courseComponent = getCachedCourseData(blocksApiVersion, courseId);
        if (courseComponent == null)
            return null;
        CourseComponent found = courseComponent.find(new Filter<CourseComponent>() {
            @Override
            public boolean apply(CourseComponent cc) {
                return componentId.equals(cc.getId());
            }
        });
        if (found != null) return found;
        // Fallback: search API may return block_id instead of full id
        return courseComponent.find(new Filter<CourseComponent>() {
            @Override
            public boolean apply(CourseComponent cc) {
                return componentId.equals(cc.getBlockId());
            }
        });
    }
}
