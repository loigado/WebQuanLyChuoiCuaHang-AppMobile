import { toastController, loadingController } from '@ionic/vue';

export const useUi = () => {
  const showToast = async (message: string, color: 'success' | 'danger' | 'warning' | 'primary' = 'primary') => {
    const toast = await toastController.create({
      message,
      duration: 3000,
      color,
      position: 'top'
    });
    await toast.present();
  };

  const showLoading = async (message: string = 'Đang xử lý...') => {
    const loading = await loadingController.create({
      message
    });
    await loading.present();
    return loading;
  };

  return {
    showToast,
    showLoading
  };
};
