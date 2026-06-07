import { chromium } from 'playwright';

const BASE = process.env.BASE_URL || 'http://localhost:5173';
const MOBILE_VIEWPORT = { width: 375, height: 812 };

async function testPage(page, url, name) {
  console.log(`\n--- Testing ${name} ---`);
  await page.setViewportSize(MOBILE_VIEWPORT);
  await page.goto(url, { waitUntil: 'networkidle', timeout: 15000 }).catch(() => {});

  await page.screenshot({ path: `screenshots/${name}.png`, fullPage: true });
  console.log(`  Screenshot saved: screenshots/${name}.png`);

  const overflowEl = await page.locator('html').evaluate((el) => {
    const w = el.scrollWidth;
    const vw = el.clientWidth;
    return w > vw + 1 ? { scrollWidth: w, viewportWidth: vw } : null;
  });
  if (overflowEl) {
    console.log(`  WARNING: Horizontal overflow detected (scrollWidth=${overflowEl.scrollWidth}, viewportWidth=${overflowEl.viewportWidth})`);
  } else {
    console.log(`  OK: No horizontal overflow`);
  }

  const body = page.locator('body');
  const bodyBox = await body.boundingBox();
  const html = page.locator('html');
  const htmlHeight = await html.evaluate((el) => el.scrollHeight);
  if (bodyBox && htmlHeight > bodyBox.height * 1.5) {
    console.log(`  INFO: Page scrolls vertically (${Math.round(htmlHeight / bodyBox.height)} screens)`);
  }
}

(async () => {
  const browser = await chromium.launch({ headless: true });

  try {
    const context = await browser.newContext({ viewport: MOBILE_VIEWPORT });
    const page = await context.newPage();

    await testPage(page, `${BASE}/login`, 'LoginPage');
    await testPage(page, `${BASE}/register`, 'RegisterPage');

    const dashboardOk = await page.goto(`${BASE}/`, { waitUntil: 'networkidle', timeout: 10000 })
      .then(() => true)
      .catch(() => false);

    if (dashboardOk) {
      await page.screenshot({ path: 'screenshots/DashboardPage.png', fullPage: true });
      console.log(`\n  Dashboard screenshot saved`);
    } else {
      console.log(`\n  Skipping Dashboard/Board (likely needs auth)`);
    }

    await context.close();
  } catch (err) {
    console.error('Error:', err.message);
  }

  await browser.close();
  console.log('\nDone.');
})();
