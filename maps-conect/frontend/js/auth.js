/**
 * MAPS Connect - Autenticación y sesión
 */

const Auth = {
    TOKEN_KEY: 'token',
    USER_KEY: 'user',

    saveSession(token, user) {
        localStorage.setItem(this.TOKEN_KEY, token);
        localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    },

    getToken() {
        return localStorage.getItem(this.TOKEN_KEY);
    },

    getUser() {
        const user = localStorage.getItem(this.USER_KEY);
        return user ? JSON.parse(user) : null;
    },

    isAuthenticated() {
        return !!this.getToken();
    },

    logout() {
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.USER_KEY);
        window.location.href = Auth.resolvePath('index.html');
    },

    /** Redirige a login si no hay sesión. Usar en páginas protegidas. */
    requireAuth() {
        if (!this.isAuthenticated()) {
            window.location.href = this.resolvePath('login.html');
            return false;
        }
        return true;
    },

    /** Redirige al dashboard si ya hay sesión. Usar en login/registro. */
    redirectIfAuthenticated() {
        if (this.isAuthenticated()) {
            window.location.href = this.resolvePath('pages/inicio.html');
        }
    },

    resolvePath(relativePath) {
        const inPages = window.location.pathname.includes('/pages/');
        if (inPages && !relativePath.startsWith('../') && !relativePath.startsWith('pages/')) {
            return '../' + relativePath;
        }
        if (!inPages && relativePath.startsWith('pages/')) {
            return relativePath;
        }
        return relativePath;
    }
};
