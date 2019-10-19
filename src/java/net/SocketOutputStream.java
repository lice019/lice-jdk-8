/*
 * Copyright (c) 1995, 2016, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package java.net;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * 这个流扩展了FileOutputStream来实现SocketOutputStream。注意，这个类不应该是公共的。
 */
class SocketOutputStream extends FileOutputStream {
    static {
        init();
    }

    private AbstractPlainSocketImpl impl = null;
    private byte temp[] = new byte[1];
    private Socket socket = null;


    SocketOutputStream(AbstractPlainSocketImpl impl) throws IOException {
        super(impl.getFileDescriptor());
        this.impl = impl;
        socket = impl.getSocket();
    }


    public final FileChannel getChannel() {
        return null;
    }


    private native void socketWrite0(FileDescriptor fd, byte[] b, int off,
                                     int len) throws IOException;


    private void socketWrite(byte b[], int off, int len) throws IOException {


        if (len <= 0 || off < 0 || len > b.length - off) {
            if (len == 0) {
                return;
            }
            throw new ArrayIndexOutOfBoundsException("len == " + len
                    + " off == " + off + " buffer length == " + b.length);
        }

        FileDescriptor fd = impl.acquireFD();
        try {
            socketWrite0(fd, b, off, len);
        } catch (SocketException se) {
            if (se instanceof sun.net.ConnectionResetException) {
                impl.setConnectionResetPending();
                se = new SocketException("Connection reset");
            }
            if (impl.isClosedOrPending()) {
                throw new SocketException("Socket closed");
            } else {
                throw se;
            }
        } finally {
            impl.releaseFD();
        }
    }


    public void write(int b) throws IOException {
        temp[0] = (byte) b;
        socketWrite(temp, 0, 1);
    }


    public void write(byte b[]) throws IOException {
        socketWrite(b, 0, b.length);
    }


    public void write(byte b[], int off, int len) throws IOException {
        socketWrite(b, off, len);
    }

    /**
     * Closes the stream.
     */
    private boolean closing = false;

    public void close() throws IOException {
        // Prevent recursion. See BugId 4484411
        if (closing)
            return;
        closing = true;
        if (socket != null) {
            if (!socket.isClosed())
                socket.close();
        } else
            impl.close();
        closing = false;
    }

    /**
     * Overrides finalize, the fd is closed by the Socket.
     */
    protected void finalize() {
    }

    /**
     * Perform class load-time initializations.
     */
    private native static void init();

}
