import os
import json
from reportlab.lib.pagesizes import A4
from reportlab.lib import colors
from reportlab.lib.units import mm
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether
)
from reportlab.pdfgen import canvas
from reportlab.graphics.shapes import (
    Drawing, Rect, Line, Circle, String, Group, PolyLine, Polygon
)
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont

# Register Unicode fonts
font_dir = r"C:\Windows\Fonts"
pdfmetrics.registerFont(TTFont("Arial", os.path.join(font_dir, "arial.ttf")))
pdfmetrics.registerFont(TTFont("Arial-Bold", os.path.join(font_dir, "arialbd.ttf")))
pdfmetrics.registerFont(TTFont("Arial-Italic", os.path.join(font_dir, "ariali.ttf")))
pdfmetrics.registerFont(TTFont("Arial-BoldItalic", os.path.join(font_dir, "arialbi.ttf")))

class NumberedCanvas(canvas.Canvas):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        num_pages = len(self._saved_page_states)
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_page_decorations(num_pages)
            super().showPage()
        super().save()

    def draw_page_decorations(self, page_count):
        self.saveState()
        self.setFont("Arial", 8.5)
        self.setFillColor(colors.HexColor("#64748b"))
        # Footer
        self.setStrokeColor(colors.HexColor("#e2e8f0"))
        self.setLineWidth(0.8)
        self.line(15 * mm, 12 * mm, 195 * mm, 12 * mm)
        self.drawString(15 * mm, 8 * mm, "Dự án GYM Management — Báo cáo Giám sát Tài nguyên & Vận hành Hạ tầng AWS")
        self.drawRightString(195 * mm, 8 * mm, f"Trang {self._pageNumber} / {page_count}")
        self.restoreState()

