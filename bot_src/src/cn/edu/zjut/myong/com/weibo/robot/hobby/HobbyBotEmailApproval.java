package cn.edu.zjut.myong.com.weibo.robot.hobby;

import cn.edu.zjut.myong.com.weibo.robot.HobbyBot;
import com.sun.mail.pop3.POP3Store;
import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.*;

import javax.json.JsonArray;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class HobbyBotEmailApproval implements Runnable {

    /*
    public static void main(String[] args) {
        HobbyBotEmailApproval app = new HobbyBotEmailApproval(null);
        app.sendApprovedCount("myong1981@sina.com", 128);
        // app.sendApprovalFile("myong1981@sina.com", new File("./HobbyBot.json"));
        // Date dl = new Date();
        // dl.setTime(dl.getTime() - 5 * 60 * 1000);
        // app.receive(dl);
    }
    */

    private HobbyBot bot;
    private Properties emailServerProp;
    private Logger log = Logger.getLogger(HobbyBotEmailApproval.class.getName());

    private static final String username = "socialbot";
    private static final String password = "1209love1026";
    private static final String eServer = "socialbot@sina.com";

    public HobbyBotEmailApproval(HobbyBot bot) {
        this.bot = bot;
        // 配置服务器属性
        emailServerProp = new Properties();
        emailServerProp.put("mail.debug", "false");
        emailServerProp.put("mail.smtp.CSS_WEIBO_POSTER", "smtp.sina.com"); // smtp服务器
        emailServerProp.put("mail.pop3.CSS_WEIBO_POSTER", "pop.sina.com"); // pop3服务器
        emailServerProp.put("mail.smtp.auth", "true"); // 是否smtp认证
        emailServerProp.put("mail.smtp.port", "25"); // 设置smtp端口
        emailServerProp.put("mail.transport.protocol", "smtp"); // 发邮件协议
        emailServerProp.put("mail.store.protocol", "pop3"); // 收邮件协议
    }

    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        Date deadline = new Date(new Date().getTime() - 60 * 60 * 1000);
        while (true) {
            try {
                // 接收邮件
                log.info("接收邮件");
                Order order = receive(deadline);
                if (order != null && !order.cmd.isEmpty()) {
                    // deadline为本次收取的时间
                    deadline = new Date();
                    //
                    log.info("接收到命令：" + order.from + " " + order.cmd);
                    // 按命令处理
                    switch (order.cmd) {
                        case "[MONITOR]":
                            JsonArray unchecked = bot.getAllUncheckedUsers().build();
                            int n = 0;
                            for (int i = 0; i < unchecked.size(); i++) {
                                n = n + unchecked.getJsonArray(i).size();
                            }
                            sendApprovedCount(order.from, n, "个用户等待验证");
                            break;

                        case "[LIST]":
                            JsonArray groups = bot.getGroupsInfo().build();
                            JsonArray unapproved = bot.getAllUncheckedUsers().build();
                            File f = buildSentExcel(groups, unapproved);
                            sendApprovalFile(order.from, f);
                            break;

                        case "[APPROVAL]":
                            List<Approval> approvals = readReceivedExcel(order.attachment);
                            for (Approval a : approvals) {
                                while (!bot.approvable) {
                                    Thread.sleep(1000);
                                }
                                bot.approve(a.gid, a.uid, a.passed);
                            }
                            sendApprovedCount(order.from, approvals.size(), "个用户通过验证");
                            break;
                    }
                }

                // 每X分钟检查一次有没有新命令
                Thread.sleep(10 * 60 * 1000);
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e);
            }
        }
    }

    public class Order {
        public String cmd = "";
        public String from;
        public File attachment;
    }

    public void sendApprovedCount(String address, int n, String info) {
        // 获取Session
        Session session = Session.getInstance(emailServerProp, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        MimeMessage message = new MimeMessage(session);
        try {
            // 设置消息
            message.setSubject(n + info, "UTF-8");
            message.setText("李伟，这是我们这次有待处理的人员的数量，你要仔细看一下");
            message.setFrom(new InternetAddress(eServer));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(address));

            // 发送
            Transport transport = session.getTransport();
            transport.connect();
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();

            log.info("发送待验证人数");
        } catch (Exception e) {
            log.fatal(e);
        }
    }

    public void sendApprovalFile(String address, File xlsx) {
        // 获取Session
        Session session = Session.getInstance(emailServerProp, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        MimeMessage message = new MimeMessage(session);
        try {
            // 设置消息
            MimeMultipart multipart = new MimeMultipart();
            multipart.setSubType("related");

            // 加入附件
            MimeBodyPart attachment = new MimeBodyPart();
            attachment.attachFile(xlsx);
            multipart.addBodyPart(attachment);

            message.setSubject("等待你处理的用户列表", "UTF-8");
            message.setFrom(new InternetAddress(eServer));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(address));
            message.setContent(multipart);

            // 发送
            Transport transport = session.getTransport();
            transport.connect();
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();

            log.info("发送待验证用户列表");
        } catch (Exception e) {
            log.fatal(e);
        }
    }

    public Order receive(Date deadline) {
        // 返回值
        Order order = new Order();
        order.cmd = "";

        // 打开收件箱
        Folder inBox;
        try {
            Session session = Session.getInstance(emailServerProp);
            POP3Store store = (POP3Store) session.getStore("pop3");
            store.connect(username, password);
            inBox = store.getFolder("INBOX");
            inBox.open(Folder.READ_WRITE);
        } catch (Exception e) {
            log.error(e);
            return order;
        }

        // 收取新的邮件
        try {
            int n = inBox.getMessageCount();
            for (int i = n; i >= 1; i--) {
                Message message = inBox.getMessage(i);
                // 上次检查过的邮件，退出
                if (message.getSentDate().before(deadline))
                    break;

                // 返回值
                order.from = message.getFrom()[0].toString();

                // 通过subject确认命令类型或者是否是管理者邮件
                String cmd = message.getSubject();
                if (cmd == null) {
                    break;

                } else if (cmd.trim().toUpperCase().startsWith("[LIST][" + bot.getBotName() + "]")) {
                    order.cmd = "[LIST]";
                    break;

                } else if (cmd.trim().toUpperCase().startsWith("[APPROVAL][" + bot.getBotName() + "]")) {
                    order.cmd = "[APPROVAL]";
                    List<File> attached = getAttachedFile(message);
                    if (attached != null && !attached.isEmpty()) {
                        order.attachment = attached.get(0);
                    } else {
                        order.cmd = "";
                    }
                    break;

                } else if (cmd.trim().toUpperCase().startsWith("[MONITOR][" + bot.getBotName() + "]")) {
                    order.cmd = "[MONITOR]";
                    break;
                }
            }

            // 无有效命令
            return order;
        } catch (Exception e) {
            log.fatal(e);
            return order;

        } finally {
            try {
                inBox.close();
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
    }

    private List<File> getAttachedFile(Part part) throws Exception {
        List<File> files = new ArrayList<>();
        if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart subPart = mp.getBodyPart(i);
                if (subPart.isMimeType("multipart/*")) {
                    files.addAll(getAttachedFile(subPart));
                } else {
                    try {
                        String name = subPart.getFileName();
                        if ((name != null)) {
                            name = MimeUtility.decodeText(name);
                            // 创立文件
                            File f = new File(bot.getTempDir() + "/" + new SimpleDateFormat("MM-dd-HH-mm-").format(new Date()) +  name);
                            FileOutputStream fos = new FileOutputStream(f);
                            InputStream is = subPart.getInputStream();
                            int c;
                            while ((c = is.read()) != -1) {
                                fos.write(c);
                            }
                            fos.close();
                            is.close();
                            //
                            files.add(f);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return files;
    }

    public File buildSentExcel(JsonArray groups, JsonArray unapproved) {
        // 由Bot类来保证两种的1-1对应
        if (groups.size() != unapproved.size())
            return null;

        // Excel表格
        XSSFWorkbook book = new XSSFWorkbook();
        XSSFSheet sheet = book.createSheet();

        XSSFCellStyle style = book.createCellStyle();
        style.setWrapText(true);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // 列举所有未检验的
        for (int i = 0, r = 0; i < unapproved.size(); i++) {
            for (int j = 0; j < unapproved.getJsonArray(i).size(); j++) {
                // 一条未检验的好友添加
                String hobby = groups.getJsonObject(i).getString("hobby");
                String uid = unapproved.getJsonArray(i).getJsonObject(j).getJsonObject("user").getString("id");
                String parent = unapproved.getJsonArray(i).getJsonObject(j).getJsonObject("parent").getJsonObject("user").getString("id");
                String text = unapproved.getJsonArray(i).getJsonObject(j).getJsonObject("weibo").getString("content");
                Date updateTime = new Date();
                updateTime.setTime(unapproved.getJsonArray(i).getJsonObject(j).getJsonNumber("updateTime").longValue());

                // 填入Excel表格
                XSSFRow row = sheet.createRow(r++);
                row.setHeightInPoints(40);
                row.createCell(0).setCellValue(i+1);
                row.createCell(1).setCellValue(parent);
                row.createCell(2).setCellValue(uid);
                row.createCell(3).setCellValue(new SimpleDateFormat("MM-dd HH:mm").format(updateTime));
                XSSFCell cell = row.createCell(4);
                cell.setCellStyle(style);
                cell.setCellValue(text);
                row.createCell(5).setCellValue(hobby);
                row.createCell(6).setCellValue(1);
            }
        }

        // 调整格式
        sheet.setColumnWidth(3, 3000);
        sheet.setColumnWidth(4, 15000);

        // 输出文件
        try {
            File f = new File(bot.getTempDir() + "/unapproved.xlsx");
            /*
            if (f.exists()) {
                f.deleteOnExit();
                f.createNewFile();
            }
            */
            book.write(new FileOutputStream(f));
            return f;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public class Approval {
        public int gid;
        public String uid;
        public boolean passed;

        @Override
        public String toString() {
            return gid + ":" + uid + ":" + passed;
        }
    }

    public List<Approval> readReceivedExcel(File checkedFile) {
        List<Approval> checked = new ArrayList<>();

        // 读取xlsx文件
        XSSFWorkbook book;
        try {
            book = new XSSFWorkbook(checkedFile);
        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
            return checked;
        }

        // 读取内容
        XSSFSheet sheet = book.getSheetAt(0);
        for (Row row : sheet) {
            Approval c = new Approval();

            c.gid = (int) row.getCell(0).getNumericCellValue() - 1;
            c.uid = row.getCell(2).getStringCellValue().trim();
            c.passed = ((int) row.getCell(6).getNumericCellValue()) == 1;

            checked.add(c);
        }

        return checked;
    }
}
