export function extractErrorMessage(err: any, fallbackMessage: string = 'An unexpected error occurred'): string {
  if (!err) return fallbackMessage;
  if (typeof err === 'string') return err;
  if (err.response?.data?.message) return err.response.data.message;
  if (err.message) return err.message;
  if (typeof err === 'object' && err.error) return String(err.error);
  return fallbackMessage;
}
