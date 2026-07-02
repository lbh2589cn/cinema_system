import request from './request'

export interface PricingRule {
    id: number
    ruleName: string
    ruleType: string
    ruleValue: number
    priority: number
    enabled: boolean
    description: string
}

export function getPricingRulesApi(): Promise<PricingRule[]> {
    return request.get('/api/pricing-rules')
}

export function updatePricingRuleApi(id: number, data: Partial<PricingRule>): Promise<PricingRule> {
    return request.put(`/api/pricing-rules/${id}`, data)
}
