package zalo.commands;

import zalo.interfaces.Command;
import zalo.message.MessageContext;
import zalo.utils.Apis;
import zalo.models.ThreadType;
import zalo.apis.SendMessageApi;
import zalo.services.NroTradeService;
import java.util.*;

public class KiemTraGiaoDich implements Command {
    

    
    @Override
    public String getName() {
        return "ctrade";
    }
    
    @Override
    public String getDescription() {
        return "Kiểm tra giao dịch theo user: .ctrade <username> [limit]";
    }
    
    @Override
    public String getTag() {
        return "group";
    }
    
    @Override
    public int getCooldown() {
        return 0;
    }
    
    @Override
    public int getRole() {
        return 2;
    }
    
    @Override
    public void run(Command.CommandContext context) {
        try {
            MessageContext message = context.getMessage();
            Apis api = context.getApi();
            List<String> args = context.getArgs();
            
            String threadId = message.getThreadId();
            ThreadType threadType = message.getThreadType();
            Map<String, Object> data = message.getData();
            
            if (!message.isGroup()) {
                SendMessageApi.MessageContent msgContent = new SendMessageApi.MessageContent();
                msgContent.setMsg("Lệnh này chỉ dùng trong nhóm.");
                msgContent.setQuote(SendMessageApi.createQuoteFromData(data));
                api.sendMessage.sendMessage(msgContent, threadId, threadType).get();
                return;
            }
            
            if (args == null || args.isEmpty()) {
                SendMessageApi.MessageContent msgContent = new SendMessageApi.MessageContent();
                msgContent.setMsg("Sử dụng: .ctrade <username> [limit]\nVí dụ: .ctrade player123 10");
                msgContent.setQuote(SendMessageApi.createQuoteFromData(data));
                api.sendMessage.sendMessage(msgContent, threadId, threadType).get();
                return;
            }
            
            String username = args.get(0).trim();
            int limit = 10;
            
            if (args.size() > 1) {
                try {
                    limit = Integer.parseInt(args.get(1).trim());
                    if (limit < 1 || limit > 50) {
                        limit = 10;
                    }
                } catch (NumberFormatException e) {
                    limit = 10;
                }
            }
            
            List<Map<String, Object>> transactions = NroTradeService.gI().getTransactionsByUser(username, limit);
            
            SendMessageApi.MessageContent msgContent = new SendMessageApi.MessageContent();
            if (transactions == null || transactions.isEmpty()) {
                msgContent.setMsg("Không tìm thấy giao dịch nào của " + username + ".");
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("📋 Giao dịch của ").append(username).append(" (").append(transactions.size()).append(" giao dịch gần nhất):\n\n");
                
                for (int i = 0; i < transactions.size(); i++) {
                    Map<String, Object> trans = transactions.get(i);
                    sb.append("[").append(i + 1).append("] ").append(trans.get("time")).append("\n");
                    sb.append("👤 ").append(trans.get("player1")).append(" ↔ ").append(trans.get("player2")).append("\n");
                    sb.append("📦 ").append(trans.get("items1")).append("\n");
                    sb.append("📦 ").append(trans.get("items2")).append("\n");
                    sb.append("\n");
                }
                
                msgContent.setMsg(sb.toString());
            }
            
            msgContent.setQuote(SendMessageApi.createQuoteFromData(data));
            api.sendMessage.sendMessage(msgContent, threadId, threadType).get();
        } catch (Exception e) {
            System.err.println("[KiemTraGiaoDich] Error: " + e.getMessage());
        }
    }
}

