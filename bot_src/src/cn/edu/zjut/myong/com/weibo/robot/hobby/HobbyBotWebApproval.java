package cn.edu.zjut.myong.com.weibo.robot.hobby;

import cn.edu.zjut.myong.com.weibo.robot.HobbyBot;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 整个机器人系统的入口，主要功能是启动服务器（用于监控和操作机器人）和机器人。
 * <p>
 * 启动后，你可以访问http://localhost:625/来看到这个监控前端
 */
public class HobbyBotWebApproval implements Runnable {

    private HobbyBot bot;

    public HobbyBotWebApproval(HobbyBot bot) {
        this.bot = bot;
    }

    @Override
    public void run() {
        // 启动Server
        Server botServer = new org.eclipse.jetty.server.Server(bot.getBotPort());
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(true);
        resourceHandler.setResourceBase(bot.getWebDir());

        ServletContextHandler botServletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        botServletHandler.setContextPath("/");

        // HobbyBotServletGroupInfo
        botServletHandler.addServlet(new ServletHolder(new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
                response.setContentType("text/html");
                response.setCharacterEncoding("UTF-8");
                response.setStatus(HttpServletResponse.SC_OK);

                JsonArrayBuilder json;
                try {
                    json = bot.getGroupsInfo();
                    response.getWriter().print(json.build().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    response.getWriter().print(Json.createArrayBuilder().build());
                }
            }
        }), "/group.ser");

        // HobbyBotServletListUnchecked
        botServletHandler.addServlet(new ServletHolder(new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
                response.setContentType("text/html");
                response.setCharacterEncoding("UTF-8");
                response.setStatus(HttpServletResponse.SC_OK);

                JsonArrayBuilder json;
                try {
                    json = bot.getAllUncheckedUsers();
                    response.getWriter().print(json.build().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    response.getWriter().print(Json.createArrayBuilder().build());
                }
            }
        }), "/list.ser");

        // HobbyBotServletCheck
        botServletHandler.addServlet(new ServletHolder(new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.setStatus(HttpServletResponse.SC_OK);
                try {
                    int gid = Integer.parseInt(request.getParameter("group")) - 1;
                    String uid = request.getParameter("user");
                    boolean passed = Boolean.valueOf(request.getParameter("passed"));
                    bot.approve(gid, uid, passed);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                response.getWriter().print(Json.createArrayBuilder().build());
            }
        }), "/check.ser");

        HandlerCollection handlerCollection = new HandlerCollection(true);
        handlerCollection.addHandler(resourceHandler);
        handlerCollection.addHandler(botServletHandler);
        botServer.setHandler(handlerCollection);

        try {
            botServer.start();
            botServer.join();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
