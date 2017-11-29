package com.skaliy.linkmanager.server.connection;

import java.io.*;

public class FileConnection {

    private File file;

    public FileConnection(String path) {
        file = new File(path);
    }

    public BufferedReader read() throws FileNotFoundException {

        if (!file.exists())
            throw new FileNotFoundException(file.getName());

        return new BufferedReader(new FileReader(file.getAbsoluteFile()));
    }

}