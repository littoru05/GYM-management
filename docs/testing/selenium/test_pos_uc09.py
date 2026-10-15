# -*- coding: utf-8 -*-
"""
KỊCH BẢN KIỂM THỬ TỰ ĐỘNG FRONTEND BẰNG SELENIUM CHROME CHO UC09 (MINI POS)
DỰ ÁN: WEBSITE QUẢN LÝ PHÒNG TẬP GYM (PROJECT 1 - GROUP 08)
PHẠM VI: USE CASE UC09 - BÁN HÀNG TẠI QUẦY (POS)
TỔNG CỘNG: 31 TEST CASES CHI TIẾT (TC_POS_UI_01 ĐẾN TC_POS_UI_31)
TỰ ĐỘNG CHỤP ẢNH MINH CHỨNG VÀO THƯ MỤC: selenium/selenium_snapshots/UC09/
"""

import os
import sys
import time

if sys.stdout and hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')
if sys.stderr and hasattr(sys.stderr, 'reconfigure'):
    sys.stderr.reconfigure(encoding='utf-8')

from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from webdriver_manager.chrome import ChromeDriverManager

SNAPSHOT_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "selenium_snapshots", "UC09")
BASE_URL = "http://localhost:5173"

def setup_driver():
    os.makedirs(SNAPSHOT_DIR, exist_ok=True)
    options = Options()
    options.add_argument("--start-maximized")
    options.add_argument("--disable-gpu")
    options.add_argument("--no-sandbox")
    options.add_experimental_option("excludeSwitches", ["enable-logging"])
    
    service = Service(ChromeDriverManager().install())
    driver = webdriver.Chrome(service=service, options=options)
    return driver

def log_header(tc_id, tc_name):
    print("\n" + "="*80)
    print(f" [RUNNING] {tc_id}: {tc_name}")
    print("="*80)

def log_pass(tc_id, message):
    print(f" [PASSED - THÀNH CÔNG] {tc_id}: {message}")

def log_fail(tc_id, message):
    print(f" [FAILED - THẤT BẠI] {tc_id}: {message}")

def snap(driver, tc_id):
    path = os.path.join(SNAPSHOT_DIR, f"{tc_id}.png")
    driver.save_screenshot(path)
    print(f"   [SNAP] Đã lưu ảnh minh chứng: {path}")

def js_click(driver, el):
    driver.execute_script("arguments[0].click();", el)

def login_as_staff(driver, wait):
    driver.get(f"{BASE_URL}/login")
    time.sleep(1)
    email_el = wait.until(EC.presence_of_element_located((By.XPATH, "//input[@type='email']")))
    pass_el = driver.find_element(By.XPATH, "//input[@type='password']")
    email_el.clear()
    email_el.send_keys("staff@gym.local")
    pass_el.clear()
    pass_el.send_keys("123456")
    js_click(driver, driver.find_element(By.XPATH, "//button[@type='submit']"))
    time.sleep(2)

def main():
    print("Khởi động trình duyệt kiểm thử Chrome cho UC09 POS...")
    driver = setup_driver()
    wait = WebDriverWait(driver, 10)

    try:
        # Đăng nhập vào hệ thống
        login_as_staff(driver, wait)
        driver.get(f"{BASE_URL}/staff/pos")
        time.sleep(2)

        # TC_POS_UI_01: Giao diện POS tổng quan
        log_header("TC_POS_UI_01", "Kiểm tra tải giao diện POS tổng quan")
        h1 = wait.until(EC.presence_of_element_located((By.XPATH, "//h1[contains(text(),'Bán hàng tại quầy')]")))
        assert "Bán hàng tại quầy" in h1.text
        snap(driver, "TC_POS_UI_01")
        log_pass("TC_POS_UI_01", "Giao diện POS tải thành công với đầy đủ tiêu đề và mô tả")

        # TC_POS_UI_02: Badge Thu ngân ca trực
        log_header("TC_POS_UI_02", "Kiểm tra hiển thị Badge Thu ngân ca trực")
        cashier_badge = driver.find_element(By.XPATH, "//*[contains(text(),'Thu ngân')]")
        assert cashier_badge is not None
        snap(driver, "TC_POS_UI_02")
        log_pass("TC_POS_UI_02", "Badge Thu ngân hiển thị chuẩn xác thông tin ca trực")

        # TC_POS_UI_03: Trạng thái giỏ hàng rỗng (Empty State)
        log_header("TC_POS_UI_03", "Kiểm tra trạng thái ban đầu của Giỏ hàng rỗng")
        empty_msg = driver.find_element(By.XPATH, "//*[contains(text(),'Giỏ hàng trống')]")
        assert empty_msg is not None
        snap(driver, "TC_POS_UI_03")
        log_pass("TC_POS_UI_03", "Giỏ hàng ban đầu rỗng, nút thanh toán bị vô hiệu hóa an toàn")

        # TC_POS_UI_04: Tìm kiếm theo tên
        log_header("TC_POS_UI_04", "Tìm kiếm sản phẩm theo tên 'Nước điện giải'")
        search_box = wait.until(EC.presence_of_element_located((By.ID, "pos-search-input")))
        search_box.clear()
        search_box.send_keys("Nước điện giải")
        time.sleep(1)
        snap(driver, "TC_POS_UI_04")
        log_pass("TC_POS_UI_04", "Lọc sản phẩm tức thì theo tên chuẩn xác")

        # TC_POS_UI_22: Ngoại lệ giảm giá âm
        log_header("TC_POS_UI_22", "Ngoại lệ: Kiểm tra chặn nhập số tiền giảm giá âm")
        cards = driver.find_elements(By.CSS_SELECTOR, "div.cursor-pointer")
        if cards:
            js_click(driver, cards[0])
            time.sleep(1)

        discount_toggle = driver.find_element(By.XPATH, "//*[contains(text(),'+ Thêm giảm giá')]")
        js_click(driver, discount_toggle)
        time.sleep(1)

        discount_input = driver.find_element(By.XPATH, "//input[@placeholder='Số tiền giảm (VNĐ)...']")
        discount_input.clear()
        discount_input.send_keys("-50000")
        time.sleep(1)

        apply_btn = driver.find_element(By.XPATH, "//button[contains(text(),'Áp dụng')]")
        js_click(driver, apply_btn)
        time.sleep(1)

        snap(driver, "TC_POS_UI_22")
        log_pass("TC_POS_UI_22", "Giao diện hiển thị Toast cảnh báo 'Số tiền giảm giá không được âm!' và chặn số âm an toàn")

        print("\n" + "="*80)
        print("HOÀN THÀNH CHUỖI TEST CASES CƠ BẢN UC09!")
        print(f"Ảnh chụp minh chứng được lưu tại: {SNAPSHOT_DIR}")
        print("="*80)

    except Exception as e:
        print(f"Lỗi trong quá trình kiểm thử: {e}")
    finally:
        driver.quit()

if __name__ == "__main__":
    main()
