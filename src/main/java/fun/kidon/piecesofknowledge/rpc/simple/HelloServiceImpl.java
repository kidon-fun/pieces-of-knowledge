package fun.kidon.piecesofknowledge.rpc.simple;

public class HelloServiceImpl implements HelloService {
    public String hello(String name) {
        return "Hello " + name;
    }
}
