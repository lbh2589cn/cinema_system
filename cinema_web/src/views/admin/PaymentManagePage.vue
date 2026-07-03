<template>
    <div class="page-container" v-loading="loading">
        <el-card class="page-card">
            <template #header>
                <span class="card-title">交易记录</span>
            </template>

            <el-table :data="payments" style="width: 100%">
                <el-table-column prop="paymentNo" label="流水号" width="180" />
                <el-table-column prop="orderNo" label="订单号" width="180" />
                <el-table-column prop="userId" label="用户ID" width="80" />
                <el-table-column prop="amount" label="金额" width="100">
                    <template #default="{ row }">¥{{ row.amount.toFixed(2) }}</template>
                </el-table-column>
                <el-table-column prop="paymentMethod" label="支付方式" width="120">
                    <template #default="{ row }">{{ methodLabel(row.paymentMethod) }}</template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                    <template #default="{ row }">
                        <el-tag :type="statusType(row.status)" size="small">
                            {{ statusLabel(row.status) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="paidAt" label="支付时间" width="170">
                    <template #default="{ row }">{{ row.paidAt || '-' }}</template>
                </el-table-column>
                <el-table-column prop="createdAt" label="创建时间" width="170" />
            </el-table>

            <div class="pagination-wrapper" v-if="total > 0">
                <el-pagination
                    v-model:current-page="page"
                    :page-size="size"
                    :total="total"
                    layout="prev, pager, next"
                    @current-change="fetchPayments"
                />
            </div>
        </el-card>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getAdminPaymentsApi } from '@/api/payment'

const payments = ref<any[]>([])
const loading = ref(false)
const page = ref(0)
const size = ref(10)
const total = ref(0)

function methodLabel(method: string) {
    const map: Record<string, string> = { WECHAT: '微信支付', ALIPAY: '支付宝', CREDIT_CARD: '信用卡', BALANCE: '余额' }
    return map[method] || method
}

function statusType(status: string) {
    const map: Record<string, string> = { SUCCESS: 'success', FAILED: 'danger', PENDING: 'warning', REFUNDED: 'info' }
    return map[status] || 'info'
}

function statusLabel(status: string) {
    const map: Record<string, string> = { PENDING: '待支付', SUCCESS: '支付成功', FAILED: '支付失败', REFUNDED: '已退款' }
    return map[status] || status
}

async function fetchPayments() {
    loading.value = true
    try {
        const res = await getAdminPaymentsApi({ page: page.value, size: size.value })
        payments.value = res.content
        total.value = res.totalElements
    } finally {
        loading.value = false
    }
}

onMounted(fetchPayments)
</script>

<style scoped lang="scss">
.card-title {
    font-size: 16px;
    font-weight: 600;
}

.pagination-wrapper {
    display: flex;
    justify-content: center;
    margin-top: 20px;
}
</style>
