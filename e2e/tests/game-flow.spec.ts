import { test, expect, Page, TestInfo } from '@playwright/test';

// Device settings per player — different viewports give us layout screenshots
// across breakpoints in a single test run
const DEVICES = {
  creator: {
    viewport: { width: 375, height: 667 },
    deviceScaleFactor: 2,
    isMobile: true,
    userAgent:
      'Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.0 Mobile/15E148 Safari/604.1',
  },
  player2: {
    viewport: { width: 393, height: 851 },
    deviceScaleFactor: 2,
    isMobile: true,
    userAgent:
      'Mozilla/5.0 (Linux; Android 12; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.0.0 Mobile Safari/537.36',
  },
  player3: {
    viewport: { width: 1280, height: 800 },
    deviceScaleFactor: 1,
  },
};

async function ss(page: Page, testInfo: TestInfo, label: string) {
  const buf = await page.screenshot();
  await testInfo.attach(label, { body: buf, contentType: 'image/png' });
}

// The shroud element is always in the DOM. Elm adds class "hidden" when not loading.
async function waitForShroudGone(page: Page) {
  await expect(page.locator('.shroud')).toHaveClass(/hidden/, { timeout: 15_000 });
}

// Spectating poll interval is 15s; allow two cycles
async function waitForSpectating(page: Page) {
  await expect(page.locator('.container.spectating')).toBeVisible({ timeout: 30_000 });
}

// Click two distinct enabled hand cards to build a pitch.
// force:true bypasses Playwright's pointer-event hit-test: the logo container
// (#logo-container, z-index:10, position:absolute) overflows the nav and can
// sit above right-column word buttons, causing the hit-test to spin forever.
async function selectTwoWords(page: Page) {
  const enabledCards = page.locator('.hand__card button:not([disabled])');
  await enabledCards.nth(0).click({ force: true });
  await enabledCards.nth(1).click({ force: true });
}

