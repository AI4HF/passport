package io.passport.server.service;

import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.BrowserContext;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.cdp.core.Puppeteer;
import com.ruiyun.jvppeteer.cdp.entities.LaunchOptions;
import com.ruiyun.jvppeteer.cdp.entities.PDFOptions;
import com.ruiyun.jvppeteer.cdp.entities.Viewport;
import com.ruiyun.jvppeteer.cdp.entities.WaitForOptions;
import com.ruiyun.jvppeteer.common.MediaType;
import com.ruiyun.jvppeteer.common.PuppeteerLifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

@Service
public class PdfRenderService {

    private static final Logger log = LoggerFactory.getLogger(PdfRenderService.class);

    @Value("${pdf.chrome.executablePath:}")
    private String chromePath;

    @Value("${pdf.page.width:1600}")
    private int viewportWidth;

    @Value("${pdf.page.height:1200}")
    private int viewportHeight;

    @Value("${pdf.page.dsf:2}")
    private Double deviceScaleFactor;

    /**
     * Max count of parallel renders
     */
    @Value("${pdf.concurrent.renders:1}")
    private int maxConcurrentRenders;

    /**
     * Render timeout to kill halted generation processes
     */
    @Value("${pdf.render.timeout.ms:45000}")
    private int renderTimeoutMs;

    private final Object launchLock = new Object();
    private volatile Browser browser;
    private Semaphore renderSlots;

    @PostConstruct
    void init() {
        renderSlots = new Semaphore(Math.max(1, maxConcurrentRenders));
    }

    /**
     * Kill browser process at exit
     */
    @PreDestroy
    void shutdown() {
        try {
            Browser b = browser;
            browser = null;
            if (b != null) b.close();
        } catch (Throwable t) {
            log.warn("Error while closing Chrome on shutdown: {}", t.toString());
        }
    }

    /**
     * Main process that waits the Semaphore and initializes the procedure
     */
    public byte[] render(String html, String baseUrl, String width, String height, Boolean landscape) throws Exception {
        renderSlots.acquire();
        try {
            return renderOnceWithRetry(html, baseUrl, width, height, landscape);
        } finally {
            renderSlots.release();
        }
    }


    /**
     * Trigger render with error handling for Browser issues
     */
    private byte[] renderOnceWithRetry(String html, String baseUrl, String width, String height, Boolean landscape) throws Exception {
        boolean retried = false;
        while (true) {
            try {
                return doRender(html, baseUrl, width, height, landscape);
            } catch (Throwable t) {
                // Close the Browser if it is not closed by the error, to prevent dangling browser
                String msg = String.valueOf(t.getMessage());
                boolean browserLikelyDead = msg.contains("Target closed")
                        || msg.contains("Target.detachedFromTarget")
                        || msg.contains("No connection")
                        || msg.contains("Session closed")
                        || msg.contains("Socket")
                        || msg.contains("not connected");
                if (!retried && browserLikelyDead) {
                    log.warn("Chrome likely dead; relaunching and retrying once: {}", msg);
                    safeCloseBrowser();
                    retried = true;
                    continue;
                }
                throw t instanceof Exception ? (Exception) t : new RuntimeException(t);
            }
        }
    }

    /**
     * Main rendering logic
     */
    private byte[] doRender(String html, String baseUrl, String width, String height, Boolean landscape) throws Exception {
        BrowserContext context = null;
        Page page = null;

        Browser b = getOrLaunchBrowser();

        try {
            context = b.createBrowserContext();
            page = context.newPage();

            setTimeouts(page, renderTimeoutMs);

            Viewport vp = new Viewport();
            vp.setWidth(viewportWidth);
            vp.setHeight(viewportHeight);
            vp.setDeviceScaleFactor(deviceScaleFactor);
            page.setViewport(vp);

            page.emulateMediaType(MediaType.Print);

            String htmlWithBase = (baseUrl != null && !baseUrl.isBlank())
                    ? html.replaceFirst("(?i)<head>", "<head><base href=\"" + baseUrl + "\">")
                    : html;

            WaitForOptions wait = new WaitForOptions();
            wait.setWaitUntil(List.of(PuppeteerLifeCycle.networkIdle));
            wait.setTimeout(renderTimeoutMs);
            page.setContent(htmlWithBase, wait);

            try {
                page.evaluate("() => { if (window.caches) { return caches.keys().then(ks => Promise.all(ks.map(k => caches.delete(k)))) } return true; }");
            } catch (Throwable ignored) {}

            PDFOptions pdf = new PDFOptions();
            pdf.setPrintBackground(true);
            pdf.setPreferCSSPageSize(true);
            if (landscape != null) pdf.setLandscape(landscape);
            if (width != null && !width.isBlank()) pdf.setWidth(width);
            if (height != null && !height.isBlank()) pdf.setHeight(height);

            byte[] out = page.pdf(pdf);

            if (log.isDebugEnabled()) {
                log.debug("PDF render OK");
            }
            return out;

        } finally {
            try { if (page != null) page.close(); } catch (Throwable t) { log.debug("page.close() error: {}", t.toString()); }
            try { if (context != null) context.close(); } catch (Throwable t) { log.debug("context.close() error: {}", t.toString()); }
        }
    }

    /**
     * Browser launch method to initially start the Browser instance
     * Utilizes many arguments to prevent caching, in turn preventing unintended passive memory bloat
     */
    private Browser getOrLaunchBrowser() throws Exception {
        Browser b = browser;
        if (b != null) return b;
        synchronized (launchLock) {
            if (browser != null) return browser;

            LaunchOptions launch = LaunchOptions.builder().build();
            launch.setHeadless(true);
            launch.setArgs(Arrays.asList(
                    "--incognito",
                    "--no-sandbox",
                    "--disable-setuid-sandbox",
                    "--disable-dev-shm-usage",
                    "--disable-gpu",
                    "--disable-extensions",
                    "--disable-background-networking",
                    "--disable-sync",
                    "--metrics-recording-only",
                    "--mute-audio",
                    "--no-first-run",
                    "--no-default-browser-check",
                    "--hide-scrollbars",
                    "--disk-cache-size=0",
                    "--disable-application-cache"
            ));
            if (chromePath != null && !chromePath.isBlank()) {
                launch.setExecutablePath(chromePath);
            }

            browser = Puppeteer.launch(launch);
            log.info("Launched headless Chrome (jvppeteer)");
            return browser;
        }
    }

    /**
     * Clear the variable and close the browser
     */
    private void safeCloseBrowser() {
        try {
            Browser b = browser;
            browser = null;
            if (b != null) b.close();
        } catch (Throwable t) {
            log.warn("Error closing Chrome: {}", t.toString());
        }
    }

    /**
     * Timeout setters
     */
    private static void setTimeouts(Page page, int timeoutMs) {
        try {
            page.setDefaultNavigationTimeout(timeoutMs);
            page.setDefaultTimeout(timeoutMs);
        } catch (Throwable ignored) {}
    }
}
