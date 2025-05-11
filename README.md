# 🚀 Teampilot

Teampilot is an open-source leave tracker designed for small and remote companies that need software to efficiently
manage and track employee leaves.

## ✨ Features

- 📊 **Modern Dashboard**: A sleek dashboard built with React for intuitive navigation and ease of use
- 📅 **Leave Management**: Efficiently manage and track employee leave requests and approvals
- 🗓️ **Calendar View**: Visualize leave schedules and team availability with an interactive calendar
- ⚙️ **Customizable Settings**: Adapt the application to your company's leave policies and workflows
- 🔔 **Notifications**: Keep employees informed with automated email notifications for leave requests and approvals
- 🔒 **Secure**: Built with security best practices to protect employee data

## 🛠️ Tech Stack

### Frontend

- ⚛️ React
- 📱 Responsive Design

### Backend

- ☕ Java 21
- 🌱 Spring Boot 3.3
- 🔐 JWT Authentication
- 📨 SMTP Email Integration

### Database & Storage

- 🐘 PostgreSQL 14
- 📦 AWS S3 Compatible Storage

### Infrastructure

- 🐳 Docker & Docker Compose
- 🌐 Caddy Reverse Proxy
- 🔄 GitHub Actions CI/CD

## 🚀 Getting Started

### Prerequisites

- 🐳 Docker and Docker Compose installed
- 📝 Text editor for configuration files

### Quick Start

1. Clone the repository:

```
bash
git clone https://github.com/yourusername/teampilot.git
cd teampilot
```

2. Create a `.env` file in the root directory with the following variables:

```
env

SMTP Configuration
SMTP_HOST=your-smtp-host
SMTP_PORT=587
SMTP_USERNAME=your-smtp-username
SMTP_PASSWORD=your-smtp-password
Application Settings
APP_BASE_URL=http://localhost
APP_COMPANY_NAME=Your Company
```

3. Start the application using Docker Compose:

```
bash
docker compose -f docker/compose.yaml up -d
```

4. Access the application:

- 🌐 Dashboard: http://localhost:8000
- 📚 API Documentation: http://localhost:8001/swagger-ui.html

### Default Ports

- 🖥️ Frontend App: 8000
- ⚙️ Backend API: 8001
- 🗄️ PostgreSQL: 8002

## 🤝 Contributing

We welcome contributions! Here's how you can help:

1. Fork the repository
2. Create your feature branch: `git checkout -b feature/AmazingFeature`
3. Commit your changes: `git commit -m 'Add some AmazingFeature'`
4. Push to the branch: `git push origin feature/AmazingFeature`
5. Open a Pull Request

Please check our [Contributing Guidelines](.github/pull_request_template.md) for more details.

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📧 Contact

For questions or support, please contact [mohsenk.work@gmail.com](mailto:mohsenk.work@gmail.com)

## 🙏 Acknowledgments

- Thanks to all contributors who help make Teampilot better
- Built with ❤️ for remote teams everywhere