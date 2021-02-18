package fun.kidon.piecesofknowledge.rpc.simple;


import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RpcFramework {

    public static final ExecutorService exec = new ThreadPoolExecutor(5, 10, 10L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(100), new ThreadPoolExecutor.AbortPolicy());

    public static void export(Object service, int port) throws Exception {
        ServerSocket server = new ServerSocket(port);
        for (; ; ) {
            Socket socket = server.accept();
            exec.execute(() -> {
                try {
                    // 反序列化
                    ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                    String methodName = input.readUTF();// 方法名
                    Class<?>[] paraType = (Class<?>[]) input.readObject();// 参数类型
                    Object[] args = (Object[]) input.readObject();// 调用参数
                    Method method = service.getClass().getMethod(methodName, paraType);
                    Object result = method.invoke(service, args);// 实际调用
                    // 返回结果
                    ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                    output.writeObject(result);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static <T> T call(Class<T> interfaceClazz, String host, int port) throws Exception {
        return (T) Proxy.newProxyInstance(interfaceClazz.getClassLoader(), new Class[]{interfaceClazz},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        Socket socket = new Socket(host,port);
                        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                        output.writeUTF(method.getName());
                        output.writeObject(method.getParameterTypes());
                        output.writeObject(args);

                        ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                        return input.readObject();
                    }
                });
    }

    public static void main(String[] args) throws Exception {
        HelloService service = new HelloServiceImpl();
        RpcFramework.export(service,7777);
    }

    @Test
    void rpc_test() throws Exception {
        HelloService service = RpcFramework.call(HelloService.class,"localhost",7777);
        System.out.println(service.hello("my rpc framework"));
    }
}
