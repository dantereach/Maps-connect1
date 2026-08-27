const Utils = {
    showAlert(containerId, message, type = 'error') {
        const container = document.getElementById(containerId);
        if (!container) return;
        container.innerHTML = `<div class="alert alert-${type}">${message}</div>`;
    },

    clearAlert(containerId) {
        const container = document.getElementById(containerId);
        if (container) container.innerHTML = '';
    }
};
