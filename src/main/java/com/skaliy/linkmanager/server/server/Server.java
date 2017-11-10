package com.skaliy.linkmanager.server.server;

import com.jfoenix.controls.JFXTextArea;
import com.skaliy.dbc.dbms.PostgreSQL;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import javafx.fxml.FXML;

import java.sql.SQLException;

public class Server implements Runnable {

    private final int port;
    static PostgreSQL db;

    @FXML
    static JFXTextArea textAreaLogs;

    public Server(int port, String url, String user, String password) {
        this.port = port;
        db = new PostgreSQL(url, user, password);
    }

    @Override
    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(workerGroup, bossGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ServerInitializer(port));

            bootstrap.bind(port).sync().channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static String[][] getResult(String query) throws SQLException {
        String[][] result = new String[0][];

        String _query = query;
        int index = 0;

        if (_query.startsWith("get_category_")
                || _query.startsWith("get_list_connects_")
                || _query.startsWith("get_list_from_section_")
                || _query.startsWith("get_category_from_")) {
            index = Integer.parseInt(_query.substring(_query.lastIndexOf("_") + 1));
            _query = _query.substring(0, _query.lastIndexOf("_"));
        }

        switch (_query) {

            case "get_sections":
                result = db.query(true, "SELECT title from sections");
                break;

            case "get_category":
                result = db.query(true, "SELECT c.title " +
                        "FROM categories c, sections s " +
                        "WHERE s.id_section = " + index +
                        " AND c.id_category = ANY(s.ids_category)");
                break;

            case "get_count_links":
                result = db.query(true, "SELECT count(link) FROM sites");
                break;

            case "get_list_connects":
                result = db.query(true, "SELECT si.* " +
                        "FROM sites si, sections se " +
                        "WHERE se.id_section = " + index +
                        " AND si.id_category = ANY(se.ids_category)");
                break;

            case "get_list_from_section":
                result = db.query(true, "SELECT si.* " +
                        "FROM sites si, sections se " +
                        "WHERE se.id_section = " + index +
                        " AND si.id_category = ANY(se.ids_category)");
                break;

            case "get_category_from":
                result = db.query(true, "SELECT title " +
                        "FROM categories " +
                        "WHERE id_category = " + index);
                break;

            case "get_logins_emails":
                result = db.query(true, "SELECT login, email FROM profiles");
                break;
        }

        return result;
    }

    public static boolean setResult(String query, String... values) {

        boolean result = true;

        String _query = query.substring(0, query.indexOf(","));

        switch (_query) {
            case "add_profile":
                try {
                    db.query(false,
                            "INSERT INTO profiles(login, password, last_name, first_name, email) " +
                                    "VALUES('" + values[0] + "', '" + values[1] + "', '" +
                                    values[2] + "', '" + values[3] + "', '" + values[4] + "')");
                } catch (SQLException e) {
                    result = false;
                }
                break;
        }

        return result;
    }

    public PostgreSQL getDb() {
        return db;
    }

    public void setTextAreaLogs(JFXTextArea textAreaLogs) {
        Server.textAreaLogs = textAreaLogs;
    }

    static void addLog(String log) {
        Server.textAreaLogs.appendText(log + "\n");
    }

}