def build_pdf_report():
    repo_root = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
    scratch_dir = os.path.join(repo_root, ".scratch")
    docs_dir = os.path.join(repo_root, "docs")
    pdf_out = os.path.join(docs_dir, "Bao_cao_giam_sat_tai_nguyen_AWS.pdf")

    # Load datasets
    with open(os.path.join(scratch_dir, "aws_billing_oct.json"), encoding="utf-8") as f:
        billing_raw = json.load(f)
    with open(os.path.join(scratch_dir, "cw_hourly_cpu_backend.json"), encoding="utf-8") as f:
        be_hourly_cpu = json.load(f)["Datapoints"]
    with open(os.path.join(scratch_dir, "cw_hourly_mem_backend.json"), encoding="utf-8") as f:
        be_hourly_mem = json.load(f)["Datapoints"]
    with open(os.path.join(scratch_dir, "cw_1m_cpu_oct19.json"), encoding="utf-8") as f:
        oct19_cpu = json.load(f)["Datapoints"]
    with open(os.path.join(scratch_dir, "cw_1m_mem_oct19.json"), encoding="utf-8") as f:
        oct19_mem = json.load(f)["Datapoints"]

    be_hourly_cpu.sort(key=lambda x: x["Timestamp"])
    be_hourly_mem.sort(key=lambda x: x["Timestamp"])
    oct19_cpu.sort(key=lambda x: x["Timestamp"])
    oct19_mem.sort(key=lambda x: x["Timestamp"])

    # Billing processing
    groups = billing_raw['ResultsByTime'][0]['Groups']
    usage_costs = {}
    credits = {}
    for g in groups:
        service = g['Keys'][0]
        rtype = g['Keys'][1]
        amount = float(g['Metrics']['UnblendedCost']['Amount'])
        if rtype == 'Usage':
            usage_costs[service] = usage_costs.get(service, 0.0) + amount
        elif rtype == 'Credit':
            credits[service] = credits.get(service, 0.0) + amount

    total_gross = sum(usage_costs.values())
    total_credit = sum(credits.values())
    net_cost = total_gross + total_credit

    doc = SimpleDocTemplate(
        pdf_out,
        pagesize=A4,
        leftMargin=14 * mm,
        rightMargin=14 * mm,
        topMargin=14 * mm,
        bottomMargin=18 * mm
    )

    styles = getSampleStyleSheet()
    
    # Custom styles
    title_style = ParagraphStyle(
        "DocTitle",
        fontName="Arial-Bold",
        fontSize=16,
        leading=20,
        textColor=colors.HexColor("#ffffff")
    )
    subtitle_style = ParagraphStyle(
        "DocSubtitle",
        fontName="Arial",
        fontSize=10,
        leading=14,
        textColor=colors.HexColor("#e2e8f0")
    )
    meta_style = ParagraphStyle(
        "DocMeta",
        fontName="Arial",
        fontSize=8.5,
        leading=12,
        textColor=colors.HexColor("#94a3b8")
    )
    h1_style = ParagraphStyle(
        "H1",
        fontName="Arial-Bold",
        fontSize=12,
        leading=16,
        textColor=colors.HexColor("#0f172a"),
        spaceBefore=10,
        spaceAfter=6
    )
    h2_style = ParagraphStyle(
        "H2",
        fontName="Arial-Bold",
        fontSize=10,
        leading=14,
        textColor=colors.HexColor("#334155"),
        spaceBefore=8,
        spaceAfter=4
    )
    body_style = ParagraphStyle(
        "Body",
        fontName="Arial",
        fontSize=9,
        leading=13,
        textColor=colors.HexColor("#1e293b")
    )
    callout_style = ParagraphStyle(
        "Callout",
        fontName="Arial",
        fontSize=8.5,
        leading=12,
        textColor=colors.HexColor("#065f46")
    )
    tbl_header = ParagraphStyle(
        "TblHeader",
        fontName="Arial-Bold",
        fontSize=8.5,
        leading=11,
        textColor=colors.HexColor("#0f172a")
    )
    tbl_cell = ParagraphStyle(
        "TblCell",
        fontName="Arial",
        fontSize=8,
        leading=11,
        textColor=colors.HexColor("#1e293b")
    )
    tbl_cell_bold = ParagraphStyle(
        "TblCellBold",
        fontName="Arial-Bold",
        fontSize=8,
        leading=11,
        textColor=colors.HexColor("#0f172a")
    )

    story = []

    # ================= HEADER BANNER =================
    header_data = [
        [
            Paragraph("<b>AWS OPERATIONAL &amp; RESOURCE MONITORING REPORT</b>", ParagraphStyle("Badge", fontName="Arial-Bold", fontSize=8, leading=10, textColor=colors.HexColor("#fdba74"))),
        ],
        [
            Paragraph("BÁO CÁO GIÁM SÁT TÀI NGUYÊN &amp; VẬN HÀNH HẠ TẦNG AWS", title_style),
        ],
        [
            Paragraph("Hệ thống Quản lý Phòng Tập GYM (GYM Management System) — Môi trường Staging", subtitle_style),
        ],
        [
            Paragraph("<b>Region:</b> ap-southeast-1 (Singapore) &nbsp;|&nbsp; <b>Account:</b> 102882775162 &nbsp;|&nbsp; <b>Kỳ đánh giá:</b> Tháng 10/2026 &nbsp;|&nbsp; <b>Ngày xuất:</b> 30/10/2026", meta_style)
        ]
    ]
    header_tbl = Table(header_data, colWidths=[182 * mm])
    header_tbl.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), colors.HexColor("#0f172a")),
        ('LINELEFT', (0, 0), (0, -1), 4 * mm, colors.HexColor("#f97316")),
        ('TOPPADDING', (0, 0), (-1, -1), 4),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 4),
        ('LEFTPADDING', (0, 0), (-1, -1), 12),
        ('RIGHTPADDING', (0, 0), (-1, -1), 12),
    ]))
    story.append(header_tbl)
    story.append(Spacer(1, 4 * mm))

    # ================= KPI STATS =================
    kpi_data = [
        [
            Paragraph("<font size=7 color='#64748b'><b>CHI PHÍ TIÊU THỤ (GROSS)</b></font><br/><font size=14 color='#0f172a'><b>$5.92</b></font><br/><font size=7 color='#f59e0b'><b>Cảnh báo Budget mốc 50%</b></font>", body_style),
            Paragraph("<font size=7 color='#64748b'><b>AWS CREDITS / GIẢM TRỪ</b></font><br/><font size=14 color='#0f172a'><b>-$5.92</b></font><br/><font size=7 color='#10b981'><b>Khấu trừ 100% hóa đơn</b></font>", body_style),
            Paragraph("<font size=7 color='#64748b'><b>HÓA ĐƠN THỰC TẾ (NET)</b></font><br/><font size=14 color='#0f172a'><b>$0.00</b></font><br/><font size=7 color='#10b981'><b>Tiết kiệm tối đa ngân sách</b></font>", body_style),
            Paragraph("<font size=7 color='#64748b'><b>ĐỘ ỔN ĐỊNH RAM (MAX)</b></font><br/><font size=14 color='#0f172a'><b>50.44%</b></font><br/><font size=7 color='#10b981'><b>100% An toàn, Không OOM</b></font>", body_style)
        ]
    ]
    kpi_tbl = Table(kpi_data, colWidths=[45.5 * mm] * 4)
    kpi_tbl.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), colors.HexColor("#f8fafc")),
        ('BOX', (0, 0), (-1, -1), 0.8, colors.HexColor("#e2e8f0")),
        ('INNERGRID', (0, 0), (-1, -1), 0.5, colors.HexColor("#e2e8f0")),
        ('TOPPADDING', (0, 0), (-1, -1), 6),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 6),
        ('LEFTPADDING', (0, 0), (-1, -1), 8),
        ('RIGHTPADDING', (0, 0), (-1, -1), 8),
    ]))
    story.append(kpi_tbl)
    story.append(Spacer(1, 4 * mm))

    # ================= PHẦN 1 =================
    story.append(Paragraph("1. Tổng Quan Kiến Trúc Hạ Tầng &amp; Tối Ưu Cấu Hình Fargate (SP4)", h1_style))
    story.append(Paragraph(
        "Môi trường Staging được chuẩn hóa triển khai hoàn toàn bằng Infrastructure as Code (Terraform) trên AWS ECS Fargate theo mô hình kiến trúc tối ưu chi phí cực hạn (Zero Idle Cost Architecture):",
        body_style
    ))
    story.append(Spacer(1, 2 * mm))

    arch_rows = [
        [Paragraph("Thành phần", tbl_header), Paragraph("Trước Tối ưu (SP3)", tbl_header), Paragraph("Chuẩn Tối ưu (SP4)", tbl_header), Paragraph("Hiệu quả Kỹ thuật &amp; Chi phí", tbl_header)],
        [Paragraph("<b>ECS Compute CPU</b>", tbl_cell), Paragraph("1024 vCPU units", tbl_cell), Paragraph("<b>512 units (0.5 vCPU)</b>", tbl_cell_bold), Paragraph("Tiết kiệm 50% chi phí compute; đáp ứng trơn tru tải Spring Boot + MySQL.", tbl_cell)],
        [Paragraph("<b>ECS Compute RAM</b>", tbl_cell), Paragraph("2048 MiB", tbl_cell), Paragraph("<b>2048 MiB</b> (Giữ vững)", tbl_cell_bold), Paragraph("Duy trì bộ nhớ an toàn ngăn chặn rủi ro Out-of-Memory (OOM) khi nạp Flyway.", tbl_cell)],
        [Paragraph("<b>Memory Soft Limits</b>", tbl_cell), Paragraph("Không thiết lập", tbl_cell), Paragraph("<b>MySQL: 768M | App: 1024M</b>", tbl_cell_bold), Paragraph("Phân bổ ranh giới rõ ràng nhưng cho phép dùng chung tài nguyên dôi dư.", tbl_cell)],
        [Paragraph("<b>Capacity Provider</b>", tbl_cell), Paragraph("FARGATE On-Demand", tbl_cell), Paragraph("<b>FARGATE_SPOT (100%)</b>", tbl_cell_bold), Paragraph("Tiết kiệm ~70% đơn giá compute so với on-demand.", tbl_cell)],
        [Paragraph("<b>Vòng đời Vận hành</b>", tbl_cell), Paragraph("Chạy liên tục 24/7", tbl_cell), Paragraph("<b>Pause / Hibernate</b>", tbl_cell_bold), Paragraph("Chủ động phá hủy ALB &amp; ECS ngoài giờ test; đưa chi phí nhàn rỗi về $0.00/h.", tbl_cell)],
        [Paragraph("<b>Log Tập trung</b>", tbl_cell), Paragraph("Log container cục bộ", tbl_cell), Paragraph("<b>CloudWatch (7d retention)</b>", tbl_cell_bold), Paragraph("Log stream realtime; xóa mã ANSI; khống chế chi phí lưu trữ CloudWatch.", tbl_cell)]
    ]
    arch_tbl = Table(arch_rows, colWidths=[36 * mm, 34 * mm, 44 * mm, 68 * mm])
    arch_tbl.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor("#f1f5f9")),
        ('BOX', (0, 0), (-1, -1), 0.5, colors.HexColor("#cbd5e1")),
        ('INNERGRID', (0, 0), (-1, -1), 0.5, colors.HexColor("#e2e8f0")),
        ('TOPPADDING', (0, 0), (-1, -1), 4),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 4),
        ('LEFTPADDING', (0, 0), (-1, -1), 6),
        ('RIGHTPADDING', (0, 0), (-1, -1), 6),
    ]))
    story.append(arch_tbl)
    story.append(Spacer(1, 3 * mm))

    callout_data = [
        [Paragraph("<b>Đánh giá Kiến trúc:</b> Việc áp dụng đồng thời Fargate Spot, giảm vCPU xuống 512 units, đặt soft limits bộ nhớ và quy trình Hibernate/DuckDNS đã giúp toàn bộ chi phí vận hành staging trong tháng 10/2026 chỉ tiêu tốn <b>$5.916 USD</b> (thấp hơn nhiều so với mức trung bình 25–40 USD/tháng của hạ tầng chạy 24/7).", callout_style)]
    ]
    callout_tbl = Table(callout_data, colWidths=[182 * mm])
    callout_tbl.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), colors.HexColor("#ecfdf5")),
        ('LINELEFT', (0, 0), (0, -1), 3 * mm, colors.HexColor("#10b981")),
        ('TOPPADDING', (0, 0), (-1, -1), 5),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 5),
        ('LEFTPADDING', (0, 0), (-1, -1), 8),
        ('RIGHTPADDING', (0, 0), (-1, -1), 8),
    ]))
    story.append(callout_tbl)

    # ================= TRANG 2 =================
    story.append(PageBreak())
    story.append(Paragraph("2. Giám Sát Tài Nguyên CloudWatch Metrics (CPU &amp; Memory Utilization)", h1_style))
    story.append(Paragraph(
        "Dữ liệu được trích xuất trực tiếp từ AWS CloudWatch Metrics Namespace <code>AWS/ECS</code> cho cả hai dịch vụ chính: <code>gym-staging-backend</code> (Spring Boot 21 + MySQL 8.4 Sidecar) và <code>gym-staging-frontend</code> (Nginx React SPA).",
        body_style
    ))
    story.append(Spacer(1, 2 * mm))

    story.append(Paragraph("2.1. Biểu đồ Toàn cảnh Vòng đời Hoạt động (Toàn bộ Tháng 10/2026 — Độ phân giải theo Giờ)", h2_style))

    # Function to create ReportLab Drawing line chart
    def create_rl_line_chart(series_list, width=515, height=130, y_max=130, y_unit="%", x_labels=None):
        d = Drawing(width, height)
        pad_l, pad_r, pad_t, pad_b = 40, 15, 15, 22
        plot_w = width - pad_l - pad_r
        plot_h = height - pad_t - pad_b

        # Background
        d.add(Rect(0, 0, width, height, fillColor=colors.HexColor("#0f172a"), strokeColor=None, rx=4, ry=4))

        # Y grid
        y_ticks = 4
        for i in range(y_ticks + 1):
            val = (y_max / y_ticks) * i
            y = pad_b + (i / y_ticks) * plot_h
            d.add(Line(pad_l, y, width - pad_r, y, strokeColor=colors.HexColor("#334155"), strokeWidth=0.5, strokeDashArray=[2, 2]))
            d.add(String(pad_l - 5, y - 3, f"{val:.0f}{y_unit}", fontName="Arial", fontSize=7, fillColor=colors.HexColor("#94a3b8"), textAnchor="end"))

        # Plot series
        for s in series_list:
            data = s["data"]
            if not data:
                continue
            stroke_col = colors.HexColor(s["color"])
            stroke_w = s.get("width", 1.5)
            n = len(data)
            points = []
            for idx, pt in enumerate(data):
                val = pt[1]
                x = pad_l + (idx / max(1, n - 1)) * plot_w
                y = pad_b + (min(val, y_max) / y_max) * plot_h
                points.extend([x, y])

            # Polyline
            d.add(PolyLine(points, strokeColor=stroke_col, strokeWidth=stroke_w))

            # Dots
            for idx, pt in enumerate(data):
                if n <= 15 or idx % 3 == 0 or idx == n - 1:
                    x = pad_l + (idx / max(1, n - 1)) * plot_w
                    y = pad_b + (min(pt[1], y_max) / y_max) * plot_h
                    d.add(Circle(x, y, 2, fillColor=stroke_col, strokeColor=colors.HexColor("#0f172a"), strokeWidth=1))

        # X labels
        if x_labels:
            step = max(1, len(x_labels) // 6)
            for idx in range(0, len(x_labels), step):
                x = pad_l + (idx / max(1, len(x_labels) - 1)) * plot_w
                lbl = x_labels[idx]
                d.add(String(x, 7, lbl, fontName="Arial", fontSize=7, fillColor=colors.HexColor("#94a3b8"), textAnchor="middle"))

        # Legend
        leg_x = width - pad_r
        offset = 0
        for s in reversed(series_list):
            lbl = s["label"]
            col = colors.HexColor(s["color"])
            d.add(Circle(leg_x - offset - 10, height - 10, 3, fillColor=col, strokeColor=None))
            d.add(String(leg_x - offset - 16, height - 13, lbl, fontName="Arial-Bold", fontSize=7.5, fillColor=colors.HexColor("#f8fafc"), textAnchor="end"))
            offset += 90

        return d

    hourly_cpu_data = [(d["Timestamp"], d["Average"]) for d in be_hourly_cpu]
    hourly_cpu_max = [(d["Timestamp"], d["Maximum"]) for d in be_hourly_cpu]
    hourly_labels = [d["Timestamp"][8:13].replace("T", " ") + "h" for d in be_hourly_cpu]

    chart1 = create_rl_line_chart([
        {"data": hourly_cpu_max, "color": "#f59e0b", "label": "Max CPU (%)", "width": 1.2},
        {"data": hourly_cpu_data, "color": "#38bdf8", "label": "Avg CPU (%)", "width": 1.8}
    ], width=515, height=125, y_max=130, y_unit="%", x_labels=hourly_labels)
    story.append(chart1)
    story.append(Spacer(1, 2.5 * mm))

    hourly_mem_data = [(d["Timestamp"], d["Average"]) for d in be_hourly_mem]
    hourly_mem_max = [(d["Timestamp"], d["Maximum"]) for d in be_hourly_mem]
    chart2 = create_rl_line_chart([
        {"data": hourly_mem_max, "color": "#a855f7", "label": "Max RAM (%)", "width": 1.2},
        {"data": hourly_mem_data, "color": "#10b981", "label": "Avg RAM (%)", "width": 1.8}
    ], width=515, height=125, y_max=100, y_unit="%", x_labels=hourly_labels)
    story.append(chart2)
    story.append(Spacer(1, 3 * mm))

    cw_stat_rows = [
        [Paragraph("Chỉ số Vận hành", tbl_header), Paragraph("Min", tbl_header), Paragraph("Avg", tbl_header), Paragraph("Max (Đỉnh)", tbl_header), Paragraph("Đánh giá Trạng thái Kỹ thuật", tbl_header)],
        [Paragraph("<b>CPU Backend</b>", tbl_cell), Paragraph("0.00%", tbl_cell), Paragraph("<b>1.72%</b>", tbl_cell_bold), Paragraph("<b>123.57%</b>", tbl_cell_bold), Paragraph("Nhàn rỗi ~0.66%. Đỉnh >100% trong 1-2 phút khi nạp Spring Bean + Flyway, sau đó hạ ngay.", tbl_cell)],
        [Paragraph("<b>Memory Backend</b>", tbl_cell), Paragraph("21.78%", tbl_cell), Paragraph("<b>44.96%</b> (~921M)", tbl_cell_bold), Paragraph("<b>50.44%</b> (~1033M)", tbl_cell_bold), Paragraph("Ổn định tuyệt đối quanh 45-50%. Dư >50% RAM an toàn; hoàn toàn không rò rỉ hay OOM.", tbl_cell)],
        [Paragraph("<b>CPU Frontend</b>", tbl_cell), Paragraph("0.00%", tbl_cell), Paragraph("<b>0.017%</b>", tbl_cell), Paragraph("<b>1.64%</b>", tbl_cell), Paragraph("Nginx SPA phục vụ static file tiêu tốn tài nguyên cực thấp.", tbl_cell)],
        [Paragraph("<b>Memory Frontend</b>", tbl_cell), Paragraph("0.00%", tbl_cell), Paragraph("<b>0.58%</b> (~3M)", tbl_cell), Paragraph("<b>0.68%</b> (~3.5M)", tbl_cell), Paragraph("Mức sử dụng RAM tĩnh siêu nhẹ, ổn định 100%.", tbl_cell)]
    ]
    cw_stat_tbl = Table(cw_stat_rows, colWidths=[32 * mm, 16 * mm, 28 * mm, 26 * mm, 80 * mm])
    cw_stat_tbl.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor("#f1f5f9")),
        ('BOX', (0, 0), (-1, -1), 0.5, colors.HexColor("#cbd5e1")),
        ('INNERGRID', (0, 0), (-1, -1), 0.5, colors.HexColor("#e2e8f0")),
        ('TOPPADDING', (0, 0), (-1, -1), 3),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 3),
        ('LEFTPADDING', (0, 0), (-1, -1), 5),
        ('RIGHTPADDING', (0, 0), (-1, -1), 5),
    ]))
    story.append(cw_stat_tbl)

    # ================= TRANG 3 =================
    story.append(PageBreak())
    story.append(Paragraph("2.2. Chi Tiết Từng Phút Trong Khung Giờ Tester Thực Thi Kịch Bản Hồi Quy", h2_style))
    story.append(Paragraph(
        "Phân tích độ phân giải 1 phút (1-minute period) trong các phiên Tester (Thanh) thực thi kịch bản hồi quy API Postman (SP03.1 – SP03.4) và kiểm thử giao dịch/rollback (ngày 19/10 và 20/10/2026):",
        body_style
    ))
    story.append(Spacer(1, 2 * mm))

    oct19_cpu_pts = [(d["Timestamp"], d["Average"]) for d in oct19_cpu]
    oct19_cpu_max_pts = [(d["Timestamp"], d["Maximum"]) for d in oct19_cpu]
    oct19_labels = [d["Timestamp"][11:16] for d in oct19_cpu]
    chart3 = create_rl_line_chart([
        {"data": oct19_cpu_max_pts, "color": "#f43f5e", "label": "Max CPU (%)", "width": 1.2},
        {"data": oct19_cpu_pts, "color": "#38bdf8", "label": "Avg CPU (%)", "width": 1.8}
    ], width=252, height=110, y_max=110, y_unit="%", x_labels=oct19_labels)

    oct19_mem_pts = [(d["Timestamp"], d["Average"]) for d in oct19_mem]
    chart4 = create_rl_line_chart([
        {"data": oct19_mem_pts, "color": "#10b981", "label": "Memory Util (%)", "width": 1.8}
    ], width=252, height=110, y_max=100, y_unit="%", x_labels=oct19_labels)

    zoom_tbl = Table([[chart3, chart4]], colWidths=[90 * mm, 90 * mm])
    zoom_tbl.setStyle(TableStyle([
        ('ALIGN', (0, 0), (-1, -1), 'CENTER'),
        ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
        ('LEFTPADDING', (0, 0), (-1, -1), 0),
        ('RIGHTPADDING', (0, 0), (-1, -1), 0),
        ('TOPPADDING', (0, 0), (-1, -1), 0),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 0),
    ]))
    story.append(zoom_tbl)
    story.append(Spacer(1, 2.5 * mm))

    callout2_data = [
        [Paragraph("<b>Phân tích Diễn tiến Phiên Test Hồi quy:</b><br/>"
                   "• <b>Pha Boot &amp; Reset DB (18:47 – 18:48):</b> CPU chạm đỉnh 100.1% do Flyway nạp schema V1-V4 + dev seed và MySQL tạo bộ đệm. RAM tăng từ 8.9% lên 44.2%.<br/>"
                   "• <b>Pha Bắn tải Kịch bản Test (18:49 – 18:59):</b> Tester chạy liên tục kịch bản POS, thanh toán, áp voucher và rollback. CPU giữ ổn định 1.15%–1.94% (đỉnh nhỏ 5.17% lúc tính voucher). RAM giữ thẳng tắp ở 45.07% (~923 MiB).<br/>"
                   "• <b>Độ trễ phản hồi ALB:</b> Trung bình <b>0.101s – 0.218s</b>, đáp ứng hoàn hảo yêu cầu trải nghiệm ứng dụng quản trị.", callout_style)]
    ]
    callout2_tbl = Table(callout2_data, colWidths=[182 * mm])
    callout2_tbl.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), colors.HexColor("#fffbeb")),
        ('LINELEFT', (0, 0), (0, -1), 3 * mm, colors.HexColor("#f59e0b")),
        ('TOPPADDING', (0, 0), (-1, -1), 4),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 4),
        ('LEFTPADDING', (0, 0), (-1, -1), 8),
        ('RIGHTPADDING', (0, 0), (-1, -1), 8),
    ]))
    story.append(callout2_tbl)
    story.append(Spacer(1, 3 * mm))

    # ================= PHẦN 3 =================
    story.append(Paragraph("3. Thống Kê Hóa Đơn Thực Tế (AWS Billing Dashboard &amp; AWS Budgets)", h1_style))

    # ReportLab Cost Bar Chart
    def create_cost_bar_chart(width=515, height=95):
        d = Drawing(width, height)
        pad_l, pad_r, pad_t, pad_b = 160, 20, 10, 16
        plot_w = width - pad_l - pad_r
        plot_h = height - pad_t - pad_b

        d.add(Rect(0, 0, width, height, fillColor=colors.HexColor("#0f172a"), strokeColor=None, rx=4, ry=4))

        top_services = [
            ("Elastic Load Balancing (ALB)", usage_costs.get("Amazon Elastic Load Balancing", 0.0), "#38bdf8"),
            ("VPC (Public IPv4 Allocation)", usage_costs.get("Amazon Virtual Private Cloud", 0.0), "#818cf8"),
            ("ECS Fargate Spot Compute", usage_costs.get("Amazon Elastic Container Service", 0.0), "#34d399"),
            ("ECR Container Registry", usage_costs.get("Amazon EC2 Container Registry (ECR)", 0.0), "#fbbf24"),
            ("S3 / DynamoDB / Logs / Other", 0.0008, "#94a3b8")
        ]
        max_c = 4.0
        row_h = plot_h / len(top_services)

        for i in range(5):
            val = (max_c / 4) * i
            x = pad_l + (i / 4) * plot_w
            d.add(Line(x, pad_b, x, height - pad_t, strokeColor=colors.HexColor("#334155"), strokeWidth=0.5, strokeDashArray=[2, 2]))
            d.add(String(x, 5, f"${val:.1f}", fontName="Arial", fontSize=7, fillColor=colors.HexColor("#94a3b8"), textAnchor="middle"))

        for idx, (name, val, col) in enumerate(top_services):
            y = height - pad_t - (idx + 1) * row_h + 2
            bar_w = (val / max_c) * plot_w
            pct = (val / total_gross) * 100 if total_gross > 0 else 0
            d.add(String(pad_l - 8, y + 2, name, fontName="Arial", fontSize=7.5, fillColor=colors.HexColor("#cbd5e1"), textAnchor="end"))
            d.add(Rect(pad_l, y, max(2, bar_w), 9, fillColor=colors.HexColor(col), strokeColor=None, rx=2, ry=2))
            d.add(String(pad_l + bar_w + 6, y + 2, f"${val:.3f} ({pct:.1f}%)", fontName="Arial-Bold", fontSize=7.5, fillColor=colors.HexColor("#f8fafc")))

        return d

    story.append(create_cost_bar_chart(width=515, height=95))
    story.append(Spacer(1, 2.5 * mm))

    bill_rows = [
        [Paragraph("Dịch vụ AWS", tbl_header), Paragraph("Khối lượng Sử dụng", tbl_header), Paragraph("Chi phí Gốc (Gross)", tbl_header), Paragraph("Tỷ trọng", tbl_header), Paragraph("AWS Credit", tbl_header), Paragraph("Thực trả (Net)", tbl_header)],
        [Paragraph("<b>Elastic Load Balancing (ALB)</b>", tbl_cell), Paragraph("147.04 LCU-Hours", tbl_cell), Paragraph("$3.705 USD", tbl_cell), Paragraph("62.6%", tbl_cell), Paragraph("-$3.705 USD", tbl_cell), Paragraph("<b>$0.00 USD</b>", tbl_cell_bold)],
        [Paragraph("<b>Virtual Private Cloud (IPv4)</b>", tbl_cell), Paragraph("265.94 In-use IPv4 Hours", tbl_cell), Paragraph("$1.330 USD", tbl_cell), Paragraph("22.5%", tbl_cell), Paragraph("-$1.330 USD", tbl_cell), Paragraph("<b>$0.00 USD</b>", tbl_cell_bold)],
        [Paragraph("<b>ECS Fargate Spot Compute</b>", tbl_cell), Paragraph("137.92 vCPU/GB Hours", tbl_cell), Paragraph("$0.833 USD", tbl_cell), Paragraph("14.1%", tbl_cell), Paragraph("-$0.833 USD", tbl_cell), Paragraph("<b>$0.00 USD</b>", tbl_cell_bold)],
        [Paragraph("<b>ECR Container Registry</b>", tbl_cell), Paragraph("0.476 GB-Mo (5 images)", tbl_cell), Paragraph("$0.048 USD", tbl_cell), Paragraph("0.8%", tbl_cell), Paragraph("-$0.048 USD", tbl_cell), Paragraph("<b>$0.00 USD</b>", tbl_cell_bold)],
        [Paragraph("<b>S3, DynamoDB, SSM, Logs</b>", tbl_cell), Paragraph("State, logs, secrets", tbl_cell), Paragraph("$0.001 USD", tbl_cell), Paragraph("< 0.1%", tbl_cell), Paragraph("-$0.001 USD", tbl_cell), Paragraph("<b>$0.00 USD</b>", tbl_cell_bold)],
        [Paragraph("<b>TỔNG CỘNG THÁNG 10/2026</b>", tbl_cell_bold), Paragraph("—", tbl_cell_bold), Paragraph("<b>$5.916 USD</b>", tbl_cell_bold), Paragraph("<b>100%</b>", tbl_cell_bold), Paragraph("<b>-$5.916 USD</b>", tbl_cell_bold), Paragraph("<font color='#047857'><b>$0.00 USD</b></font>", tbl_cell_bold)]
    ]
    bill_tbl = Table(bill_rows, colWidths=[48 * mm, 38 * mm, 26 * mm, 16 * mm, 26 * mm, 28 * mm])
    bill_tbl.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor("#f1f5f9")),
        ('BACKGROUND', (0, -1), (-1, -1), colors.HexColor("#e2e8f0")),
        ('BOX', (0, 0), (-1, -1), 0.5, colors.HexColor("#cbd5e1")),
        ('INNERGRID', (0, 0), (-1, -1), 0.5, colors.HexColor("#e2e8f0")),
        ('TOPPADDING', (0, 0), (-1, -1), 3),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 3),
        ('LEFTPADDING', (0, 0), (-1, -1), 5),
        ('RIGHTPADDING', (0, 0), (-1, -1), 5),
    ]))
    story.append(bill_tbl)
    story.append(Spacer(1, 2.5 * mm))

    budget_rows = [
        [Paragraph("Tên Ngân sách (AWS Budgets)", tbl_header), Paragraph("Trần Ngân sách", tbl_header), Paragraph("Thực tế (Actual)", tbl_header), Paragraph("Dự báo (Forecast)", tbl_header), Paragraph("Trạng thái Kích hoạt Cảnh báo", tbl_header)],
        [
            Paragraph("<b>gym-staging-monthly-budget</b><br/><i>(Terraform cost-monitoring)</i>", tbl_cell),
            Paragraph("<b>$10.00 / tháng</b>", tbl_cell_bold),
            Paragraph("<b>$5.916 USD</b>", tbl_cell),
            Paragraph("<b>$5.946 USD</b>", tbl_cell),
            Paragraph("• Mốc 50% ($5.00): Đã kích hoạt cảnh báo sớm.<br/>• Mốc 80% ($8.00) &amp; 100% Dự báo: Chưa chạm (An toàn).", tbl_cell)
        ]
    ]
    budget_tbl = Table(budget_rows, colWidths=[46 * mm, 26 * mm, 26 * mm, 26 * mm, 58 * mm])
    budget_tbl.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor("#f1f5f9")),
        ('BOX', (0, 0), (-1, -1), 0.5, colors.HexColor("#cbd5e1")),
        ('INNERGRID', (0, 0), (-1, -1), 0.5, colors.HexColor("#e2e8f0")),
        ('TOPPADDING', (0, 0), (-1, -1), 3),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 3),
        ('LEFTPADDING', (0, 0), (-1, -1), 5),
        ('RIGHTPADDING', (0, 0), (-1, -1), 5),
    ]))
    story.append(budget_tbl)
    story.append(Spacer(1, 2.5 * mm))

    summary_callout = [
        [Paragraph("<b>Đánh giá Hiệu Quả &amp; Khuyến Nghị DevOps:</b><br/>"
                   "1. <b>Kiểm soát Ngân sách Xuất sắc:</b> Hạ tầng vận hành cả tháng chỉ tiêu thụ $5.92 USD, sau khi trừ credit thì thực trả là $0.00 USD, hoàn toàn nằm dưới trần $10.00 của AWS Budgets.<br/>"
                   "2. <b>Độ Tin cậy Tuyệt đối:</b> Trong suốt toàn bộ các đợt chạy test hồi quy của Tester Thanh, không xảy ra bất kỳ sự cố OOM hay khởi động lại nào. RAM task definition 2048 MiB dư thừa hơn 50% dung lượng an toàn.<br/>"
                   "3. <b>Khuyến nghị Vận hành:</b> Tiếp tục duy trì quy trình <code>Hibernate</code> sau các đợt kiểm thử để giữ chi phí nhàn rỗi ở mức $0.00/h.", callout_style)]
    ]
    sum_tbl = Table(summary_callout, colWidths=[182 * mm])
    sum_tbl.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), colors.HexColor("#ecfdf5")),
        ('LINELEFT', (0, 0), (0, -1), 3 * mm, colors.HexColor("#10b981")),
        ('TOPPADDING', (0, 0), (-1, -1), 4),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 4),
        ('LEFTPADDING', (0, 0), (-1, -1), 8),
        ('RIGHTPADDING', (0, 0), (-1, -1), 8),
    ]))
    story.append(sum_tbl)

    # Build document
    doc.build(story, canvasmaker=NumberedCanvas)
    print(f"SUCCESS: Generated PDF at {pdf_out} ({os.path.getsize(pdf_out)} bytes)")

if __name__ == "__main__":
    build_pdf_report()
