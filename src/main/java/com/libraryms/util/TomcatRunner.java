package com.libraryms.util;

import java.awt.Desktop;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;

/**
 * 本地一键启动入口：负责启动本机 Tomcat 9 并自动打开浏览器访问系统首页。
 * 若 8080 端口已有服务在运行，则直接打开页面，不再重复启动。
 * 使用前请先将打包好的 LibraryWeb2026.war 部署到 TOMCAT_HOME/webapps 目录下。
 */
public final class TomcatRunner {

    private static final String TOMCAT_HOME = "D:\\java\\env\\tomcat9";
    private static final String APP_URL = "http://127.0.0.1:8080/LibraryWeb2026/login.html";
    private static final int PORT = 8080;

    private TomcatRunner() { }

    public static void main(String[] args) throws Exception {
        System.out.println("=== 图书馆管理系统 一键启动 ===");
        if (isPortOpen()) {
            System.out.println("Tomcat 已在 8080 端口运行，直接打开系统页面：" + APP_URL);
            openBrowser();
            return;
        }

        File bin = new File(TOMCAT_HOME, "bin");
        ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "catalina.bat", "run");
        pb.directory(bin);
        pb.environment().put("CATALINA_HOME", TOMCAT_HOME);
        pb.environment().put("CATALINA_BASE", TOMCAT_HOME);
        pb.inheritIO();
        System.out.println("正在启动 Tomcat 9：" + TOMCAT_HOME);
        final Process tomcat = pb.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessBuilder stop = new ProcessBuilder("cmd.exe", "/c", "catalina.bat", "stop");
                    stop.directory(new File(TOMCAT_HOME, "bin"));
                    stop.environment().put("CATALINA_HOME", TOMCAT_HOME);
                    stop.environment().put("CATALINA_BASE", TOMCAT_HOME);
                    stop.inheritIO().start().waitFor();
                } catch (Exception ignored) {
                    // 停止时 Tomcat 可能已经退出
                }
            }
        }));

        for (int i = 0; i < 60; i++) {
            if (!tomcat.isAlive()) {
                System.out.println("Tomcat 进程已退出，退出码 " + tomcat.exitValue() + "，请查看上方日志。");
                return;
            }
            if (isPortOpen()) {
                System.out.println("启动成功，访问地址：" + APP_URL);
                openBrowser();
                tomcat.waitFor();
                return;
            }
            Thread.sleep(500);
        }
        System.out.println("等待 8080 端口超时，请查看 Tomcat 日志。");
    }

    private static boolean isPortOpen() {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress("127.0.0.1", PORT), 400);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void openBrowser() {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(APP_URL));
            } else {
                System.out.println("请手动打开：" + APP_URL);
            }
        } catch (Exception e) {
            System.out.println("浏览器打开失败，请手动访问：" + APP_URL);
        }
    }
}
