import { ref } from 'vue'

export function usePricing() {
    const totalAmount = ref(0)
    const discountAmount = ref(0)
    const finalAmount = ref(0)

    function calculate(seats: any[], snacks: any[], isMember: boolean) {
        const seatTotal = seats.reduce((sum: number, s: any) => sum + s.price, 0)
        const snackTotal = snacks.reduce((sum: number, s: any) => sum + s.price * s.quantity, 0)
        totalAmount.value = seatTotal + snackTotal

        // Apply member discount
        let discount = 0
        if (isMember) {
            discount += totalAmount.value * 0.2
        }

        discountAmount.value = discount
        finalAmount.value = totalAmount.value - discount
    }

    return {
        totalAmount,
        discountAmount,
        finalAmount,
        calculate,
    }
}
