/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.utils;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;

public abstract class AbstractTestSocketChannel extends SocketChannel
{
    protected AbstractTestSocketChannel(SelectorProvider provider)
    {
        super(null);
    }

    @Override
    public SocketChannel bind(SocketAddress local) throws IOException
    {
        return null;
    }

    @Override
    public <T> SocketChannel setOption(SocketOption<T> name, T value) throws IOException
    {
        return null;
    }

    @Override
    public <T> T getOption(SocketOption<T> name) throws IOException
    {
        return null;
    }

    @Override
    public Set<SocketOption<?>> supportedOptions()
    {
        return null;
    }

    @Override
    public SocketChannel shutdownInput() throws IOException
    {
        return null;
    }

    @Override
    public SocketChannel shutdownOutput() throws IOException
    {
        return null;
    }

    @Override
    public Socket socket()
    {
        return null;
    }

    @Override
    public boolean isConnected()
    {
        return false;
    }

    @Override
    public boolean isConnectionPending()
    {
        return false;
    }

    @Override
    public boolean connect(SocketAddress remote) throws IOException
    {
        return false;
    }

    @Override
    public boolean finishConnect() throws IOException
    {
        return false;
    }

    @Override
    public SocketAddress getRemoteAddress() throws IOException
    {
        return null;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException
    {
        return 0;
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException
    {
        return 0;
    }

    @Override
    public int write(ByteBuffer src) throws IOException
    {
        return 0;
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException
    {
        return 0;
    }

    @Override
    public SocketAddress getLocalAddress() throws IOException
    {
        return null;
    }

    @Override
    protected void implCloseSelectableChannel() throws IOException
    {

    }

    @Override
    protected void implConfigureBlocking(boolean block) throws IOException
    {

    }
}
