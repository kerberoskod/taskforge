import { test, expect } from '@playwright/test';

const TEST_USER = {
  name: 'Test User',
  email: `test-${Date.now()}@example.com`,
  password: 'password123',
};

test.describe('Authentication', () => {
  test('should register a new user', async ({ page }) => {
    await page.goto('/register');

    await page.getByLabel('Name').fill(TEST_USER.name);
    await page.getByLabel('Email').fill(TEST_USER.email);
    await page.getByLabel('Password').fill(TEST_USER.password);
    await page.getByRole('button', { name: 'Create Account' }).click();

    await expect(page).toHaveURL('/');
    await expect(page.getByText('Projects')).toBeVisible();
  });

  test('should login with existing credentials', async ({ page }) => {
    await page.goto('/login');

    await page.getByLabel('Email').fill(TEST_USER.email);
    await page.getByLabel('Password').fill(TEST_USER.password);
    await page.getByRole('button', { name: 'Sign In' }).click();

    await expect(page).toHaveURL('/');
    await expect(page.getByText('Projects')).toBeVisible();
  });

  test('should show error on invalid login', async ({ page }) => {
    await page.goto('/login');

    await page.getByLabel('Email').fill('wrong@example.com');
    await page.getByLabel('Password').fill('wrongpass');
    await page.getByRole('button', { name: 'Sign In' }).click();

    await expect(page.getByText('Login failed')).toBeVisible();
  });

  test('should navigate between login and register', async ({ page }) => {
    await page.goto('/login');
    await page.getByText('Register').click();
    await expect(page).toHaveURL('/register');
    await page.getByText('Sign In').click();
    await expect(page).toHaveURL('/login');
  });
});

test.describe('Projects', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.getByLabel('Email').fill(TEST_USER.email);
    await page.getByLabel('Password').fill(TEST_USER.password);
    await page.getByRole('button', { name: 'Sign In' }).click();
    await page.waitForURL('/');
  });

  test('should create a new project', async ({ page }) => {
    await page.getByRole('button', { name: 'New Project' }).click();
    await page.getByLabel('Project Name').fill('Test Project');
    await page.getByRole('button', { name: 'Create' }).click();
    await expect(page.getByText('Test Project')).toBeVisible();
  });

  test('should delete a project', async ({ page }) => {
    await page.getByRole('button', { name: 'New Project' }).click();
    await page.getByLabel('Project Name').fill('To Delete');
    await page.getByRole('button', { name: 'Create' }).click();
    await expect(page.getByText('To Delete')).toBeVisible();

    await page.getByText('Delete').click();
    await expect(page.getByText('To Delete')).not.toBeVisible();
  });
});
