package fun.kidon.piecesofknowledge.nio.simple;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

/**
 * @Author kidon
 * @Date 2021-03-22 13:58
 */
public class NonBlockClient {
    public static void main(String[] args) throws IOException {
        SocketChannel client = SocketChannel.open(new InetSocketAddress("127.0.0.1",7071));
        client.configureBlocking(false);
        Selector selector = Selector.open();
        client.register(selector, SelectionKey.OP_READ);
        FileChannel fileChannel = FileChannel.open(Paths.get("C:\\Users\\kidon\\Desktop\\box\\cover.jpeg"), StandardOpenOption.READ);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while (fileChannel.read(buffer) != -1){
            buffer.flip();
            client.write(buffer);
            buffer.clear();
        }
        while (selector.select() > 0){
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()){
                SelectionKey selectionKey = iterator.next();
                if (selectionKey.isReadable()){
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    ByteBuffer responseBuffer = ByteBuffer.allocate(1024);
                    int readBytes;
                    if ((readBytes = socketChannel.read(responseBuffer)) > 0){
                        responseBuffer.flip();
                        System.out.println(new String(responseBuffer.array(),0,readBytes));
                    }
                }
                iterator.remove();
            }
        }
    }
}
