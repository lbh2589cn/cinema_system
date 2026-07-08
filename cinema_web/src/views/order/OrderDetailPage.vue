<template>
    <div class="page-container" v-loading="loading">
        <el-card class="page-card" v-if="order">
            <template #header>
                <span class="card-title">订单详情</span>
            </template>
            <el-descriptions :column="1" border>
                <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
                <el-descriptions-item label="状态">
                    <el-tag :type="statusType">{{ statusLabel }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="座位数量">{{ order.seatCount }}</el-descriptions-item>
                <el-descriptions-item label="总价">¥{{ order.totalAmount.toFixed(2) }}</el-descriptions-item>
                <el-descriptions-item label="优惠">¥{{ order.discountAmount.toFixed(2) }}</el-descriptions-item>
                <el-descriptions-item label="实付">¥{{ order.finalAmount.toFixed(2) }}</el-descriptions-item>
                <el-descriptions-item label="创建时间">{{ order.createdAt }}</el-descriptions-item>
            </el-descriptions>

            <el-divider />
            <h3>订单明细</h3>
            <el-table :data="order.items" style="width: 100%; margin-top: 12px">
                <el-table-column prop="itemName" label="项目" />
                <el-table-column prop="itemType" label="类型" width="100">
                    <template #default="{ row }">{{ row.itemType === 'SEAT' ? '座位' : '零食' }}</template>
                </el-table-column>
                <el-table-column prop="quantity" label="数量" width="80" />
                <el-table-column prop="unitPrice" label="单价" width="100">
                    <template #default="{ row }">¥{{ row.unitPrice.toFixed(2) }}</template>
                </el-table-column>
                <el-table-column prop="subtotal" label="小计" width="100">
                    <template #default="{ row }">¥{{ row.subtotal.toFixed(2) }}</template>
                </el-table-column>
            </el-table>

            <el-divider v-if="order.payments && order.payments.length" />
            <h3 v-if="order.payments && order.payments.length">支付记录</h3>
            <el-table v-if="order.payments && order.payments.length" :data="order.payments" style="width: 100%; margin-top: 12px">
                <el-table-column prop="paymentNo" label="流水号" min-width="160" />
                <el-table-column prop="amount" label="金额" width="100">
                    <template #default="{ row }">¥{{ row.amount.toFixed(2) }}</template>
                </el-table-column>
                <el-table-column prop="paymentMethod" label="支付方式" min-width="120">
                    <template #default="{ row }">{{ paymentMethodLabel(row.paymentMethod) }}</template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                    <template #default="{ row }">
                        <el-tag :type="row.status === 'SUCCESS' ? 'success' : row.status === 'FAILED' ? 'danger' : 'info'" size="small">
                            {{ paymentStatusLabel(row.status) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="paidAt" label="支付时间" min-width="160">
                    <template #default="{ row }">{{ row.paidAt || '-' }}</template>
                </el-table-column>
            </el-table>

            <div class="actions" v-if="order.status === 'PENDING'">
                <el-button type="primary" @click="router.push(`/order/payment?orderId=${order.id}`)">去支付</el-button>
            </div>
        </el-card>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderApi } from '@/api/order'

const route = useRoute()
const router = useRouter()
const order = ref<any>(null)
const loading = ref(false)

const statusType = computed(() => {
    const map: Record<string, string> = { PENDING: 'warning', PAID: 'success', REFUNDED: 'info', CANCELLED: 'info' }
    return map[order.value?.status] || 'info'
})

const statusLabel = computed(() => {
    const map: Record<string, string> = { PENDING: '待支付', PAID: '已支付', REFUNDED: '已退款', CANCELLED: '已取消' }
    return map[order.value?.status] || order.value?.status
})

function paymentMethodLabel(method: string) {
    const map: Record<string, string> = { WECHAT: '微信支付', ALIPAY: '支付宝', CREDIT_CARD: '信用卡', BALANCE: '余额' }
    return map[method] || method
}

function paymentStatusLabel(status: string) {
    const map: Record<string, string> = { PENDING: '待支付', SUCCESS: '支付成功', FAILED: '支付失败', REFUNDED: '已退款' }
    return map[status] || status
}

onMounted(async () => {
    loading.value = true
    try {
        const id = Number(route.params.id)
        order.value = await getOrderApi(id)
    } finally {
        loading.value = false
    }
})
</script>

<style scoped lang="scss">
.card-title {
    font-size: 16px;
    font-weight: 600;
}

h3 {
    font-size: 16px;
    font-weight: 600;
}

.actions {
    display: flex;
    justify-content: flex-end;
    margin-top: 24px;
}
</style>
