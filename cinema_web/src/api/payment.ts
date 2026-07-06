import request from './request'

export interface Payment {
    id: number
    orderId: number
    paymentNo: string
    amount: number
    paymentMethod: string
    status: string
    paidAt: string
}

export interface PaymentRecord {
    id: number
    paymentNo: string
    amount: number
    paymentMethod: string
    status: string
    paidAt: string
    createdAt: string
}

export function payApi(data: { orderId: number; paymentMethod: string }): Promise<Payment> {
    return request.post('/api/payments/pay', data)
}

export function getAdminPaymentsApi(params: {
    page?: number
    size?: number
}): Promise<{ content: any[]; total: number }> {
    return request.get('/api/admin/payments', { params })
}

export function deleteAdminPaymentApi(id: number): Promise<void> {
    return request.delete(`/api/admin/payments/${id}`)
}
