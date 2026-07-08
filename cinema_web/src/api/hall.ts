import request from './request'

export interface Hall {
    id: number
    name: string
    rows: number
    cols: number
    seatCount: number
    description: string
}

export interface HallSeat {
    id: number
    hallId: number
    rowNum: number
    colNum: number
    status: string
}

export interface SeatTypeUpdate {
    rowNum: number
    colNum: number
    status: string
}

export function getHallsApi(): Promise<Hall[]> {
    return request.get('/api/halls')
}

export function getHallDetailApi(id: number): Promise<{ hall: Hall; seats: HallSeat[] }> {
    return request.get(`/api/halls/${id}`)
}

export function createHallApi(data: { name: string; rows: number; cols: number; description?: string }): Promise<Hall> {
    return request.post('/api/halls', data)
}

export function updateHallApi(id: number, data: { name: string; rows: number; cols: number; description?: string; seats?: SeatTypeUpdate[] }): Promise<Hall> {
    return request.put(`/api/halls/${id}`, data)
}

export function deleteHallApi(id: number): Promise<void> {
    return request.delete(`/api/halls/${id}`)
}
