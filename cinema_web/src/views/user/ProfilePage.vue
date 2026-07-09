<template>
    <div class="page-container">
        <el-card class="page-card">
            <template #header>
                <span class="card-title">My Account</span>
            </template>

            <el-descriptions :column="1" border>
                <el-descriptions-item label="Account">
                    {{ profile.userId }}
                </el-descriptions-item>
                <el-descriptions-item label="Username">
                    <div class="desc-content">
                        <template v-if="editingKey === 'username'">
                            <el-input v-model="editValue" size="small" :style="{ width: Math.max(120, editValue.length * 14 + 32) + 'px' }" class="dynamic-input" />
                            <span class="btn-group">
                                <el-button type="primary" size="small" @click="saveEdit('username')" :loading="saving">Save</el-button>
                                <el-button size="small" @click="cancelEdit">Cancel</el-button>
                            </span>
                        </template>
                        <template v-else>
                            <span>{{ profile.username }}</span>
                            <el-button type="primary" size="small" @click="startEdit('username')">
                                <el-icon><Edit /></el-icon>
                            </el-button>
                        </template>
                    </div>
                </el-descriptions-item>
                <el-descriptions-item label="Phone Number">
                    <div class="desc-content">
                        <template v-if="editingKey === 'phone'">
                            <el-input v-model="editValue" size="small" :style="{ width: Math.max(120, editValue.length * 14 + 32) + 'px' }" class="dynamic-input" />
                            <span class="btn-group">
                                <el-button type="primary" size="small" @click="saveEdit('phone')" :loading="saving">Save</el-button>
                                <el-button size="small" @click="cancelEdit">Cancel</el-button>
                            </span>
                        </template>
                        <template v-else>
                            <span>{{ profile.phone || '-' }}</span>
                            <el-button type="primary" size="small" @click="startEdit('phone')">
                                <el-icon><Edit /></el-icon>
                            </el-button>
                        </template>
                    </div>
                </el-descriptions-item>
                <el-descriptions-item label="Role">
                    <el-tag :type="profile.role === 'ADMIN' ? 'danger' : 'primary'">
                        {{ profile.role === 'ADMIN' ? 'Admin' : 'User' }}
                    </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="VIP Member">
                    <div class="desc-content">
                        <el-tag :type="profile.isMember ? 'success' : 'info'">
                            {{ profile.isMember ? 'Yes' : 'No' }}
                        </el-tag>
                        <el-button
                            :type="profile.isMember ? 'danger' : 'success'"
                            plain
                            size="small"
                            @click="handleMembership"
                        >
                            {{ profile.isMember ? 'Cancel Membership' : 'Become a Member' }}
                        </el-button>
                    </div>
                </el-descriptions-item>
                <el-descriptions-item label="Status">
                    <el-tag :type="profile.status === 'ACTIVE' ? 'success' : 'danger'">
                        {{ profile.status === 'ACTIVE' ? 'Active' : 'Disabled' }}
                    </el-tag>
                </el-descriptions-item>
            </el-descriptions>

            <div class="card-actions">
                <el-button type="warning" plain @click="showChangePasswordDialog">
                    Change Password
                </el-button>
                <el-button type="danger" plain @click="confirmDeleteAccount">
                    Delete Account
                </el-button>
            </div>
        </el-card>

        <!-- Change Password Dialog -->
        <el-dialog v-model="passwordDialogVisible" title="Change Password" width="400px">
            <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
                <el-form-item label="Current Password" prop="oldPassword">
                    <el-input v-model="passwordForm.oldPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="New Password" prop="newPassword">
                    <el-input v-model="passwordForm.newPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="Confirm New Password" prop="confirmPassword">
                    <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="passwordDialogVisible = false">Cancel</el-button>
                <el-button type="primary" @click="handleChangePassword" :loading="changingPwd">Confirm Change</el-button>
            </template>
        </el-dialog>

        <!-- VIP Member Payment Dialog -->
        <el-dialog v-model="memberPayDialogVisible" title="Become a Member" width="420px">
            <div class="member-pay-info">
                <div class="member-price">¥10</div>
                <div class="member-desc">Monthly fee · Enjoy exclusive discounts</div>
            </div>
            <el-divider />
            <h3 class="pay-method-title">— Select Payment Method —</h3>
            <el-radio-group v-model="memberPaymentMethod" class="pay-methods">
                <el-radio value="WECHAT" class="pay-method-item" border>
                    <el-icon><Iphone /></el-icon>
                    WeChat Pay
                </el-radio>
                <el-radio value="ALIPAY" class="pay-method-item" border>
                    <el-icon><Wallet /></el-icon>
                    Alipay
                </el-radio>
            </el-radio-group>
            <template #footer>
                <el-button @click="memberPayDialogVisible = false">Cancel</el-button>
                <el-button type="primary" @click="handleMemberPay" :loading="memberPaying">
                    Confirm Payment
                </el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Iphone, Wallet } from '@element-plus/icons-vue'
import { getProfileApi, updateProfileApi, changePasswordApi, deleteAccountApi } from '@/api/auth'
import type { UserProfile } from '@/api/auth'
import { useAuth } from '@/composables/useAuth'
import type { FormInstance } from 'element-plus'

const router = useRouter()
const { logout } = useAuth()

