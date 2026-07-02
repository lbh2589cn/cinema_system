import request from './request'

export interface OrderItem {
    id: number
    orderId: number
    itemType: string
    itemId: number
    itemName: string
    quantity: number
    unitPrice: number
    subtotal: number
}

export interface Order {
    id: number
    orderNo: string
    userId: number
    showingId: number
    seatCount: number
    totalAmount: number
    discountAmount: number
    finalAmount: number
    status: string
    remark: string
    createdAt: string
}

export interface OrderDetail extends Order {
    items: OrderItem[]
}

export interface OrderCreateRequest {
    showingId: number
    seatBookingIds: number[]
    snackItems?: { snackId: number; quantity: number }[]
}

export function createOrderApi(data: OrderCreateRequest): Promise<Order> {
    return request.post('/api/orders', data)
}

export function getOrdersApi(params?: { page?: number; size?: number }): Promise<{ content: Order[]; total: number }> {
    return request.get('/api/orders', { params })
}

export function getOrderApi(id: number): Promise<OrderDetail> {
    return request.get(`/api/orders/${id}`)
}

export function refundOrderApi(id: number): Promise<void> {
    return request.post(`/api/orders/${id}/refund`)
}
