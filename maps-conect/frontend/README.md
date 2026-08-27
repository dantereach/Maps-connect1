# Frontend - MAPS Connect

Entorno listo para programar. La estructura de archivos está definida; el contenido de cada módulo lo implementan ustedes.

## Estructura

```
frontend/
├── index.html, login.html, registro.html
├── css/          base.css, layout.css, components.css
├── js/
│   ├── api.js     Cliente HTTP base
│   ├── auth.js    Sesión JWT
│   ├── utils.js   Helpers mínimos
│   ├── layout.js  Menú lateral
│   └── pages/     Un .js por pantalla
├── pages/         8 módulos (HTML vacío + layout)
└── assets/        Imágenes
```

## Iniciar

1. Live Server en `index.html` → `http://localhost:5500`
2. Backend en `http://localhost:8080/api`

## Por módulo

| Pantalla | HTML | JS |
|----------|------|-----|
| Inicio | `pages/inicio.html` | `js/pages/inicio.js` |
| Perfil | `pages/perfil.html` | `js/pages/perfil.js` |
| Foro | `pages/foro.html` | `js/pages/foro.js` |
| Tips | `pages/tips.html` | `js/pages/tips.js` |
| Apuntes | `pages/recursos.html` | `js/pages/recursos.js` |
| Mensajes | `pages/mensajes.html` | `js/pages/mensajes.js` |
| Empresarial | `pages/empresarial.html` | `js/pages/empresarial.js` |
| Círculos | `pages/circulos.html` | `js/pages/circulos.js` |

El contenido de cada pantalla va en `#main-content`.
