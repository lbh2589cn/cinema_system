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
    seatType: string
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
