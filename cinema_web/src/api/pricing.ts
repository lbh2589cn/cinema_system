import request from './request'

export interface PricingRule {
    id: number
    ruleName: string
    ruleValue: number
    priority: number
    enabled: boolean
    description: string

    conditionMember: boolean | null
    conditionWeekdays: string | null
    conditionTimeStart: string | null
    conditionTimeEnd: string | null
    conditionTicketMin: number | null
    conditionTicketMax: number | null
    conditionSeatRatioMin: number | null
    conditionSeatRatioMax: number | null
}

export function getPricingRulesApi(): Promise<PricingRule[]> {
    return request.get('/api/pricing-rules')
}

export function updatePricingRuleApi(id: number, data: Partial<PricingRule>): Promise<PricingRule> {
    return request.put(`/api/pricing-rules/${id}`, data)
}

export function createPricingRuleApi(data: Omit<PricingRule, 'id' | 'priority'>): Promise<PricingRule> {
    return request.post('/api/pricing-rules', data)
}

export interface DiscountItem {
    ruleName: string
    description: string
    impact: number
}

export interface DiscountResult {
    originalAmount: number
    finalAmount: number
    totalDiscount: number
    items: DiscountItem[]
}

export function calculatePriceApi(data: { showingId: number; ticketCount: number; totalAmount: number }): Promise<DiscountResult> {
    return request.post('/api/pricing-rules/calculate', data)
}