const profile = ref<UserProfile>({} as UserProfile)

// Inline editing
const editingKey = ref<string | null>(null)
const editValue = ref('')
const saving = ref(false)

function startEdit(key: string) {
    editingKey.value = key
    editValue.value = String(profile.value[key as keyof UserProfile] ?? '')
}

function cancelEdit() {
    editingKey.value = null
    editValue.value = ''
}

async function saveEdit(key: string) {
    saving.value = true
    try {
        await updateProfileApi({ [key]: editValue.value } as Partial<UserProfile>)
        ;(profile.value as any)[key] = editValue.value
        editingKey.value = null
        ElMessage.success('Update successful')
    } catch {
        ElMessage.error('Update failed')
    } finally {
        saving.value = false
    }
}

// VIP Membership Management
const memberPayDialogVisible = ref(false)
const memberPaymentMethod = ref('WECHAT')
const memberPaying = ref(false)

function handleMembership() {
    if (profile.value.isMember) {
        ElMessageBox.confirm('Are you sure you want to cancel your membership?', 'Cancel Membership', {
            confirmButtonText: 'Confirm Cancel',
            cancelButtonText: 'Think Again',
            type: 'warning',
        }).then(async () => {
            await updateProfileApi({ isMember: false } as any)
            profile.value.isMember = false
            ElMessage.success('Membership cancelled')
        }).catch(() => {})
    } else {
        memberPaymentMethod.value = 'WECHAT'
        memberPayDialogVisible.value = true
    }
}

async function handleMemberPay() {
    memberPaying.value = true
    // Simulate payment delay
    await new Promise(resolve => setTimeout(resolve, 300))
    try {
        await updateProfileApi({ isMember: true } as any)
        profile.value.isMember = true
        memberPayDialogVisible.value = false
        ElMessage.success('Welcome to Membership!')
    } catch {
        ElMessage.error('Operation failed')
    } finally {
        memberPaying.value = false
    }
}

// Change Password
const passwordDialogVisible = ref(false)
const changingPwd = ref(false)
const passwordFormRef = ref<FormInstance>()

const passwordForm = ref({
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
})

const passwordRules = {
    oldPassword: [{ required: true, message: 'Please enter your current password', trigger: 'blur' }],
    newPassword: [
        { required: true, message: 'Please enter a new password', trigger: 'blur' },
        { min: 6, message: 'Password must be at least 6 characters', trigger: 'blur' },
    ],
    confirmPassword: [
        { required: true, message: 'Please confirm your new password', trigger: 'blur' },
        {
            validator: (_rule: any, value: string, callback: (e?: Error) => void) => {
                if (value !== passwordForm.value.newPassword) {
                    callback(new Error('The passwords entered do not match'))
                } else {
                    callback()
                }
            },
            trigger: 'blur',
        },
    ],
}

function showChangePasswordDialog() {
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
    passwordDialogVisible.value = true
}

async function handleChangePassword() {
    const valid = await passwordFormRef.value?.validate().catch(() => false)
    if (!valid) return
    changingPwd.value = true
    try {
        await changePasswordApi({
            oldPassword: passwordForm.value.oldPassword,
            newPassword: passwordForm.value.newPassword,
        })
        ElMessage.success('Password changed successfully')
        passwordDialogVisible.value = false
    } catch {
        ElMessage.error('Password change failed, please check your current password')
    } finally {
        changingPwd.value = false
    }
}

// Delete Account
async function confirmDeleteAccount() {
    try {
        await ElMessageBox.confirm(
            'Are you sure you want to delete your account? Your account will be disabled and cannot be recovered.',
            'Delete Account',
            { confirmButtonText: 'Confirm Delete', cancelButtonText: 'Cancel', type: 'warning' }
        )
        await deleteAccountApi()
        ElMessage.success('Account has been deleted')
        logout()
        router.push('/login')
    } catch {
        // Do nothing on cancel
    }
}

onMounted(async () => {
    try {
        profile.value = await getProfileApi()
    } catch {
        ElMessage.error('Failed to fetch user information')
    }
})
</script>

<style scoped lang="scss">
.card-title {
    font-size: 16px;
    font-weight: 600;
}

.el-descriptions {
    :deep(.el-descriptions__label) {
        width: 80px;
    }
}

.desc-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    min-height: 28px;
    width: 100%;
}

.dynamic-input {
    min-width: 120px;
    max-width: 600px;
    width: auto;
    flex-shrink: 1;
}

.btn-group {
    display: flex;
    gap: 6px;
    flex-shrink: 0;
}

.card-actions {
    margin-top: 24px;
    padding-top: 16px;
    border-top: 1px solid #ebeef5;
    display: flex;
    justify-content: flex-end;
    gap: 12px;
}

.member-pay-info {
    text-align: center;
    padding: 16px 0;

    .member-price {
        font-size: 36px;
        font-weight: 700;
        color: #f56c6c;
    }

    .member-desc {
        margin-top: 8px;
        font-size: 14px;
        color: #909399;
    }
}

.pay-method-title {
    text-align: center;
    font-size: 15px;
    font-weight: 600;
    margin: 0 0 16px;
}

.pay-methods {
    display: flex;
    justify-content: center;
    gap: 16px;
}

.pay-method-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 24px;
    border-radius: 8px;
}
</style>
