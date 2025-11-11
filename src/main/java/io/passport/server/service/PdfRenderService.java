package io.passport.server.service;

import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.cdp.core.Puppeteer;
import com.ruiyun.jvppeteer.cdp.entities.LaunchOptions;
import com.ruiyun.jvppeteer.cdp.entities.PDFOptions;
import com.ruiyun.jvppeteer.cdp.entities.Viewport;
import com.ruiyun.jvppeteer.cdp.entities.WaitForOptions;
import com.ruiyun.jvppeteer.common.MediaType;
import com.ruiyun.jvppeteer.common.PuppeteerLifeCycle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * PDF rendering service from HTML to generate Passport PDF files and sign them later
 */
@Service
public class PdfRenderService {

    @Value("${pdf.chrome.executablePath:}")
    private String chromePath;

    @Value("${pdf.page.width:1600}")
    private int viewportWidth;

    @Value("${pdf.page.height:1200}")
    private int viewportHeight;

    @Value("${pdf.page.dsf:2}")
    private Double deviceScaleFactor;

    /**
     * HTML rendering method to generate a Passport PDF
     * @param html Full HTML content string
     * @param baseUrl URL for Puppeteer to retrieve styling
     * @param width Page width
     * @param height Page height
     * @param landscape Whether to print in landscape orientation
     * @return Generated PDF in byte array form
     */
    public byte[] render(String html, String baseUrl, String width, String height, Boolean landscape) throws Exception {
        LaunchOptions launch = LaunchOptions.builder().build();
        launch.setHeadless(true);
        launch.setArgs(Arrays.asList(
                "--no-sandbox", "--disable-setuid-sandbox",
                "--disable-dev-shm-usage", "--font-render-hinting=medium"
        ));

        // Using existing Chrome Browser if a path to it is provided
        // Not used in our use case, but remains just in case
        if (chromePath != null && !chromePath.isBlank()) {
            launch.setExecutablePath(chromePath);
        }

        Browser browser = null;
        try {
            browser = Puppeteer.launch(launch);
            Page page = browser.newPage();

            page.setViewport(new Viewport());
            page.viewport().setHeight(viewportHeight);
            page.viewport().setWidth(viewportWidth);
            page.viewport().setDeviceScaleFactor(deviceScaleFactor);

            page.emulateMediaType(MediaType.Screen);

            String htmlWithBase = (baseUrl != null && !baseUrl.isBlank())
                    ? html.replaceFirst("(?i)<head>", "<head><base href=\"" + baseUrl + "\">")
                    : html;

            WaitForOptions waitMe = new WaitForOptions();
            waitMe.setWaitUntil(List.of(PuppeteerLifeCycle.networkIdle));
            page.setContent(htmlWithBase, waitMe);

            PDFOptions pdf = new PDFOptions();
            pdf.setPrintBackground(true);
            if (landscape != null) pdf.setLandscape(landscape);
            if (width != null && !width.isBlank()) pdf.setWidth(width);
            if (height != null && !height.isBlank()) pdf.setHeight(height);

            return page.pdf(pdf);
        } finally {
            if (browser != null) browser.close();
        }
    }
}
