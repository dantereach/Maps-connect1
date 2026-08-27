const API_BASE_URL = 'http://localhost:8080/api';

const API = {
    async request(endpoint, options = {}) {
        const url = `${API_BASE_URL}${endpoint}`;
        const token = localStorage.getItem('token');

        const config = {
            headers: {
                'Content-Type': 'application/json',
                ...(token && { Authorization: `Bearer ${token}` })
            },
            ...options
        };

        const response = await fetch(url, config);
        const data = await response.json().catch(() => null);

        if (!response.ok) {
            throw new Error(data?.message || `Error ${response.status}`);
        }

        return data;
    }
};
