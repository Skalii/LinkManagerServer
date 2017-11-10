package com.skaliy.linkmanager.server.connection;

import java.io.*;

public class DBConnectionFile {

    private File file;

    public DBConnectionFile(String path) {
        file = new File(path);
    }

    public BufferedReader read() throws FileNotFoundException {

        if (!file.exists())
            throw new FileNotFoundException(file.getName());

        return new BufferedReader(new FileReader(file.getAbsoluteFile()));
    }

}