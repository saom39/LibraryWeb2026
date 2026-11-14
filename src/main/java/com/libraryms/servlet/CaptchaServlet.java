package com.libraryms.servlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;

/** 登录验证码（图片验证码，文本保存在 session 中） */
@WebServlet("/api/captcha")
public class CaptchaServlet extends HttpServlet {

    private static final char[] CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final Random RANDOM = new Random();

    static {
        System.setProperty("java.awt.headless", "true");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 4; i++) code.append(CHARS[RANDOM.nextInt(CHARS.length)]);
        req.getSession().setAttribute("captcha", code.toString());

        int width = 120, height = 40;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(245, 248, 252));
        g.fillRect(0, 0, width, height);
        for (int i = 0; i < 12; i++) {
            g.setColor(new Color(180 + RANDOM.nextInt(60), 190 + RANDOM.nextInt(60), 200 + RANDOM.nextInt(55)));
            g.drawLine(RANDOM.nextInt(width), RANDOM.nextInt(height),
                    RANDOM.nextInt(width), RANDOM.nextInt(height));
        }
        for (int i = 0; i < 4; i++) {
            g.setFont(new Font("Arial", Font.BOLD, 24 + RANDOM.nextInt(5)));
            g.setColor(new Color(30 + RANDOM.nextInt(80), 50 + RANDOM.nextInt(80), 100 + RANDOM.nextInt(100)));
            g.drawString(String.valueOf(code.charAt(i)), 12 + i * 27, 27 + RANDOM.nextInt(6));
        }
        g.dispose();

        resp.setContentType("image/png");
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        ImageIO.write(image, "png", resp.getOutputStream());
    }
}
