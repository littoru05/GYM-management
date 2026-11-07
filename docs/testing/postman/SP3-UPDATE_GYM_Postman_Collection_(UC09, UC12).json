{
  "info": {
    "_postman_id": "e9adb2b1-c2e7-4143-b4f2-ce00b8f4ca28",
    "name": "GYM Management System - SP3-update Postman Collection (UC12, UC09)",
    "description": "Bộ kịch bản kiểm thử API tự động cho KAN-287: Quản lý ảnh sản phẩm (Admin UC12) và Bán hàng lẻ POS (Staff UC09) bao gồm trọn bộ luồng chính & ngoại lệ.",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
    "_exporter_id": "53839025",
    "_collection_link": "https://go.postman.co/collection/53839025-e9adb2b1-c2e7-4143-b4f2-ce00b8f4ca28?source=collection_link"
  },
  "item": [
    {
      "name": "0. Xác thực & Khởi tạo Token (Pre-auth Setup)",
      "item": [
        {
          "name": "0. Admin Login (Lay Token quan tri)",
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"Status code is 200 OK\", function () {",
                  "    pm.response.to.have.status(200);",
                  "});",
                  "var res = pm.response.json();",
                  "pm.test(\"Tra ve token JWT hop le\", function () {",
                  "    pm.expect(res.token).to.be.a('string');",
                  "});",
                  "pm.environment.set(\"admin_token\", res.token);"
                ]
              }
            }
          ],
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"email\": \"admin@gym.local\",\n  \"password\": \"123456\"\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            },
            "url": {
              "raw": "{{base_url}}/api/auth/login",
              "host": [
                "{{base_url}}"
              ],
              "path": [
                "api",
                "auth",
                "login"
              ]
            }
          },
          "response": []
        },
        {
          "name": "0. Staff Login (Lay Token nhan vien quay)",
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"Status code is 200 OK\", function () {",
                  "    pm.response.to.have.status(200);",
                  "});",
                  "var res = pm.response.json();",
                  "pm.test(\"Tra ve token JWT hop le\", function () {",
                  "    pm.expect(res.token).to.be.a('string');",
                  "});",
                  "pm.environment.set(\"staff_token\", res.token);"
                ]
              }
            }
          ],
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"email\": \"staff@gym.local\",\n  \"password\": \"123456\"\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            },
            "url": {
              "raw": "{{base_url}}/api/auth/login",
              "host": [
                "{{base_url}}"
              ],
              "path": [
                "api",
                "auth",
                "login"
              ]
            }
          },
          "response": []
        }
      ]
    },
    {
      "name": "0. Xác thực & Khởi tạo Token (Pre-auth Setup) Copy",
      "item": [
        {
          "name": "0. Admin Login (Lay Token quan tri)",
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"Status code is 200 OK\", function () {",
                  "    pm.response.to.have.status(200);",
                  "});",
                  "var res = pm.response.json();",
                  "pm.test(\"Tra ve token JWT hop le\", function () {",
                  "    pm.expect(res.token).to.be.a('string');",
                  "});",
                  "pm.environment.set(\"admin_token\", res.token);"
                ]
              }
            }
          ],
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"email\": \"admin@gym.local\",\n  \"password\": \"123456\"\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            },
            "url": {
              "raw": "{{base_url}}/api/auth/login",
              "host": [
                "{{base_url}}"
              ],
              "path": [
                "api",
                "auth",
                "login"
              ]
            }
          },
          "response": []
        },
        {
          "name": "0. Staff Login (Lay Token nhan vien quay)",
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"Status code is 200 OK\", function () {",
                  "    pm.response.to.have.status(200);",
                  "});",
                  "var res = pm.response.json();",
                  "pm.test(\"Tra ve token JWT hop le\", function () {",
                  "    pm.expect(res.token).to.be.a('string');",
                  "});",
                  "pm.environment.set(\"staff_token\", res.token);"
                ]
              }
            }
          ],
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"email\": \"staff@gym.local\",\n  \"password\": \"123456\"\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            },
            "url": {
              "raw": "{{base_url}}/api/auth/login",
              "host": [
                "{{base_url}}"
              ],
              "path": [
                "api",
                "auth",
                "login"
              ]
            }
          },
          "response": []
        }
      ]
    },
    {
      "name": "1. Kịch bản kiểm thử API KAN-287 (Mục 3.4.12 & 3.4.9 SRS)",
      "item": [
        {
          "name": "TC249 - POST Them San Pham UC12 | 201 - Admin them moi san pham kem link anh hop le (image URL HTTPS)",
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"Status code is 201 Created\", function () {\n    pm.response.to.have.status(201);\n});\nvar res = pm.response.json();\npm.test(\"Tra ve ProductDto day du thong tin va link anh\", function () {\n    pm.expect(res.sku).to.eql(\"SUP-WHEY-TC252-01\");\n    pm.expect(res.imageUrl).to.include(\"photo-1593095948071\");\n});\nif (res.id) {\n    pm.environment.set(\"created_product_id\", res.id);\n}"
                ]
              }
            }
          ],
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              },
              {
                "key": "Authorization",
                "value": "Bearer {{admin_token}}"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"sku\": \"SUP-WHEY-TC252-01\",\n  \"name\": \"Whey Isolate Vanilla TC252\",\n  \"category\": \"CAT-SUPPLEMENT\",\n  \"unit\": \"Hộp\",\n  \"price\": 850000,\n  \"cost\": 500000,\n  \"stock\": 50,\n  \"minStock\": 10,\n  \"status\": \"ACTIVE\",\n  \"imageUrl\": \"https://images.unsplash.com/photo-1593095948071-474c5cc2989d?q=80&w=600\"\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            },
            "url": {
              "raw": "{{base_url}}/api/admin/products",
              "host": [
                "{{base_url}}"
              ],
              "path": [
                "api",
                "admin",
                "products"
              ]
            }
          },
          "response": []
        },
        {
          "name": "TC250 - POST Them San Pham UC12 | 201 - Admin them moi san pham khi de trong link anh (image null -> fallback)",
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"Status code is 201 Created\", function () {\n    pm.response.to.have.status(201);\n});\nvar res = pm.response.json();\npm.test(\"San pham tu dong co anh placeholder hoac chap nhan null\", function () {\n    pm.expect(res.sku).to.eql(\"ACC-ROPE-TC253-01\");\n});"
                ]
              }
            }
          ],
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              },
              {
                "key": "Authorization",
                "value": "Bearer {{admin_token}}"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"sku\": \"ACC-ROPE-TC253-01\",\n  \"name\": \"Dây nhảy tốc độ không ảnh TC253\",\n  \"category\": \"CAT-ACCESSORY\",\n  \"unit\": \"Cái\",\n  \"price\": 200000,\n  \"cost\": 100000,\n  \"stock\": 30,\n  \"minStock\": 5,\n  \"status\": \"ACTIVE\",\n  \"imageUrl\": null\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            },
            "url": {
              "raw": "{{base_url}}/api/admin/products",
              "host": [
                "{{base_url}}"
              ],
              "path": [
                "api",
                "admin",
                "products"
              ]
            }
          },
          "response": []
        },
        {
          "name": "TC251 - PUT Cap Nhat San Pham UC12 | 200 - Admin cap nhat thay doi link anh san pham thanh cong",
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"Status code is 200 OK\", function () {\n    pm.response.to.have.status(200);\n});\nvar res = pm.response.json();\npm.test(\"Cap nhat thanh cong link anh moi\", function () {\n    pm.expect(res.imageUrl).to.include(\"photo-1583473848882\");\n});"
                ]
              }
            }
          ],
          "request": {
            "method": "PUT",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              },
              {
                "key": "Authorization",
                "value": "Bearer {{admin_token}}"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"name\": \"Whey Protein Vanilla 1kg - Đã đổi ảnh mới\",\n  \"category\": \"CAT-SUPPLEMENT\",\n  \"unit\": \"Hộp\",\n  \"price\": 890000,\n  \"cost\": 520000,\n  \"stock\": 48,\n  \"minStock\": 10,\n  \"status\": \"ACTIVE\",\n  \"imageUrl\": \"https://images.unsplash.com/photo-1583473848882-f9a5bc7fd208?q=80&w=600\"\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            },
            "url": {
              "raw": "{{base_url}}/api/admin/products/4",
              "host": [
                "{{base_url}}"
              ],
              "path": [
                "api",
                "admin",
                "products",
                "4"
              ]
            }
          },
          "response": []
        },
        {
          "name": "TC252 - GET Danh Sach San Pham POS UC09 | 200 - Nhan vien POS lay danh sach san pham day du truong anh image",
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"Status code is 200 OK\", function () {\n    pm.response.to.have.status(200);\n});\nvar res = pm.response.json();\npm.test(\"Danh sach san pham hop le va chua truong imageUrl\", function () {\n    pm.expect(res.content).to.be.an('array');\n    pm.expect(res.content.length).to.be.above(0);\n    pm.expect(res.content[0]).to.have.property('imageUrl');\n});"
                ]
              }
            }
          ],
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              },
              {
                "key": "Authorization",
                "value": "Bearer {{staff_token}}"
              }
            ],
            "url": {
              "raw": "{{base_url}}/api/pos/products?page=0&size=20&status=ACTIVE",
              "host": [
                "{{base_url}}"
              ],
              "path": [
                "api",
                "pos",
                "products"
              ],
              "query": [
                {
                  "key": "page",
                  "value": "0"
                },
                {
                  "key": "size",
                  "value": "20"
                },
                {
                  "key": "status",
                  "value": "ACTIVE"
                }
              ]
            }
          },
          "response": []
        },
        {
          "name": "TC253 - POST Thanh Toan POS UC09 | 201 - Don hang POS chua san pham co anh duoc thanh toan thanh cong",
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"Status code is 201 Created\", function () {\n    pm.response.to.have.status(201);\n});\nvar res = pm.response.json();\npm.test(\"Don hang thanh toan thanh cong va co ID hop le\", function () {\n    pm.expect(res.orderId || res.id).to.not.be.undefined;\n    pm.expect(res.totalAmount).to.be.above(0);\n});"
                ]
              }
            }
          ],
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              },
              {
                "key": "Authorization",
                "value": "Bearer {{staff_token}}"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"items\": [\n    {\n      \"productId\": 1,\n      \"quantity\": 1\n    }\n  ],\n  \"paymentMethod\": \"CASH\",\n  \"discountAmount\": 0,\n  \"notes\": \"Thanh toán đơn hàng POS TC256\"\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            },
            "url": {
              "raw": "{{base_url}}/api/pos/checkout",
              "host": [
                "{{base_url}}"
              ],
              "path": [
                "api",
                "pos",
                "checkout"
              ]
            }
          },
          "response": []
        },
        {
          "name": "TC254 - POST Them San Pham UC12 | 409 - Bat loi trung ma SKU da ton tai (SUP-WHEY-001) [SRS 3.4.12]",
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"Status code is 409 Conflict or 400 Bad Request\", function () {\n    pm.expect(pm.response.code).to.be.oneOf([409, 400]);\n});\npm.test(\"Thong bao loi trung lap SKU tu he thong\", function () {\n    var body = pm.response.text();\n    pm.expect(body).to.match(/SKU|tồn tại|duplicate/i);\n});"
                ]
              }
            }
          ],
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              },
              {
                "key": "Authorization",
                "value": "Bearer {{admin_token}}"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"sku\": \"SUP-WHEY-001\",\n  \"name\": \"Whey Demo Duplicate SKU TC257\",\n  \"category\": \"CAT-SUPPLEMENT\",\n  \"unit\": \"Hộp\",\n  \"price\": 750000,\n  \"cost\": 500000,\n  \"stock\": 20,\n  \"minStock\": 5,\n  \"status\": \"ACTIVE\"\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            },
            "url": {
              "raw": "{{base_url}}/api/admin/products",
              "host": [
                "{{base_url}}"
              ],
              "path": [
                "api",
                "admin",
                "products"
              ]
            }
          },
          "response": []
        },
        {
          "name": "TC255 - POST Them San Pham UC12 | 400 - Bat loi Validation khi gia ban la so am (price < 0)",
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"Status code is 400 Bad Request\", function () {\n    pm.response.to.have.status(400);\n});\npm.test(\"Validation chan so am thanh cong\", function () {\n    var body = pm.response.text();\n    pm.expect(body).to.match(/Giá bán|lớn hơn|âm|positive|invalid/i);\n});"
                ]
              }
            }
          ],
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              },
              {
                "key": "Authorization",
                "value": "Bearer {{admin_token}}"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"sku\": \"ACC-NEG-PRICE-001\",\n  \"name\": \"Sản phẩm giá âm TC258\",\n  \"category\": \"CAT-ACCESSORY\",\n  \"unit\": \"Cái\",\n  \"price\": -50000,\n  \"cost\": 20000,\n  \"stock\": 10,\n  \"minStock\": 2,\n  \"status\": \"ACTIVE\"\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            },
            "url": {
              "raw": "{{base_url}}/api/admin/products",
              "host": [
                "{{base_url}}"
              ],
              "path": [
                "api",
                "admin",
                "products"
              ]
            }
          },
          "response": []
        },
        {
          "name": "TC256 - POST Them San Pham UC12 | 400 - Bat loi Validation khi de trong Ten san pham",
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"Status code is 400 Bad Request\", function () {\n    pm.response.to.have.status(400);\n});\npm.test(\"Thong bao loi truong Ten san pham bat buoc\", function () {\n    var body = pm.response.text();\n    pm.expect(body).to.match(/Tên sản phẩm|name|trống|blank/i);\n});"
                ]
              }
            }
          ],
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              },
              {
                "key": "Authorization",
                "value": "Bearer {{admin_token}}"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"sku\": \"ACC-BLANK-NAME-001\",\n  \"name\": \"\",\n  \"category\": \"CAT-ACCESSORY\",\n  \"unit\": \"Cái\",\n  \"price\": 100000,\n  \"cost\": 50000,\n  \"stock\": 10,\n  \"minStock\": 2,\n  \"status\": \"ACTIVE\"\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            },
            "url": {
              "raw": "{{base_url}}/api/admin/products",
              "host": [
                "{{base_url}}"
              ],
              "path": [
                "api",
                "admin",
                "products"
              ]
            }
          },
          "response": []
        },
        {
          "name": "TC257 - POST Them San Pham UC12 | 403 - Nhan vien Staff goi API Admin tao san pham (Chan quyen)",
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"Status code is 403 Forbidden\", function () {\n    pm.response.to.have.status(403);\n});"
                ]
              }
            }
          ],
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              },
              {
                "key": "Authorization",
                "value": "Bearer {{staff_token}}"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"sku\": \"STAFF-HACK-001\",\n  \"name\": \"Nhân viên cố tình tạo sản phẩm\",\n  \"category\": \"CAT-ACCESSORY\",\n  \"unit\": \"Cái\",\n  \"price\": 100000,\n  \"cost\": 50000,\n  \"stock\": 10,\n  \"minStock\": 2,\n  \"status\": \"ACTIVE\"\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            },
            "url": {
              "raw": "{{base_url}}/api/admin/products",
              "host": [
                "{{base_url}}"
              ],
              "path": [
                "api",
                "admin",
                "products"
              ]
            }
          },
          "response": []
        },
        {
          "name": "TC258 - POST Thanh Toan POS UC09 | 400 - Bat loi khi gio hang rong items = [] [SRS 3.4.9 Luong 4a]",
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"Status code is 400 Bad Request\", function () {\n    pm.response.to.have.status(400);\n});\npm.test(\"Thong bao loi gio hang items khong duoc de trong\", function () {\n    var body = pm.response.text();\n    pm.expect(body).to.match(/items|trống|empty|giỏ hàng/i);\n});"
                ]
              }
            }
          ],
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              },
              {
                "key": "Authorization",
                "value": "Bearer {{staff_token}}"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"items\": [],\n  \"paymentMethod\": \"CASH\",\n  \"discountAmount\": 0,\n  \"notes\": \"Thanh toán giỏ hàng trống\"\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            },
            "url": {
              "raw": "{{base_url}}/api/pos/checkout",
              "host": [
                "{{base_url}}"
              ],
              "path": [
                "api",
                "pos",
                "checkout"
              ]
            }
          },
          "response": []
        },
        {
          "name": "TC259 - POST Thanh Toan POS UC09 | 400 - Bat loi mua so luong vuot qua ton kho [SRS 3.4.9 Luong 5a]",
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"Status code is 400 Bad Request\", function () {\n    pm.response.to.have.status(400);\n});\npm.test(\"Thong bao loi khong du so luong ton kho\", function () {\n    var body = pm.response.text();\n    pm.expect(body).to.match(/tồn kho|stock|không đủ/i);\n});"
                ]
              }
            }
          ],
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              },
              {
                "key": "Authorization",
                "value": "Bearer {{staff_token}}"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"items\": [\n    {\n      \"productId\": 1,\n      \"quantity\": 99999\n    }\n  ],\n  \"paymentMethod\": \"CASH\",\n  \"discountAmount\": 0,\n  \"notes\": \"Mua vượt quá tồn kho\"\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            },
            "url": {
              "raw": "{{base_url}}/api/pos/checkout",
              "host": [
                "{{base_url}}"
              ],
              "path": [
                "api",
                "pos",
                "checkout"
              ]
            }
          },
          "response": []
        },
        {
          "name": "TC260 - POST Thanh Toan POS UC09 | 401 - Nguoi dung nac danh khong co Token goi API thanh toan",
          "event": [
            {
              "listen": "test",
              "script": {
                "type": "text/javascript",
                "exec": [
                  "pm.test(\"Status code is 401 Unauthorized\", function () {\n    pm.response.to.have.status(401);\n});"
                ]
              }
            }
          ],
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"items\": [\n    {\n      \"productId\": 1,\n      \"quantity\": 1\n    }\n  ],\n  \"paymentMethod\": \"CASH\",\n  \"discountAmount\": 0\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            },
            "url": {
              "raw": "{{base_url}}/api/pos/checkout",
              "host": [
                "{{base_url}}"
              ],
              "path": [
                "api",
                "pos",
                "checkout"
              ]
            }
          },
          "response": []
        }
      ]
    }
  ]
}