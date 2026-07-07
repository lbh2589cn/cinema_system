<template>
    <div>
        <div class="page-header">
            <h2 class="page-title">定价规则管理</h2>
            <el-button type="primary" @click="handleAdd">新增规则</el-button>
        </div>
        <el-card class="page-card">
            <el-table :data="rules" v-loading="loading" style="width: 100%">
                <el-table-column prop="ruleName" label="规则名称" min-width="150" />
                <el-table-column prop="ruleValue" label="规则值" width="100">
                    <template #default="{ row }">{{ row.ruleValue }}</template>
                </el-table-column>
                <el-table-column prop="priority" label="优先级" width="160">
                    <template #default="{ row }">
                        <el-button text type="primary" size="small" :disabled="isFirst(row)" @click="moveUp(row)">↑</el-button>
                        <span style="margin: 0 8px">{{ row.priority }}</span>
                        <el-button text type="primary" size="small" :disabled="isLast(row)" @click="moveDown(row)">↓</el-button>
                    </template>
                </el-table-column>
                <el-table-column prop="enabled" label="启用" width="80">
                    <template #default="{ row }">
                        <el-switch v-model="row.enabled" @change="handleToggle(row)" />
                    </template>
                </el-table-column>
                <el-table-column prop="description" label="说明" min-width="200" />
                <el-table-column label="条件" min-width="250">
                    <template #default="{ row }">{{ formatConditions(row) }}</template>
                </el-table-column>
                <el-table-column label="操作" width="160">
                    <template #default="{ row }">
                        <el-button text type="primary" @click="handleEdit(row)">编辑</el-button>
                        <el-button text type="danger" @click="handleDelete(row)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-card>

        <div class="pagination-wrapper" v-if="total > 0">
            <el-pagination
                v-model:current-page="page"
                :page-size="size"
                :total="total"
                layout="prev, pager, next"
                @current-change="loadRules"
            />
        </div>

        <el-dialog v-model="showDialog" :title="isEdit ? '编辑定价规则' : '新增定价规则'" width="600px">
            <el-form :model="editForm" label-width="120px">
                <el-form-item label="规则名称">
                    <el-input v-model="editForm.ruleName" />
                </el-form-item>
                <el-form-item label="规则值">
                    <el-input-number v-model="editForm.ruleValue" :step="0.01" :precision="4" />
                </el-form-item>
                <el-form-item v-if="isEdit" label="优先级">
                    <el-input-number v-model="editForm.priority" :min="0" />
                </el-form-item>
                <el-form-item label="启用">
                    <el-switch v-model="editForm.enabled" />
                </el-form-item>
                <el-form-item label="说明">
                    <el-input v-model="editForm.description" type="textarea" />
                </el-form-item>

                <el-divider content-position="left">条件配置</el-divider>

                <el-form-item label="是否为会员">
                    <el-select v-model="editForm.conditionMember" placeholder="不限制" style="width: 100%" clearable>
                        <el-option label="是" :value="true" />
                        <el-option label="否" :value="false" />
                    </el-select>
                </el-form-item>

                <el-form-item label="星期几">
                    <el-select v-model="editForm.conditionWeekdays" multiple placeholder="不限制" style="width: 100%">
                        <el-option label="周一" value="MONDAY" />
                        <el-option label="周二" value="TUESDAY" />
                        <el-option label="周三" value="WEDNESDAY" />
                        <el-option label="周四" value="THURSDAY" />
                        <el-option label="周五" value="FRIDAY" />
                        <el-option label="周六" value="SATURDAY" />
                        <el-option label="周日" value="SUNDAY" />
                    </el-select>
                </el-form-item>

                <el-form-item label="时间范围">
                    <div style="display: flex; gap: 10px">
                        <el-time-picker v-model="editForm.conditionTimeStart" value-format="HH:mm" format="HH:mm" placeholder="开始" />
                        <span>-</span>
                        <el-time-picker v-model="editForm.conditionTimeEnd" value-format="HH:mm" format="HH:mm" placeholder="结束" />
                    </div>
                </el-form-item>

                <el-form-item label="购买票数">
                    <div style="display: flex; gap: 10px; align-items: center">
                        <span>≥</span>
                        <el-input-number v-model="editForm.conditionTicketMin" :min="0" :precision="0" />
                        <span style="margin: 0 10px">且</span>
                        <span>≤</span>
                        <el-input-number v-model="editForm.conditionTicketMax" :min="0" :precision="0" />
                    </div>
                </el-form-item>

                <el-form-item label="剩余座位比例">
                    <div style="display: flex; gap: 10px; align-items: center">
                        <span>≥</span>
                        <el-input-number v-model="editForm.conditionSeatRatioMin" :min="0" :max="1" :step="0.05" :precision="2" />
                        <span style="margin: 0 10px">且</span>
                        <span>≤</span>
                        <el-input-number v-model="editForm.conditionSeatRatioMax" :min="0" :max="1" :step="0.05" :precision="2" />
                    </div>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="showDialog = false">取消</el-button>
                <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { updatePricingRuleApi, createPricingRuleApi, deletePricingRuleApi } from '@/api/pricing'
