import request from './request'

export interface SeatStatus {
    id: number
    seatId: number
    rowNum: number
    colNum: number
    seatType: string
    status: string
}

export interface LockSeatRequest {
    showingId: number
    seatIds: number[]
}

export function getSeatsApi(showingId: number): Promise<SeatStatus[]> {
    return request.get(`/api/showings/${showingId}/seats`)
}

export function lockSeatsApi(data: LockSeatRequest): Promise<void> {
    return request.post('/api/seats/lock', data)
}

export function unlockSeatsApi(data: LockSeatRequest): Promise<void> {
    return request.post('/api/seats/unlock', data)
}
