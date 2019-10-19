
package java.nio.channels;

import java.net.SocketOption;
import java.net.SocketAddress;
import java.util.Set;
import java.io.IOException;


//NetworkChannel:网络通道socket的接口规范
public interface NetworkChannel extends Channel {

    //绑定socket的服务地址，返回一个网络通道对象
    NetworkChannel bind(SocketAddress local) throws IOException;


    //获取本地地址，返回一个socket的地址
    SocketAddress getLocalAddress() throws IOException;


    //设置可选项
    <T> NetworkChannel setOption(SocketOption<T> name, T value) throws IOException;


    <T> T getOption(SocketOption<T> name) throws IOException;


    //返回管道支持的选项
    Set<SocketOption<?>> supportedOptions();
}
