<template>
    <div>
        <h2 class="page-title">Order Management</h2>
        <el-card class="page-card">
            <el-table :data="orders" v-loading="loading" style="width: 100%">
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="orderNo" label="Order No." width="200" />
                <el-table-column prop="userId" label="User ID" width="80" />
                <el-table-column prop="seatCount" label="Seats" width="80" />
                <el-table-column prop="totalAmount" label="Total" width="100">
                    <template #default="{ row }">¥{{ row.totalAmount.toFixed(2) }}</template>
                </el-table-column>
                <el-table-column prop="finalAmount" label="Amount Paid" width="100">
                    <template #default="{ row }">¥{{ row.finalAmount.toFixed(2) }}</template>
                </el-table-column>
                <el-table-column prop="status" label="Status" width="120">
                    <template #default="{ row }">
                        <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="Created At" width="180">
                    <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
                </el-table-column>
                <el-table-column label="Actions" width="250">
                    <template #default="{ row }">
                        <el-button text type="primary" @click="viewDetail(row)">Details</el-button>
                        <el-button text type="danger" @click="handleDelete(row)">Delete</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <div class="pagination" v-if="total > 0">
                <el-pagination v-model:current-page="page" :page-size="size" :total="total" layout="prev, pager, next" @current-change="loadOrders" />
            </div>
        </el-card>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminOrdersApi, hardDeleteOrderApi } from '@/api/admin'
import type { Order } from '@/api/order'
import { formatDate } from '@/composables/useDateFormatter'

const router = useRouter()
const orders = ref<Order[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

function statusType(status: string) {
    const map: Record<string, string> = { PENDING: 'warning', PAID: 'success', REFUNDING: 'warning', REFUNDED: 'info', CANCELLED: 'info' }
    return map[status] || 'info'
}

function statusLabel(status: string) {
    const map: Record<string, string> = { PENDING: 'Pending', PAID: 'Paid', REFUNDING: 'Refunding', REFUNDED: 'Refunded', CANCELLED: 'Cancelled' }
    return map[status] || status
}

function viewDetail(order: Order) {
    router.push(`/orders/${order.id}?from=admin`)
}

async function handleDelete(order: Order) {
    try {
        await ElMessageBox.confirm(`Confirm permanent deletion of order ${order.orderNo}? This will also delete related payment records and cannot be undone.`, 'Confirm Delete', {
            confirmButtonText: 'Confirm Delete',
            cancelButtonText: 'Cancel',
            type: 'warning',
        })
        await hardDeleteOrderApi(order.id)
        ElMessage.success('Order permanently deleted')
        await loadOrders()
    } catch {
        // cancelled or error
    }
}

async function loadOrders() {
    loading.value = true
    try {
        const result = await getAdminOrdersApi({ page: page.value - 1, size: size.value })
        orders.value = result.content
        total.value = result.total
    } finally {
        loading.value = false
    }
}

onMounted(loadOrders)
</script>

<style scoped lang="scss">
.page-title {
    font-size: 20px;
    margin-bottom: 20px;
}

.pagination {
    display: flex;
    justify-content: center;
    margin-top: 20px;
}
</style>
