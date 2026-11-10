// Real local stack smoke check. Creates one clearly labelled Submitted test registration.
// Node is only a browser-test runner; the application backend remains Spring Boot.
import {readFile} from 'node:fs/promises';
import {createRequire} from 'node:module';
import {fileURLToPath} from 'node:url';
import path from 'node:path';
import os from 'node:os';
import assert from 'node:assert/strict';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const env=Object.fromEntries((await readFile(path.join(root,'.env.local'),'utf8')).split(/\r?\n/).filter(l=>/^[A-Z_]+=/.test(l)).map(l=>{const i=l.indexOf('=');return [l.slice(0,i),l.slice(i+1)]}));
const require=createRequire(path.join(os.tmpdir(),'onlinelearn-build','frontend','package.json'));
const {chromium}=require('playwright');
const base='http://127.0.0.1:5173';
const health=await fetch('http://127.0.0.1:8801/actuator/health');assert.equal(health.status,200);assert.equal((await health.json()).status,'UP');console.log('PASS Backend health UP (SQL datasource enabled)');
const catalog=await fetch(base+'/api/public/courses');assert.equal(catalog.status,200);const courses=await catalog.json();assert.ok(courses.content.length>0);console.log('PASS Frontend proxy -> Spring catalog API');
const demo=courses.content.find(c=>c.name.startsWith('[DEMO]'));assert.ok(demo,'Enable DEMO_SEED=true for this explicitly labelled test');
const browser=await chromium.launch({executablePath:process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE||'C:/Program Files/Google/Chrome/Application/chrome.exe',headless:true});
try{
  const context=await browser.newContext();const page=await context.newPage();await page.goto(base);await page.getByRole('heading',{name:demo.name}).waitFor();console.log('PASS Browser displays course fetched from SQL-backed API');
  await page.goto(base+'/login');await page.getByLabel('Email',{exact:true}).fill('customer@onlinelearn.test');await page.getByLabel('Mật khẩu',{exact:true}).fill(env.DEMO_PASSWORD);await page.getByRole('button',{name:'Tiếp tục'}).click();await page.waitForURL('**/my-courses');console.log('PASS Customer login through real frontend form');
  const token=await page.evaluate(()=>sessionStorage.getItem('onlinelearn.token'));assert.ok(token);
  const detail=await (await fetch(base+'/api/public/courses/'+demo.subjectId)).json();const lesson=detail.lessons.find(l=>l.lessonType==='Lesson');assert.ok(lesson);
  const denied=await fetch(base+'/api/learning/lessons/'+lesson.lessonId,{headers:{Authorization:'Bearer '+token}});assert.equal(denied.status,403);console.log('PASS Unpaid customer denied protected lesson (HTTP 403)');
  const guestContext=await browser.newContext();const guest=await guestContext.newPage();await guest.goto(base+'/courses/'+demo.subjectId);
  await guest.getByLabel('Họ và tên').fill('[SMOKE TEST] Guest SQL verification');await guest.getByLabel('Email',{exact:true}).fill('customer@onlinelearn.test');await guest.getByLabel('Số điện thoại').fill('0900000000');await guest.getByRole('button',{name:'Đăng ký khóa học'}).click();await guest.waitForURL('**/registrations/*');await guest.getByText('Submitted',{exact:true}).waitFor();
  const id=Number(new URL(guest.url()).pathname.split('/').at(-1));console.log('PASS Guest registration persisted as Submitted; registrationId='+id);
  if(!env.VNPAY_TMN_CODE||!env.VNPAY_HASH_SECRET){
    const paymentResponse=guest.waitForResponse(r=>r.url().endsWith('/registrations/'+id+'/payments')&&r.request().method()==='POST');await guest.getByRole('button',{name:'Thanh toán VNPAY sandbox'}).click();assert.equal((await paymentResponse).status(),503);await guest.getByText(/Chưa cấu hình VNPAY sandbox/).waitFor();
    console.log('PASS Missing VNPAY credentials rejected safely (HTTP 503), registration remains Submitted');
  }else console.log('VNPAY credentials present: complete checkout manually with the provider sandbox test card');
  await page.screenshot({path:path.join(os.tmpdir(),'onlinelearn-build','customer-sql-smoke.png'),fullPage:true});
  console.log('SMOKE PASSED: real React + Spring Boot + SQL Server; no successful payment fabricated');
}finally{await browser.close();}
