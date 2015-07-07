package com.bitbitbitbit.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.bitbitbitbit.login.ParseConstants;

import com.bitbitbitbit.login.R;
import com.bitbitbitbit.utils.BitmapUtils;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;
import com.parse.twitter.Twitter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * A login screen that offers login via email/password and via Google+ sign in.
 * <p/>
 * ************ IMPORTANT SETUP NOTES: ************
 * In order for Google+ sign in to work with your app, you must first go to:
 * https://developers.google.com/+/mobile/android/getting-started#step_1_enable_the_google_api
 * and follow the steps in "Step 1" to create an OAuth 2.0 client for your package.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int PROFILE_PIC_HEIGHT = 200;
    private static final int PROFILE_PIC_WIDTH = 200;
    private static final int PROFILE_PIC_THUMBNAIL_HEIGHT = 60;
    private static final int PROFILE_PIC_THUMBNAIL_WIDTH = 60;
    private static final String PROFILE_PIC_FILENAME = "profile.jpg";
    private static final String PROFILE_PIC_THUMBNAIL_FILENAME = "profile-thumbnail.jpg";
    private static final int IMAGE_QUALITY = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, ".onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void onClickFacebookLogin(View view) {
        // TODO progress dialog

        Log.d(TAG, ".onClickFacebookLogin()");

        final List<String> permissions = new ArrayList<>();
        permissions.add("public_profile");
        permissions.add("email");
        permissions.add("user_friends");

        // TODO should we handle changes in the accesstoken?
        ParseFacebookUtils.logInWithReadPermissionsInBackground(this, permissions, new LogInCallback() {
            @Override
            public void done(final ParseUser parseUser, ParseException parseException) {

                if (parseUser == null) {
                    Log.d(TAG, "ParseFacebookUtils.loginBackground - Failed - User has cancelled",
                            parseException);
                } else if (parseUser.isNew()) {
                    Log.d(TAG, "ParseFacebookUtils.loginBackground - user is new. Retrieve data " +
                            "from Facebook");
                    final GraphRequest.GraphJSONObjectCallback graphJSONObjectCallback = new
                            GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                                    Log.d(TAG, "meRequest - onCompleted");
                                    if (graphResponse.getError() == null) {
                                        Log.d(TAG, "meRequest.success!");
                                        Log.d(TAG, jsonObject.toString());

                                        try {
                                            final String email = jsonObject.getString("email");
                                            parseUser.setEmail(email);
                                            Log.d(TAG, "Added email to user details");
                                        } catch (JSONException e) {
                                            Log.w(TAG, "Failed to retrieve email", e);
                                        }

                                        Log.d(TAG, "Adding display name");
                                        parseUser.put(ParseConstants.User.FIELD_DISPLAY_NAME, Profile.getCurrentProfile()
                                                .getName());

                                        // Retrieve profile picture
                                        Log.d(TAG, "Retrieving profile pic from Facebook");
                                        final Profile profile = Profile.getCurrentProfile();
                                        final Uri profilePicUri = profile.getProfilePictureUri(PROFILE_PIC_WIDTH, PROFILE_PIC_HEIGHT);
                                        final Uri profilePicUriThumbnail = profile.getProfilePictureUri
                                                (PROFILE_PIC_THUMBNAIL_WIDTH, PROFILE_PIC_THUMBNAIL_HEIGHT);


                                        new AsyncTask<Void, Void, Void>() {
                                            @Override
                                            protected Void doInBackground(Void... params) {
                                                // Send profile picture and thumbnail
                                                addProfilePic(profilePicUri.toString(),
                                                        profilePicUriThumbnail.toString(), parseUser);

                                                // Save the changes to the user details
                                                Log.d(TAG, "Sending user details update");
                                                try {
                                                    parseUser.save();
                                                    Log.i(TAG, "User details update complete.");
                                                } catch (ParseException parseException) {
                                                    Log.e(TAG, "Failed to update parse user",
                                                            parseException);
                                                }
                                                return null;
                                            }

                                            @Override
                                            protected void onPostExecute(Void aVoid) {
                                                super.onPostExecute(aVoid);

                                                // Launch the main activity regardless of the
                                                // result of the updating of the user details.
                                                // The Facebook log-in has been successful even
                                                // without the update.
                                                finishLogin();
                                            }
                                        }.execute();

                                    } else {
                                        Log.d(TAG, "meRequest.failed! Continuing without updating" +
                                                " user's profile pic and display name.");

                                        // TODO should force user to provide a display name and
                                        // optionally his email address
                                        // TODO be sure to control the allowable display name

                                        finishLogin();
                                    }
                                }
                            };

                    final GraphRequest request = GraphRequest.newMeRequest(AccessToken
                            .getCurrentAccessToken(), graphJSONObjectCallback);
                    request.executeAsync();
                } else {
                    Log.d("MyApp", "User logged in through Facebook!");
                    finishLogin();
                }
            }
        });
    }

    private void addProfilePic(String profilePicUrl, String profilePicThumbnailUrl, ParseUser
            parseUser) {
        try {
            final Bitmap bitmapProfile = retrieveBitmaps(profilePicUrl);
            final ParseFile parseFileProfile = sendParseFile
                    (bitmapProfile, PROFILE_PIC_FILENAME);
            final Bitmap bitmapThumbnail = retrieveBitmaps(profilePicThumbnailUrl);
            final ParseFile parseFileThumbnail =
                    sendParseFile(bitmapThumbnail, PROFILE_PIC_THUMBNAIL_FILENAME);

            if (parseFileProfile == null || parseFileThumbnail == null) {
                Log.e(TAG, "Profile pic is null");
                Log.i(TAG, "Continuing without the profile " +
                        "pictures");
                return;
            }

            parseUser.put(ParseConstants.User.FIELD_PROFILE_PIC, parseFileProfile);
            parseUser.put(ParseConstants.User.FIELD_PROFILE_PIC_THUMBNAIL, parseFileThumbnail);
        } catch (InterruptedException
                interruptedException) {
            Log.e(TAG, "Retrieval of bitmaps was " +
                            "interrupted",
                    interruptedException);
            Log.i(TAG, "Continuing without the profile " +
                    "pictures");
        } catch (ParseException parseException) {
            Log.e(TAG, "Failed to upload profile picture");
            Log.i(TAG, "Continuing without the profile " +
                    "pictures");
        }
    }

    private void finishLogin() {
        setResult(RESULT_OK);
        finish();
    }

    @Nullable
    private ParseFile sendParseFile(final Bitmap bitmap, final String filename) throws
            InterruptedException, ParseException {
        Log.d(TAG, ".sendParseFiles()");
        if (bitmap == null) {
            Log.d(TAG, ".sendParseFiles(null)");
            return null;
        }
        final byte[] imageData = BitmapUtils.bitmapToByteArray(bitmap, IMAGE_QUALITY);
        final ParseFile parseFile = new ParseFile(filename, imageData);
        Log.d(TAG, "Saving image file. Size:" + imageData.length);
        parseFile.save();
        return parseFile;
    }


    /**
     * Helper method for downloading bitmaps. Be sure to regulate the bitmaps to be downloaded as
     * all of them will be kept in memory
     */
    @WorkerThread
    @Nullable
    private Bitmap retrieveBitmaps(final String url) throws InterruptedException {
        Log.d(TAG, ".retrieveBitmap()");
        if (url == null) {
            Log.d(TAG, ".retrieveBitmap(null)");
            return null;
        }
        final Bitmap[] bitmapContainer = new Bitmap[1];
        final CountDownLatch latch = new CountDownLatch(1);
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final ImageRequest profileImageRequest = new ImageRequest(url, new
                Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        Log.i(TAG, "Successfully downloaded bitmap");
                        bitmapContainer[0] = bitmap;
                        latch.countDown();
                    }
                }, 0, 0, null, Bitmap.Config.RGB_565, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (volleyError != null) {
                    Log.e(TAG, "Failed to download bitmap:" + volleyError.getMessage());
                }
                // TODO handle
                latch.countDown();
            }
        });
        requestQueue.add(profileImageRequest);

        //TODO wait a few seconds then stop the attempt
        latch.await();
        return bitmapContainer[0];
    }


    public void onClickTwitterLogin(View view) {
        Log.d(TAG, ".onClickTwitterLogin()");

        //TODO progress dialog

        ParseTwitterUtils.logIn(this, new LogInCallback() {
            @Override
            public void done(final ParseUser parseUser, ParseException parseException) {
                Log.i(TAG, "Login done");
                if (parseUser == null) {
                    Log.d(TAG, "ParseTwitterUtils.login - Failed - User has cancelled",
                            parseException);

                } else if (parseUser.isNew()) {

                    Log.d(TAG, "ParseTwitterUtils.loginBackground - user is new. Retrieve data " +
                            "from Twitter");

                    final Twitter twitter = ParseTwitterUtils.getTwitter();
                    final String displayName = twitter.getScreenName();

                    Log.d(TAG, "Adding display name");
                    parseUser.put(ParseConstants.User.FIELD_DISPLAY_NAME, displayName);

                    // TODO save?
                    String userId = twitter.getUserId();


                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            final String screenName = ParseTwitterUtils.getTwitter()
                                    .getScreenName();
                            final String profilePicUrl = String
                                    .format("https://api.twitter.com/1.1/users/show.json?screen_name=%s",
                                            screenName);

                            // TODO resize for profile, thumbnail
                            // Send profile picture and thumbnail
                            Log.d(TAG, "Profile Pic Url:" + profilePicUrl);

                            // TODO API is deprecated but this is the only API exposed by Parse.
                            // Try to explore alternatives
                            AndroidHttpClient httpClient = AndroidHttpClient.newInstance(null);
                            HttpGet verifyGet = new HttpGet(profilePicUrl);
                            ParseTwitterUtils.getTwitter().signRequest(verifyGet);

                            try {
                                HttpResponse response = httpClient.execute(verifyGet);
                                HttpEntity responseEntity = response.getEntity();
                                if (responseEntity != null) {
                                    String strResponse = EntityUtils
                                            .toString(responseEntity);
                                    Log.d(TAG, "<<<(httpResponse):" + strResponse);

                                    JSONObject jsonObject = new JSONObject(strResponse);
                                    String profileImageUrl = jsonObject
                                            .getString("profile_image_url");
                                    Log.d(TAG, "profileUrl:" + profileImageUrl);

                                    addProfilePic(profileImageUrl, profileImageUrl, parseUser);
                                }
                            } catch (JSONException | IOException e) {
                                Log.w(TAG, "Failed to retrieve profile image url. Continuing " +
                                        "anyways.", e);
                            } finally {
                                httpClient.close();
                            }


                            // Save the changes to the user details
                            Log.d(TAG, "Sending user details update");
                            try {
                                parseUser.save();
                                Log.i(TAG, "User details update complete.");
                            } catch (ParseException parseException) {
                                Log.e(TAG, "Failed to update parse user",
                                        parseException);
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);

                            // Launch the main activity regardless of the
                            // result of the updating of the user details.
                            // The Facebook log-in has been successful even
                            // without the update.
                            finishLogin();
                        }
                    }.execute();

                    Log.d(TAG, "Done");

                } else {
                    Log.d("MyApp", "User logged in through Twitter!");
                    finishLogin();
                }
            }
        });
    }

    public void onClickGooglePlus(View view) {
        Log.d(TAG, ".onClickGooglePlus()");
    }

    public void onClickParseLogin(View view) {
        Log.d(TAG, ".onClickParseLogin()");
        // TODO link facebook and/or twitter with parse if not yet linked
        // TODO Check if email already exists, if it doesn't then retrieve that
        // TODO progress dialog
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }
}

