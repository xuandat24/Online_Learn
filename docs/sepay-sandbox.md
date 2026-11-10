# SePay sandbox on Windows

Fill the root `.env.local` (already ignored by Git):

```properties
SEPAY_MERCHANT_ID=
SEPAY_SECRET_KEY=
SEPAY_IPN_SECRET_KEY=
SEPAY_CHECKOUT_URL=https://pay-sandbox.sepay.vn/v1/checkout/init
SEPAY_API_URL=https://pgapi-sandbox.sepay.vn
PUBLIC_BACKEND_URL=https://YOUR-TUNNEL.trycloudflare.com
FRONTEND_URL=http://127.0.0.1:5173
```

Merchant credentials must come from SePay Test Mode. The IPN key is a separate value configured in SePay's IPN settings. Never put either key in a `VITE_*` variable or send it to the browser. Local Spring profile imports `.env.local`; the backend launcher also loads it. Frontend receives only signed checkout fields.

1. Install Cloudflare's `cloudflared` CLI if it is not installed. Run `cloudflared tunnel --url http://127.0.0.1:8801` and copy the HTTPS URL into `PUBLIC_BACKEND_URL`. Quick Tunnel URLs change on restart. Do not expose SQL Server.
2. Restart Backend with `.\scripts\start-backend.ps1`; run Frontend with `.\scripts\start-frontend.ps1`, or `npm run dev` from `frontend`.
3. In SePay **Test Mode**, enable BANK_TRANSFER. Set IPN URL to `https://YOUR-TUNNEL.trycloudflare.com/api/payments/sepay/ipn`, auth type `SECRET_KEY`, and the same `SEPAY_IPN_SECRET_KEY`.
4. Checkout form includes Backend `/api/payments/sepay/success`, `/error`, `/cancel` callbacks. Each redirects to `FRONTEND_URL/payment/return`, without updating payment records. Keep the browser on the same Frontend origin so the guest registration token in sessionStorage is available.
5. Register a course using clearly labeled test data, open the SePay checkout and use its sandbox simulator. Observe the authenticated IPN, Paid registration, one access record and protected lesson access. Retry the same IPN to check idempotency. Never transfer real money for this test.

SePay checkout uses HMAC-SHA256 over the documented ordered field list and Base64 of raw digest bytes. VND is an integer number of dong, without multiplying by 100. `lang` is not signed. Zero and empty signed values are omitted.

IPN accepts only authenticated ORDER_PAID + CAPTURED + APPROVED/PAYMENT with matching invoice, both amounts and currencies. The stable SePay transaction UUID (`transaction.id`) is stored with a SEPAY prefix; both IPN and REST detail responses contain this UUID. Unknown/void events never grant access or revoke an already paid course. Timestamp tolerance defaults to 24 hours (`SEPAY_IPN_MAX_AGE_SECONDS`); older deliveries require a Sale/Admin REST recheck instead. Repeated valid deliveries are acknowledged without creating another access.

Sale/Admin's recheck uses Basic Auth against the sandbox order detail API by invoice. CAPTURED with an APPROVED payment can grant access through the same transaction logic; CANCELLED can close a pending payment. Unrecognized, declined-attempt or unavailable results stay Pending for review. Browser error/cancel callbacks do not decide financial state. Failed legacy transactions remain visible; pending VNPAY orders must be verified with the old provider before another checkout can be created. Existing VNPAY callback support is retained only for old records.

No migration is required: existing unique order, provider-reference, pending-registration and access-registration indexes are reused. Tests use isolated H2 fixtures and test-only secrets; passing tests do not establish a real SePay transaction.

Official references:
- https://developer.sepay.vn/vi/cong-thanh-toan/sandbox
- https://developer.sepay.vn/vi/cong-thanh-toan/API/don-hang/form-thanh-toan
- https://developer.sepay.vn/vi/cong-thanh-toan/IPN
- https://developer.sepay.vn/vi/cong-thanh-toan/API/don-hang/chi-tiet-don-hang
