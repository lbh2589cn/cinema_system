<template>
    <div class="price-summary">
        <div class="summary-row" v-if="totalAmount > 0">
            <span>总价</span>
            <span class="amount">¥{{ totalAmount.toFixed(2) }}</span>
        </div>
        <div class="summary-row discount" v-if="discountAmount > 0">
            <span>优惠</span>
            <span class="amount">-¥{{ discountAmount.toFixed(2) }}</span>
        </div>
        <div class="summary-row final" v-if="finalAmount > 0">
            <span>实付</span>
            <span class="amount">¥{{ finalAmount.toFixed(2) }}</span>
        </div>
    </div>
</template>

<script setup lang="ts">
defineProps<{
    totalAmount: number
    discountAmount: number
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

    &.discount .amount {
        color: #67c23a;
    }

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
</style>
