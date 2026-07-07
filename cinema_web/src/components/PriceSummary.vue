<template>
    <div class="price-summary">
        <div class="summary-row" v-if="totalAmount > 0">
            <span>原价</span>
            <span class="amount">¥{{ totalAmount.toFixed(2) }}</span>
        </div>
        <div class="breakdown" v-if="items && items.length > 0">
            <div class="breakdown-item" v-for="item in items" :key="item.ruleName">
                <span>{{ item.ruleName }}</span>
                <span class="amount" :class="item.impact > 0 ? 'surcharge' : 'discount'">
                    {{ item.impact > 0 ? '+' : '-' }}¥{{ Math.abs(item.impact).toFixed(2) }}
                </span>
            </div>
        </div>
        <el-divider style="margin: 8px 0" />
        <div class="summary-row final">
            <span>应付金额</span>
            <span class="amount">¥{{ finalAmount.toFixed(2) }}</span>
        </div>
    </div>
</template>

<script setup lang="ts">
import type { DiscountItem } from '@/api/pricing'

defineProps<{
    totalAmount: number
    items?: DiscountItem[]
    finalAmount: number
}>()
</script>

<style scoped lang="scss">
.price-summary {
    padding: 16px 0;
    border-top: 1px solid #ebeef5;
}

.summary-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 4px 0;
    font-size: 14px;
    color: #606266;

    &.final {
        font-size: 16px;
        font-weight: 600;
        color: #303133;

        .amount {
            color: #f56c6c;
            font-size: 22px;
        }
    }
}

.breakdown {
    padding: 4px 0 4px 16px;

    .breakdown-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 3px 0;
        font-size: 13px;
        color: #909399;

        .amount {
            &.discount {
                color: #67c23a;
            }
            &.surcharge {
                color: #e6a23c;
            }
        }
    }
}
</style>
