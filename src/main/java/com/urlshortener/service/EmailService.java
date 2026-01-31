package com.urlshortener.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    @Async("emailExecutor")
    public void sendWelcomeEmail(String toEmail, String name) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Welcome to URL Shortener!");

            String htmlContent = buildWelcomeEmailTemplate(name);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Welcome email sent to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send welcome email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async("emailExecutor")
    public void sendUrlCreatedEmail(String toEmail, String shortUrl, String originalUrl) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Your Short URL is Ready!");

            String htmlContent = buildUrlCreatedEmailTemplate(shortUrl, originalUrl);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("URL created email sent to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send URL created email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async("emailExecutor")
    public void sendWeeklyReportEmail(String toEmail, String name, long totalClicks,
                                       long newUrls, long topClickUrl) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Your Weekly URL Analytics Report");

            String htmlContent = buildWeeklyReportTemplate(name, totalClicks, newUrls, topClickUrl);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Weekly report email sent to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send weekly report email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async("emailExecutor")
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Request");

            String resetLink = baseUrl + "/reset-password?token=" + resetToken;
            String htmlContent = buildPasswordResetTemplate(resetLink);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
        }
    }

    private String buildWelcomeEmailTemplate(String name) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #4F46E5; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 8px 8px; }
                    .button { display: inline-block; background: #4F46E5; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin-top: 20px; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Welcome to URL Shortener!</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>Thank you for joining URL Shortener! We're excited to have you on board.</p>
                        <p>With our platform, you can:</p>
                        <ul>
                            <li>Create short, memorable URLs</li>
                            <li>Track click analytics in real-time</li>
                            <li>Generate QR codes for your links</li>
                            <li>Set expiration dates and password protection</li>
                        </ul>
                        <a href="%s" class="button">Get Started</a>
                    </div>
                    <div class="footer">
                        <p>© 2024 URL Shortener. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(name, baseUrl);
    }

    private String buildUrlCreatedEmailTemplate(String shortUrl, String originalUrl) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #10B981; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 8px 8px; }
                    .url-box { background: white; padding: 15px; border-radius: 5px; margin: 15px 0; border-left: 4px solid #10B981; }
                    .short-url { font-size: 18px; font-weight: bold; color: #10B981; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Your Short URL is Ready!</h1>
                    </div>
                    <div class="content">
                        <p>Your new short URL has been created successfully:</p>
                        <div class="url-box">
                            <p><strong>Short URL:</strong></p>
                            <p class="short-url">%s</p>
                        </div>
                        <div class="url-box">
                            <p><strong>Original URL:</strong></p>
                            <p style="word-break: break-all;">%s</p>
                        </div>
                        <p>Start sharing your link and track its performance in your dashboard!</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 URL Shortener. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(shortUrl, originalUrl);
    }

    private String buildWeeklyReportTemplate(String name, long totalClicks, long newUrls, long topClickUrl) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #6366F1; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 8px 8px; }
                    .stat-box { display: inline-block; background: white; padding: 20px; border-radius: 8px; margin: 10px; text-align: center; min-width: 120px; }
                    .stat-number { font-size: 32px; font-weight: bold; color: #6366F1; }
                    .stat-label { color: #666; font-size: 14px; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Weekly Analytics Report</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>Here's your weekly URL performance summary:</p>
                        <div style="text-align: center;">
                            <div class="stat-box">
                                <div class="stat-number">%d</div>
                                <div class="stat-label">Total Clicks</div>
                            </div>
                            <div class="stat-box">
                                <div class="stat-number">%d</div>
                                <div class="stat-label">New URLs</div>
                            </div>
                            <div class="stat-box">
                                <div class="stat-number">%d</div>
                                <div class="stat-label">Top URL Clicks</div>
                            </div>
                        </div>
                        <p style="margin-top: 20px;">Visit your dashboard for detailed analytics.</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 URL Shortener. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(name, totalClicks, newUrls, topClickUrl);
    }

    private String buildPasswordResetTemplate(String resetLink) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #EF4444; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 8px 8px; }
                    .button { display: inline-block; background: #EF4444; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin-top: 20px; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Password Reset Request</h1>
                    </div>
                    <div class="content">
                        <p>We received a request to reset your password. Click the button below to create a new password:</p>
                        <div style="text-align: center;">
                            <a href="%s" class="button">Reset Password</a>
                        </div>
                        <p style="margin-top: 20px; font-size: 14px; color: #666;">
                            This link will expire in 1 hour. If you didn't request a password reset, you can safely ignore this email.
                        </p>
                    </div>
                    <div class="footer">
                        <p>© 2024 URL Shortener. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(resetLink);
    }
}
