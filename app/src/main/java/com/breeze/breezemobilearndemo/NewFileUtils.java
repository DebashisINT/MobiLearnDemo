package com.breeze.breezemobilearndemo;

import java.io.File;

public class NewFileUtils {

    public static String getExtension(File file) {
        return file.getName().substring(file.getName().lastIndexOf(".") + 1);
    }


}
