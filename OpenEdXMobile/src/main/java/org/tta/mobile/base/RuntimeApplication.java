package org.tta.mobile.base;

/**
 * Put any custom application configuration here.
 * This file will not be edited by edX unless absolutely necessary.
 */
public class RuntimeApplication extends MainApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        // If you have any custom extensions, add them here. For example:
        // extensionRegistry.forType(SettingsExtension.class).add(new MyCustomSettingsExtension());
    }
}
