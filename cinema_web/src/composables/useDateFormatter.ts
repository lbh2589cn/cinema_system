/**
 * Format a date string from ISO format to DD/MM/YYYY
 * Supports both "YYYY-MM-DD" and "YYYY-MM-DDTHH:mm:ss" formats
 */
export function formatDate(dateStr: string | null | undefined): string {
    if (!dateStr) return '-'
    const parts = dateStr.split('T')[0].split('-')
    if (parts.length !== 3) return dateStr
    return `${parts[2]}/${parts[1]}/${parts[0]}`
}
