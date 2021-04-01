package fun.kidon.piecesofknowledge.nio.simple;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

/**
 * @Author kidon
 * @Date 2021-03-22 13:38
 */
public class NonBlockServer {
    public static void main(String[] args) throws IOException {
        // 获取通道
        ServerSocketChannel server = ServerSocketChannel.open();
        // 切换成非阻塞模式
        server.configureBlocking(false);
        // 绑定连接
        server.bind(new InetSocketAddress(7071));
        // 获取选择器
        Selector selector = Selector.open();
        // 将通道注册到选择器上，指定接收“监听通道”事件
        server.register(selector, SelectionKey.OP_ACCEPT);
        // 轮训地获取选择器上已“就绪”的事件--->只要select()>0，说明已就绪
        while (selector.select() > 0){
            // 获取当前选择器所有注册的“选择键”(已就绪的监听事件)
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            // 获取已“就绪”的事件，(不同的事件做不同的事)
            while (iterator.hasNext()){
                SelectionKey selectionKey = iterator.next();
                if (selectionKey.isAcceptable()){// 接收事件就绪
                    // 获取客户端的链接
                    SocketChannel client = server.accept();
                    // 切换成非阻塞状态
                    client.configureBlocking(false);
                    // 注册到选择器上-->拿到客户端的连接为了读取通道的数据(监听读就绪事件)
                    client.register(selector,SelectionKey.OP_READ);
                }else if (selectionKey.isReadable()){
                    // 获取当前选择器读就绪状态的通道
                    SocketChannel client = (SocketChannel) selectionKey.channel();
                    // 读取数据
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    // 得到文件通道，将客户端传递过来的图片写到本地项目下(写模式、没有则创建)
                    FileChannel outChannel = FileChannel.open(Paths.get("1.png"), StandardOpenOption.WRITE,StandardOpenOption.CREATE);;
                    while (client.read(buffer) > 0){
                        // 在读之前都要切换成读模式
                        buffer.flip();
                        outChannel.write(buffer);
                        // 读完切换成写模式，能让管道继续读取文件的数据
                        buffer.clear();
                    }
                }
                // 取消选择键(已经处理过的事件，就应该取消掉了)
                iterator.remove();
            }
        }
    }
}