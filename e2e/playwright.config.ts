import { defineConfig } from '@playwright/test';
import path from 'path';

const baseURL = process.env.E2E_BASE_URL ?? 'http://localhost:3000';

export default defineConfig({
  testDir: path.join(__dirname, 'tests'),
  outputDir: path.join(__dirname, 'test-results'),
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  reporter: [
    ['html', { outputFolder: path.join(__dirname, 'playwright-report'), open: 'never' }],
    ['list'],
  ],
  timeout: 3 * 60 * 1000,
  use: {
    baseURL,
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure',
    reducedMotion: 'reduce',
  },
  projects: [
    { name: 'game-flow', use: {} },
  ],
  webServer: process.env.E2E_BASE_URL ? undefined : [
    {
      command: 'npm run build --prefix frontend && npx serve frontend/dist -p 3000 -s',
      url: 'http://localhost:3000',
      reuseExistingServer: !process.env.CI,
      stdout: 'ignore',
      stderr: 'pipe',
      cwd: path.join(__dirname, '..'),
      env: { CI: 'true' },
    },
    {
      command: 'sbt devServer/run',
      url: 'http://localhost:9001/healthcheck',
      reuseExistingServer: !process.env.CI,
      timeout: 30_000,
      cwd: path.join(__dirname, '..'),
    },
  ],
});
