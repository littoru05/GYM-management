// E2E Verification of Admin <-> Staff Workflow & Revenue Update
import assert from 'node:assert'

const BASE_URL = 'http://localhost:8080/api'

async function request(endpoint, options = {}) {
  const url = `${BASE_URL}${endpoint}`
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {}),
  }

  const res = await fetch(url, {
    method: options.method || 'GET',
    headers,
    body: options.body ? JSON.stringify(options.body) : undefined,
  })

  const text = await res.text()
  let data
  try {
    data = JSON.parse(text)
  } catch {
    data = text
  }

  return { status: res.status, data }
}

async function runE2E() {
  console.log('=== STARTING E2E INTEGRATION FLOW (ADMIN <-> STAFF) ===\n')

  // 1. ADMIN LOGIN
  console.log('[Step 1] Logging in as Admin (admin@gym.local)...')
  const adminLogin = await request('/auth/login', {
    method: 'POST',
    body: { email: 'admin@gym.local', password: '123456' },
  })
  assert.strictEqual(adminLogin.status, 200, `Admin login failed: ${JSON.stringify(adminLogin.data)}`)
  const adminToken = adminLogin.data.token
  assert.ok(adminToken, 'Admin token must be returned')
  console.log('✓ Admin login successful. Role:', adminLogin.data.role)

  // 2. CHECK INITIAL REVENUE BASELINE (UC14)
  console.log('\n[Step 2] Capturing initial revenue baseline on Admin Dashboard/Reports (UC14)...')
  const initialReports = await request('/admin/reports/revenue?startDate=2026-01-01&endDate=2026-12-31&type=ALL', {
    headers: { Authorization: `Bearer ${adminToken}` },
  })
  assert.strictEqual(initialReports.status, 200, `Fetch initial revenue failed: ${JSON.stringify(initialReports.data)}`)
  const baselineSummary = initialReports.data.summary || {}
  const baselineTotal = Number(baselineSummary.totalRevenue || 0)
  const baselineMembership = Number(baselineSummary.membershipRevenue || 0)
  const baselineRetail = Number(baselineSummary.retailRevenue || 0)
  console.log(`✓ Baseline Revenue: Total=${baselineTotal.toLocaleString()} đ, Gói tập=${baselineMembership.toLocaleString()} đ, Bán lẻ=${baselineRetail.toLocaleString()} đ`)

  // 3. UC11: ADMIN CREATES NEW MEMBERSHIP PACKAGE
  const uniqueSuffix = Date.now().toString().slice(-5)
  const packageName = `Gói Kim Cương E2E ${uniqueSuffix}`
  const packagePrice = 3000000
  console.log(`\n[Step 3] UC11: Admin creates membership package "${packageName}" (Price: ${packagePrice.toLocaleString()} đ, 6 tháng)...`)
  const createPkgRes = await request('/memberships', {
    method: 'POST',
    headers: { Authorization: `Bearer ${adminToken}` },
    body: {
      name: packageName,
      durationMonths: 6,
      price: packagePrice,
      description: 'Gói VIP kiểm thử luồng tích hợp E2E',
      status: 'ACTIVE',
    },
  })
  assert.strictEqual(createPkgRes.status, 201, `Create membership failed: ${JSON.stringify(createPkgRes.data)}`)
  const createdPackage = createPkgRes.data
  assert.ok(createdPackage.id, 'Created package ID must exist')
  console.log(`✓ Package created successfully! ID=${createdPackage.id}, Name="${createdPackage.name}"`)

  // 4. UC13: ADMIN CREATES VOUCHER
  const voucherCode = `E2EVIP${uniqueSuffix}`
  const voucherDiscount = 100000
  const today = new Date().toISOString().slice(0, 10)
  const nextMonth = new Date(Date.now() + 30 * 24 * 3600 * 1000).toISOString().slice(0, 10)
  console.log(`\n[Step 4] UC13: Admin creates Voucher "${voucherCode}" (Giảm ${voucherDiscount.toLocaleString()} đ, 50 lượt)...`)
  const createVoucherRes = await request('/admin/vouchers', {
    method: 'POST',
    headers: { Authorization: `Bearer ${adminToken}` },
    body: {
      code: voucherCode,
      name: `Khuyến mãi E2E ${uniqueSuffix}`,
      description: 'Voucher giảm giá cho kiểm thử E2E',
      discountType: 'FIXED',
      discountValue: voucherDiscount,
      startDate: today,
      endDate: nextMonth,
      usageLimit: 50,
      status: 'ACTIVE',
    },
  })
  assert.strictEqual(createVoucherRes.status, 201, `Create voucher failed: ${JSON.stringify(createVoucherRes.data)}`)
  console.log(`✓ Voucher created successfully! Code="${createVoucherRes.data.code}", Giảm=${createVoucherRes.data.discountValue} đ`)

  // 5. STAFF LOGIN
  console.log('\n[Step 5] Logging in as Staff (staff@gym.local)...')
  const staffLogin = await request('/auth/login', {
    method: 'POST',
    body: { email: 'staff@gym.local', password: '123456' },
  })
  assert.strictEqual(staffLogin.status, 200, `Staff login failed: ${JSON.stringify(staffLogin.data)}`)
  const staffToken = staffLogin.data.token
  assert.ok(staffToken, 'Staff token must be returned')
  console.log('✓ Staff login successful. Role:', staffLogin.data.role)

  // 6. UC07: STAFF REGISTERS MEMBERSHIP PACKAGE WITH VOUCHER
  console.log('\n[Step 6] UC07: Staff registers membership package for Member with Voucher...')
  // 6.1 Get member
  const membersRes = await request('/members', {
    headers: { Authorization: `Bearer ${staffToken}` },
  })
  assert.strictEqual(membersRes.status, 200, 'Fetch members failed')
  const memberList = Array.isArray(membersRes.data) ? membersRes.data : membersRes.data.content || []
  assert.ok(memberList.length > 0, 'Must have at least one member')
  const targetMember = memberList[0]
  console.log(`- Selected Member: [${targetMember.id}] ${targetMember.name} (SĐT: ${targetMember.phone})`)

  // 6.2 Validate voucher application
  const validateVoucherRes = await request('/vouchers/validate', {
    method: 'POST',
    headers: { Authorization: `Bearer ${staffToken}` },
    body: {
      code: voucherCode,
      membershipId: createdPackage.id,
      orderAmount: packagePrice,
      memberId: targetMember.id,
    },
  })
  assert.strictEqual(validateVoucherRes.status, 200, `Validate voucher failed: ${JSON.stringify(validateVoucherRes.data)}`)
  const expectedSubPrice = packagePrice - voucherDiscount
  console.log(`- Voucher "${voucherCode}" applied! Giảm ${validateVoucherRes.data.discountAmount} đ, Giá thanh toán: ${expectedSubPrice.toLocaleString()} đ`)

  // 6.3 Register Subscription
  const subRes = await request('/subscriptions', {
    method: 'POST',
    headers: { Authorization: `Bearer ${staffToken}` },
    body: {
      memberId: targetMember.id,
      membershipId: createdPackage.id,
      voucherCode: voucherCode,
      startDate: today,
      paymentMethod: 'CASH',
      notes: 'Thanh toán tại quầy Staff - E2E verification',
    },
  })
  assert.strictEqual(subRes.status, 201, `Create subscription failed: ${JSON.stringify(subRes.data)}`)
  console.log(`✓ Subscription created successfully! ID=${subRes.data.id}, Member=${targetMember.name}, Package=${createdPackage.name}`)

  // 7. UC09: STAFF SELLS RETAIL PRODUCTS AT POS
  console.log('\n[Step 7] UC09: Staff performs retail POS sale...')
  const productsRes = await request('/products?status=ACTIVE&size=20', {
    headers: { Authorization: `Bearer ${staffToken}` },
  })
  assert.strictEqual(productsRes.status, 200, 'Fetch products failed')
  const productList = Array.isArray(productsRes.data) ? productsRes.data : productsRes.data.content || []
  const availableProduct = productList.find((p) => p.status === 'ACTIVE' && (p.stock || p.stockQuantity || 0) >= 2) || productList[0]
  assert.ok(availableProduct, 'Must have active product available for sale')
  const qty = 2
  const itemUnitPrice = Number(availableProduct.price || 20000)
  const expectedRetailTotal = itemUnitPrice * qty

  console.log(`- Selected Product: [${availableProduct.sku}] ${availableProduct.name} x ${qty} @ ${itemUnitPrice.toLocaleString()} đ = ${expectedRetailTotal.toLocaleString()} đ`)

  const checkoutRes = await request('/pos/checkout', {
    method: 'POST',
    headers: { Authorization: `Bearer ${staffToken}` },
    body: {
      items: [
        {
          productId: availableProduct.id,
          quantity: qty,
        },
      ],
      paymentMethod: 'CASH',
      discountAmount: 0,
      notes: 'POS checkout E2E verification',
    },
  })
  assert.strictEqual(checkoutRes.status, 201, `POS Checkout failed: ${JSON.stringify(checkoutRes.data)}`)
  console.log(`✓ POS Order checked out successfully! Order Code=${checkoutRes.data.code}, Total=${Number(checkoutRes.data.totalAmount).toLocaleString()} đ`)

  // 8. UC14: ADMIN CHECKS REVENUE CHART & REPORT METRICS
  console.log('\n[Step 8] UC14: Admin verifies revenue increment on Reports & Charts...')
  const updatedReports = await request('/admin/reports/revenue?startDate=2026-01-01&endDate=2026-12-31&type=ALL', {
    headers: { Authorization: `Bearer ${adminToken}` },
  })
  assert.strictEqual(updatedReports.status, 200, 'Fetch updated reports failed')
  const updatedSummary = updatedReports.data.summary || {}
  const newTotal = Number(updatedSummary.totalRevenue || 0)
  const newMembership = Number(updatedSummary.membershipRevenue || 0)
  const newRetail = Number(updatedSummary.retailRevenue || 0)

  console.log(`- Updated Revenue: Total=${newTotal.toLocaleString()} đ (tăng +${(newTotal - baselineTotal).toLocaleString()} đ)`)
  console.log(`  * Gói tập: ${newMembership.toLocaleString()} đ (tăng +${(newMembership - baselineMembership).toLocaleString()} đ, kỳ vọng +${expectedSubPrice.toLocaleString()} đ)`)
  console.log(`  * Bán lẻ POS: ${newRetail.toLocaleString()} đ (tăng +${(newRetail - baselineRetail).toLocaleString()} đ, kỳ vọng +${expectedRetailTotal.toLocaleString()} đ)`)

  assert.ok(
    newMembership >= baselineMembership + expectedSubPrice,
    `Membership revenue should increment by at least ${expectedSubPrice}`
  )
  assert.ok(
    newRetail >= baselineRetail + expectedRetailTotal,
    `Retail revenue should increment by at least ${expectedRetailTotal}`
  )
  assert.ok(
    newTotal >= baselineTotal + expectedSubPrice + expectedRetailTotal,
    `Total revenue should increment by at least ${expectedSubPrice + expectedRetailTotal}`
  )

  // 9. CHECK AUDIT TRANSACTION HISTORY
  console.log('\n[Step 9] Checking recent transactions list in ReportController...')
  const txRes = await request('/admin/reports/transactions?page=0&size=5', {
    headers: { Authorization: `Bearer ${adminToken}` },
  })
  if (txRes.status === 200) {
    const txList = txRes.data?.content || txRes.data || []
    console.log(`✓ Recent transactions recorded: ${txList.length} transactions found.`)
  }

  console.log('\n=============================================================')
  console.log('🎉 E2E INTEGRATION FLOW (ADMIN <-> STAFF <-> REVENUE) PASSED 100%!')
  console.log('=============================================================')
}

runE2E().catch((err) => {
  console.error('\n❌ E2E INTEGRATION TEST FAILED:', err)
  process.exit(1)
})
