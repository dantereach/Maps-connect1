/** Acceso provisional solo para revisar la interfaz (sin backend). */
const DEV_LOGIN = {
    email: 'demo@tecmilenio.mx',
    password: 'Demo1234',
    user: {
        nombre: 'Usuario Demo',
        email: 'demo@tecmilenio.mx',
        rol: 'ESTUDIANTE'
    }
};

document.addEventListener('DOMContentLoaded', () => {
    Auth.redirectIfAuthenticated();

    document.getElementById('login-form').addEventListener('submit', (e) => {
        e.preventDefault();
        Utils.clearAlert('alert-container');

        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value;

        if (email === DEV_LOGIN.email && password === DEV_LOGIN.password) {
            Auth.saveSession('token-provisional-dev', DEV_LOGIN.user);
            window.location.href = 'pages/inicio.html';
            return;
        }

        Utils.showAlert('alert-container', 'Correo o contraseña incorrectos. Usa las credenciales provisionales.');
    });
});