test('complete game flow', async ({ browser }, testInfo) => {
  const baseURL = testInfo.project.use.baseURL;
  const creatorCtx = await browser.newContext({ ...DEVICES.creator, baseURL });
  const player2Ctx = await browser.newContext({ ...DEVICES.player2, baseURL });
  const player3Ctx = await browser.newContext({ ...DEVICES.player3, baseURL });
  const [cp, p2, p3] = [
    await creatorCtx.newPage(),
    await player2Ctx.newPage(),
    await player3Ctx.newPage(),
  ];

  try {
    // ── WELCOME ──────────────────────────────────────────────────────────────
    await Promise.all([cp.goto('/'), p2.goto('/'), p3.goto('/')]);
    // Wait for the "backendAwake" ping to complete before any interaction
    await waitForShroudGone(cp);
    await Promise.all([waitForShroudGone(p2), waitForShroudGone(p3)]);
    await Promise.all([
      ss(cp, testInfo, 'welcome — creator (iPhone SE)'),
      ss(p2, testInfo, 'welcome — player2 (Pixel 5)'),
      ss(p3, testInfo, 'welcome — player3 (desktop)'),
    ]);

    // ── CREATE GAME ───────────────────────────────────────────────────────────
    await cp.getByRole('button', { name: 'Create game' }).click();
    await cp.fill('input#create-game-id', 'TestGame');
    await cp.fill('input#player-name-id', 'Creator');
    await ss(cp, testInfo, 'create game form');
    await cp.getByRole('button', { name: 'Create game' }).click();
    await waitForShroudGone(cp);
    await expect(cp.locator('input#game-code')).toBeVisible({ timeout: 10_000 });
    const gameCode = await cp.locator('input#game-code').inputValue();
    await ss(cp, testInfo, `creator waiting — game code: ${gameCode}`);

    // ── JOIN GAME ─────────────────────────────────────────────────────────────
    await Promise.all([
      (async () => {
        await p2.getByRole('button', { name: 'Join game' }).click();
        await p2.fill('input#game-code-id', gameCode);
        await p2.fill('input#player-name-id', 'Player2');
        await ss(p2, testInfo, 'join form — player2');
        await p2.getByRole('button', { name: 'Join game' }).click();
        await waitForShroudGone(p2);
      })(),
      (async () => {
        await p3.getByRole('button', { name: 'Join game' }).click();
        await p3.fill('input#game-code-id', gameCode);
        await p3.fill('input#player-name-id', 'Player3');
        await ss(p3, testInfo, 'join form — player3');
        await p3.getByRole('button', { name: 'Join game' }).click();
        await waitForShroudGone(p3);
      })(),
    ]);
    await ss(p2, testInfo, 'waiting — player2');
    await ss(p3, testInfo, 'waiting — player3');

    // ── START GAME ────────────────────────────────────────────────────────────
    // Lobby pings every 5s; wait for "You" + Player2 + Player3 in the list (indices 0..2)
    await expect(cp.locator('ul.collection.with-header .collection-item').nth(2))
      .toBeVisible({ timeout: 20_000 });
    await ss(cp, testInfo, 'creator waiting — both players in lobby');
    await cp.getByRole('button', { name: 'Start game' }).click();
    await waitForShroudGone(cp);
    await waitForSpectating(cp);
    await Promise.all([waitForSpectating(p2), waitForSpectating(p3)]);
    await Promise.all([
      ss(cp, testInfo, 'spectating — creator'),
      ss(p2, testInfo, 'spectating — player2'),
      ss(p3, testInfo, 'spectating — player3'),
    ]);

    // ── ROUND 1: player2 buys, creator + player3 pitch ───────────────────────

    // ── BECOME BUYER ─────────────────────────────────────────────────────────
    await p2.getByRole('button', { name: 'Buyer' }).click();
    await waitForShroudGone(p2);
    await expect(p2.locator('span.buyer-role__text')).toBeVisible({ timeout: 10_000 });
    const role = await p2.locator('span.buyer-role__text').innerText();
    await ss(p2, testInfo, `player2 is buyer — role: "${role}"`);

    // ── CREATOR PITCHES ───────────────────────────────────────────────────────
    await selectTwoWords(cp);
    await ss(cp, testInfo, 'creator selected 2 words');
    await cp.getByRole('button', { name: 'Start pitch' }).click();
    await expect(cp.locator('.container.pitching')).toBeVisible({ timeout: 10_000 });
    await ss(cp, testInfo, 'creator pitching');
    await cp.getByRole('button', { name: 'Finish pitch' }).click();
    await waitForShroudGone(cp);
    await waitForSpectating(cp);
    await ss(cp, testInfo, 'creator finished pitch');

    // ── PLAYER3 PITCHES ───────────────────────────────────────────────────────
    await selectTwoWords(p3);
    await ss(p3, testInfo, 'player3 selected 2 words');
    await p3.getByRole('button', { name: 'Start pitch' }).click();
    await expect(p3.locator('.container.pitching')).toBeVisible({ timeout: 10_000 });
    await ss(p3, testInfo, 'player3 pitching');
    await p3.getByRole('button', { name: 'Finish pitch' }).click();
    await waitForShroudGone(p3);
    await waitForSpectating(p3);
    await ss(p3, testInfo, 'player3 finished pitch');

    // ── AWARD POINT ──────────────────────────────────────────────────────────
    // Both award buttons must be enabled before the buyer can choose
    // (buyer's 5s poll returns updated products after each pitch)
    await expect(p2.locator('button.award-winner__button').nth(1))
      .toBeEnabled({ timeout: 30_000 });
    await ss(p2, testInfo, 'player2 can award');
    await p2.locator('button.award-winner__button').first().click();
    await waitForShroudGone(p2);
    await waitForSpectating(p2);
    await ss(p2, testInfo, 'player2 awarded point');

    // ── VERIFY SCORES ────────────────────────────────────────────────────────
    await waitForSpectating(cp);
    await waitForSpectating(p3);
    // At least one player now has a non-zero score
    await expect(cp.locator('ul.collection .badge').filter({ hasText: /^[1-9]/ }))
      .toBeVisible({ timeout: 30_000 });
    await Promise.all([
      ss(cp, testInfo, 'scores — creator view'),
      ss(p2, testInfo, 'scores — player2 view'),
      ss(p3, testInfo, 'scores — player3 view'),
    ]);
  } finally {
    await creatorCtx.close();
    await player2Ctx.close();
    await player3Ctx.close();
  }
});