import { getAdminPricingRulesApi } from '@/api/admin'
import type { PricingRule } from '@/api/pricing'

const rules = ref<PricingRule[]>([])
const loading = ref(false)
const saving = ref(false)
const showDialog = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const editForm = reactive({
    ruleName: '',
    ruleValue: 1,
    priority: 0,
    enabled: true,
    description: '',

    conditionMember: null as boolean | null,
    conditionWeekdays: null as string[] | null,
    conditionTimeStart: null as string | null,
    conditionTimeEnd: null as string | null,
    conditionTicketMin: null as number | null,
    conditionTicketMax: null as number | null,
    conditionSeatRatioMin: null as number | null,
    conditionSeatRatioMax: null as number | null,
})

const weekdayMap: Record<string, string> = {
    MONDAY: '周一',
    TUESDAY: '周二',
    WEDNESDAY: '周三',
    THURSDAY: '周四',
    FRIDAY: '周五',
    SATURDAY: '周六',
    SUNDAY: '周日',
}

function formatConditions(row: PricingRule): string {
    const parts: string[] = []
    if (row.conditionMember !== null) {
        parts.push(row.conditionMember ? '会员' : '非会员')
    }
    if (row.conditionWeekdays) {
        const weekdays = (Array.isArray(row.conditionWeekdays) ? row.conditionWeekdays : row.conditionWeekdays.split(',')).map(d => weekdayMap[d] || d)
        parts.push(weekdays.join('、'))
    }
    if (row.conditionTimeStart || row.conditionTimeEnd) {
        const start = row.conditionTimeStart || '--:--'
        const end = row.conditionTimeEnd || '--:--'
        parts.push(`${start}-${end}`)
    }
    if (row.conditionTicketMin !== null || row.conditionTicketMax !== null) {
        const min = row.conditionTicketMin !== null ? `≥${row.conditionTicketMin}` : ''
        const max = row.conditionTicketMax !== null ? `≤${row.conditionTicketMax}` : ''
        parts.push(min && max ? `${min}且${max}` : min || max)
    }
    if (row.conditionSeatRatioMin !== null || row.conditionSeatRatioMax !== null) {
        const min = row.conditionSeatRatioMin !== null ? `≥${(row.conditionSeatRatioMin * 100).toFixed(0)}%` : ''
        const max = row.conditionSeatRatioMax !== null ? `≤${(row.conditionSeatRatioMax * 100).toFixed(0)}%` : ''
        parts.push(min && max ? `${min}且${max}` : min || max)
    }
    return parts.length > 0 ? parts.join('，') : '无限制'
}

async function loadRules() {
    loading.value = true
    try {
        const result = await getAdminPricingRulesApi({ page: page.value - 1, size: size.value })
        rules.value = result.content
        total.value = result.total
    } finally {
        loading.value = false
    }
}

function resetForm() {
    editForm.ruleName = ''
    editForm.ruleValue = 1
    editForm.priority = 0
    editForm.enabled = true
    editForm.description = ''
    editForm.conditionMember = null
    editForm.conditionWeekdays = null
    editForm.conditionTimeStart = null
    editForm.conditionTimeEnd = null
    editForm.conditionTicketMin = null
    editForm.conditionTicketMax = null
    editForm.conditionSeatRatioMin = null
    editForm.conditionSeatRatioMax = null
}

