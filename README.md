####A download util for android with muti-task downloads, resuming downloads, traffic counter and the basic task controls such as pause, continue and delete.

###usage
```
  DownloadTool dlTool = new DownloadRequest.DLBuilder(localPath, "letushigh2", dlURL, this).build();
            dlTool2.start(new YDLListener() {
                @Override
                public void onProcess(int percent) {
                    Log.e(TAG, "onProcess2:" + percent);
                }

                @Override
                public void onSuccess() {
                    Log.e(TAG, "onSuccess2");
                }

                @Override
                public void onFaile(String error) {
                    Log.e(TAG, "onFaile2:" + error);
                }

                @Override
                public void onStart(int fileSize) {
                    isDownloading2 = true;
                    Log.e(TAG, "onStart2:" + fileSize);
                }
            });
```
