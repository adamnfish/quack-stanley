import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  outputDir: './test-results',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  reporter: [
    ['html', { outputFolder: 'playwright-report', open: 'never' }],
    ['list'],
  ],
  timeout: 3 * 60 * 1000,
  use: {
    baseURL: 'http://localhost:3000',
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure',
    reducedMotion: 'reduce',
  },
  projects: [
    { name: 'game-flow', use: {} },
  ],
  webServer: [
    {
      command: 'npx serve ../frontend/dist -p 3000 -s',
      url: 'http://localhost:3000',
      reuseExistingServer: !process.env.CI,
      stdout: 'ignore',
      stderr: 'pipe',
    },
    {
      command: 'sbt devServer/run',
      url: 'http://localhost:9001/healthcheck',
      reuseExistingServer: !process.env.CI,
      timeout: 30_000,
      cwd: '..',
    },
  ],
});
