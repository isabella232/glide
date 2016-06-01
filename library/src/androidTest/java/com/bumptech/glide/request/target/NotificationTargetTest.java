package com.bumptech.glide.request.target;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.RemoteViews;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowNotificationManager;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, emulateSdk = 18, shadows = NotificationTargetTest.UpdateShadowNotificationManager.class)
public class NotificationTargetTest {

    private UpdateShadowNotificationManager shadowManager;
    private RemoteViews remoteViews;
    private int viewId;
    private Notification notification;
    private int notificationId;
    private NotificationTarget target;

    @Before
    public void setUp() {
        NotificationManager notificationManager =
                (NotificationManager) Robolectric.application.getSystemService(Context.NOTIFICATION_SERVICE);
        shadowManager = Robolectric.shadowOf_(notificationManager);

        remoteViews = mock(RemoteViews.class);
        viewId = 123;
        notification = mock(Notification.class);
        notificationId = 456;


        target = new NotificationTarget(Robolectric.application, remoteViews, viewId, 100 /*width*/, 100 /*height*/,
                notification, notificationId);
    }

    @Test
    public void testSetsBitmapOnRemoteViewsWithGivenImageIdOnResourceReady() {
        Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        target.onResourceReady(bitmap, null /*glideAnimation*/, false);
        verify(remoteViews).setImageViewBitmap(eq(viewId), eq(bitmap));
    }

    @Test
    public void updatesNotificationManagerWithNotificationIdAndNotificationOnResourceReady() {
        target.onResourceReady(Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888), null /*glideAnimation*/, false);

        assertEquals(notificationId, shadowManager.updatedNotificationId);
        assertEquals(notification, shadowManager.updatedNotification);
    }

    @Test(expected = NullPointerException.class)
    public void testThrowsIfContextIsNull() {
        new NotificationTarget(null /*context*/, mock(RemoteViews.class), 123 /*viewId*/, 100 /*width*/,
                100 /*height*/, mock(Notification.class), 456 /*notificationId*/);
    }


    @Test(expected = NullPointerException.class)
    public void testThrowsIfNotificationIsNull() {
        new NotificationTarget(Robolectric.application, mock(RemoteViews.class), 123 /*viewId*/, 100 /*width*/,
                100 /*height*/, null /*notification*/, 456 /*notificationId*/);
    }

    @Test(expected = NullPointerException.class)
    public void testThrowsIfRemoteViewsIsNull() {
        new NotificationTarget(Robolectric.application, null /*remoteViews*/, 123 /*viewId*/, 100 /*width*/,
                100 /*height*/, mock(Notification.class), 456 /*notificationId*/);
    }

    @Implements(NotificationManager.class)
    public static class UpdateShadowNotificationManager extends ShadowNotificationManager {
        int updatedNotificationId;
        Notification updatedNotification;

        @Implementation
        public void notify(int notificationId, Notification notification) {
            updatedNotificationId = notificationId;
            updatedNotification = notification;
        }
    }
}