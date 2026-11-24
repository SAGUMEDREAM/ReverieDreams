package cc.thonly.reverie_dreams.ai;

import javax.net.SocketFactory;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketOption;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.net.Proxy;

/**
 * TLS to an HTTPS forward proxy, underneath OkHttp's normal HTTP CONNECT and origin TLS.
 * The configured proxy hostname is verified separately from the API hostname.
 */
final class HttpsProxySocketFactory extends SocketFactory {
    private final String host;
    private final int port;
    private final SSLSocketFactory tlsFactory;

    HttpsProxySocketFactory(String host, int port, SSLSocketFactory tlsFactory) {
        this.host = host;
        this.port = port;
        this.tlsFactory = tlsFactory;
    }

    @Override
    public Socket createSocket() {
        return new ProxySocket();
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        Socket socket = createSocket();
        socket.connect(new InetSocketAddress(host, port));
        return socket;
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        Socket socket = createSocket();
        socket.connect(new InetSocketAddress(host, port));
        return socket;
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress local, int localPort) throws IOException {
        Socket socket = createSocket();
        socket.bind(new InetSocketAddress(local, localPort));
        socket.connect(new InetSocketAddress(host, port));
        return socket;
    }

    @Override
    public Socket createSocket(InetAddress host, int port, InetAddress local, int localPort) throws IOException {
        Socket socket = createSocket();
        socket.bind(new InetSocketAddress(local, localPort));
        socket.connect(new InetSocketAddress(host, port));
        return socket;
    }

    private final class ProxySocket extends Socket {
        private final Socket transport = new Socket(Proxy.NO_PROXY);
        private volatile SSLSocket tls;

        @Override
        public void connect(SocketAddress endpoint) throws IOException {
            connect(endpoint, 0);
        }

        @Override
        public void connect(SocketAddress endpoint, int timeout) throws IOException {
            long started = System.nanoTime();
            try {
                transport.connect(endpoint, timeout);
                int readTimeout = transport.getSoTimeout();
                SSLSocket connected = (SSLSocket) tlsFactory.createSocket(transport, host, port, true);
                tls = connected;
                SSLParameters parameters = connected.getSSLParameters();
                parameters.setEndpointIdentificationAlgorithm("HTTPS");
                parameters.setApplicationProtocols(new String[]{"http/1.1"});
                connected.setSSLParameters(parameters);
                if (timeout > 0) {
                    int remaining = (int) Math.max(1, timeout - TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started));
                    connected.setSoTimeout(readTimeout > 0 ? Math.min(readTimeout, remaining) : remaining);
                }
                connected.startHandshake();
                connected.setSoTimeout(readTimeout);
            } catch (IOException | RuntimeException exception) {
                try {
                    close();
                } catch (IOException suppressed) {
                    exception.addSuppressed(suppressed);
                }
                throw exception;
            }
        }

        private SSLSocket connected() throws SocketException {
            SSLSocket socket = tls;
            if (socket == null || transport.isClosed())
                throw new SocketException("HTTPS proxy socket is not connected");
            return socket;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return connected().getInputStream();
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return connected().getOutputStream();
        }

        @Override
        public void bind(SocketAddress address) throws IOException {
            transport.bind(address);
        }

        @Override
        public void close() throws IOException {
            // Close the transport first so cancellation also interrupts a proxy TLS handshake.
            try {
                transport.close();
            } finally {
                if (tls != null)
                    tls.close();
            }
        }

        @Override
        public boolean isConnected() {
            return transport.isConnected();
        }

        @Override
        public boolean isBound() {
            return transport.isBound();
        }

        @Override
        public boolean isClosed() {
            return transport.isClosed();
        }

        @Override
        public InetAddress getInetAddress() {
            return transport.getInetAddress();
        }

        @Override
        public InetAddress getLocalAddress() {
            return transport.getLocalAddress();
        }

        @Override
        public int getPort() {
            return transport.getPort();
        }

        @Override
        public int getLocalPort() {
            return transport.getLocalPort();
        }

        @Override
        public SocketAddress getRemoteSocketAddress() {
            return transport.getRemoteSocketAddress();
        }

        @Override
        public SocketAddress getLocalSocketAddress() {
            return transport.getLocalSocketAddress();
        }

        @Override
        public SocketChannel getChannel() {
            return transport.getChannel();
        }

        @Override
        public void setSoTimeout(int timeout) throws SocketException {
            if (tls != null)
                tls.setSoTimeout(timeout);
            else
                transport.setSoTimeout(timeout);
        }

        @Override
        public int getSoTimeout() throws SocketException {
            return transport.getSoTimeout();
        }

        @Override
        public void setTcpNoDelay(boolean on) throws SocketException {
            transport.setTcpNoDelay(on);
        }

        @Override
        public boolean getTcpNoDelay() throws SocketException {
            return transport.getTcpNoDelay();
        }

        @Override
        public void setKeepAlive(boolean on) throws SocketException {
            transport.setKeepAlive(on);
        }

        @Override
        public boolean getKeepAlive() throws SocketException {
            return transport.getKeepAlive();
        }

        @Override
        public void setReuseAddress(boolean on) throws SocketException {
            transport.setReuseAddress(on);
        }

        @Override
        public boolean getReuseAddress() throws SocketException {
            return transport.getReuseAddress();
        }

        @Override
        public void setSendBufferSize(int size) throws SocketException {
            transport.setSendBufferSize(size);
        }

        @Override
        public int getSendBufferSize() throws SocketException {
            return transport.getSendBufferSize();
        }

        @Override
        public void setReceiveBufferSize(int size) throws SocketException {
            transport.setReceiveBufferSize(size);
        }

        @Override
        public int getReceiveBufferSize() throws SocketException {
            return transport.getReceiveBufferSize();
        }

        @Override
        public void setSoLinger(boolean on, int linger) throws SocketException {
            transport.setSoLinger(on, linger);
        }

        @Override
        public int getSoLinger() throws SocketException {
            return transport.getSoLinger();
        }

        @Override
        public void setTrafficClass(int value) throws SocketException {
            transport.setTrafficClass(value);
        }

        @Override
        public int getTrafficClass() throws SocketException {
            return transport.getTrafficClass();
        }

        @Override
        public void setOOBInline(boolean on) throws SocketException {
            transport.setOOBInline(on);
        }

        @Override
        public boolean getOOBInline() throws SocketException {
            return transport.getOOBInline();
        }

        @Override
        public void sendUrgentData(int data) throws IOException {
            throw new SocketException("Urgent data is not supported over a TLS proxy");
        }

        @Override
        public void shutdownInput() throws IOException {
            connected().shutdownInput();
        }

        @Override
        public void shutdownOutput() throws IOException {
            connected().shutdownOutput();
        }

        @Override
        public boolean isInputShutdown() {
            return tls != null ? tls.isInputShutdown() : transport.isInputShutdown();
        }

        @Override
        public boolean isOutputShutdown() {
            return tls != null ? tls.isOutputShutdown() : transport.isOutputShutdown();
        }

        @Override
        public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
            transport.setPerformancePreferences(connectionTime, latency, bandwidth);
        }

        @Override
        public <T> Socket setOption(SocketOption<T> name, T value) throws IOException {
            transport.setOption(name, value);
            return this;
        }

        @Override
        public <T> T getOption(SocketOption<T> name) throws IOException {
            return transport.getOption(name);
        }

        @Override
        public Set<SocketOption<?>> supportedOptions() {
            return transport.supportedOptions();
        }
    }
}
