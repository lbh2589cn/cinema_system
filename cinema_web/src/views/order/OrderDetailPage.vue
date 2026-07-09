<template>
    <div class="page-container" v-loading="loading">
        <el-card class="page-card" v-if="order">
            <template #header>
                <span class="card-title">Order Details</span>
            </template>
            <el-descriptions :column="1" border>
                <el-descriptions-item label="Order No.">{{ order.orderNo }}</el-descriptions-item>
                <el-descriptions-item label="Status">
                    <el-tag :type="statusType">{{ statusLabel }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="Seat Count">{{ order.seatCount }}</el-descriptions-item>
                <el-descriptions-item label="Total Price">¥{{ order.totalAmount.toFixed(2) }}</el-descriptions-item>
                <el-descriptions-item label="Discount">¥{{ order.discountAmount.toFixed(2) }}</el-descriptions-item>
                <el-descriptions-item label="Amount Paid">¥{{ order.finalAmount.toFixed(2) }}</el-descriptions-item>
                <el-descriptions-item label="Created At">{{ formatDate(order.createdAt) }}</el-descriptions-item>
            </el-descriptions>

            <el-divider />
            <h3>Order Items</h3>
            <el-table :data="order.items" style="width: 100%; margin-top: 12px">
                <el-table-column prop="itemName" label="Item" />
                <el-table-column prop="itemType" label="Type" width="100">
                    <template #default="{ row }">{{ row.itemType === 'SEAT' ? 'Seat' : 'Snack' }}</template>
                </el-table-column>
                <el-table-column prop="quantity" label="Qty" width="80" />
                <el-table-column prop="unitPrice" label="Unit Price" width="100">
                    <template #default="{ row }">¥{{ row.unitPrice.toFixed(2) }}</template>
                </el-table-column>
                <el-table-column prop="subtotal" label="Subtotal" width="100">
                    <template #default="{ row }">¥{{ row.subtotal.toFixed(2) }}</template>
                </el-table-column>
            </el-table>

            <el-divider v-if="order.payments && order.payments.length" />
            <h3 v-if="order.payments && order.payments.length">Payment Records</h3>
            <el-table v-if="order.payments && order.payments.length" :data="order.payments" style="width: 100%; margin-top: 12px">
                <el-table-column prop="paymentNo" label="Transaction No." min-width="160" />
                <el-table-column prop="amount" label="Amount" width="100">
                    <template #default="{ row }">¥{{ row.amount.toFixed(2) }}</template>
                </el-table-column>
                <el-table-column prop="paymentMethod" label="Payment Method" min-width="120">
                    <template #default="{ row }">{{ paymentMethodLabel(row.paymentMethod) }}</template>
                </el-table-column>
                <el-table-column prop="status" label="Status" width="100">
                    <template #default="{ row }">
                        <el-tag :type="row.status === 'SUCCESS' ? 'success' : row.status === 'FAILED' ? 'danger' : 'info'" size="small">
                            {{ paymentStatusLabel(row.status) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="Payment Time" min-width="160">
                    <template #default="{ row }">{{ formatDate(row.paidAt) }}</template>
                </el-table-column>
            </el-table>

            <div class="actions" v-if="order.status === 'PENDING'">
                <el-button type="primary" @click="router.push(`/order/payment?orderId=${order.id}`)">Go to Payment</el-button>
            </div>
        </el-card>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderApi } from '@/api/order'
import { formatDate } from '@/composables/useDateFormatter'

const route = useRoute()
const router = useRouter()
const order = ref<any>(null)
const loading = ref(false)

const statusType = computed(() => {
    const map: Record<string, string> = { PENDING: 'warning', PAID: 'success', REFUNDED: 'info', CANCELLED: 'info' }
    return map[order.value?.status] || 'info'
})

const statusLabel = computed(() => {
    const map: Record<string, string> = { PENDING: 'Pending Payment', PAID: 'Paid', REFUNDED: 'Refunded', CANCELLED: 'Cancelled' }
    return map[order.value?.status] || order.value?.status
})

function paymentMethodLabel(method: string) {
    const map: Record<string, string> = { WECHAT: 'WeChat Pay', ALIPAY: 'Alipay', CREDIT_CARD: 'Credit Card', BALANCE: 'Balance' }
    return map[method] || method
}

function paymentStatusLabel(status: string) {
    const map: Record<string, string> = { PENDING: 'Pending', SUCCESS: 'Success', FAILED: 'Failed', REFUNDED: 'Refunded' }
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
