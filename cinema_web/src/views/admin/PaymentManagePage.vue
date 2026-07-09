<template>
    <div class="page-container" v-loading="loading">
        <el-card class="page-card">
            <template #header>
                <span class="card-title">Payments</span>
            </template>

            <el-table :data="payments" style="width: 100%">
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="paymentNo" label="Payment No." width="180" />
                <el-table-column prop="orderNo" label="Order No." width="180" />
                <el-table-column prop="userId" label="User ID" width="80" />
                <el-table-column prop="amount" label="Amount" width="100">
                    <template #default="{ row }">¥{{ row.amount.toFixed(2) }}</template>
                </el-table-column>
                <el-table-column prop="paymentMethod" label="Payment Method" width="120">
                    <template #default="{ row }">{{ methodLabel(row.paymentMethod) }}</template>
                </el-table-column>
                <el-table-column prop="status" label="Status" width="100">
                    <template #default="{ row }">
                        <el-tag :type="statusType(row.status)" size="small">
                            {{ statusLabel(row.status) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="Paid At" width="170">
                    <template #default="{ row }">{{ formatDate(row.paidAt) }}</template>
                </el-table-column>
                <el-table-column label="Created At" width="170">
                    <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
                </el-table-column>
                <el-table-column label="Actions" width="150">
                    <template #default="{ row }">
                        <el-button text type="danger" size="small" @click="handleDelete(row)">Delete</el-button>
                    </template>
                </el-table-column>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminPaymentsApi, deleteAdminPaymentApi } from '@/api/payment'
import { formatDate } from '@/composables/useDateFormatter'

const payments = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

function methodLabel(method: string) {
    const map: Record<string, string> = { WECHAT: 'WeChat Pay', ALIPAY: 'Alipay', CREDIT_CARD: 'Credit Card', BALANCE: 'Balance' }
    return map[method] || method
}

function statusType(status: string) {
    const map: Record<string, string> = { SUCCESS: 'success', FAILED: 'danger', PENDING: 'warning', REFUNDED: 'info' }
    return map[status] || 'info'
}

function statusLabel(status: string) {
    const map: Record<string, string> = { PENDING: 'Pending', SUCCESS: 'Success', FAILED: 'Failed', REFUNDED: 'Refunded' }
    return map[status] || status
}

async function handleDelete(row: any) {
    try {
        await ElMessageBox.confirm(
            `Confirm permanent deletion of transaction record ${row.paymentNo}? This action cannot be undone.`,
            'Confirm Delete',
            { confirmButtonText: 'Confirm Delete', cancelButtonText: 'Cancel', type: 'warning' }
        )
        await deleteAdminPaymentApi(row.id)
        ElMessage.success('Transaction record permanently deleted')
        await fetchPayments()
    } catch {
        // cancelled or error
    }
}

async function fetchPayments() {
    loading.value = true
    try {
        const res = await getAdminPaymentsApi({ page: page.value - 1, size: size.value })
        payments.value = res.content
        total.value = res.total
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
