<template>
    <div class="layout">
        <AppHeader />
        <div class="main-content">
            <router-view />
        </div>
    </div>
</template>

<style scoped lang="scss">
.layout {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
}

.main-content {
    flex: 1;
    padding-top: 60px;
}
</style>
