package com.example.GYM_management_api.services;

import com.example.GYM_management_api.dtos.CheckoutRequestDTO;
import com.example.GYM_management_api.dtos.OrderResponseDTO;

import java.util.List;

public interface IPosOrderService {

    /**
     * Tạo đơn hàng bán lẻ Mini POS với Database Transaction (@Transactional)
     * Đối soát tồn kho, trừ kho tức thời và rollback nếu có lỗi
     */
    OrderResponseDTO createOrder(CheckoutRequestDTO request, Long currentUserId);

    /**
     * Lấy chi tiết đơn hàng theo ID (xem/in phiếu hóa đơn)
     */
    OrderResponseDTO getOrderById(Long id);

    /**
     * Thống kê danh sách đơn hàng phát sinh trong ngày
     */
    List<OrderResponseDTO> getTodayOrders();
}
