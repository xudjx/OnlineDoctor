/*
 * Copyright (C) 2014 pengjianbo(pengjianbosoft@gmail.com), Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package cn.finalteam.galleryfinal;

import android.content.Intent;
import android.widget.Toast;
import cn.finalteam.toolsfinal.DeviceUtils;

/**
 * Desction:
 * Author:pengjianbo
 * Date:15/12/2 上午11:05
 */
public class GalleryFinal {
    public static final int GALLERY_RESULT_SUCCESS = 1000;
    public static final int GALLERY_REQUEST_CODE = 1002;
    public static final String GALLERY_RESULT_LIST_DATA = "gallery_result_list_data";

    static final int TAKE_REQUEST_CODE = 1001;

    static final int EDIT_OK = 1003;
    static final int EDIT_REQUEST_CODE = 1003;

    private static GalleryConfig mGalleryConfig;

    public static GalleryConfig getGalleryConfig() {
        return mGalleryConfig;
    }

    public static void open(GalleryConfig config) {
        if ( config == null ) {
            return;
        }
        if ( config.getImageLoader() == null ) {
            Toast.makeText(config.getActivity(), R.string.open_gallery_fail, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!DeviceUtils.existSDCard()) {
            Toast.makeText(config.getActivity(), R.string.empty_sdcard, Toast.LENGTH_SHORT).show();
            return;
        }

        //清理裁剪文件夹


        mGalleryConfig = config;

        Intent intent = new Intent(config.getActivity(), PhotoSelectActivity.class);
        config.getActivity().startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

}