function handleAdd() {
    isEdit.value = false
    editId.value = null
    resetForm()
    showDialog.value = true
}

function handleEdit(rule: PricingRule) {
    isEdit.value = true
    editId.value = rule.id
    editForm.ruleName = rule.ruleName
    editForm.ruleValue = rule.ruleValue
    editForm.priority = rule.priority
    editForm.enabled = rule.enabled
    editForm.description = rule.description || ''
    editForm.conditionMember = rule.conditionMember
    editForm.conditionWeekdays = rule.conditionWeekdays ? rule.conditionWeekdays.split(',') : null
    editForm.conditionTimeStart = rule.conditionTimeStart
    editForm.conditionTimeEnd = rule.conditionTimeEnd
    editForm.conditionTicketMin = rule.conditionTicketMin
    editForm.conditionTicketMax = rule.conditionTicketMax
    editForm.conditionSeatRatioMin = rule.conditionSeatRatioMin
    editForm.conditionSeatRatioMax = rule.conditionSeatRatioMax
    showDialog.value = true
}

async function handleSave() {
    saving.value = true
    try {
        if (isEdit.value && editId.value) {
            const updateData = { ...editForm, conditionWeekdays: editForm.conditionWeekdays?.join(',') || null }
            await updatePricingRuleApi(editId.value, updateData)
            ElMessage.success('更新成功')
        } else {
            await createPricingRuleApi({
                ruleName: editForm.ruleName,
                ruleValue: editForm.ruleValue,
                enabled: editForm.enabled,
                description: editForm.description,
                conditionMember: editForm.conditionMember,
                conditionWeekdays: editForm.conditionWeekdays?.join(',') || null,
                conditionTimeStart: editForm.conditionTimeStart,
                conditionTimeEnd: editForm.conditionTimeEnd,
                conditionTicketMin: editForm.conditionTicketMin,
                conditionTicketMax: editForm.conditionTicketMax,
                conditionSeatRatioMin: editForm.conditionSeatRatioMin,
                conditionSeatRatioMax: editForm.conditionSeatRatioMax,
            })
            ElMessage.success('创建成功')
        }
        showDialog.value = false
        await loadRules()
    } finally {
        saving.value = false
    }
}

async function handleToggle(rule: PricingRule) {
    try {
        await updatePricingRuleApi(rule.id, { enabled: rule.enabled })
        ElMessage.success('更新成功')
    } catch {
        rule.enabled = !rule.enabled
    }
}

async function handleDelete(rule: PricingRule) {
    try {
        await ElMessageBox.confirm('确定要删除该规则吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
        await deletePricingRuleApi(rule.id)
        ElMessage.success('删除成功')
        await loadRules()
    } catch {
        // cancelled
    }
}

function isFirst(rule: PricingRule): boolean {
    return rules.value.indexOf(rule) === 0
}

function isLast(rule: PricingRule): boolean {
    return rules.value.indexOf(rule) === rules.value.length - 1
}

async function moveUp(rule: PricingRule) {
    const index = rules.value.indexOf(rule)
    if (index <= 0) return
    const prevRule = rules.value[index - 1]
    const tempPriority = rule.priority
    try {
        await updatePricingRuleApi(rule.id, { priority: prevRule.priority })
        await updatePricingRuleApi(prevRule.id, { priority: tempPriority })
        await loadRules()
        ElMessage.success('排序已更新')
    } catch {
        ElMessage.error('排序更新失败')
    }
}

async function moveDown(rule: PricingRule) {
    const index = rules.value.indexOf(rule)
    if (index >= rules.value.length - 1) return
    const nextRule = rules.value[index + 1]
    const tempPriority = rule.priority
    try {
        await updatePricingRuleApi(rule.id, { priority: nextRule.priority })
        await updatePricingRuleApi(nextRule.id, { priority: tempPriority })
        await loadRules()
        ElMessage.success('排序已更新')
    } catch {
        ElMessage.error('排序更新失败')
    }
}

onMounted(loadRules)
</script>

<style scoped lang="scss">
.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.page-title {
    font-size: 20px;
}

.pagination-wrapper {
    display: flex;
    justify-content: center;
    margin-top: 20px;
}
</style>
