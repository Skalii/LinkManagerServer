package com.skaliy.linkmanager.server.server;

import com.skaliy.dbc.dbms.PostgreSQL;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import org.codehaus.plexus.util.StringUtils;

import java.sql.SQLException;

public class Server implements Runnable {

    private final int port;
    static PostgreSQL db;

    @FXML
    static TextArea textAreaLogs;

    public Server(int port, String url, String user, String password) throws SQLException, ClassNotFoundException {
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

    static String[][] getResult(String query) throws SQLException {
        String[][] result = new String[0][];

        String _query = query, parameter = "0";
        String[] parameters = new String[0];

        if (_query.startsWith("get_category_p-")
                || _query.startsWith("get_list_connects_p-")
                || _query.startsWith("get_list_from_section_p-")
                || _query.startsWith("get_category_from_p-")
                || _query.startsWith("get_bookmarks_from_p-")
                || _query.startsWith("get_bookmarks_ids_from_p-")
                || _query.startsWith("get_login_p-")
                || _query.startsWith("get_email_p-")) {
            parameter = _query.substring(_query.lastIndexOf("_") + 3);
            _query = _query.substring(0, _query.lastIndexOf("_") + 2);

        } else if (_query.startsWith("get_pass_login_c-")
                || _query.startsWith("get_profile_c-")) {
            parameters = _query.substring(_query.lastIndexOf("_c-") + 3).split(";");
            _query = _query.substring(0, _query.lastIndexOf("_c-") + 2);
        }

        switch (_query) {

            case "get_sections":
                result = db.query(true, "SELECT title FROM sections");
                break;

            case "get_category_p":
                result = db.query(true,
                        "SELECT c.title " +
                                "FROM categories c, sections s " +
                                "WHERE s.id_section = " + parameter +
                                " AND c.id_category = ANY(s.ids_category)");
                break;

            case "get_count_links":
                result = db.query(true, "SELECT count(link) FROM sites");
                break;

            case "get_list_connects_p":
                result = db.query(true,
                        "SELECT si.* " +
                                "FROM sites si, sections se " +
                                "WHERE se.id_section = " + parameter +
                                " AND si.id_category = ANY(se.ids_category)");
                break;

            case "get_list_from_section_p":
                result = db.query(true,
                        "SELECT si.* " +
                                "FROM sites si, sections se " +
                                "WHERE se.id_section = " + parameter +
                                " AND si.id_category = ANY(se.ids_category)");
                break;

            case "get_category_from_p":
                result = db.query(true,
                        "SELECT title " +
                                "FROM categories " +
                                "WHERE id_category = " + parameter);
                break;

            case "get_login_p":
                result = db.query(true,
                        "SELECT login " +
                                "FROM profiles " +
                                "WHERE login = '" + parameter + "'");
                break;

            case "get_email_p":
                result = db.query(true,
                        "SELECT email " +
                                "FROM profiles " +
                                "WHERE email = '" + parameter + "'");
                break;

            case "get_pass_login_c":
                for (int i = 0; i < parameters.length; i++) {
                    if (parameters[i].startsWith("L")) {
                        parameters[i] = parameters[i].replace("L", "login");
                    } else if (parameters[i].startsWith("P")) {
                        parameters[i] = parameters[i].replace("P", "password");
                    }
                }
                result = db.query(true,
                        "SELECT password " +
                                "FROM profiles " +
                                "WHERE " + StringUtils.join(parameters, " AND "));
                break;

            case "get_profile_c":
                for (int i = 0; i < parameters.length; i++) {
                    if (parameters[i].startsWith("L")) {
                        parameters[i] = parameters[i].replace("L", "login");
                    } else if (parameters[i].startsWith("P")) {
                        parameters[i] = parameters[i].replace("P", "password");
                    }
                }
                result = db.query(true,
                        "SELECT * " +
                                "FROM profiles " +
                                "WHERE " + StringUtils.join(parameters, " AND "));
                break;

            case "get_bookmarks_from_p":
                result = db.query(true,
                        "SELECT s.link " +
                                "FROM sites s, profiles p " +
                                "WHERE p.id_profile = " + parameter +
                                " AND s.id_site = ANY(p.ids_bookmark)");
                break;

            case "get_bookmarks_ids_from_p":
                result = db.query(true,
                        "SELECT ids_bookmark " +
                                "FROM profiles " +
                                "WHERE id_profile = " + parameter);
                break;

            case "get_logins_emails":
                result = db.query(true, "SELECT login, email FROM profiles");
                break;

        }

        return result;
    }

    static boolean setResult(String query, String... values) {

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

            case "add_bookmark":
                try {
                    db.query(false,
                            "INSERT INTO bookmarks VALUES (" +
                                    values[0] + ", " +
                                    "(SELECT id_site " +
                                    "FROM sites " +
                                    "WHERE link = '" + values[1] + "')" + ")");
                } catch (SQLException e) {
                    result = false;
                }
                break;

            case "delete_bookmark":
                try {
                    db.query(false,
                            "DELETE FROM bookmarks " +
                                    "WHERE id_profile = " + values[0] +
                                    " AND id_site = (SELECT id_site " +
                                    "FROM sites " +
                                    "WHERE link = '" + values[1] + "')");
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

    public void setTextAreaLogs(TextArea textAreaLogs) {
        Server.textAreaLogs = textAreaLogs;
    }

    static void addLog(String log) {
        Server.textAreaLogs.appendText(log + "\n");
    }

}