import {test,expect} from '@playwright/test';
import {readFile} from 'node:fs/promises';
test('public catalog and guest registration use real Spring APIs',async({page})=>{
  await page.goto('/');await expect(page.getByRole('heading',{name:'Học điều mới.' ,exact:false})).toBeVisible();
  await page.getByRole('link',{name:'Khám phá khóa học',exact:false}).first().click();
  await expect(page.getByRole('link').filter({has:page.getByRole('heading',{name:'[DEMO] Java Spring Boot từ nền tảng'})})).toBeVisible();
  await page.getByRole('heading',{name:'[DEMO] Java Spring Boot từ nền tảng'}).click();
  await page.getByLabel('Họ và tên').fill('BROWSER TEST Guest');await page.getByLabel('Email',{exact:true}).fill('browser-guest@example.test');await page.getByLabel('Số điện thoại').fill('0900000000');
  await page.getByRole('button',{name:'Đăng ký khóa học'}).click();await expect(page).toHaveURL(/registrations\/\d+/);await expect(page.getByText('Submitted',{exact:true})).toBeVisible();
  await page.getByRole('button',{name:'Thanh toán VNPAY sandbox'}).click();await expect(page.getByText(/Chưa cấu hình VNPAY sandbox/)).toBeVisible();await expect(page.getByText('Submitted',{exact:true})).toBeVisible();
});
test('admin login, dashboard and course editor',async({page})=>{
  await page.goto('/login');await page.getByLabel('Email',{exact:true}).fill('admin@onlinelearn.test');await page.getByLabel('Mật khẩu',{exact:true}).fill('BrowserTestOnly!2026');await page.getByRole('button',{name:'Tiếp tục'}).click();
  await page.getByRole('link',{name:'Workspace',exact:true}).click();await expect(page.getByRole('heading',{name:'Tổng quan',exact:true})).toBeVisible();
  await page.locator('.management-nav').getByRole('link',{name:'Khóa học',exact:true}).click();await page.getByRole('row').filter({hasText:'[DEMO] Java Spring Boot từ nền tảng'}).getByRole('link',{name:'Quản lý nội dung'}).click();
  await expect(page.getByRole('heading',{name:'[DEMO] Java Spring Boot từ nền tảng'})).toBeVisible();
  await page.getByRole('link',{name:'Ngân hàng câu hỏi & Quiz'}).click();await expect(page.getByRole('heading',{name:'Ngân hàng câu hỏi & Quiz'})).toBeVisible();
  const downloadEvent=page.waitForEvent('download');await page.getByRole('button',{name:'Tải mẫu câu hỏi JSON'}).click();const downloaded=await downloadEvent;
  const imported=JSON.parse(await readFile(await downloaded.path(),'utf8'));imported.questions[0].content='BROWSER TEST imported question '+Date.now();
  await page.locator('input[type=file]').setInputFiles({name:'question-test.json',mimeType:'application/json',buffer:Buffer.from(JSON.stringify(imported))});
  await expect(page.getByText(imported.questions[0].content,{exact:false})).toBeVisible();
});
test('mobile layout and navigation',async({page})=>{
  await page.setViewportSize({width:390,height:844});await page.goto('/');await expect(page.getByRole('heading',{name:'Học điều mới.',exact:false})).toBeVisible();
  expect(await page.evaluate(()=>document.documentElement.scrollWidth<=window.innerWidth)).toBe(true);
  await page.getByRole('button',{name:'Mở menu'}).click();await page.locator('nav').getByRole('link',{name:'Khóa học',exact:true}).click();await expect(page).toHaveURL(/courses/);
});
test('admin creates a course and assigned author adds a lesson through forms',async({page})=>{
  const name='BROWSER TEST Course '+Date.now();
  await page.goto('/login');await page.getByLabel('Email',{exact:true}).fill('admin@onlinelearn.test');await page.getByLabel('Mật khẩu',{exact:true}).fill('BrowserTestOnly!2026');await page.getByRole('button',{name:'Tiếp tục'}).click();
  await page.getByRole('link',{name:'Workspace',exact:true}).click();await page.locator('.management-nav').getByRole('link',{name:'Khóa học',exact:true}).click();
  await page.getByRole('button',{name:'Tạo mới'}).click();const modal=page.getByRole('dialog');
  await modal.getByLabel('Tên khóa học').fill(name);await modal.getByLabel('Author phụ trách').selectOption({label:'[DEMO] Author'});await modal.getByLabel('Danh mục').selectOption({label:'Lập trình'});await modal.getByLabel('Trạng thái').selectOption('Published');
  await modal.getByRole('button',{name:'Lưu thay đổi'}).click();await expect(modal).not.toBeVisible();await expect(page.getByRole('row').filter({hasText:name})).toBeVisible();
  await page.getByRole('button',{name:'Đăng xuất',exact:true}).click();await page.goto('/login');await page.getByLabel('Email',{exact:true}).fill('author@onlinelearn.test');await page.getByLabel('Mật khẩu',{exact:true}).fill('BrowserTestOnly!2026');await page.getByRole('button',{name:'Tiếp tục'}).click();
  await page.getByRole('link',{name:'Workspace',exact:true}).click();await page.getByRole('row').filter({hasText:name}).getByRole('link',{name:'Quản lý nội dung'}).click();
  await page.getByRole('button',{name:'Thêm mới'}).click();const lessonModal=page.getByRole('dialog');await lessonModal.getByLabel('Tên bài học').fill('BROWSER TEST Lesson');await lessonModal.getByRole('combobox',{name:'Loại',exact:true}).selectOption('Lesson');await lessonModal.getByLabel('Thứ tự').fill('1');await lessonModal.getByLabel('Nội dung HTML').fill('<p>Lesson created through the real Author UI.</p>');await lessonModal.getByRole('button',{name:'Lưu thay đổi'}).click();await expect(lessonModal).not.toBeVisible();await expect(page.getByText('BROWSER TEST Lesson',{exact:false})).toBeVisible();
});
