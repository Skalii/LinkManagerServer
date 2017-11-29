package com.skaliy.linkmanager.server.server;

import com.skaliy.linkmanager.server.fxapp.Controller;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;

import java.sql.SQLException;
import java.util.Arrays;

public class ServerHandler extends ChannelInboundMessageHandlerAdapter<String> {

    private static final ChannelGroup channels = new DefaultChannelGroup();

    @Override
    public void handlerAdded(ChannelHandlerContext channelHandlerContext) throws Exception {
        Channel incoming = channelHandlerContext.channel();
        Controller.addLog("[CLIENT] - " + incoming.remoteAddress() + " | has joined!");
        channels.add(channelHandlerContext.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext channelHandlerContext) throws Exception {
        Channel incoming = channelHandlerContext.channel();
        Controller.addLog("[CLIENT] - " + incoming.remoteAddress() + " | has left!");
        channels.remove(channelHandlerContext.channel());
    }

    @Override
    public void messageReceived(final ChannelHandlerContext channelHandlerContext, String message) {
        Channel incoming = channelHandlerContext.channel();

        if (message.startsWith("false") || message.startsWith("true")) {
//            String bool = message.substring(0, message.indexOf(":")),
            message = message.substring(message.indexOf(":") + 1);

            Controller.addLog("[CLIENT] - " + incoming.remoteAddress() + " | query: " + message);

            String[] values = message.substring(message.indexOf(",") + 1).split(",");
            boolean queryResult = Server.setResult(message, values);

            for (Channel channel : channels) {
                if (channel == incoming) {
                    Controller.addLog("[CLIENT] - " + incoming.remoteAddress() + " | query state: " + queryResult);
                    channel.write("[SERVER] - query state: " + "\r\n");
                    channel.write("[" + queryResult + "]" + "\r\n");
                }
            }

        } else {

            String[][] quertResult;
            String queryState = "true";

            Controller.addLog("[CLIENT] - " + incoming.remoteAddress() + " | query: " + message);

            try {
                quertResult = Server.getResult(message);
                if (quertResult.length == 0) {
                    quertResult = new String[][]{{null}};
                    queryState = "null";
                }
            } catch (SQLException | ArrayIndexOutOfBoundsException e) {
                quertResult = new String[][]{{null}};
                queryState = "false";
            }

            for (Channel channel : channels) {

                if (channel == incoming) {
                    Controller.addLog("[CLIENT] - " + incoming.remoteAddress() + " | query state: " + queryState);
                    Controller.addLog("[CLIENT] - " + incoming.remoteAddress() + " | result size: " + quertResult.length);
                    channel.write("[SERVER] - accepted the query: " + message + "\r\n");
                    channel.write("[SERVER] - result size: " + quertResult.length + "\r\n");

                    for (String[] record : quertResult) {
                        Controller.addLog(Arrays.toString(record));
                        channel.write(Arrays.toString(record) + "\r\n");
                    }
                }

            }

        }
    }

}