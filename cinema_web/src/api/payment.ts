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

export function payApi(data: { orderId: number; paymentMethod: string }): Promise<Payment> {
    return request.post('/api/payments/pay', data)
}
