package com.skaliy.linkmanager.server.server;

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

        Server.addLog("[CLIENT] - " + incoming.remoteAddress() + " has joined!");

        channels.add(channelHandlerContext.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext channelHandlerContext) throws Exception {
        Channel incoming = channelHandlerContext.channel();

        Server.addLog("[CLIENT] - " + incoming.remoteAddress() + " has left!");

        channels.remove(channelHandlerContext.channel());
    }

    @Override
    public void messageReceived(final ChannelHandlerContext channelHandlerContext, String message) {
        Channel incoming = channelHandlerContext.channel();

        if (message.startsWith("false") || message.startsWith("true")) {

//            String bool = message.substring(0, message.indexOf(":")),
            boolean queryResult = true;
            message = message.substring(message.indexOf(":") + 1);

            Server.addLog("[CLIENT] - " + incoming.remoteAddress() + " query set: " + message);

            int indexStart = 0;
            String[] values = new String[Integer.parseInt(message.substring(message.lastIndexOf("_") + 1))];

            for (int i = 0; i < values.length; i++) {
                indexStart = message.indexOf(",", indexStart);
                int indexEnd = message.indexOf(",", indexStart);
                if (indexEnd == -1) indexEnd = message.indexOf("_", indexStart);
                System.out.println(indexStart + " " + indexEnd);
                values[i] = message.substring(indexStart + 1, indexEnd);
            }

            queryResult = Server.setResult(message, values);

            for (Channel channel : channels) {
                if (channel == incoming) {
                    Server.addLog("[CLIENT] - query state: " + queryResult);
                    channel.write("[SERVER] - query state: " + "\r\n");
                    channel.write("[" + queryResult + "]" + "\r\n");
                }
            }

        } else {

            String[][] quertResult;
            String queryState = "true";

            Server.addLog("[CLIENT] - " + incoming.remoteAddress() + " query get: " + message);

            try {
                quertResult = Server.getResult(message);
            } catch (SQLException e) {
                quertResult = new String[][]{{null}};
                queryState = "false";
            }

            for (Channel channel : channels) {
                if (channel == incoming) {
                    Server.addLog("[CLIENT] - query state: " + queryState);
                    Server.addLog("[CLIENT] - result size: " + quertResult.length);
                    channel.write("[SERVER] - result size: " + quertResult.length + "\r\n");

                    for (String[] record : quertResult) {
                        Server.addLog(Arrays.toString(record));
                        channel.write(Arrays.toString(record) + "\r\n");
                    }
                }
            }
        }
    }

}