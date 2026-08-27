# MAPS Connect

**Plataforma Académica y de Mentoría** para Universidad Tecmilenio que conecta estudiantes, docentes y egresados.

## 🎯 ¿Qué es?

MAPS Connect es una red colaborativa que permite:
- 💬 Foro de dudas académicas
- 💡 Compartir tips de estudio
- 📚 Repositorio de apuntes
- 👥 Círculos de estudio
- 💌 Mensajería privada
- 🏢 Reseñas de empresas (Semestre Empresarial)
- 🎓 Gestión del modelo académico MAPS

## 🏗️ Tecnología

| Capa | Tecnología |
|------|-----------|
| **Backend** | Java 25, Spring Boot 3.3.0 |
| **Frontend** | HTML5, CSS3, JavaScript Vanilla |
| **BD** | MySQL 8.0 (22 entidades, 3FN) |

## 🚀 Quick Start

### Backend
```bash
# Crear bases de datos
CREATE DATABASE maps_conect;
CREATE DATABASE maps_conect_dev;

# Editar: src/main/resources/application.yml
# (actualizar credenciales MySQL)

# Ejecutar
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
# API en: http://localhost:8080/api
```

### Frontend
```bash
# Abrir en Visual Studio Code
code frontend/

# Live Server (extensión VS Code)
# Clic derecho en index.html > "Open with Live Server"
# En: http://localhost:5500
```

## 📋 Requisitos

- Java 25+
- Maven 3.8+
- MySQL 8.0+
- Node.js (opcional, para frontend)

## 🔐 Autenticación

- **JWT Tokens** (24 horas)
- Email institucional: `@tecmilenio.mx`
- Contraseña segura: 8+ caracteres, mayúscula, número

## 📂 Estructura

```
maps-conect/
├── src/main/java/...          # Backend Java/Spring
├── frontend/                   # Frontend HTML/CSS/JS
│   ├── index.html
│   ├── css/
│   ├── js/
│   └── pages/
└── pom.xml
```

## 📚 Módulos

| Módulo | Descripción |
|--------|------------|
| 1️⃣ Gestión MAPS | Carreras, certificados, materias |
| 2️⃣ Usuarios | Login, perfiles, roles |
| 3️⃣ Foro | Dudas y respuestas académicas |
| 4️⃣ Tips | Consejos de estudio con votación |
| 5️⃣ Recursos | Apuntes y material de estudio |
| 6️⃣ Mensajes | Chat 1 a 1 estudiante-profesor |
| 7️⃣ Empresarial | Reseñas y experiencias vinculación |
| 8️⃣ Círculos | Grupos de estudio colaborativos |

## 🔗 Enlaces

- 📖 Backend: `http://localhost:8080/api`
- 🌐 Frontend: `http://localhost:5500`
- ✅ Health: `http://localhost:8080/api/health`

## 👨‍💻 Contacto

**Universidad Tecmilenio** - Proyecto MAPS Connect  
[GitHub Repository](https://github.com/danielsilva7577-bit/maps-conect)

---

**v1.0.0** | Agosto 2024 | En Desarrollo
