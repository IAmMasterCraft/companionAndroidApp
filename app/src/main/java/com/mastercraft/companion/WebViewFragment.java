package com.mastercraft.companion;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

//import class for Uploading part start

//import class for Uploading part End



/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WebViewFragment #newInstance} factory method to
 * create an instance of this fragment.
 */
public class WebViewFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    private String url = String.valueOf(MainActivity.myBundle.get("url"));

    private WebView webView;

    private static final int FILECHOOSER_RESULTCODE = 1;
    private ValueCallback<Uri> mUploadMessage;
    private Uri mCapturedImageURI = null;

    // the same for Android 5.0 methods only
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;


    public WebViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_web_view, container, false);

        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        if (url.equals("https://iammastercraft.github.io/questionList/")) {
            progressDialog.setMessage("Loading Question List...");
        }

        if (url.equals("https://iammastercraft.github.io/cgpa-v1/")) {
            progressDialog.setMessage("Loading CGPA Calculator...");
        }

        if (url.equals("https://iammastercraft.github.io/virtualIdCard/")) {
            progressDialog.setMessage("Loading Virtual ID Card...");
        }

        progressDialog.setCancelable(false);

        webView = v.findViewById(R.id.webPageView);
        webView.setVerticalScrollBarEnabled(true);
        webView.requestFocus();
        webView.getSettings().setDefaultTextEncodingName("utf-8");
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);

        //webView.getSettings().setUseWideViewPort(true);

        //Other webview settings
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowContentAccess(true);

        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading (WebView webView, String url) {
                webView.loadUrl(url);
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged (WebView webView, int progress) {
                if (progress < 100) {
                    progressDialog.show();
                }
                if (progress == 100) {
                    progressDialog.dismiss();
                }
            }

            // for Lollipop, all in one
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @SuppressLint("UseRequireInsteadOfGet")
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback,
                    WebChromeClient.FileChooserParams fileChooserParams) {
                if (mFilePathCallback != null) {
                    mFilePathCallback.onReceiveValue(null);
                }
                mFilePathCallback = filePathCallback;

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(Objects.requireNonNull(getActivity()).getPackageManager()) != null) {

                    // create the file where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                        takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        // Log.e(TAG, "Unable to create Image File", ex);
                    }

                    // continue only if the file was successfully created
                    if (photoFile != null) {
                        mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile));
                    } else {
                        takePictureIntent = null;
                    }
                }

                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType("image/*");

                Intent[] intentArray;
                if (takePictureIntent != null) {
                    intentArray = new Intent[]{takePictureIntent};
                } else {
                    intentArray = new Intent[0];
                }

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.image_chooser));
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

                startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);

                return true;
            }

            // creating image files (Lollipop only)
            @SuppressWarnings("ResultOfMethodCallIgnored")
            private File createImageFile() throws IOException {

                File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "DirectoryNameHere");

                if (!imageStorageDir.exists()) {
                    imageStorageDir.mkdirs();
                }

                // create an image file name
                imageStorageDir = new File(imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
                return imageStorageDir;
            }

            // openFileChooser for Android 3.0+
            @SuppressWarnings("ResultOfMethodCallIgnored")
            void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                mUploadMessage = uploadMsg;

                try {
                    File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "DirectoryNameHere");

                    if (!imageStorageDir.exists()) {
                        imageStorageDir.mkdirs();
                    }

                    File file = new File(imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");

                    mCapturedImageURI = Uri.fromFile(file); // save to the private variable

                    final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
                    // captureIntent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("image/*");

                    Intent chooserIntent = Intent.createChooser(i, getString(R.string.image_chooser));
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{captureIntent});

                    startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Camera Exception:" + e, Toast.LENGTH_LONG).show();
                }

            }

            // openFileChooser for Android < 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                openFileChooser(uploadMsg, "");
            }

            // openFileChooser for other Android versions
            /* may not work on KitKat due to lack of implementation of openFileChooser() or onShowFileChooser()
               https://code.google.com/p/android/issues/detail?id=62220
               however newer versions of KitKat fixed it on some devices */
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                openFileChooser(uploadMsg, acceptType);
            }


        });

        //handles the back navigation in the fragment
        webView.canGoBack();
        webView.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK
                        && event.getAction() == MotionEvent.ACTION_UP
                        && webView.canGoBack()) {
                    webView.goBack();
                    return true;
                }
                return false;
            }
        });

        webViewDownloads(webView);

        return v;
    }


    // return here when file selected from camera or from SD Card
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // code for all versions except of Lollipop
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {

            if (requestCode == FILECHOOSER_RESULTCODE) {
                if (null == this.mUploadMessage) {
                    return;
                }

                Uri result = null;

                try {
                    if (resultCode != getActivity().RESULT_OK) {
                        result = null;
                    } else {
                        // retrieve from the private variable if the intent is null
                        result = data == null ? mCapturedImageURI : data.getData();
                    }
                } catch (Exception e) {
                    Toast.makeText(getContext(), "activity :" + e, Toast.LENGTH_LONG).show();
                }

                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }

        } // end of code for all versions except of Lollipop

        // start of code for Lollipop only
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            if (requestCode != FILECHOOSER_RESULTCODE || mFilePathCallback == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }

            Uri[] results = null;

            // check that the response is a good one
            if (resultCode == Activity.RESULT_OK) {
                if (data == null || data.getData() == null) {
                    // if there is not data, then we may have taken a photo
                    if (mCameraPhotoPath != null) {
                        results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                    }
                } else {
                    String dataString = data.getDataString();
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                }
            }

            mFilePathCallback.onReceiveValue(results);
            mFilePathCallback = null;

        } // end of code for Lollipop only
    }

    private void webViewDownloads (WebView webView) {
        webView.setDownloadListener(new DownloadListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                webView.loadUrl(JavaScriptInterface.getBase64StringFromBlobUrl(url));
//                if (url.startsWith("data:")) {  //when url is base64 encoded data
//                    String path = createAndSaveFileFromBase64Url(url);
//                    return;
//                }
//                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
//                request.setMimeType(mimetype);
//                String cookies = CookieManager.getInstance().getCookie(url);
//                request.addRequestHeader("cookie", cookies);
//                request.addRequestHeader("USer-Agent", userAgent);
//                request.setDescription("Downloading File...");
//                request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype));
//                request.allowScanningByMediaScanner();
//                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//                request.setDestinationInExternalPublicDir(
//                        Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimetype)
//                );
//                @SuppressLint("UseRequireInsteadOfGet") DownloadManager downloadManager = (DownloadManager) Objects.requireNonNull(getContext()).getSystemService(DOWNLOAD_SERVICE);
//                assert downloadManager != null;
//                downloadManager.enqueue(request);
//                Toast.makeText(getContext(), "Downloading ID Card...", Toast.LENGTH_LONG).show();
            }
        });
    }

    private String createAndSaveFileFromBase64Url(String url) {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String filetype = url.substring(url.indexOf("/") + 1, url.indexOf(";"));
        String filename = "NOUN_ID_CARD_" + System.currentTimeMillis() + "." + filetype;
        File file = new File(path, filename);
        try {
            if(!path.exists())
                path.mkdirs();
            if(!file.exists())
                file.createNewFile();

            String base64EncodedString = url.substring(url.indexOf(",") + 1);
            byte[] decodedBytes = Base64.decode(base64EncodedString, Base64.DEFAULT);
            OutputStream os = new FileOutputStream(file);
            os.write(decodedBytes);
            os.close();

            //Tell the media scanner about the new file so that it is immediately available to the user.
            MediaScannerConnection.scanFile(getContext(),
                    new String[]{file.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
//                            Log.i("ExternalStorage", "Scanned " + path + ":");
//                            Log.i("ExternalStorage", "-> uri=" + uri);
                        }
                    });

            //Set notification after download complete and add "click to view" action to that
            String mimetype = url.substring(url.indexOf(":") + 1, url.indexOf("/"));
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), (mimetype + "/*"));
            PendingIntent pIntent = PendingIntent.getActivity(getContext(), 0, intent, 0);

            Notification notification = new NotificationCompat.Builder(getContext())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentText(getString(R.string.msg_file_downloaded))
                    .setContentTitle(filename)
                    .setContentIntent(pIntent)
                    .build();

            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            int notificationId = 85851;
            NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(getContext().NOTIFICATION_SERVICE);
            notificationManager.notify(notificationId, notification);
        } catch (IOException e) {
//            Log.w("ExternalStorage", "Error writing " + file, e);
            Toast.makeText(getContext(), R.string.error_downloading, Toast.LENGTH_LONG).show();
        }

        return file.toString();
    }

}
