<template>
    <div>
        <div class="page-header">
            <h2 class="page-title">Pricing Rules</h2>
            <el-button type="primary" @click="handleAdd">Add Rule</el-button>
        </div>
        <el-card class="page-card">
            <el-table :data="rules" v-loading="loading" style="width: 100%">
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="ruleName" label="Rule Name" min-width="150" />
                <el-table-column prop="ruleValue" label="Rule Value" width="100">
                    <template #default="{ row }">{{ row.ruleValue }}</template>
                </el-table-column>
                <el-table-column prop="priority" label="Priority" width="160">
                    <template #default="{ row }">
                        <el-button text type="primary" size="small" :disabled="isFirst(row)" @click="moveUp(row)">↑</el-button>
                        <span style="margin: 0 8px">{{ row.priority }}</span>
                        <el-button text type="primary" size="small" :disabled="isLast(row)" @click="moveDown(row)">↓</el-button>
                    </template>
                </el-table-column>
                <el-table-column prop="enabled" label="Enabled" width="80">
                    <template #default="{ row }">
                        <el-switch v-model="row.enabled" @change="handleToggle(row)" />
                    </template>
                </el-table-column>
                <el-table-column prop="description" label="Description" min-width="200" />
                <el-table-column label="Conditions" min-width="250">
                    <template #default="{ row }">{{ formatConditions(row) }}</template>
                </el-table-column>
                <el-table-column label="Actions" width="250">
                    <template #default="{ row }">
                        <el-button text type="primary" @click="handleEdit(row)">Edit</el-button>
                        <el-button text type="danger" @click="handleDelete(row)">Delete</el-button>
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

        <el-dialog v-model="showDialog" :title="isEdit ? 'Edit Pricing Rule' : 'Add Pricing Rule'" width="600px">
            <el-form :model="editForm" label-width="120px">
                <el-form-item label="Rule Name">
                    <el-input v-model="editForm.ruleName" />
                </el-form-item>
                <el-form-item label="Rule Value">
                    <el-input-number v-model="editForm.ruleValue" :step="0.01" :precision="4" />
                </el-form-item>
                <el-form-item v-if="isEdit" label="Priority">
                    <el-input-number v-model="editForm.priority" :min="0" />
                </el-form-item>
                <el-form-item label="Enabled">
                    <el-switch v-model="editForm.enabled" />
                </el-form-item>
                <el-form-item label="Description">
                    <el-input v-model="editForm.description" type="textarea" />
                </el-form-item>

                <el-divider content-position="left">Condition Configuration</el-divider>

                <el-form-item label="Is VIP Member">
                    <el-select v-model="editForm.conditionMember" placeholder="No restriction" style="width: 100%" clearable>
                        <el-option label="Yes" :value="true" />
                        <el-option label="No" :value="false" />
                    </el-select>
                </el-form-item>

                <el-form-item label="Day of Week">
                    <el-select v-model="editForm.conditionWeekdays" multiple placeholder="No restriction" style="width: 100%">
                        <el-option label="Monday" value="MONDAY" />
                        <el-option label="Tuesday" value="TUESDAY" />
                        <el-option label="Wednesday" value="WEDNESDAY" />
                        <el-option label="Thursday" value="THURSDAY" />
                        <el-option label="Friday" value="FRIDAY" />
                        <el-option label="Saturday" value="SATURDAY" />
                        <el-option label="Sunday" value="SUNDAY" />
                    </el-select>
                </el-form-item>

                <el-form-item label="Time Range">
                    <div style="display: flex; gap: 10px">
                        <el-time-picker v-model="editForm.conditionTimeStart" value-format="HH:mm" format="HH:mm" placeholder="Start" />
                        <span>-</span>
                        <el-time-picker v-model="editForm.conditionTimeEnd" value-format="HH:mm" format="HH:mm" placeholder="End" />
                    </div>
                </el-form-item>

                <el-form-item label="Ticket Quantity">
                    <div style="display: flex; gap: 10px; align-items: center">
                        <span>≥</span>
                        <el-input-number v-model="editForm.conditionTicketMin" :min="0" :precision="0" />
                        <span style="margin: 0 10px">and</span>
                        <span>≤</span>
                        <el-input-number v-model="editForm.conditionTicketMax" :min="0" :precision="0" />
                    </div>
                </el-form-item>

                <el-form-item label="Remaining Seat Ratio">
                    <div style="display: flex; gap: 10px; align-items: center">
                        <span>≥</span>
                        <el-input-number v-model="editForm.conditionSeatRatioMin" :min="0" :max="1" :step="0.05" :precision="2" />
                        <span style="margin: 0 10px">and</span>
                        <span>≤</span>
                        <el-input-number v-model="editForm.conditionSeatRatioMax" :min="0" :max="1" :step="0.05" :precision="2" />
                    </div>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="showDialog = false">Cancel</el-button>
                <el-button type="primary" @click="handleSave" :loading="saving">Save</el-button>
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
    MONDAY: 'Monday',
    TUESDAY: 'Tuesday',
    WEDNESDAY: 'Wednesday',
    THURSDAY: 'Thursday',
    FRIDAY: 'Friday',
    SATURDAY: 'Saturday',
    SUNDAY: 'Sunday',
}

function formatConditions(row: PricingRule): string {
    const parts: string[] = []
    if (row.conditionMember !== null) {
        parts.push(row.conditionMember ? 'VIP' : 'Non-VIP')
    }
    if (row.conditionWeekdays) {
        const weekdays = (Array.isArray(row.conditionWeekdays) ? row.conditionWeekdays : row.conditionWeekdays.split(',')).map(d => weekdayMap[d] || d)
        parts.push(weekdays.join(', '))
    }
    if (row.conditionTimeStart || row.conditionTimeEnd) {
        const start = row.conditionTimeStart || '--:--'
        const end = row.conditionTimeEnd || '--:--'
        parts.push(`${start}-${end}`)
    }
    if (row.conditionTicketMin !== null || row.conditionTicketMax !== null) {
        const min = row.conditionTicketMin !== null ? `≥${row.conditionTicketMin}` : ''
        const max = row.conditionTicketMax !== null ? `≤${row.conditionTicketMax}` : ''
        parts.push(min && max ? `${min} and ${max}` : min || max)
    }
    if (row.conditionSeatRatioMin !== null || row.conditionSeatRatioMax !== null) {
        const min = row.conditionSeatRatioMin !== null ? `≥${(row.conditionSeatRatioMin * 100).toFixed(0)}%` : ''
        const max = row.conditionSeatRatioMax !== null ? `≤${(row.conditionSeatRatioMax * 100).toFixed(0)}%` : ''
        parts.push(min && max ? `${min} and ${max}` : min || max)
    }
    return parts.length > 0 ? parts.join(', ') : 'No restrictions'
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
            ElMessage.success('Update successful')
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
            ElMessage.success('Created successfully')
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
        ElMessage.success('Update successful')
    } catch {
        rule.enabled = !rule.enabled
    }
}

async function handleDelete(rule: PricingRule) {
    try {
        await ElMessageBox.confirm('Are you sure you want to delete this rule?', 'Notice', { confirmButtonText: 'Confirm', cancelButtonText: 'Cancel', type: 'warning' })
        await deletePricingRuleApi(rule.id)
        ElMessage.success('Deleted successfully')
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
        ElMessage.success('Order updated')
    } catch {
        ElMessage.error('Failed to update order')
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
        ElMessage.success('Order updated')
    } catch {
        ElMessage.error('Failed to update order')
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
