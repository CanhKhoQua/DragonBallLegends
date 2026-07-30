
package zalo.server;

// 1/ MỒM NÓI GWEN SPAM BOX NÓ NHƯNG KHI TÌM LẠI TIN NHẮN CHỈ CÓ 1 TIN NHẮN ???
// 2/ BẢO ACC ĐỨC RYO ĐI SCAM NHƯNG TRONG KHI FB ĐẤY LẠI BỊ MẤY THẰNG BÊN NRO SCAM NGƯỢC ??????
// 3/ MỒM NÓI 2K9 CHECK CCCD LẠI RA 2K2 MÀ LẠI KHAI ĐI HỌC 2K6

public class ServerManager {
    
    public static void main(String[] args) {
        try {
            Bot bot = new Bot();
            bot.start();
            
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down bot...");
                bot.stop();
            }));
            Thread.currentThread().join();
        } catch (Exception e) {
            System.err.println("FATAL ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

